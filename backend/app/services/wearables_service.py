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
            device = await prisma.wearabledevice.find_unique(where={"deviceId": device_id})
            if device:
                await prisma.wearabledevice.update(
                    where={"deviceId": device_id},
                    data={
                        "lastSyncTime": datetime.utcnow(),
                        "dataPointsSynced": device.dataPointsSynced + 1,
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
