# CloudSync iOS App Documentation

**Version:** 1.0  
**Last Updated:** November 2025  
**Minimum iOS:** 16.0  
**Platform:** iPhone, Apple Watch

---

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Technology Stack](#technology-stack)
- [Features](#features)
- [HealthKit Integration](#healthkit-integration)
- [QR Code Pairing](#qr-code-pairing)
- [Setup & Build](#setup--build)

---

## Overview

CloudSync is an iOS companion app for CloudCare that syncs health data from iPhone and Apple Watch to the CloudCare backend. It enables patients to:

- **Pair with Android**: QR code-based pairing with Android CloudCare app
- **HealthKit Integration**: Sync health metrics from Apple Health
- **Automatic Background Sync**: Upload health data periodically
- **Privacy First**: User controls what metrics are shared

**Key Features:**
- SwiftUI interface
- HealthKit authorization and data querying
- QR code generation for device pairing
- Background data uploads
- Upload history tracking
- Configurable metric selection

---

## Architecture

### SwiftUI + MVVM Pattern

```
┌─────────────────────────────────────┐
│        SwiftUI Views                │
│  ContentView, SettingsView, etc.    │
└──────────────┬──────────────────────┘
               │ observes @Published
┌──────────────▼──────────────────────┐
│          Managers                   │
│  HealthKitManager, PairingManager   │
│  NetworkManager, UploadTracker      │
└──────────────┬──────────────────────┘
               │ calls
┌──────────────▼──────────────────────┐
│          iOS Frameworks             │
│  HealthKit, CoreImage (QR), URLSession│
└─────────────────────────────────────┘
```

### File Structure

```
CloudSync/
├── CloudSyncApp.swift           # App entry point
├── ContentView.swift            # Main view
├── SettingsView.swift           # Settings screen
├── PairingQRView.swift          # QR code generation
├── UploadHistoryView.swift      # Upload history
├── Models.swift                 # Data models
├── HealthKitManager.swift       # HealthKit operations
├── NetworkManager.swift         # API calls
├── PairingManager.swift         # QR pairing logic
├── DataProcessor.swift          # Data aggregation
├── UploadTracker.swift          # Upload tracking
├── SettingsManager.swift        # User preferences
├── Info.plist                   # App config
└── CloudSync.entitlements       # Capabilities
```

---

## Technology Stack

| Component | Technology |
|-----------|-----------|
| Language | Swift 5.9+ |
| UI Framework | SwiftUI |
| Architecture | MVVM |
| Health Data | HealthKit |
| QR Codes | CoreImage |
| Networking | URLSession |
| Data Persistence | UserDefaults |
| Background Tasks | BackgroundTasks framework |

---

## Features

### 1. Device Pairing (QR Code)

**Purpose:** Link Apple Watch data to Android CloudCare account

**Flow:**
1. User opens CloudSync app
2. Tap "Generate Pairing QR Code"
3. QR code displays with pairing data
4. Android app scans QR code
5. Devices are paired via backend API
6. Health data syncs to Android account

**QR Code Data Structure:**
```json
{
  "iosUserId": "uuid",
  "iosDeviceId": "device-identifier",
  "deviceName": "iPhone 13",
  "deviceType": "iphone",
  "pairingCode": "random-code",
  "timestamp": "2025-11-20T10:00:00Z"
}
```

**Implementation:**
```swift
class PairingManager: ObservableObject {
    func generatePairingQRCode() -> UIImage? {
        let pairingData = PairingData(
            iosUserId: getUserId(),
            iosDeviceId: getDeviceId(),
            deviceName: getDeviceName(),
            deviceType: "apple_watch",
            pairingCode: generateRandomCode(),
            timestamp: ISO8601DateFormatter().string(from: Date())
        )
        
        let jsonData = try? JSONEncoder().encode(pairingData)
        return generateQRCode(from: jsonData)
    }
}
```

### 2. HealthKit Integration

**Supported Metrics:**

| Metric Type | HealthKit Identifier | Unit |
|-------------|---------------------|------|
| Step Count | `HKQuantityTypeIdentifierStepCount` | count |
| Heart Rate | `HKQuantityTypeIdentifierHeartRate` | bpm |
| Resting Heart Rate | `HKQuantityTypeIdentifierRestingHeartRate` | bpm |
| Active Energy | `HKQuantityTypeIdentifierActiveEnergyBurned` | kcal |
| Walking/Running Distance | `HKQuantityTypeIdentifierDistanceWalkingRunning` | km |
| Flights Climbed | `HKQuantityTypeIdentifierFlightsClimbed` | count |
| VO2 Max | `HKQuantityTypeIdentifierVO2Max` | ml/kg/min |
| Sleep Analysis | `HKCategoryTypeIdentifierSleepAnalysis` | minutes |
| Workouts | `HKWorkoutTypeIdentifier` | - |

**Authorization Request:**
```swift
class HealthKitManager: ObservableObject {
    @Published var isAuthorized = false
    
    func requestAuthorization() async {
        let typesToRead = getAllHealthTypes()
        
        do {
            try await healthStore.requestAuthorization(toShare: [], read: typesToRead)
            UserDefaults.standard.set(true, forKey: "CloudSync_HealthKitAuthorized")
            isAuthorized = true
        } catch {
            authorizationError = error
        }
    }
}
```

**Data Querying:**
```swift
func queryHealthData(
    for type: HealthMetricType,
    from startDate: Date,
    to endDate: Date
) async -> [HealthMetric] {
    
    let predicate = HKQuery.predicateForSamples(
        withStart: startDate,
        end: endDate,
        options: .strictStartDate
    )
    
    return await withCheckedContinuation { continuation in
        let query = HKSampleQuery(
            sampleType: type.hkType,
            predicate: predicate,
            limit: HKObjectQueryNoLimit,
            sortDescriptors: [NSSortDescriptor(key: HKSampleSortIdentifierStartDate, ascending: true)]
        ) { _, samples, error in
            // Process samples
            let metrics = samples?.compactMap { sample -> HealthMetric? in
                guard let quantitySample = sample as? HKQuantitySample else { return nil }
                return HealthMetric(
                    type: type.identifier,
                    startDate: ISO8601DateFormatter().string(from: sample.startDate),
                    endDate: ISO8601DateFormatter().string(from: sample.endDate),
                    value: quantitySample.quantity.doubleValue(for: type.unit),
                    unit: type.unit,
                    sourceApp: sample.sourceRevision.source.bundleIdentifier,
                    metadata: nil
                )
            }
            continuation.resume(returning: metrics ?? [])
        }
        healthStore.execute(query)
    }
}
```

### 3. Data Synchronization

**Sync Modes:**
- **Manual Sync**: User triggers sync from settings
- **Automatic Background Sync**: Scheduled uploads (configurable interval)
- **Real-time Sync**: Upload immediately after new data (future)

**Sync Intervals:**
```swift
enum SyncInterval: String, CaseIterable {
    case manual = "Manual"
    case hourly = "Every Hour"
    case daily = "Once Daily"
    case weekly = "Once Weekly"
}
```

**Upload Process:**
```swift
class NetworkManager: ObservableObject {
    func uploadHealthData(export: HealthDataExport) async throws {
        let url = URL(string: "\(serverURL)/wearables/import/apple-health")!
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.setValue("Bearer \(apiKey)", forHTTPHeaderField: "Authorization")
        
        let jsonData = try JSONEncoder().encode(export)
        request.httpBody = jsonData
        
        let (data, response) = try await URLSession.shared.data(for: request)
        
        guard let httpResponse = response as? HTTPURLResponse,
              (200...299).contains(httpResponse.statusCode) else {
            throw NetworkError.uploadFailed
        }
        
        print("✅ Uploaded \(export.metrics.count) metrics successfully")
    }
}
```

### 4. Settings Management

**User Preferences:**
- **Server URL**: CloudCare backend URL
- **API Key**: Authentication token
- **Enabled Metrics**: Select which metrics to sync
- **Sync Interval**: How often to upload
- **Last Sync Date**: Track last successful sync

**SettingsView:**
```swift
struct SettingsView: View {
    @StateObject private var settingsManager = SettingsManager.shared
    
    var body: some View {
        Form {
            Section("Server Configuration") {
                TextField("Server URL", text: $settingsManager.settings.serverURL)
                SecureField("API Key", text: $settingsManager.settings.apiKey)
            }
            
            Section("Health Metrics to Sync") {
                ForEach(HealthMetricType.allCases, id: \.self) { metric in
                    Toggle(metric.displayName, isOn: binding(for: metric))
                }
            }
            
            Section("Sync Settings") {
                Picker("Sync Interval", selection: $settingsManager.settings.syncInterval) {
                    ForEach(SyncInterval.allCases, id: \.self) { interval in
                        Text(interval.rawValue).tag(interval)
                    }
                }
            }
        }
    }
}
```

### 5. Upload History

**Track Uploads:**
```swift
struct UploadRecord: Identifiable, Codable {
    let id: UUID
    let timestamp: Date
    let metricsCount: Int
    let dataRange: DateRange
    let success: Bool
    let errorMessage: String?
}

class UploadTracker: ObservableObject {
    @Published var uploadHistory: [UploadRecord] = []
    
    func recordUpload(metricsCount: Int, dataRange: DateRange, success: Bool, error: String? = nil) {
        let record = UploadRecord(
            id: UUID(),
            timestamp: Date(),
            metricsCount: metricsCount,
            dataRange: dataRange,
            success: success,
            errorMessage: error
        )
        uploadHistory.insert(record, at: 0)
        saveHistory()
    }
}
```

**UploadHistoryView:**
```swift
struct UploadHistoryView: View {
    @StateObject private var tracker = UploadTracker.shared
    
    var body: some View {
        List(tracker.uploadHistory) { record in
            VStack(alignment: .leading) {
                HStack {
                    Image(systemName: record.success ? "checkmark.circle.fill" : "xmark.circle.fill")
                        .foregroundColor(record.success ? .green : .red)
                    Text("\(record.metricsCount) metrics")
                    Spacer()
                    Text(record.timestamp, style: .relative)
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
                
                if let error = record.errorMessage {
                    Text(error)
                        .font(.caption)
                        .foregroundColor(.red)
                }
            }
        }
        .navigationTitle("Upload History")
    }
}
```

---

## HealthKit Integration

### Authorization

**Info.plist Entries:**
```xml
<key>NSHealthShareUsageDescription</key>
<string>CloudSync needs access to your health data to sync with CloudCare.</string>

<key>NSHealthUpdateUsageDescription</key>
<string>CloudSync will read your health data (not write).</string>
```

**Entitlements:**
```xml
<key>com.apple.developer.healthkit</key>
<true/>
<key>com.apple.developer.healthkit.access</key>
<array>
    <string>health-records</string>
</array>
```

### Data Fetching

**Query All Metrics:**
```swift
func fetchAllHealthData(from startDate: Date, to endDate: Date) async -> [HealthMetric] {
    var allMetrics: [HealthMetric] = []
    
    for metricType in settingsManager.settings.enabledMetrics {
        let metrics = await queryHealthData(for: metricType, from: startDate, to: endDate)
        allMetrics.append(contentsOf: metrics)
    }
    
    return allMetrics
}
```

**Export Structure:**
```swift
struct HealthDataExport: Codable {
    let userId: String
    let deviceId: String
    let exportTimestamp: String
    let dataRange: DateRange
    let metrics: [HealthMetric]
}
```

### Background Fetch

**Register Background Task:**
```swift
import BackgroundTasks

class AppDelegate: NSObject, UIApplicationDelegate {
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
    ) -> Bool {
        BGTaskScheduler.shared.register(
            forTaskWithIdentifier: "com.cloudcare.healthsync",
            using: nil
        ) { task in
            self.handleHealthSync(task: task as! BGAppRefreshTask)
        }
        return true
    }
    
    func scheduleHealthSync() {
        let request = BGAppRefreshTaskRequest(identifier: "com.cloudcare.healthsync")
        request.earliestBeginDate = Date(timeIntervalSinceNow: 3600) // 1 hour
        
        try? BGTaskScheduler.shared.submit(request)
    }
    
    func handleHealthSync(task: BGAppRefreshTask) {
        Task {
            do {
                let metrics = await HealthKitManager.shared.fetchAllHealthData()
                try await NetworkManager.shared.uploadHealthData(metrics)
                task.setTaskCompleted(success: true)
            } catch {
                task.setTaskCompleted(success: false)
            }
            scheduleHealthSync()
        }
    }
}
```

---

## QR Code Pairing

### QR Code Generation

**PairingQRView:**
```swift
struct PairingQRView: View {
    @StateObject private var pairingManager = PairingManager.shared
    @State private var qrCodeImage: UIImage?
    
    var body: some View {
        VStack(spacing: 20) {
            Text("Pair with Android Device")
                .font(.title)
            
            if let qrImage = qrCodeImage {
                Image(uiImage: qrImage)
                    .interpolation(.none)
                    .resizable()
                    .scaledToFit()
                    .frame(width: 250, height: 250)
                    .padding()
                    .background(Color.white)
                    .cornerRadius(12)
            }
            
            Text("Scan this QR code with CloudCare Android app")
                .font(.caption)
                .foregroundColor(.secondary)
            
            Button("Generate New Code") {
                qrCodeImage = pairingManager.generatePairingQRCode()
            }
            .buttonStyle(.borderedProminent)
        }
        .onAppear {
            qrCodeImage = pairingManager.generatePairingQRCode()
        }
    }
}
```

**QR Code Generation Logic:**
```swift
func generateQRCode(from data: Data?) -> UIImage? {
    guard let data = data else { return nil }
    
    let filter = CIFilter.qrCodeGenerator()
    filter.setValue(data, forKey: "inputMessage")
    filter.setValue("H", forKey: "inputCorrectionLevel")
    
    guard let ciImage = filter.outputImage else { return nil }
    
    let transform = CGAffineTransform(scaleX: 10, y: 10)
    let scaledImage = ciImage.transformed(by: transform)
    
    let context = CIContext()
    guard let cgImage = context.createCGImage(scaledImage, from: scaledImage.extent) else { return nil }
    
    return UIImage(cgImage: cgImage)
}
```

### Pairing Data Model

```swift
struct PairingData: Codable {
    let iosUserId: String        // iOS user identifier
    let iosDeviceId: String      // Apple device identifier
    let deviceName: String       // "iPhone 13", "Apple Watch Series 9"
    let deviceType: String       // "iphone" or "apple_watch"
    let pairingCode: String      // Random code for verification
    let timestamp: String        // ISO8601 timestamp
}
```

### Android Scanning

**Android receives QR data:**
1. Scans QR code
2. Parses JSON
3. Sends to backend: `POST /wearables/devices/pair`
4. Backend creates device pairing record
5. iOS syncs start uploading to paired account

---

## Setup & Build

### Prerequisites
- Xcode 15.0+
- iOS 16.0+ SDK
- Apple Developer account (for device testing)
- HealthKit entitlements

### Build Steps

```bash
# 1. Clone repository
git clone https://github.com/Priyo13o4/MAD_CloudCare.git
cd MAD_CloudCare/CloudSync

# 2. Open in Xcode
open CloudSync.xcodeproj

# 3. Configure signing
# - Select project in navigator
# - Go to Signing & Capabilities
# - Select your team
# - Enable "Automatically manage signing"

# 4. Add HealthKit capability
# - Click "+ Capability"
# - Add "HealthKit"

# 5. Build and run
# - Select target device (iPhone or simulator)
# - Click Run (Cmd+R)
```

### Configuration

**Info.plist:**
```xml
<key>UIBackgroundModes</key>
<array>
    <string>fetch</string>
    <string>processing</string>
</array>

<key>NSHealthShareUsageDescription</key>
<string>CloudSync syncs your health data with CloudCare for comprehensive health tracking.</string>
```

**CloudSync.entitlements:**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>com.apple.developer.healthkit</key>
    <true/>
</dict>
</plist>
```

---

## API Integration

### Backend Endpoints

**Upload Health Data:**
```swift
// Single export
POST /api/v1/wearables/import/apple-health
Content-Type: application/json
Authorization: Bearer <token>

{
  "userId": "uuid",
  "deviceId": "device-id",
  "exportTimestamp": "2025-11-20T10:00:00Z",
  "dataRange": {
    "startDate": "2025-11-19T00:00:00Z",
    "endDate": "2025-11-20T00:00:00Z"
  },
  "metrics": [...]
}

// Batch upload
POST /api/v1/wearables/import/apple-health/batch
[{...}, {...}]
```

**Device Pairing:**
```swift
POST /api/v1/wearables/devices/pair
{
  "ios_user_id": "uuid",
  "ios_device_id": "device-id",
  "android_user_id": "uuid",
  "device_name": "Apple Watch",
  "device_type": "apple_watch",
  "pairing_code": "qr-code-data"
}
```

---

## Data Processing

### DataProcessor

**Aggregation:**
```swift
class DataProcessor {
    func aggregateMetrics(_ metrics: [HealthMetric]) -> [AggregatedMetric] {
        // Group by type and time bucket (hourly)
        let grouped = Dictionary(grouping: metrics) { metric in
            (metric.type, hourBucket(for: metric.startDate))
        }
        
        return grouped.map { key, values in
            AggregatedMetric(
                type: key.0,
                timestamp: key.1,
                count: values.count,
                sum: values.reduce(0) { $0 + $1.value },
                average: values.reduce(0) { $0 + $1.value } / Double(values.count),
                min: values.map(\.value).min() ?? 0,
                max: values.map(\.value).max() ?? 0
            )
        }
    }
}
```

### Deduplication

**Client-side:**
```swift
func removeDuplicates(_ metrics: [HealthMetric]) -> [HealthMetric] {
    var seen = Set<String>()
    return metrics.filter { metric in
        let key = "\(metric.type)_\(metric.startDate)_\(metric.value)"
        if seen.contains(key) {
            return false
        } else {
            seen.insert(key)
            return true
        }
    }
}
```

**Server-side:** Backend MongoDB has compound unique index preventing duplicates

---

## Testing

### Test Data
- **Device**: iPhone 13, Apple Watch Series 9
- **HealthKit Data**: 30,186 metric samples
- **Date Range**: 34 days (2025-10-16 to 2025-11-19)
- **Metrics**: Heart rate, steps, calories, distance, sleep, flights, VO2 max

### Test Scenarios
1. **Authorization**: Request HealthKit access → Grant → isAuthorized = true
2. **Data Fetch**: Fetch last 7 days → Receive metrics array
3. **Upload**: Upload to backend → Response 201 Created
4. **QR Pairing**: Generate QR → Android scans → Pairing successful
5. **Background Sync**: Enable → Wait 1 hour → Automatic upload
6. **Settings**: Change enabled metrics → Save → Only selected metrics uploaded

---

## Performance

### Optimizations
- **Batch Uploads**: Send multiple days in single request
- **Deduplication**: Remove duplicates client-side before upload
- **Incremental Sync**: Only fetch data since last sync
- **Background Tasks**: Offload uploads to background
- **Error Handling**: Retry failed uploads with exponential backoff

### Capacity
- Tested with 30K+ metrics (34 days)
- Upload time: ~2-3 seconds for batch
- Memory usage: < 100 MB during sync

---

## Privacy & Security

### Data Protection
- ✅ **HealthKit Permissions**: User grants explicit access
- ✅ **Data Encryption**: TLS/HTTPS for all uploads
- ✅ **API Authentication**: Bearer token required
- ✅ **Local Storage**: No health data stored on device (query on-demand)

### User Control
- ✅ **Metric Selection**: User chooses what to share
- ✅ **Sync Control**: Manual or automatic sync
- ✅ **Revoke Access**: Can disable in Settings > Health > Data Access

---

## Troubleshooting

### Common Issues

**1. "HealthKit authorization failed"**
- **Cause**: HealthKit not available or permissions denied
- **Fix**: Check Info.plist, add HealthKit capability, request authorization

**2. "Upload failed"**
- **Cause**: Invalid API key, network error, backend down
- **Fix**: Check server URL, verify API key, check network connection

**3. "No data fetched"**
- **Cause**: No health data in date range, permissions not granted
- **Fix**: Check HealthKit authorization, verify data exists in Health app

**4. "QR code won't scan"**
- **Cause**: Low resolution, incorrect format
- **Fix**: Regenerate QR code, ensure JSON format correct

---

## Summary

CloudSync iOS app provides seamless health data integration for CloudCare:
- ✅ **HealthKit Integration** (9+ metric types)
- ✅ **QR Code Pairing** (iOS ↔ Android)
- ✅ **Background Sync** (automatic uploads)
- ✅ **User Privacy** (granular control)
- ✅ **Upload History** (track all syncs)
- ✅ **Configurable Settings** (metrics, intervals)
- ✅ **Tested at Scale** (30K+ metrics)

**Minimum iOS:** 16.0  
**Backend:** https://cloudcare.pipfactor.com/api/v1  
**Tested Devices:** iPhone 13, Apple Watch Series 9

---

*Last Updated: November 2025 | Version 1.0*
