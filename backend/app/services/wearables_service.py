"""
Wearables Service

Manages wearable device data collection and storage in MongoDB.
Handles high-frequency health metrics data efficiently.
"""

from datetime import datetime, timedelta
from typing import List, Dict, Any, Optional
import structlog
import hashlib
import json
from pymongo import ASCENDING
from pymongo.errors import DuplicateKeyError

from app.core.database import get_mongodb, get_prisma
from app.models.wearables import WearableDataCreate, HealthMetrics
from dateutil.parser import parse as parse_date

logger = structlog.get_logger(__name__)


class WearablesService:
    """
    Service for managing wearable device data and health metrics.
    
    Uses MongoDB for high-performance time-series data storage.
    """
    
    _index_ensured = False
    
    @staticmethod
    async def ensure_deduplication_index():
        """
        Ensure unique index exists on health_metrics collection.
        
        For individual metrics, creates index on:
        - (patient_id, device_id, metric_type, timestamp) for uniqueness
        - (patient_id, metric_type, timestamp) for fast queries
        """
        if WearablesService._index_ensured:
            return
        
        try:
            mongodb = get_mongodb()
            
            # Unique index to prevent exact duplicates
            await mongodb.health_metrics.create_index(
                [
                    ("patient_id", ASCENDING),
                    ("device_id", ASCENDING),
                    ("metric_type", ASCENDING),
                    ("timestamp", ASCENDING),
                ],
                unique=True,
                name="unique_patient_device_metric_timestamp",
                background=True
            )
            
            # Query optimization index
            await mongodb.health_metrics.create_index(
                [
                    ("patient_id", ASCENDING),
                    ("metric_type", ASCENDING),
                    ("timestamp", ASCENDING),
                ],
                name="query_patient_metric_time",
                background=True
            )
            
            logger.info("Ensured indexes on health_metrics collection")
            WearablesService._index_ensured = True
            
        except Exception as e:
            logger.debug(f"Index creation note: {e}")
            WearablesService._index_ensured = True
    
    @staticmethod
    async def store_health_metrics(patient_id: str, device_id: str, metrics: HealthMetrics) -> Dict[str, Any]:
        """
        Store health metrics from a wearable device.
        
        Uses content-based deduplication via hash of metric values.
        If the same data is uploaded multiple times, it will be deduplicated.
        
        Args:
            patient_id: Patient's unique ID
            device_id: Wearable device ID
            metrics: Health metrics data
            
        Returns:
            Dict: Stored document with ID and deduplication status
        """
        # Ensure deduplication index exists
        await WearablesService.ensure_deduplication_index()
        
        mongodb = get_mongodb()
        current_time = datetime.utcnow()
        
        # Create a hash of the metric values for content-based deduplication
        # This allows detecting when the same data is uploaded multiple times
        metrics_dict = metrics.model_dump()
        
        # Check if all metric values are None (empty upload)
        has_any_data = any(value is not None for value in metrics_dict.values())
        
        if not has_any_data:
            logger.warning(
                "Attempted to store empty health metrics (all values are null)",
                patient_id=patient_id,
                device_id=device_id
            )
            # Generate a special hash for empty metrics to avoid null constraint issues
            metrics_hash = hashlib.sha256(f"empty_{current_time.isoformat()}".encode()).hexdigest()
        else:
            # Sort keys to ensure consistent hash
            metrics_str = json.dumps(metrics_dict, sort_keys=True)
            metrics_hash = hashlib.sha256(metrics_str.encode()).hexdigest()
        
        document = {
            "patient_id": patient_id,
            "device_id": device_id,
            "timestamp": current_time,
            "metrics_hash": metrics_hash,  # Used for unique index
            "heart_rate": metrics.heart_rate,
            "steps": metrics.steps,
            "sleep_hours": metrics.sleep_hours,
            "calories": metrics.calories,
            "oxygen_level": metrics.oxygen_level,
            "blood_pressure_systolic": metrics.blood_pressure_systolic,
            "blood_pressure_diastolic": metrics.blood_pressure_diastolic,
        }
        
        # Use upsert to update if duplicate exists, insert if new
        query = {
            "patient_id": patient_id,
            "device_id": device_id,
            "metrics_hash": metrics_hash,
        }
        
        try:
            result = await mongodb.health_metrics.update_one(
                query,
                {"$set": document},
                upsert=True
            )
            
            was_duplicate = result.matched_count > 0
            
            if was_duplicate:
                logger.info(
                    "Updated existing health metrics (deduplicated)",
                    patient_id=patient_id,
                    device_id=device_id,
                    metrics_hash=metrics_hash[:16]
                )
            else:
                logger.info(
                    "Stored new health metrics",
                    patient_id=patient_id,
                    device_id=device_id
                )
            
            # Update device sync info in PostgreSQL
            await WearablesService._update_device_sync(device_id)
            
            # Check for health alerts only for new data
            if not was_duplicate:
                await WearablesService._check_health_alerts(patient_id, metrics)
            
            # Get the document ID
            doc_id = result.upserted_id if result.upserted_id else None
            if not doc_id:
                # Document was updated, fetch its ID
                existing = await mongodb.health_metrics.find_one(query)
                doc_id = existing["_id"] if existing else None
            
            return {
                "id": str(doc_id) if doc_id else "updated",
                "was_duplicate": was_duplicate,
                **document
            }
            
        except DuplicateKeyError:
            # Race condition: another request created the document between our check and insert
            logger.warning(
                "Duplicate health metrics detected (race condition)",
                patient_id=patient_id,
                device_id=device_id,
                metrics_hash=metrics_hash[:16]
            )
            # Fetch and return the existing document
            existing = await mongodb.health_metrics.find_one(query)
            return {
                "id": str(existing["_id"]) if existing else "unknown",
                "was_duplicate": True,
                **document
            }
    
    @staticmethod
    async def get_recent_metrics(patient_id: str, hours: int = 24) -> List[Dict[str, Any]]:
        """
        Get recent health metrics for a patient.
        
        Args:
            patient_id: Patient's unique ID
            hours: Number of hours to look back (default 24)
            
        Returns:
            List of health metrics
        """
        mongodb = get_mongodb()
        
        since = datetime.utcnow() - timedelta(hours=hours)
        
        cursor = mongodb.health_metrics.find({
            "patient_id": patient_id,
            "timestamp": {"$gte": since}
        }).sort("timestamp", -1)
        
        metrics = await cursor.to_list(length=1000)
        
        # Convert ObjectId to string for JSON serialization
        for metric in metrics:
            metric["_id"] = str(metric["_id"])
        
        logger.info("Retrieved recent metrics", patient_id=patient_id, count=len(metrics))
        return metrics
    
    @staticmethod
    async def get_health_summary(patient_id: str) -> Dict[str, Any]:
        """
        Calculate health summary from recent metrics.
        
        Args:
            patient_id: Patient's unique ID
            
        Returns:
            Dict with aggregated health stats
        """
        metrics = await WearablesService.get_recent_metrics(patient_id, hours=24)
        
        if not metrics:
            return {
                "steps": 0,
                "avg_heart_rate": 0,
                "total_sleep_hours": 0,
                "calories": 0,
                "data_points": 0,
            }
        
        # Calculate aggregates
        total_steps = sum(m.get("steps", 0) for m in metrics)
        heart_rates = [m.get("heart_rate") for m in metrics if m.get("heart_rate")]
        avg_heart_rate = sum(heart_rates) / len(heart_rates) if heart_rates else 0
        total_sleep = sum(m.get("sleep_hours", 0) for m in metrics)
        total_calories = sum(m.get("calories", 0) for m in metrics)
        
        summary = {
            "steps": total_steps,
            "avg_heart_rate": round(avg_heart_rate, 1),
            "total_sleep_hours": round(total_sleep, 1),
            "calories": total_calories,
            "data_points": len(metrics),
            "last_sync": metrics[0]["timestamp"] if metrics else None,
        }
        
        logger.info("Calculated health summary", patient_id=patient_id)
        return summary
    
    @staticmethod
    async def _update_device_sync(device_id: str):
        """
        Update device last sync time and data points count.
        
        Internal method called after storing metrics.
        """
        prisma = get_prisma()
        
        try:
            device = await prisma.wearabledevice.find_unique(where={"device_id": device_id})
            if device:
                await prisma.wearabledevice.update(
                    where={"device_id": device_id},
                    data={
                        "last_sync_time": datetime.utcnow(),
                        "data_points_synced": device.data_points_synced + 1,
                    }
                )
        except Exception as e:
            logger.error("Failed to update device sync", device_id=device_id, error=str(e))
    
    @staticmethod
    async def store_individual_metrics(patient_id: str, device_id: str, metrics: List[Dict[str, Any]]) -> Dict[str, Any]:
        """
        Store individual health metrics (time-series data).
        
        Each metric is stored as a separate document for detailed analysis and graphing.
        Deduplication based on patient + device + metric_type + timestamp.
        
        Args:
            patient_id: Patient's unique ID
            device_id: Device ID
            metrics: List of individual metric dictionaries
            
        Returns:
            Dict with storage statistics
        """
        await WearablesService.ensure_deduplication_index()
        
        mongodb = get_mongodb()
        
        stored_count = 0
        duplicate_count = 0
        error_count = 0
        
        for metric in metrics:
            try:
                # Parse the timestamp
                timestamp_str = metric.get("end_date") or metric.get("start_date")
                timestamp = parse_date(timestamp_str)
                
                document = {
                    "patient_id": patient_id,
                    "device_id": device_id,
                    "metric_type": metric["metric_type"],
                    "value": metric["value"],
                    "unit": metric["unit"],
                    "timestamp": timestamp,
                    "start_date": parse_date(metric["start_date"]) if metric.get("start_date") else timestamp,
                    "end_date": parse_date(metric["end_date"]) if metric.get("end_date") else timestamp,
                    "source_app": metric.get("source_app", "Unknown"),
                    "metadata": metric.get("metadata", {}),
                    "created_at": datetime.utcnow(),
                }
                
                # Try to insert
                try:
                    await mongodb.health_metrics.insert_one(document)
                    stored_count += 1
                except DuplicateKeyError:
                    duplicate_count += 1
                    
            except Exception as e:
                error_count += 1
                logger.warning(f"Failed to store individual metric: {e}")
        
        logger.info(
            "Stored individual metrics",
            patient_id=patient_id,
            device_id=device_id,
            stored=stored_count,
            duplicates=duplicate_count,
            errors=error_count,
            total=len(metrics)
        )
        
        # Update device sync info
        if stored_count > 0:
            await WearablesService._update_device_sync(device_id)
        
        return {
            "stored_count": stored_count,
            "duplicate_count": duplicate_count,
            "error_count": error_count,
            "total_metrics": len(metrics),
            "was_duplicate": duplicate_count == len(metrics) and stored_count == 0
        }
    
    @staticmethod
    async def _check_health_alerts(patient_id: str, metrics: HealthMetrics):
        """
        Check if health metrics trigger any alerts.
        
        Creates emergency alerts for critical values.
        """
        prisma = get_prisma()
        alerts = []
        
        # Heart rate alerts
        if metrics.heart_rate:
            if metrics.heart_rate > 120:
                alerts.append({
                    "severity": "HIGH",
                    "alertType": "HEART_RATE",
                    "message": "Elevated heart rate detected",
                    "currentValue": str(metrics.heart_rate),
                })
            elif metrics.heart_rate < 40:
                alerts.append({
                    "severity": "HIGH",
                    "alertType": "HEART_RATE",
                    "message": "Low heart rate detected",
                    "currentValue": str(metrics.heart_rate),
                })
        
        # Oxygen level alerts
        if metrics.oxygen_level and metrics.oxygen_level < 90:
            alerts.append({
                "severity": "CRITICAL",
                "alertType": "OXYGEN_LEVEL",
                "message": "Low oxygen saturation",
                "currentValue": str(metrics.oxygen_level),
            })
        
        # Blood pressure alerts
        if metrics.blood_pressure_systolic and metrics.blood_pressure_systolic > 140:
            alerts.append({
                "severity": "MEDIUM",
                "alertType": "BLOOD_PRESSURE",
                "message": "Elevated blood pressure",
                "currentValue": f"{metrics.blood_pressure_systolic}/{metrics.blood_pressure_diastolic}",
            })
        
        # Store alerts in database
        for alert in alerts:
            try:
                await prisma.emergencyalert.create(
                    data={
                        "patientId": patient_id,
                        **alert,
                    }
                )
                logger.warning("Health alert created", patient_id=patient_id, alert_type=alert["alertType"])
            except Exception as e:
                logger.error("Failed to create alert", error=str(e))
    
    @staticmethod
    async def get_aggregated_metrics(patient_id: str, period: str = "daily", days: int = 30) -> Dict[str, Any]:
        """
        Get aggregated metrics by time period using MongoDB aggregation.
        
        Args:
            patient_id: Patient's unique ID
            period: Aggregation period - "hourly", "daily", or "weekly"
            days: Number of days to look back
            
        Returns:
            Dict with aggregated metrics by type
        """
        mongodb = get_mongodb()
        
        since = datetime.utcnow() - timedelta(days=days)
        
        # Define date grouping based on period
        if period == "hourly":
            date_format = "%Y-%m-%d %H:00"
        elif period == "weekly":
            date_format = "%Y-W%U"  # Year-Week format
        else:  # daily
            date_format = "%Y-%m-%d"
        
        # Aggregation pipeline
        pipeline = [
            {
                "$match": {
                    "patient_id": patient_id,
                    "timestamp": {"$gte": since}
                }
            },
            {
                "$group": {
                    "_id": {
                        "date": {"$dateToString": {"format": date_format, "date": "$timestamp"}},
                        "metric_type": "$metric_type"
                    },
                    "total": {"$sum": "$value"},
                    "avg": {"$avg": "$value"},
                    "min": {"$min": "$value"},
                    "max": {"$max": "$value"},
                    "count": {"$sum": 1}
                }
            },
            {
                "$sort": {"_id.date": 1}
            }
        ]
        
        cursor = mongodb.health_metrics.aggregate(pipeline)
        results = await cursor.to_list(length=10000)
        
        # Organize results by metric type
        aggregated = {}
        for item in results:
            metric_type = item["_id"]["metric_type"]
            date = item["_id"]["date"]
            
            if metric_type not in aggregated:
                aggregated[metric_type] = []
            
            aggregated[metric_type].append({
                "date": date,
                "total": round(item["total"], 2),
                "avg": round(item["avg"], 2),
                "min": round(item["min"], 2),
                "max": round(item["max"], 2),
                "count": item["count"]
            })
        
        logger.info(
            "Aggregated metrics",
            patient_id=patient_id,
            period=period,
            days=days,
            metric_types=len(aggregated)
        )
        
        return aggregated
    
    @staticmethod
    async def get_metrics_by_type(
        patient_id: str,
        metric_type: str,
        start_date: Optional[str] = None,
        end_date: Optional[str] = None
    ) -> List[Dict[str, Any]]:
        """
        Get specific metric type over date range.
        
        Args:
            patient_id: Patient's unique ID
            metric_type: Type of metric (heart_rate, steps, etc.)
            start_date: Optional start date (ISO format)
            end_date: Optional end date (ISO format)
            
        Returns:
            List of metrics sorted by timestamp
        """
        mongodb = get_mongodb()
        
        # Build query
        query = {
            "patient_id": patient_id,
            "metric_type": metric_type
        }
        
        # Add date filters if provided
        if start_date or end_date:
            query["timestamp"] = {}
            if start_date:
                query["timestamp"]["$gte"] = parse_date(start_date)
            if end_date:
                query["timestamp"]["$lte"] = parse_date(end_date)
        
        cursor = mongodb.health_metrics.find(query).sort("timestamp", 1)
        metrics = await cursor.to_list(length=10000)
        
        # Convert ObjectId to string
        for metric in metrics:
            metric["_id"] = str(metric["_id"])
        
        logger.info(
            "Retrieved metrics by type",
            patient_id=patient_id,
            metric_type=metric_type,
            count=len(metrics)
        )
        
        return metrics
    
    @staticmethod
    async def get_today_summary(patient_id: str) -> Dict[str, Any]:
        """
        Get aggregated summary for today with comparison to yesterday.
        
        Args:
            patient_id: Patient's unique ID
            
        Returns:
            Dict with today's totals and percentage changes
        """
        mongodb = get_mongodb()
        
        # Define time ranges
        now = datetime.utcnow()
        today_start = now.replace(hour=0, minute=0, second=0, microsecond=0)
        yesterday_start = today_start - timedelta(days=1)
        
        # Get today's data
        today_pipeline = [
            {
                "$match": {
                    "patient_id": patient_id,
                    "timestamp": {"$gte": today_start}
                }
            },
            {
                "$group": {
                    "_id": "$metric_type",
                    "total": {"$sum": "$value"},
                    "avg": {"$avg": "$value"},
                    "min": {"$min": "$value"},
                    "max": {"$max": "$value"},
                    "count": {"$sum": 1}
                }
            }
        ]
        
        # Get yesterday's data
        yesterday_pipeline = [
            {
                "$match": {
                    "patient_id": patient_id,
                    "timestamp": {
                        "$gte": yesterday_start,
                        "$lt": today_start
                    }
                }
            },
            {
                "$group": {
                    "_id": "$metric_type",
                    "total": {"$sum": "$value"},
                    "avg": {"$avg": "$value"},
                    "count": {"$sum": 1}
                }
            }
        ]
        
        today_results = await mongodb.health_metrics.aggregate(today_pipeline).to_list(length=100)
        yesterday_results = await mongodb.health_metrics.aggregate(yesterday_pipeline).to_list(length=100)
        
        # Organize today's data
        today_data = {item["_id"]: item for item in today_results}
        yesterday_data = {item["_id"]: item for item in yesterday_results}
        
        # Calculate summary with changes
        summary = {}
        
        for metric_type in ["steps", "heart_rate", "calories", "distance", "flights_climbed", "sleep"]:
            if metric_type in today_data:
                today_item = today_data[metric_type]
                
                # Calculate change from yesterday
                change = None
                if metric_type in yesterday_data:
                    yesterday_value = yesterday_data[metric_type].get("total" if metric_type in ["steps", "calories", "distance", "flights_climbed", "sleep"] else "avg", 0)
                    today_value = today_item.get("total" if metric_type in ["steps", "calories", "distance", "flights_climbed", "sleep"] else "avg", 0)
                    
                    if yesterday_value > 0:
                        change_pct = ((today_value - yesterday_value) / yesterday_value) * 100
                        change = f"{'+' if change_pct > 0 else ''}{round(change_pct)}%"
                
                # Build summary based on metric type
                if metric_type in ["steps", "calories", "flights_climbed"]:
                    summary[metric_type] = {
                        "total": round(today_item["total"], 2),
                        "change": change
                    }
                elif metric_type == "distance":
                    summary[metric_type] = {
                        "total": round(today_item["total"], 2),
                        "unit": "km",
                        "change": change
                    }
                elif metric_type == "sleep":
                    summary[metric_type] = {
                        "total": round(today_item["total"], 2),
                        "unit": "hours",
                        "change": change
                    }
                elif metric_type == "heart_rate":
                    summary[metric_type] = {
                        "avg": round(today_item["avg"], 1),
                        "min": round(today_item["min"], 1),
                        "max": round(today_item["max"], 1),
                        "change": change
                    }
                else:
                    summary[metric_type] = {
                        "total": round(today_item["total"], 2),
                        "avg": round(today_item["avg"], 2),
                        "change": change
                    }
            else:
                # No data for this metric today
                if metric_type == "heart_rate":
                    summary[metric_type] = {
                        "avg": 0,
                        "min": 0,
                        "max": 0,
                        "change": None
                    }
                elif metric_type == "sleep":
                    summary[metric_type] = {
                        "total": 0,
                        "unit": "hours",
                        "change": None
                    }
                elif metric_type == "distance":
                    summary[metric_type] = {
                        "total": 0,
                        "unit": "km",
                        "change": None
                    }
                else:
                    summary[metric_type] = {
                        "total": 0,
                        "change": None
                    }
        
        logger.info("Calculated today's summary", patient_id=patient_id)
        
        return summary
