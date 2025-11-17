# Data Consistency Report: iOS ↔ Backend ↔ Android

## ✅ Database Reset Complete

Database has been reset with snake_case schema. All old data deleted.

---

## iOS App → Backend Data Flow

### What iOS Sends (HealthDataExport)
```swift
{
  "userId": "3228128A-7110-4D47-8EDB-3A9160E3808A",
  "deviceId": "207791ED-2518-485D-B4D8-55A23525A485",
  "exportTimestamp": "2025-11-17T06:49:53.123Z",
  "dataRange": {
    "startDate": "2025-11-16T06:49:53.123Z",
    "endDate": "2025-11-17T06:49:53.123Z"
  },
  "metrics": [
    {
      "type": "HKQuantityTypeIdentifierHeartRate",
      "startDate": "2025-11-17T06:52:56Z",
      "endDate": "2025-11-17T06:52:56Z",
      "value": 79,
      "unit": "bpm",
      "sourceApp": "com.apple.health",
      "metadata": {"device": "Apple Watch"}
    }
  ]
}
```

**Field Names**: ✅ **camelCase** (userId, deviceId, exportTimestamp, startDate, endDate, sourceApp)

### What iOS Sends (PairingData for QR Code)
```swift
{
  "userId": "3228128A-7110-4D47-8EDB-3A9160E3808A",
  "deviceId": "207791ED-2518-485D-B4D8-55A23525A485",
  "deviceName": "Priyodip's iPhone",
  "deviceType": "apple_health_iphone",
  "generatedAt": "2025-11-17T10:30:00.000Z",
  "expiresAt": "2025-11-17T10:45:00.000Z",
  "pairingCode": "ABCD-1234"
}
```

**Field Names**: ✅ **camelCase** (userId, deviceId, deviceName, deviceType, generatedAt, expiresAt, pairingCode)

---

## Backend → Android Data Flow

### What Backend Returns (After Snake_Case Migration)
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "patient_id": "123e4567-e89b-12d3-a456-426614174000",
  "name": "Apple Watch",
  "type": "apple_health_iphone",
  "device_id": "207791ED-2518-485D-B4D8-55A23525A485",
  "is_connected": true,
  "battery_level": 100,
  "last_sync_time": "2025-11-17T10:00:00Z",
  "data_points_synced": 1234,
  "created_at": "2025-11-16T08:00:00Z"
}
```

**Field Names**: ✅ **snake_case** (patient_id, device_id, is_connected, battery_level, last_sync_time, data_points_synced, created_at)

### What Android Expects (CURRENT - NEEDS UPDATE)
```kotlin
data class WearableDevice(
    val id: Int,  // ❌ WRONG TYPE - Should be String (UUID)
    @SerializedName("name")  // ⚠️ UNNECESSARY - Backend now uses snake_case
    val name: String?,
    @SerializedName("type")
    val type: String?,
    @SerializedName("is_connected")
    val isConnected: Boolean,  // ❌ WRONG NAME - Should be is_connected
    @SerializedName("battery_level")
    val batteryLevel: Int,  // ❌ WRONG NAME - Should be battery_level
    @SerializedName("last_sync_time")
    val lastSyncTime: String?,  // ❌ WRONG NAME - Should be last_sync_time
    @SerializedName("data_points_synced")
    val dataPointsSynced: Int,  // ❌ WRONG NAME - Should be data_points_synced
    val iconType: DeviceIconType = DeviceIconType.FITNESS_TRACKER
)
```

---

## 🔴 CRITICAL INCONSISTENCIES

### Issue 1: iOS Uses camelCase, Backend Expects camelCase (in Pydantic models)
**Status**: ✅ **WORKING** - Backend Pydantic models accept camelCase from iOS

Backend has these models that accept iOS format:
```python
class AppleHealthExport(BaseModel):
    deviceId: str  # Accepts camelCase from iOS
    userId: Optional[str]
    exportTimestamp: str
    metrics: List[AppleHealthMetric]

class AppleHealthMetric(BaseModel):
    type: str
    value: float
    unit: str
    startDate: str
    endDate: str
    sourceApp: Optional[str]
```

### Issue 2: Backend Prisma Uses snake_case, Android Has @SerializedName
**Status**: ⚠️ **NEEDS FIX** - Remove @SerializedName annotations

Backend now returns pure snake_case:
```python
class WearableDeviceResponse(BaseModel):
    patient_id: str
    device_id: str
    is_connected: bool
    # NO aliases anymore
```

Android should receive:
```json
{
  "patient_id": "uuid",
  "device_id": "uuid",
  "is_connected": true
}
```

But Android models use camelCase property names with @SerializedName mappings:
```kotlin
@SerializedName("is_connected")
val isConnected: Boolean  // ❌ Wrong - should be: val is_connected: Boolean
```

### Issue 3: Android Uses Int for IDs, Backend Uses String (UUID)
**Status**: ❌ **BREAKING** - Type mismatch

```kotlin
data class WearableDevice(
    val id: Int,  // ❌ Backend returns String UUID
```

Backend returns:
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000"
}
```

---

## 🔧 REQUIRED FIXES

### Fix 1: Update Android WearableDevice Model

**File**: `CloudCare Android App/app/src/main/java/com/example/cloudcareapp/data/model/Patient.kt`

**BEFORE** (Current - Incorrect):
```kotlin
data class WearableDevice(
    val id: Int,
    @SerializedName("name")
    val name: String?,
    @SerializedName("type")
    val type: String?,
    @SerializedName("is_connected")
    val isConnected: Boolean,
    @SerializedName("battery_level")
    val batteryLevel: Int,
    @SerializedName("last_sync_time")
    val lastSyncTime: String?,
    @SerializedName("data_points_synced")
    val dataPointsSynced: Int,
    val iconType: DeviceIconType = DeviceIconType.FITNESS_TRACKER
)
```

**AFTER** (Correct - snake_case):
```kotlin
data class WearableDevice(
    val id: String,  // Changed to String for UUID
    val name: String?,
    val type: String?,
    val is_connected: Boolean,  // Removed @SerializedName
    val battery_level: Int,
    val last_sync_time: String?,
    val data_points_synced: Int,
    val created_at: String,  // Added missing field
    val patient_id: String,  // Added missing field
    val icon_type: DeviceIconType = DeviceIconType.FITNESS_TRACKER
)
```

### Fix 2: Update Android HealthMetric Models

**File**: `CloudCare Android App/app/src/main/java/com/example/cloudcareapp/data/model/HealthMetric.kt`

**Remove ALL @SerializedName annotations** - They're no longer needed!

**BEFORE**:
```kotlin
data class HealthMetric(
    @SerializedName("metric_type")
    val metricType: String,
    @SerializedName("start_date")
    val startDate: String?,
)
```

**AFTER**:
```kotlin
data class HealthMetric(
    val metric_type: String,  // No annotation needed
    val start_date: String?,
)
```

### Fix 3: Update All Other Response Models

Apply same fix to:
- `TodaySummaryResponse`
- `AggregatedMetricsResponse`
- `MetricsByTypeResponse`
- `RecentMetricsResponse`

All should use pure snake_case without @SerializedName.

---

## 📊 CURRENT DATA FLOW (After Fixes)

```
iOS CloudSync App
    ↓ (camelCase JSON)
POST /api/v1/wearables/import/apple-health
{
  "deviceId": "...",
  "userId": "...",
  "metrics": [...]
}
    ↓
Backend (Pydantic accepts camelCase)
class AppleHealthExport:
    deviceId: str  ← Accepts from iOS
    userId: str
    ↓
Backend (Prisma stores as snake_case)
await prisma.wearabledevice.create(
    data={
        "device_id": device_id,  ← Stores as snake_case
        "patient_id": patient_id,
        "is_connected": True
    }
)
    ↓ (snake_case JSON)
GET /api/v1/wearables/devices/paired
{
  "device_id": "...",
  "patient_id": "...",
  "is_connected": true
}
    ↓
Android App (Receives snake_case)
data class WearableDevice(
    val device_id: String,  ← Matches backend
    val patient_id: String,
    val is_connected: Boolean
)
```

---

## ✅ VERIFICATION CHECKLIST

After applying fixes:

- [ ] iOS app can upload health data (POST /import/apple-health)
- [ ] Backend stores data with snake_case fields in PostgreSQL
- [ ] Backend stores metrics with snake_case in MongoDB
- [ ] Android app can retrieve devices (GET /devices/paired)
- [ ] Android models parse JSON without errors
- [ ] No @SerializedName annotations in Android models
- [ ] All IDs are String type (UUID format)
- [ ] QR pairing works (iOS camelCase → Backend accepts → Android snake_case)

---

## 🎯 SUMMARY

**iOS → Backend**: ✅ Working (Pydantic accepts camelCase)
**Backend → Android**: ⚠️ Needs Fix (Remove @SerializedName, use snake_case)
**Database Schema**: ✅ All snake_case (migration complete)

**Action Required**: Update Android models to match snake_case backend responses.
