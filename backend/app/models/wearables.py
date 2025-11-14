"""
Pydantic Models for Wearables

Data validation models for wearable devices and health metrics.
"""

from pydantic import BaseModel, Field
from typing import Optional, List, Dict, Any
from datetime import datetime


class HealthMetrics(BaseModel):
    """Health metrics from wearable devices."""
    heart_rate: Optional[int] = Field(None, ge=30, le=250, description="Heart rate in BPM")
    steps: Optional[int] = Field(None, ge=0, description="Step count")
    sleep_hours: Optional[float] = Field(None, ge=0, le=24, description="Sleep duration in hours")
    calories: Optional[int] = Field(None, ge=0, description="Calories burned")
    oxygen_level: Optional[int] = Field(None, ge=0, le=100, description="Blood oxygen saturation %")
    blood_pressure_systolic: Optional[int] = Field(None, ge=60, le=250, description="Systolic BP")
    blood_pressure_diastolic: Optional[int] = Field(None, ge=40, le=150, description="Diastolic BP")


class WearableDataCreate(BaseModel):
    """Request model for creating wearable data entry."""
    device_id: str = Field(..., description="Unique device identifier")
    metrics: HealthMetrics


class WearableDeviceCreate(BaseModel):
    """Request model for registering a wearable device."""
    name: str = Field(..., min_length=1, max_length=100)
    type: str = Field(..., description="Device type (e.g., 'fitness_tracker', 'smart_watch')")
    device_id: str = Field(..., description="External device ID")


class WearableDeviceResponse(BaseModel):
    """Response model for wearable device."""
    id: str
    patient_id: str
    name: str
    type: str
    device_id: str
    is_connected: bool
    battery_level: int
    last_sync_time: Optional[datetime]
    data_points_synced: int
    created_at: datetime
    
    class Config:
        from_attributes = True


class AppleHealthMetric(BaseModel):
    """Single health metric from Apple Health export."""
    type: str = Field(..., description="HealthKit type identifier")
    value: float = Field(..., description="Metric value")
    unit: str = Field(..., description="Unit of measurement")
    startDate: str = Field(..., description="ISO timestamp when measurement started")
    endDate: str = Field(..., description="ISO timestamp when measurement ended")
    sourceApp: Optional[str] = Field(None, description="Source app identifier")
    metadata: Optional[Dict[str, Any]] = Field(None, description="Additional metadata")


class AppleHealthDataRange(BaseModel):
    """Date range for Apple Health export."""
    startDate: str = Field(..., description="Start date of data range")
    endDate: str = Field(..., description="End date of data range")


class AppleHealthExport(BaseModel):
    """
    Complete Apple Health export structure.
    
    This matches the format exported from iPhone/Apple Watch.
    """
    deviceId: str = Field(..., description="Apple device unique identifier")
    userId: Optional[str] = Field(None, description="User identifier (if available)")
    exportTimestamp: str = Field(..., description="When data was exported")
    dataRange: Optional[AppleHealthDataRange] = Field(None, description="Date range of data")
    metrics: List[AppleHealthMetric] = Field(..., description="List of health metrics")
    
    class Config:
        json_schema_extra = {
            "example": {
                "deviceId": "5233E7EE-EF98-4AEB-9E01-1A81FAB21C43",
                "userId": "CB99F596-9067-4786-AD08-639BBF0A96C0",
                "exportTimestamp": "2025-11-13T06:49:53Z",
                "dataRange": {
                    "startDate": "2025-11-12T06:49:53Z",
                    "endDate": "2025-11-13T06:49:53Z"
                },
                "metrics": [
                    {
                        "type": "HKQuantityTypeIdentifierHeartRate",
                        "value": 79,
                        "unit": "bpm",
                        "startDate": "2025-11-12T06:52:56Z",
                        "endDate": "2025-11-12T06:52:56Z",
                        "sourceApp": "com.apple.health",
                        "metadata": {"device": "Apple Watch"}
                    }
                ]
            }
        }

