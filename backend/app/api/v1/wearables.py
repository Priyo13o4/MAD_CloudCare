"""
Wearables Router

Handles wearable device management and health data sync.
"""

from fastapi import APIRouter, HTTPException, status, Depends
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from typing import List, Dict, Any, Optional
from datetime import datetime
import structlog

from app.models.wearables import (
    WearableDataCreate,
    WearableDeviceCreate,
    WearableDeviceResponse,
    AppleHealthExport,
    DevicePairingRequest,
    DevicePairingResponse,
    PairedDeviceInfo,
)
from app.services.wearables_service import WearablesService
from app.services.apple_health_parser import AppleHealthParser, AppleHealthBatchProcessor
from app.services.auth_service import AuthService
from app.core.database import get_prisma
from app.core.config import settings

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
        existing = await prisma.wearabledevice.find_unique(where={"device_id": device.device_id})
        if existing:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Device already registered"
            )
        
        # Create device
        new_device = await prisma.wearabledevice.create(
            data={
                "patient_id": patient_id,
                "name": device.name,
                "type": device.type,
                "device_id": device.device_id,
                "is_connected": True,
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
        where={"patient_id": patient_id},
        order={"created_at": "desc"}
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


@router.head("/import/apple-health")
async def test_apple_health_connection():
    """
    Test connection endpoint for iOS app.
    
    Responds to HEAD requests to verify the endpoint is accessible.
    Used by the CloudSync iOS app's "Test Connection" feature.
    """
    return None


@router.get("/import/apple-health")
async def get_apple_health_info():
    """
    Get information about the Apple Health import endpoint.
    
    Returns endpoint details and requirements.
    Also used for connection testing.
    """
    base_url = f"{settings.CLOUDFLARE_TUNNEL_URL}/api/{settings.API_VERSION}"
    return {
        "endpoint": "/api/v1/wearables/import/apple-health",
        "method": "POST",
        "description": "Import Apple Health/HealthKit data from iPhone or Apple Watch",
        "status": "available",
        "accepts": "application/json",
        "auth_required": False,  # Set to True when auth is enforced
        "public_url": settings.CLOUDFLARE_TUNNEL_URL,
        "base_url": base_url,
        "supported_metrics": [
            "HKQuantityTypeIdentifierStepCount",
            "HKQuantityTypeIdentifierHeartRate",
            "HKQuantityTypeIdentifierActiveEnergyBurned",
            "HKQuantityTypeIdentifierDistanceWalkingRunning",
            "HKQuantityTypeIdentifierFlightsClimbed",
            "HKQuantityTypeIdentifierRestingHeartRate",
            "HKQuantityTypeIdentifierVO2Max",
            "HKQuantityTypeIdentifierOxygenSaturation",
            "HKQuantityTypeIdentifierBloodPressureSystolic",
            "HKQuantityTypeIdentifierBloodPressureDiastolic",
        ],
        "format": {
            "userId": "string",
            "deviceId": "string",
            "exportTimestamp": "ISO8601 timestamp",
            "dataRange": {
                "startDate": "ISO8601 timestamp",
                "endDate": "ISO8601 timestamp"
            },
            "metrics": [
                {
                    "type": "HealthKit type identifier",
                    "startDate": "ISO8601 timestamp",
                    "endDate": "ISO8601 timestamp",
                    "value": "number",
                    "unit": "string",
                    "sourceApp": "string",
                    "metadata": "object (optional)"
                }
            ]
        }
    }


@router.post("/import/apple-health", status_code=status.HTTP_201_CREATED)
async def import_apple_health(export: AppleHealthExport):
    """
    Import Apple Health JSON export data.
    
    **Public endpoint - No authentication required for testing.**
    
    Accepts JSON data from iPhone/Apple Watch HealthKit exports.
    Automatically parses and stores metrics in MongoDB.
    
    The export should contain:
    - userId: User identifier from iOS app
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
        
        # Extract device info
        device_info = AppleHealthParser.extract_device_info(export.dict())
        
        # Convert to individual metrics for time-series storage
        raw_metrics = export.dict().get("metrics", [])
        individual_metrics = AppleHealthParser.convert_to_individual_metrics(raw_metrics)
        
        # Extract sleep stages for aggregated data
        sleep_stages = AppleHealthParser.extract_sleep_stages(raw_metrics)
        
        # Use userId from export or default for testing
        patient_id = export.userId if export.userId else "test_patient_001"
        
        # Register or update device (no patient ID constraint for now)
        prisma = get_prisma()
        device = await prisma.wearabledevice.find_unique(
            where={"device_id": device_info["device_id"]}
        )
        
        if not device:
            # Create test patient if doesn't exist
            test_patient = await prisma.patient.find_first(
                where={"id": patient_id}
            )
            
            if not test_patient:
                # Create a test user for anonymous uploads
                test_email = f"{patient_id}@test.cloudcare.local"
                test_user = await prisma.user.find_unique(where={"email": test_email})
                
                if not test_user:
                    test_user = await prisma.user.create(
                        data={
                            "email": test_email,
                            "password_hash": "test_hash_not_used",
                            "is_active": True,
                            "role": "PATIENT",
                        }
                    )
                
                # Create patient profile with required fields
                test_patient = await prisma.patient.create(
                    data={
                        "id": patient_id,
                        "user_id": test_user.id,
                        "aadhar_uid": f"test_uid_{patient_id[:8]}",
                        "name": "iOS App User",
                        "age": 0,
                        "gender": "Not specified",
                        "blood_type": "Unknown",
                        "contact": "+910000000000",
                        "email": test_email,
                        "address": "iOS App",
                        "family_contact": "+910000000000",
                    }
                )
                logger.info("Created test patient for iOS uploads", patient_id=patient_id)
            
            # Create new device
            # Try to get device name from pairing, otherwise use device metadata
            device_name = device_info.get('device_name', 'Apple Device')
            if not device_name or device_name == 'Apple Device':
                # Check if there's a pairing with a custom device name
                pairing = await prisma.devicepairing.find_first(
                    where={"ios_device_id": device_info["device_id"]}
                )
                if pairing:
                    device_name = pairing.device_name
            
            device = await prisma.wearabledevice.create(
                data={
                    "patient_id": patient_id,
                    "name": device_name,
                    "type": device_info["device_type"],
                    "device_id": device_info["device_id"],
                    "is_connected": device_info["is_connected"],
                }
            )
            logger.info("Registered new Apple device", device_id=device_info["device_id"])
        
        # Store individual metrics in MongoDB
        result = await WearablesService.store_individual_metrics(
            patient_id=patient_id,
            device_id=device_info["device_id"],
            metrics=individual_metrics
        )
        
        # Also store aggregated sleep stages in a summary document
        if any(s > 0 for s in sleep_stages.values()):
            mongodb = get_mongodb()
            sleep_summary = {
                "patient_id": patient_id,
                "device_id": device_info["device_id"],
                "type": "sleep_stages_summary",
                "timestamp": datetime.utcnow(),
                **sleep_stages,
                "total_sleep": sum(sleep_stages.values()),
            }
            await mongodb.sleep_summaries.update_one(
                {"patient_id": patient_id, "device_id": device_info["device_id"]},
                {"$set": sleep_summary},
                upsert=True
            )
            logger.info("Stored sleep stages summary", patient_id=patient_id, **sleep_stages)
        
        logger.info(
            "Imported Apple Health data",
            patient_id=patient_id,
            device_id=device_info["device_id"],
            total_metrics=result["total_metrics"],
            stored=result["stored_count"],
            duplicates=result["duplicate_count"],
            errors=result["error_count"],
            was_duplicate=result["was_duplicate"]
        )
        
        return {
            "message": "Apple Health data imported successfully" if not result["was_duplicate"] else "Apple Health data already exists (deduplicated)",
            "device_id": device_info["device_id"],
            "device_type": device_info["device_type"],
            "total_metrics": result["total_metrics"],
            "stored_count": result["stored_count"],
            "duplicate_count": result["duplicate_count"],
            "error_count": result["error_count"],
            "was_duplicate": result["was_duplicate"],
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
async def import_apple_health_batch(exports: List[AppleHealthExport]):
    """
    Import multiple Apple Health JSON exports at once.
    
    **Public endpoint - No authentication required for testing.**
    
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
        
        # Use userId from first export or default
        patient_id = exports[0].userId if exports and exports[0].userId else "test_patient_001"
        
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
            
            was_duplicate = result.get("was_duplicate", False)
            
            logger.info(
                "Imported Apple Health batch",
                patient_id=patient_id,
                export_count=batch_result["total_exports"],
                metrics_count=batch_result["total_metrics"],
                was_duplicate=was_duplicate
            )
        
        return {
            "message": "Apple Health batch imported successfully",
            "exports_processed": batch_result["total_exports"],
            "total_metrics": batch_result["total_metrics"],
            "unique_metrics": batch_result["unique_metrics"],
            "devices": batch_result["devices"],
            "date_range": batch_result["date_range"],
            "aggregated_values": cloudcare_metrics,
            "was_duplicate": was_duplicate if cloudcare_metrics else False,
        }
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error("Failed to import Apple Health batch", error=str(e), exc_info=True)
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Failed to import batch: {str(e)}"
        )


@router.post("/devices/pair", response_model=DevicePairingResponse, status_code=status.HTTP_201_CREATED)
async def pair_ios_device(pairing: DevicePairingRequest):
    """
    Pair an iOS device with an Android user account.
    
    **Flow**:
    1. iOS CloudSync app generates QR code with device info
    2. Android app scans QR code
    3. Android app calls this endpoint with scanned data + androidUserId
    4. Backend creates pairing record
    5. Future health data uploads from iOS are linked to Android user
    
    **Validation**:
    - Checks pairing code not expired (15 minutes)
    - Verifies iOS device exists in database
    - Creates or updates pairing record
    """
    prisma = get_prisma()
    
    try:
        # Validate expiration
        from datetime import datetime
        expires_at = datetime.fromisoformat(pairing.expiresAt.replace('Z', '+00:00'))
        now = datetime.now(expires_at.tzinfo)
        
        if now > expires_at:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Pairing code expired. Please generate a new QR code on iOS device."
            )
        
        # Check if iOS device is already registered
        ios_device = await prisma.wearabledevice.find_unique(
            where={"device_id": pairing.ios_device_id}
        )
        
        if not ios_device:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="iOS device not found. Please sync health data from iOS app first."
            )
        
        # Check if pairing already exists
        existing_pairing = await prisma.devicepairing.find_first(
            where={
                "ios_device_id": pairing.ios_device_id,
                "android_user_id": pairing.android_user_id
            }
        )
        
        if existing_pairing:
            # Update existing pairing
            updated_pairing = await prisma.devicepairing.update(
                where={"id": existing_pairing.id},
                data={
                    "is_active": True,
                    "pairing_code": pairing.pairing_code,
                    "device_name": pairing.device_name,
                    "device_type": pairing.deviceType,
                }
            )
            
            logger.info(
                "Updated existing device pairing",
                ios_device_id=pairing.ios_device_id,
                android_user_id=pairing.android_user_id
            )
            
            return DevicePairingResponse(
                message="Device pairing updated successfully",
                pairing_id=updated_pairing.id,
                ios_user_id=pairing.userId,
                ios_device_id=pairing.ios_device_id,
                android_user_id=pairing.android_user_id,
                paired_at=updated_pairing.paired_at,
                device_name=pairing.device_name
            )
        
        # Create new pairing
        new_pairing = await prisma.devicepairing.create(
            data={
                "ios_user_id": pairing.userId,
                "ios_device_id": pairing.ios_device_id,
                "android_user_id": pairing.android_user_id,
                "device_name": pairing.device_name,
                "device_type": pairing.deviceType,
                "pairing_code": pairing.pairing_code,
                "is_active": True,
            }
        )
        
        # Also create a WearableDevice entry for the Android user if it doesn't exist
        # This allows the device to show up in the user's device list
        android_device = await prisma.wearabledevice.find_first(
            where={
                "device_id": pairing.ios_device_id,
                "patient_id": pairing.android_user_id
            }
        )
        
        if not android_device:
            await prisma.wearabledevice.create(
                data={
                    "patient_id": pairing.android_user_id,
                    "name": pairing.device_name,
                    "type": pairing.deviceType,
                    "device_id": pairing.ios_device_id,
                    "is_connected": True,
                }
            )
        
        logger.info(
            "Created new device pairing",
            pairing_id=new_pairing.id,
            ios_device_id=pairing.ios_device_id,
            android_user_id=pairing.android_user_id
        )
        
        return DevicePairingResponse(
            message="Device paired successfully! Health data from iOS will now be available on Android.",
            pairing_id=new_pairing.id,
            ios_user_id=pairing.userId,
            ios_device_id=pairing.ios_device_id,
            android_user_id=pairing.android_user_id,
            paired_at=new_pairing.paired_at,
            device_name=pairing.device_name
        )
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error("Failed to pair device", error=str(e), exc_info=True)
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Failed to pair device: {str(e)}"
        )


@router.get("/devices/paired", response_model=List[WearableDeviceResponse])
async def get_paired_devices(android_user_id: str):
    """
    Get all iOS devices paired with an Android user account.
    
    **Query Parameters**:
    - android_user_id: The Android user ID to get paired devices for
    
    **Returns**: List of paired iOS devices as WearableDeviceResponse
    """
    prisma = get_prisma()
    
    try:
        # Get all wearable devices for this user
        devices = await prisma.wearabledevice.find_many(
            where={
                "patient_id": android_user_id
            },
            order={"created_at": "desc"}
        )
        
        return [WearableDeviceResponse.model_validate(d) for d in devices]
        
    except Exception as e:
        logger.error("Failed to get paired devices", error=str(e))
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to retrieve paired devices"
        )


@router.delete("/devices/unpair/{pairing_id}", status_code=status.HTTP_204_NO_CONTENT)
async def unpair_device(pairing_id: str):
    """
    Unpair an iOS device from Android user account.
    
    This marks the pairing as inactive but doesn't delete historical data.
    """
    prisma = get_prisma()
    
    try:
        pairing = await prisma.devicepairing.find_unique(where={"id": pairing_id})
        
        if not pairing:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="Pairing not found"
            )
        
        await prisma.devicepairing.update(
            where={"id": pairing_id},
            data={"is_active": False}
        )
        
        logger.info("Unpaired device", pairing_id=pairing_id)
        return None
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error("Failed to unpair device", error=str(e))
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to unpair device"
        )


@router.get("/metrics/aggregated")
async def get_aggregated_metrics(
    patient_id: str,
    period: str = "daily",
    days: int = 30
):
    """
    Get aggregated metrics by time period.
    
    **Public endpoint for testing - accepts patient_id as query parameter.**
    
    Query Parameters:
    - patient_id: Patient's unique ID (required)
    - period: Aggregation period - "hourly", "daily", or "weekly" (default: daily)
    - days: Number of days to look back (default: 30)
    
    Returns aggregated data grouped by metric type and time period:
    {
        "steps": [{"date": "2025-11-15", "total": 8523, "avg": 8523, ...}, ...],
        "heart_rate": [{"date": "2025-11-15", "avg": 75, "min": 62, "max": 145}, ...],
        ...
    }
    """
    try:
        metrics = await WearablesService.get_aggregated_metrics(patient_id, period, days)
        return {
            "patient_id": patient_id,
            "period": period,
            "days": days,
            "metrics": metrics
        }
    except Exception as e:
        logger.error("Failed to get aggregated metrics", error=str(e))
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Failed to retrieve aggregated metrics: {str(e)}"
        )


@router.get("/metrics/by-type")
async def get_metrics_by_type(
    patient_id: str,
    metric_type: str,
    start_date: Optional[str] = None,
    end_date: Optional[str] = None
):
    """
    Get specific metric type over date range.
    
    **Public endpoint for testing - accepts patient_id as query parameter.**
    
    Query Parameters:
    - patient_id: Patient's unique ID (required)
    - metric_type: Type of metric - "heart_rate", "steps", "calories", etc. (required)
    - start_date: Optional start date in ISO format (e.g., "2025-11-01T00:00:00Z")
    - end_date: Optional end date in ISO format
    
    Returns time-series array of individual metrics:
    [
        {
            "metric_type": "heart_rate",
            "value": 85,
            "unit": "count/min",
            "timestamp": "2025-11-15T17:55:21.000Z",
            ...
        },
        ...
    ]
    """
    try:
        metrics = await WearablesService.get_metrics_by_type(
            patient_id, metric_type, start_date, end_date
        )
        return {
            "patient_id": patient_id,
            "metric_type": metric_type,
            "start_date": start_date,
            "end_date": end_date,
            "count": len(metrics),
            "metrics": metrics
        }
    except Exception as e:
        logger.error("Failed to get metrics by type", error=str(e))
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Failed to retrieve metrics: {str(e)}"
        )


@router.get("/summary/today")
async def get_today_summary(patient_id: str):
    """
    Get aggregated summary for today with comparison to yesterday.
    
    **Public endpoint for testing - accepts patient_id as query parameter.**
    
    Query Parameters:
    - patient_id: Patient's unique ID (required)
    
    Returns today's aggregated metrics with percentage changes:
    {
        "steps": {"total": 8523, "change": "+12%"},
        "heart_rate": {"avg": 75, "min": 62, "max": 145, "change": "-3%"},
        "calories": {"total": 2145, "change": "+8%"},
        "distance": {"total": 6.2, "unit": "km", "change": "+15%"},
        ...
    }
    """
    try:
        summary = await WearablesService.get_today_summary(patient_id)
        return {
            "patient_id": patient_id,
            "date": datetime.now().strftime("%Y-%m-%d"),
            "summary": summary
        }
    except Exception as e:
        logger.error("Failed to get today summary", error=str(e))
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Failed to retrieve today's summary: {str(e)}"
        )

