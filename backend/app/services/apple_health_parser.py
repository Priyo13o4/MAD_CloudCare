"""
Apple Health Data Parser

Parses JSON exports from iPhone/Apple Watch HealthKit data.
Converts Apple Health format to CloudCare's internal format.
"""

from typing import Dict, List, Any, Optional
from datetime import datetime
import structlog

logger = structlog.get_logger(__name__)


class AppleHealthParser:
    """
    Parser for Apple Health JSON exports.
    
    Handles HealthKit metric types and converts them to CloudCare format.
    """
    
    # Mapping of Apple HealthKit types to our metric names
    HEALTH_KIT_TYPE_MAPPING = {
        "HKQuantityTypeIdentifierHeartRate": "heart_rate",
        "HKQuantityTypeIdentifierStepCount": "steps",
        "HKQuantityTypeIdentifierActiveEnergyBurned": "calories",
        "HKQuantityTypeIdentifierDistanceWalkingRunning": "distance",
        "HKQuantityTypeIdentifierFlightsClimbed": "flights_climbed",
        "HKQuantityTypeIdentifierOxygenSaturation": "oxygen_level",
        "HKQuantityTypeIdentifierBloodPressureSystolic": "blood_pressure_systolic",
        "HKQuantityTypeIdentifierBloodPressureDiastolic": "blood_pressure_diastolic",
        "HKQuantityTypeIdentifierSleepAnalysis": "sleep",
        "HKCategoryTypeIdentifierSleepAnalysis": "sleep_category",
    }
    
    @staticmethod
    def parse_health_export(data: Dict[str, Any]) -> Dict[str, Any]:
        """
        Parse Apple Health export JSON file.
        
        Args:
            data: Raw JSON data from Apple Health export
            
        Returns:
            Dict with parsed and aggregated health data
            
        Example Input:
        {
            "deviceId": "5233E7EE-EF98-4AEB-9E01-1A81FAB21C43",
            "userId": "CB99F596-9067-4786-AD08-639BBF0A96C0",
            "exportTimestamp": "2025-11-13T06:49:53Z",
            "dataRange": {"startDate": "...", "endDate": "..."},
            "metrics": [...]
        }
        """
        try:
            device_id = data.get("deviceId")
            user_id = data.get("userId")
            export_timestamp = data.get("exportTimestamp")
            metrics = data.get("metrics", [])
            
            # Aggregate metrics by type
            aggregated = AppleHealthParser._aggregate_metrics(metrics)
            
            # Extract latest values for each metric type
            latest_values = AppleHealthParser._extract_latest_values(metrics)
            
            logger.info(
                "Parsed Apple Health export",
                device_id=device_id,
                metric_count=len(metrics),
                types=list(aggregated.keys())
            )
            
            return {
                "device_id": device_id,
                "user_id": user_id,
                "export_timestamp": export_timestamp,
                "aggregated_metrics": aggregated,
                "latest_values": latest_values,
                "total_data_points": len(metrics),
            }
            
        except Exception as e:
            logger.error("Failed to parse Apple Health export", error=str(e))
            raise ValueError(f"Invalid Apple Health export format: {e}")
    
    @staticmethod
    def _aggregate_metrics(metrics: List[Dict[str, Any]]) -> Dict[str, Any]:
        """
        Aggregate metrics by type for summary statistics.
        
        Returns:
            Dict with aggregated data for each metric type
        """
        aggregated = {}
        
        for metric in metrics:
            metric_type = metric.get("type")
            our_type = AppleHealthParser.HEALTH_KIT_TYPE_MAPPING.get(metric_type)
            
            if not our_type:
                continue  # Skip unknown types
            
            value = metric.get("value")
            if value is None:
                continue
            
            if our_type not in aggregated:
                aggregated[our_type] = {
                    "values": [],
                    "sum": 0,
                    "count": 0,
                    "unit": metric.get("unit"),
                }
            
            aggregated[our_type]["values"].append(value)
            aggregated[our_type]["sum"] += value
            aggregated[our_type]["count"] += 1
        
        # Calculate averages and totals
        for metric_type, data in aggregated.items():
            if metric_type in ["steps", "calories", "distance", "flights_climbed"]:
                # These should be summed
                data["total"] = data["sum"]
            else:
                # These should be averaged (heart rate, oxygen, BP)
                data["average"] = data["sum"] / data["count"] if data["count"] > 0 else 0
                data["max"] = max(data["values"]) if data["values"] else 0
                data["min"] = min(data["values"]) if data["values"] else 0
        
        return aggregated
    
    @staticmethod
    def _extract_latest_values(metrics: List[Dict[str, Any]]) -> Dict[str, Any]:
        """
        Extract the most recent value for each metric type.
        
        Returns:
            Dict with latest values for each metric
        """
        latest = {}
        
        # Sort metrics by timestamp (most recent first)
        sorted_metrics = sorted(
            metrics,
            key=lambda m: m.get("endDate", ""),
            reverse=True
        )
        
        for metric in sorted_metrics:
            metric_type = metric.get("type")
            our_type = AppleHealthParser.HEALTH_KIT_TYPE_MAPPING.get(metric_type)
            
            if not our_type or our_type in latest:
                continue  # Skip if already have latest for this type
            
            latest[our_type] = {
                "value": metric.get("value"),
                "timestamp": metric.get("endDate"),
                "unit": metric.get("unit"),
                "device": metric.get("metadata", {}).get("device", "Unknown"),
            }
        
        return latest
    
    @staticmethod
    def convert_to_cloudcare_format(aggregated: Dict[str, Any]) -> Dict[str, Any]:
        """
        Convert aggregated metrics to CloudCare's HealthMetrics format.
        
        Args:
            aggregated: Aggregated metrics from parse_health_export
            
        Returns:
            Dict in CloudCare HealthMetrics format
        """
        metrics = {}
        
        # Heart rate (average)
        if "heart_rate" in aggregated:
            metrics["heart_rate"] = int(aggregated["heart_rate"].get("average", 0))
        
        # Steps (total)
        if "steps" in aggregated:
            metrics["steps"] = int(aggregated["steps"].get("total", 0))
        
        # Calories (total)
        if "calories" in aggregated:
            metrics["calories"] = int(aggregated["calories"].get("total", 0))
        
        # Oxygen level (average)
        if "oxygen_level" in aggregated:
            metrics["oxygen_level"] = int(aggregated["oxygen_level"].get("average", 0))
        
        # Blood pressure
        if "blood_pressure_systolic" in aggregated:
            metrics["blood_pressure_systolic"] = int(
                aggregated["blood_pressure_systolic"].get("average", 0)
            )
        
        if "blood_pressure_diastolic" in aggregated:
            metrics["blood_pressure_diastolic"] = int(
                aggregated["blood_pressure_diastolic"].get("average", 0)
            )
        
        # Sleep (if available - would need to be calculated from sleep analysis data)
        # This is more complex as Apple stores sleep as categories, not hours
        # For now, we'll skip it and handle it separately if needed
        
        return metrics
    
    @staticmethod
    def extract_device_info(data: Dict[str, Any]) -> Dict[str, Any]:
        """
        Extract device information from Apple Health export.
        
        Args:
            data: Raw Apple Health export data
            
        Returns:
            Dict with device information
        """
        metrics = data.get("metrics", [])
        devices = set()
        
        for metric in metrics:
            device = metric.get("metadata", {}).get("device")
            if device:
                devices.add(device)
        
        # Determine primary device type
        device_type = "unknown"
        if "Apple Watch" in devices:
            device_type = "apple_watch"
        elif "iPhone" in devices:
            device_type = "iphone"
        
        return {
            "device_id": data.get("deviceId"),
            "device_type": device_type,
            "devices_used": list(devices),
            "is_connected": True,  # Assuming connected if we have recent data
        }
    
    @staticmethod
    def validate_export(data: Dict[str, Any]) -> bool:
        """
        Validate that the data is a valid Apple Health export.
        
        Args:
            data: Data to validate
            
        Returns:
            True if valid, False otherwise
        """
        required_fields = ["deviceId", "exportTimestamp", "metrics"]
        
        for field in required_fields:
            if field not in data:
                logger.warning(f"Missing required field: {field}")
                return False
        
        if not isinstance(data.get("metrics"), list):
            logger.warning("Metrics field is not a list")
            return False
        
        return True


class AppleHealthBatchProcessor:
    """
    Process multiple Apple Health exports efficiently.
    
    Handles batch uploads and deduplication.
    """
    
    @staticmethod
    def process_batch(exports: List[Dict[str, Any]]) -> Dict[str, Any]:
        """
        Process multiple Apple Health exports.
        
        Args:
            exports: List of Apple Health export dictionaries
            
        Returns:
            Dict with combined results and statistics
        """
        all_metrics = []
        devices = set()
        earliest_date = None
        latest_date = None
        
        for export in exports:
            if not AppleHealthParser.validate_export(export):
                logger.warning("Skipping invalid export")
                continue
            
            metrics = export.get("metrics", [])
            all_metrics.extend(metrics)
            
            # Track devices
            device_id = export.get("deviceId")
            if device_id:
                devices.add(device_id)
            
            # Track date range
            data_range = export.get("dataRange", {})
            start = data_range.get("startDate")
            end = data_range.get("endDate")
            
            if start and (not earliest_date or start < earliest_date):
                earliest_date = start
            if end and (not latest_date or end > latest_date):
                latest_date = end
        
        # Remove duplicates based on timestamp and type
        unique_metrics = AppleHealthBatchProcessor._deduplicate_metrics(all_metrics)
        
        # Aggregate combined metrics
        aggregated = AppleHealthParser._aggregate_metrics(unique_metrics)
        
        logger.info(
            "Processed batch of Apple Health exports",
            export_count=len(exports),
            total_metrics=len(all_metrics),
            unique_metrics=len(unique_metrics),
            devices=len(devices)
        )
        
        return {
            "total_exports": len(exports),
            "total_metrics": len(all_metrics),
            "unique_metrics": len(unique_metrics),
            "devices": list(devices),
            "date_range": {
                "start": earliest_date,
                "end": latest_date,
            },
            "aggregated": aggregated,
            "cloudcare_format": AppleHealthParser.convert_to_cloudcare_format(aggregated),
        }
    
    @staticmethod
    def _deduplicate_metrics(metrics: List[Dict[str, Any]]) -> List[Dict[str, Any]]:
        """
        Remove duplicate metrics based on timestamp and type.
        
        Args:
            metrics: List of metric dictionaries
            
        Returns:
            List of unique metrics
        """
        seen = set()
        unique = []
        
        for metric in metrics:
            # Create unique key from type, timestamp, and value
            key = (
                metric.get("type"),
                metric.get("endDate"),
                metric.get("value"),
            )
            
            if key not in seen:
                seen.add(key)
                unique.append(metric)
        
        return unique
