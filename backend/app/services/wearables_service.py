"""
Wearables Service

Manages wearable device data collection and storage in MongoDB.
Handles high-frequency health metrics data efficiently.
"""

from datetime import datetime, timedelta
from typing import List, Dict, Any, Optional
import structlog

from app.core.database import get_mongodb, get_prisma
from app.models.wearables import WearableDataCreate, HealthMetrics

logger = structlog.get_logger(__name__)


class WearablesService:
    """
    Service for managing wearable device data and health metrics.
    
    Uses MongoDB for high-performance time-series data storage.
    """
    
    @staticmethod
    async def store_health_metrics(patient_id: str, device_id: str, metrics: HealthMetrics) -> Dict[str, Any]:
        """
        Store health metrics from a wearable device.
        
        Args:
            patient_id: Patient's unique ID
            device_id: Wearable device ID
            metrics: Health metrics data
            
        Returns:
            Dict: Stored document with ID
        """
        mongodb = get_mongodb()
        
        document = {
            "patient_id": patient_id,
            "device_id": device_id,
            "timestamp": datetime.utcnow(),
            "heart_rate": metrics.heart_rate,
            "steps": metrics.steps,
            "sleep_hours": metrics.sleep_hours,
            "calories": metrics.calories,
            "oxygen_level": metrics.oxygen_level,
            "blood_pressure_systolic": metrics.blood_pressure_systolic,
            "blood_pressure_diastolic": metrics.blood_pressure_diastolic,
        }
        
        result = await mongodb.health_metrics.insert_one(document)
        
        # Update device sync info in PostgreSQL
        await WearablesService._update_device_sync(device_id)
        
        # Check for health alerts
        await WearablesService._check_health_alerts(patient_id, metrics)
        
        logger.info("Stored health metrics", patient_id=patient_id, device_id=device_id)
        
        return {"id": str(result.inserted_id), **document}
    
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
