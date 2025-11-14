"""
Wearables Router

Handles wearable device management and health data sync.
"""

from fastapi import APIRouter, HTTPException, status, Depends
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from typing import List, Dict, Any
import structlog

from app.models.wearables import (
    WearableDataCreate,
    WearableDeviceCreate,
    WearableDeviceResponse,
    AppleHealthExport,
)
from app.services.wearables_service import WearablesService
from app.services.apple_health_parser import AppleHealthParser, AppleHealthBatchProcessor
from app.services.auth_service import AuthService
from app.core.database import get_prisma

logger = structlog.get_logger(__name__)
router = APIRouter(prefix="/wearables")
security = HTTPBearer()


async def get_current_patient_id(credentials: HTTPAuthorizationCredentials = Depends(security)) -> str:
    """Dependency to get current patient ID from token."""
    token = credentials.credentials
    payload = AuthService.decode_token(token)
    
    if not payload or payload.get("role") != "PATIENT":
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Patient access required"
        )
    
    prisma = get_prisma()
    user = await prisma.user.find_unique(
        where={"id": payload["sub"]},
        include={"patient": True}
    )
    
    if not user or not user.patient:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Patient profile not found"
        )
    
    return user.patient.id


@router.post("/devices", response_model=WearableDeviceResponse, status_code=status.HTTP_201_CREATED)
async def register_device(
    device: WearableDeviceCreate,
    patient_id: str = Depends(get_current_patient_id)
):
    """
    Register a new wearable device for the current patient.
    """
    prisma = get_prisma()
    
    try:
        # Check if device already exists
        existing = await prisma.wearabledevice.find_unique(where={"deviceId": device.device_id})
        if existing:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Device already registered"
            )
        
        # Create device
        new_device = await prisma.wearabledevice.create(
            data={
                "patientId": patient_id,
                "name": device.name,
                "type": device.type,
                "deviceId": device.device_id,
                "isConnected": True,
            }
        )
        
        logger.info("Device registered", device_id=device.device_id, patient_id=patient_id)
        return WearableDeviceResponse.model_validate(new_device)
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error("Failed to register device", error=str(e))
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to register device"
        )


@router.get("/devices", response_model=List[WearableDeviceResponse])
async def get_devices(patient_id: str = Depends(get_current_patient_id)):
    """
    Get all wearable devices for the current patient.
    """
    prisma = get_prisma()
    
    devices = await prisma.wearabledevice.find_many(
        where={"patientId": patient_id},
        order={"createdAt": "desc"}
    )
    
    return [WearableDeviceResponse.model_validate(d) for d in devices]


@router.post("/sync")
async def sync_data(
    data: WearableDataCreate,
    patient_id: str = Depends(get_current_patient_id)
):
    """
    Sync health data from a wearable device.
    
    Stores metrics in MongoDB and checks for health alerts.
    """
    try:
        result = await WearablesService.store_health_metrics(
            patient_id=patient_id,
            device_id=data.device_id,
            metrics=data.metrics
        )
        
        return {
            "message": "Data synced successfully",
            "id": result["id"],
            "timestamp": result["timestamp"]
        }
        
    except Exception as e:
        logger.error("Failed to sync data", error=str(e))
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to sync data"
        )


@router.get("/metrics/recent")
async def get_recent_metrics(
    hours: int = 24,
    patient_id: str = Depends(get_current_patient_id)
):
    """
    Get recent health metrics for the current patient.
    """
    try:
        metrics = await WearablesService.get_recent_metrics(patient_id, hours)
        return {
            "patient_id": patient_id,
            "hours": hours,
            "count": len(metrics),
            "metrics": metrics
        }
    except Exception as e:
        logger.error("Failed to get metrics", error=str(e))
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to retrieve metrics"
        )


@router.get("/summary")
async def get_health_summary(patient_id: str = Depends(get_current_patient_id)):
    """
    Get aggregated health summary for the current patient.
    """
    try:
        summary = await WearablesService.get_health_summary(patient_id)
        return summary
    except Exception as e:
        logger.error("Failed to get summary", error=str(e))
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to retrieve summary"
        )


@router.post("/import/apple-health", status_code=status.HTTP_201_CREATED)
async def import_apple_health(
    export: AppleHealthExport,
    patient_id: str = Depends(get_current_patient_id)
):
    """
    Import Apple Health JSON export data.
    
    Accepts JSON data from iPhone/Apple Watch HealthKit exports.
    Automatically parses and stores metrics in MongoDB.
    
    The export should contain:
    - deviceId: Apple device identifier
    - exportTimestamp: When the data was exported
    - metrics: Array of health metrics with timestamps
    """
    try:
        # Validate the export
        if not AppleHealthParser.validate_export(export.dict()):
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Invalid Apple Health export format"
            )
        
        # Parse the export
        parsed_data = AppleHealthParser.parse_health_export(export.dict())
        
        # Convert to CloudCare format
        cloudcare_metrics = AppleHealthParser.convert_to_cloudcare_format(
            parsed_data["aggregated_metrics"]
        )
        
        # Extract device info
        device_info = AppleHealthParser.extract_device_info(export.dict())
        
        # Register or update device
        prisma = get_prisma()
        device = await prisma.wearabledevice.find_unique(
            where={"deviceId": device_info["device_id"]}
        )
        
        if not device:
            # Create new device
            device = await prisma.wearabledevice.create(
                data={
                    "patientId": patient_id,
                    "name": f"Apple {device_info['device_type'].replace('_', ' ').title()}",
                    "type": device_info["device_type"],
                    "deviceId": device_info["device_id"],
                    "isConnected": device_info["is_connected"],
                }
            )
            logger.info("Registered new Apple device", device_id=device_info["device_id"])
        
        # Store metrics in MongoDB
        from app.models.wearables import HealthMetrics
        metrics_obj = HealthMetrics(**cloudcare_metrics)
        
        result = await WearablesService.store_health_metrics(
            patient_id=patient_id,
            device_id=device_info["device_id"],
            metrics=metrics_obj
        )
        
        logger.info(
            "Imported Apple Health data",
            patient_id=patient_id,
            device_id=device_info["device_id"],
            metrics_count=parsed_data["total_data_points"]
        )
        
        return {
            "message": "Apple Health data imported successfully",
            "device_id": device_info["device_id"],
            "device_type": device_info["device_type"],
            "metrics_imported": parsed_data["total_data_points"],
            "aggregated_values": cloudcare_metrics,
            "stored_id": result["id"],
        }
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error("Failed to import Apple Health data", error=str(e), exc_info=True)
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Failed to import Apple Health data: {str(e)}"
        )


@router.post("/import/apple-health/batch", status_code=status.HTTP_201_CREATED)
async def import_apple_health_batch(
    exports: List[AppleHealthExport],
    patient_id: str = Depends(get_current_patient_id)
):
    """
    Import multiple Apple Health JSON exports at once.
    
    Useful for bulk importing historical data.
    Automatically deduplicates metrics.
    """
    try:
        if not exports:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="No exports provided"
            )
        
        # Process batch
        batch_result = AppleHealthBatchProcessor.process_batch(
            [export.dict() for export in exports]
        )
        
        # Store aggregated metrics
        cloudcare_metrics = batch_result["cloudcare_format"]
        
        if cloudcare_metrics:
            from app.models.wearables import HealthMetrics
            metrics_obj = HealthMetrics(**cloudcare_metrics)
            
            # Use the first device ID for batch storage
            device_id = batch_result["devices"][0] if batch_result["devices"] else "batch_import"
            
            result = await WearablesService.store_health_metrics(
                patient_id=patient_id,
                device_id=device_id,
                metrics=metrics_obj
            )
            
            logger.info(
                "Imported Apple Health batch",
                patient_id=patient_id,
                export_count=batch_result["total_exports"],
                metrics_count=batch_result["total_metrics"]
            )
        
        return {
            "message": "Apple Health batch imported successfully",
            "exports_processed": batch_result["total_exports"],
            "total_metrics": batch_result["total_metrics"],
            "unique_metrics": batch_result["unique_metrics"],
            "devices": batch_result["devices"],
            "date_range": batch_result["date_range"],
            "aggregated_values": cloudcare_metrics,
        }
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error("Failed to import Apple Health batch", error=str(e), exc_info=True)
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Failed to import batch: {str(e)}"
        )
