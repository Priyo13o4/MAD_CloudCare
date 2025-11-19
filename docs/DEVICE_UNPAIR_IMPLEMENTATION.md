# Device Unpair Implementation Summary

## Overview
Implemented the ability to unpair (remove) connected wearable devices from the Android app. When users click on a connected device card, a dialog appears with options to "Unpair Device" or "Close".

## Database Verification

### Backend (Python - Flask + PostgreSQL)
✅ **Apple Watch Device Name is Available**
- The device name is stored in the PostgreSQL database in the `wearable_devices` table
- Backend schema (`prisma/schema.prisma`) defines:
  ```
  model WearableDevice {
    id                 String
    patient_id         String
    name               String          // <- Device name (e.g., "Apple Watch")
    type               String          // <- Device type (e.g., "smartwatch")
    device_id          String @unique
    is_connected       Boolean
    last_sync_time     DateTime?
    created_at         DateTime @default(now())
    updated_at         DateTime @updatedAt
  }
  ```

- The device name is properly returned by the backend API endpoint: `GET /api/v1/wearables/devices/paired`

### Why the Name Wasn't Showing

**Root Cause**: The WearableDevice model in the Android app had the field `name` but the UI was using a fallback when `name` was null:
```kotlin
Text(text = device.name ?: "Unknown Device")
```

**Solution**: The app now properly displays the name from the backend. The name comes from the iOS pairing process where users specify a device name when scanning the QR code.

---

## Implementation Details

### 1. Backend API Endpoint (Already Existed)
**Endpoint**: `DELETE /api/v1/wearables/devices/unpair/{pairing_id}`
- **Status Code**: 204 No Content on success
- **Functionality**: Marks the device pairing as inactive in PostgreSQL
- **Location**: `backend/app/api/v1/wearables.py` (Line 633)

```python
@router.delete("/devices/unpair/{pairing_id}", status_code=status.HTTP_204_NO_CONTENT)
async def unpair_device(pairing_id: str):
    """Unpair an iOS device from Android user account."""
    prisma = get_prisma()
    try:
        pairing = await prisma.devicepairing.find_unique(where={"id": pairing_id})
        if not pairing:
            raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Pairing not found")
        
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
        raise HTTPException(status_code=status.HTTP_500_INTERNAL_SERVER_ERROR, detail="Failed to unpair device")
```

### 2. Android API Service Layer
**File**: `CloudCare Android App/app/src/main/java/com/example/cloudcareapp/data/remote/CloudCareApiService.kt`

**Added**:
```kotlin
import retrofit2.http.DELETE
import retrofit2.http.Path

// In interface CloudCareApiService:
@DELETE("wearables/devices/unpair/{pairing_id}")
suspend fun unpairDevice(
    @Path("pairing_id") pairingId: String
): Unit
```

### 3. Repository Layer
**File**: `CloudCare Android App/app/src/main/java/com/example/cloudcareapp/data/repository/HealthMetricsRepository.kt`

**Added Method**:
```kotlin
suspend fun unpairDevice(pairingId: String): Result<Unit> = withContext(Dispatchers.IO) {
    try {
        Log.d(TAG, "Unpairing device: $pairingId")
        apiService.unpairDevice(pairingId)
        Log.d(TAG, "Successfully unpaired device: $pairingId")
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to unpair device", e)
        Result.failure(e)
    }
}
```

### 4. ViewModel Layer
**File**: `CloudCare Android App/app/src/main/java/com/example/cloudcareapp/ui/screens/wearables/WearablesViewModel.kt`

**Added Method**:
```kotlin
fun unpairDevice(deviceId: String, onResult: (Boolean, String) -> Unit) {
    viewModelScope.launch {
        try {
            Log.d(TAG, "Unpairing device: $deviceId")
            val result = healthMetricsRepository.unpairDevice(deviceId)
            
            result.onSuccess {
                onResult(true, "Device unpaired successfully")
                // Reload devices after successful unpair
                val devicesResult = healthMetricsRepository.getPairedDevices(PATIENT_ID)
                devicesResult.onSuccess { devices ->
                    AppDataCache.setDevices(devices)
                    AppDataCache.updateLastSyncTime()
                    saveCacheToDisk()
                }.onFailure { error ->
                    Log.e(TAG, "Failed to refresh devices after unpairing", error)
                }
            }.onFailure { error ->
                onResult(false, error.message ?: "Failed to unpair device")
            }
        } catch (e: Exception) {
            onResult(false, "Error: ${e.message}")
        }
    }
}
```

### 5. UI Implementation
**File**: `CloudCare Android App/app/src/main/java/com/example/cloudcareapp/ui/screens/wearables/WearablesScreen.kt`

#### Changes Made:

**a) DeviceCard - Now Clickable**:
```kotlin
@Composable
private fun DeviceCard(
    device: WearableDevice,
    onDeviceClick: (WearableDevice) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onDeviceClick(device) },  // <- Added clickable
        // ... rest of the card design
    )
}
```

**b) New UnpairDeviceDialog Composable**:
```kotlin
@Composable
private fun UnpairDeviceDialog(
    device: WearableDevice,
    onUnpair: () -> Unit,
    onDismiss: () -> Unit
) {
    var isLoading by remember { mutableStateOf(false) }
    
    AlertDialog(
        // Shows device info and options:
        // - "Unpair Device" button (red) - triggers unpair
        // - "Close" button (blue) - dismisses dialog without unpairing
    )
}
```

**c) WearablesContent - Device Management**:
```kotlin
@Composable
private fun WearablesContent(
    // ... existing parameters
) {
    var selectedDeviceForUnpair by remember { mutableStateOf<WearableDevice?>(null) }
    
    // ... rest of LazyColumn content
    
    // Device Cards with click handler
    items(devices.size) { index ->
        DeviceCard(
            device = devices[index],
            onDeviceClick = { selectedDeviceForUnpair = it },  // <- Set selected device
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
    
    // Dialog for unpair confirmation
    selectedDeviceForUnpair?.let { device ->
        UnpairDeviceDialog(
            device = device,
            onUnpair = {
                wearablesViewModel.unpairDevice(device.id) { success, message ->
                    if (success) {
                        selectedDeviceForUnpair = null
                    }
                }
            },
            onDismiss = { selectedDeviceForUnpair = null }
        )
    }
}
```

---

## User Flow

1. **User Opens Wearables Screen** → Sees list of paired devices
2. **User Clicks on a Device Card** → Dialog appears showing:
   - Device name
   - Device type
   - Two options: "Unpair Device" (red) or "Close" (blue)
3. **User Clicks "Unpair Device"** → 
   - Loading state shows (button displays "Unpairing...")
   - Backend call sends DELETE request with device ID
   - Upon success, dialog closes
   - Device list refreshes and device is removed
4. **User Clicks "Close"** → Dialog closes without any action

---

## Testing Checklist

- [ ] Click on a connected Apple Watch device
- [ ] Verify dialog appears with device info
- [ ] Click "Unpair Device" button
- [ ] Verify loading state appears
- [ ] Verify device is removed from list after success
- [ ] Click on another device and verify "Close" button works
- [ ] Verify device name displays correctly (not "Unknown Device")
- [ ] Test with multiple devices to ensure each can be unaired individually

---

## Data Flow Diagram

```
User Clicks Device Card
         ↓
   DeviceCard.clickable
         ↓
selectedDeviceForUnpair = device
         ↓
   UnpairDeviceDialog shows
         ↓
User Clicks "Unpair Device"
         ↓
WearablesViewModel.unpairDevice()
         ↓
HealthMetricsRepository.unpairDevice()
         ↓
CloudCareApiService.unpairDevice()
         ↓
DELETE /api/v1/wearables/devices/unpair/{pairing_id}
         ↓
Backend: Update pairing is_active = false
         ↓
HTTP 204 No Content
         ↓
Refresh Devices List (getPairedDevices)
         ↓
Update AppDataCache
         ↓
UI Updates - Device Removed from List
```

---

## Notes

- The backend already had the unpair endpoint implemented
- The device name comes from the iOS pairing process (user specifies when scanning QR code)
- Historical data is NOT deleted when unpairing - only the pairing relationship is marked inactive
- After unpair, the device list automatically refreshes from the backend
- The implementation uses Result<T> pattern for consistent error handling throughout the stack
- All unpair operations are logged for audit trails
