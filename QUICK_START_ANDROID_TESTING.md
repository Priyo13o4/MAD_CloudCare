# 🚀 Quick Start Guide - Android Health Metrics Integration

## Testing the Backend Endpoints (curl)

```bash
# 1. Test Today's Summary (Main endpoint for dashboard)
curl "https://cloudcare.pipfactor.com/api/v1/wearables/summary/today?patient_id=3228128A-7110-4D47-8EDB-3A9160E3808A"

# 2. Test Recent Metrics (Last 24 hours)
curl "https://cloudcare.pipfactor.com/api/v1/wearables/metrics/recent?patient_id=3228128A-7110-4D47-8EDB-3A9160E3808A&hours=24"

# 3. Test Aggregated Metrics (Daily for last 7 days)
curl "https://cloudcare.pipfactor.com/api/v1/wearables/metrics/aggregated?patient_id=3228128A-7110-4D47-8EDB-3A9160E3808A&period=daily&days=7"

# 4. Test Specific Metric Type (Heart Rate)
curl "https://cloudcare.pipfactor.com/api/v1/wearables/metrics/by-type?patient_id=3228128A-7110-4D47-8EDB-3A9160E3808A&metric_type=heart_rate"
```

## Testing the Android App

### 1. Build & Run
```bash
cd "CloudCare Android App"
# Open in Android Studio
# Click Run (Shift+F10)
```

### 2. What to Look For

**On Dashboard Screen:**
- ✅ "Today's Health Summary" card appears
- ✅ Green "Live" badge in top-right corner
- ✅ Real metrics displayed:
  - Steps: Actual count (not mock 4932)
  - Heart Rate: Real average with "Normal" status
  - Calories: Real count with goal percentage
  - Sleep: Shows "No data" (not implemented yet)
- ✅ Trend arrows showing percentage changes

### 3. Check Logs (Logcat)

**Filter:** `HealthMetricsRepository` or `DashboardViewModel`

**Expected Logs:**
```
D/HealthMetricsRepository: Fetching today's summary for patient: 3228128A-7110-4D47-8EDB-3A9160E3808A
D/HealthMetricsRepository: Successfully fetched today's summary for 2025-11-16
```

**HTTP Logs:**
```
I/okhttp.OkHttpClient: --> GET https://cloudcare.pipfactor.com/api/v1/wearables/summary/today?patient_id=3228128A-7110-4D47-8EDB-3A9160E3808A
I/okhttp.OkHttpClient: <-- 200 OK (235ms)
```

### 4. Test Error Handling

**Turn off WiFi:**
1. Disable device internet
2. Pull down to refresh dashboard
3. Should show:
   - Gray "Cached" badge
   - Orange warning message
   - Last successful data

## Key Files to Review

### Backend
- `backend/app/api/v1/wearables.py` - New endpoints (lines ~600-750)
- `backend/app/services/wearables_service.py` - New methods (lines ~400-650)

### Android
- `app/src/main/java/com/example/cloudcareapp/data/model/HealthMetric.kt` - Data models
- `app/src/main/java/com/example/cloudcareapp/data/remote/RetrofitClient.kt` - API client
- `app/src/main/java/com/example/cloudcareapp/data/repository/HealthMetricsRepository.kt` - Repository
- `app/src/main/java/com/example/cloudcareapp/ui/screens/dashboard/DashboardViewModel.kt` - Real data integration
- `app/src/main/java/com/example/cloudcareapp/ui/screens/dashboard/DashboardScreen.kt` - UI component

## Patient Test Data

**Patient ID:** `3228128A-7110-4D47-8EDB-3A9160E3808A`

**Available Metrics:**
- Heart Rate: 12,305 readings
- Calories: 6,960 readings  
- Distance: 4,491 readings
- Steps: 3,233 readings
- Flights Climbed: 163 readings
- Resting Heart Rate: 30 readings
- VO2 Max: 3 readings

**Total:** 27,185+ metrics from iOS CloudSync app

## Troubleshooting Quick Fixes

### "Build failed" in Android Studio
```bash
# Clean build
Build → Clean Project
Build → Rebuild Project
```

### "Network error" in app
1. Check internet connection
2. Verify URL: `https://cloudcare.pipfactor.com`
3. Test with curl first
4. Check Logcat for details

### "No data showing"
1. Check patient_id is correct
2. Verify backend is running
3. Look for "Live" vs "Cached" badge
4. Check error message in card

### Backend returns 500 error
1. Check MongoDB is running
2. Verify patient exists in database
3. Check backend logs
4. Test with different patient_id

## Expected Results

### Backend (curl)
```json
{
  "patient_id": "3228128A-7110-4D47-8EDB-3A9160E3808A",
  "date": "2025-11-16",
  "summary": {
    "steps": {"total": 8523.0, "change": "+12%"},
    "heart_rate": {"avg": 75.0, "min": 62.0, "max": 145.0, "change": "-3%"},
    "calories": {"total": 2145.0, "change": "+8%"},
    "distance": {"total": 6.2, "unit": "km", "change": "+15%"},
    "flights_climbed": {"total": 12.0, "change": "+5%"}
  }
}
```

### Android Dashboard
![Health Summary Card showing:
- Steps: 8,523 steps (+12% ↑)
- Heart Rate: 75 bpm (Normal)
- Calories: 2,145 (107% of goal)
- Sleep: No data
- Green "Live" badge]

## Success Checklist

- [ ] Backend endpoints respond with 200 OK
- [ ] Actual patient data returned (not empty)
- [ ] Android app builds without errors
- [ ] Dashboard shows Health Summary card
- [ ] Metrics match backend data
- [ ] "Live" badge is green when online
- [ ] Falls back to "Cached" when offline
- [ ] Logcat shows successful API calls
- [ ] No crashes or freezes

## Next: Run in Android Studio

**You mentioned you'll handle Android testing manually - perfect!**

1. Open `CloudCare Android App` folder in Android Studio
2. Wait for Gradle sync
3. Click Run
4. Check Dashboard for real data
5. Verify "Live" badge and metrics

All code is ready for you to test! 🎉
