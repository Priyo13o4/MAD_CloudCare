# iOS CloudSync - QR Code Pairing Feature

## Context
You're working on **CloudSync**, an iOS app that syncs Apple Health/HealthKit data to a FastAPI backend. The app currently uploads health metrics (heart rate, steps, calories, etc.) anonymously using a device-generated UUID.

**Current Implementation**:
- App uses `HealthKitManager` to fetch Apple Health data
- `DataProcessor` converts HealthKit samples to JSON format
- `NetworkManager` uploads data to backend in batches
- `UploadTracker` prevents duplicate uploads using date range tracking
- Device ID is generated on first launch: `UIDevice.current.identifierForVendor?.uuidString`
- User ID is also generated: `UUID().uuidString`

**Project Structure**:
```
CloudSync/
├── CloudSyncApp.swift (Main app entry)
├── ContentView.swift (Main sync UI)
├── SettingsView.swift (Backend URL configuration)
├── HealthKitManager.swift (HealthKit integration)
├── DataProcessor.swift (Data conversion)
├── NetworkManager.swift (API communication)
├── UploadTracker.swift (Deduplication)
├── UploadHistoryView.swift (Sync history)
├── SettingsManager.swift (Persistent settings)
└── Models.swift (Data models)
```

---

## 🎯 Task: Add QR Code Pairing Feature

### Requirements

**1. Add "Pair with Android" Button to SettingsView**

Add a new section in `SettingsView.swift` that:
- Shows current device pairing status
- Displays a "Generate Pairing QR Code" button
- Shows the pairing QR code in a modal sheet when tapped

**2. Create QR Code Generation Logic**

Create a new Swift file: `PairingManager.swift`

**Data to encode in QR code**:
```json
{
  "userId": "3228128A-7110-4D47-8EDB-3A9160E3808A",
  "deviceId": "207791ED-2518-485D-B4D8-55A23525A485",
  "deviceName": "iPhone 15 Pro",
  "deviceType": "apple_watch",
  "generatedAt": "2025-11-15T18:30:00Z",
  "expiresAt": "2025-11-15T18:45:00Z",
  "pairingCode": "ABCD-1234"
}
```

**Field Descriptions**:
- `userId`: The user ID currently used for uploads (stored in SettingsManager/UserDefaults)
- `deviceId`: The device ID (UIDevice.current.identifierForVendor)
- `deviceName`: Human-readable device name (UIDevice.current.name)
- `deviceType`: Always "apple_watch" for this app
- `generatedAt`: ISO8601 timestamp when QR was generated
- `expiresAt`: 15 minutes from generation (security)
- `pairingCode`: Random 9-character code (format: XXXX-YYYY) for verification

**3. Create Pairing QR View**

Create a new Swift file: `PairingQRView.swift`

This view should:
- Display the generated QR code prominently
- Show the pairing code in large text below QR (for manual entry fallback)
- Show device information (name, type)
- Show expiration countdown timer
- Have a "Regenerate" button
- Have a "Done" button to dismiss

**4. UI Design Guidelines**

Follow the existing app's SwiftUI style:
- Use `Color.accentColor` for primary buttons
- Use SF Symbols for icons: `qrcode`, `iphone`, `applewatch`
- Use `.sheet` or `.fullScreenCover` for QR display
- Match existing card/section styling from SettingsView
- Show success confirmation after pairing (if backend supports it)

**5. Integration with SettingsManager**

Update `SettingsManager.swift` to store:
```swift
@AppStorage("isPaired") private var isPaired: Bool = false
@AppStorage("pairedDeviceName") private var pairedDeviceName: String = ""
@AppStorage("pairedAt") private var pairedAt: String = ""
```

**6. Backend Integration (Optional)**

If you want to verify pairing with backend:
```swift
// POST to https://cloudcare.pipfactor.com/api/v1/wearables/devices/pair
// Body: Same JSON as QR code
// Response: { "success": true, "message": "Device paired successfully" }
```

---

## Implementation Steps

### Step 1: Create PairingManager.swift
```swift
import Foundation
import CoreImage.CIFilterBuiltins

class PairingManager: ObservableObject {
    @Published var pairingData: PairingData?
    @Published var qrCodeImage: UIImage?
    
    struct PairingData: Codable {
        let userId: String
        let deviceId: String
        let deviceName: String
        let deviceType: String
        let generatedAt: String
        let expiresAt: String
        let pairingCode: String
    }
    
    func generatePairingCode() -> String {
        // Generate random 9-char code (XXXX-YYYY format)
        let chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        let part1 = String((0..<4).map { _ in chars.randomElement()! })
        let part2 = String((0..<4).map { _ in chars.randomElement()! })
        return "\(part1)-\(part2)"
    }
    
    func generateQRCode(userId: String, deviceId: String) {
        // Create pairing data
        let now = ISO8601DateFormatter().string(from: Date())
        let expires = ISO8601DateFormatter().string(from: Date().addingTimeInterval(900)) // 15 min
        
        let data = PairingData(
            userId: userId,
            deviceId: deviceId,
            deviceName: UIDevice.current.name,
            deviceType: "apple_watch",
            generatedAt: now,
            expiresAt: expires,
            pairingCode: generatePairingCode()
        )
        
        self.pairingData = data
        
        // Generate QR code image
        if let jsonData = try? JSONEncoder().encode(data),
           let jsonString = String(data: jsonData, encoding: .utf8) {
            self.qrCodeImage = createQRCode(from: jsonString)
        }
    }
    
    private func createQRCode(from string: String) -> UIImage? {
        let context = CIContext()
        let filter = CIFilter.qrCodeGenerator()
        
        filter.message = Data(string.utf8)
        filter.correctionLevel = "M"
        
        if let outputImage = filter.outputImage {
            let transform = CGAffineTransform(scaleX: 10, y: 10)
            let scaledImage = outputImage.transformed(by: transform)
            
            if let cgImage = context.createCGImage(scaledImage, from: scaledImage.extent) {
                return UIImage(cgImage: cgImage)
            }
        }
        return nil
    }
    
    func isExpired() -> Bool {
        guard let data = pairingData,
              let expiresAt = ISO8601DateFormatter().date(from: data.expiresAt) else {
            return true
        }
        return Date() > expiresAt
    }
}
```

### Step 2: Create PairingQRView.swift
```swift
import SwiftUI

struct PairingQRView: View {
    @Environment(\.dismiss) var dismiss
    @StateObject private var manager = PairingManager()
    @State private var timeRemaining: Int = 900 // 15 minutes
    
    let userId: String
    let deviceId: String
    let timer = Timer.publish(every: 1, on: .main, in: .common).autoconnect()
    
    var body: View {
        NavigationView {
            VStack(spacing: 20) {
                // QR Code
                if let qrImage = manager.qrCodeImage {
                    Image(uiImage: qrImage)
                        .interpolation(.none)
                        .resizable()
                        .frame(width: 250, height: 250)
                        .padding()
                        .background(Color.white)
                        .cornerRadius(16)
                        .shadow(radius: 5)
                } else {
                    ProgressView()
                        .frame(width: 250, height: 250)
                }
                
                // Pairing Code
                if let pairingCode = manager.pairingData?.pairingCode {
                    VStack(spacing: 8) {
                        Text("Pairing Code")
                            .font(.caption)
                            .foregroundColor(.secondary)
                        Text(pairingCode)
                            .font(.system(size: 32, weight: .bold, design: .monospaced))
                    }
                    .padding()
                    .background(Color.secondary.opacity(0.1))
                    .cornerRadius(12)
                }
                
                // Device Info
                VStack(spacing: 8) {
                    Label(UIDevice.current.name, systemImage: "iphone")
                    Label("Apple Watch", systemImage: "applewatch")
                }
                .font(.subheadline)
                .foregroundColor(.secondary)
                
                // Timer
                Text("Expires in \(formatTime(timeRemaining))")
                    .font(.caption)
                    .foregroundColor(.orange)
                    .padding(.top)
                
                Spacer()
                
                // Buttons
                VStack(spacing: 12) {
                    Button("Regenerate QR Code") {
                        manager.generateQRCode(userId: userId, deviceId: deviceId)
                        timeRemaining = 900
                    }
                    .buttonStyle(.borderedProminent)
                    
                    Button("Done") {
                        dismiss()
                    }
                    .buttonStyle(.bordered)
                }
                .padding()
            }
            .navigationTitle("Pair with Android")
            .navigationBarTitleDisplayMode(.inline)
            .onAppear {
                manager.generateQRCode(userId: userId, deviceId: deviceId)
            }
            .onReceive(timer) { _ in
                if timeRemaining > 0 {
                    timeRemaining -= 1
                } else if !manager.isExpired() {
                    manager.generateQRCode(userId: userId, deviceId: deviceId)
                    timeRemaining = 900
                }
            }
        }
    }
    
    private func formatTime(_ seconds: Int) -> String {
        let mins = seconds / 60
        let secs = seconds % 60
        return String(format: "%d:%02d", mins, secs)
    }
}
```

### Step 3: Update SettingsView.swift

Add a new section after the "Backend URL" section:

```swift
// Add this section in SettingsView body
Section("Device Pairing") {
    HStack {
        Label("Pairing Status", systemImage: isPaired ? "checkmark.circle.fill" : "xmark.circle.fill")
        Spacer()
        Text(isPaired ? "Paired" : "Not Paired")
            .foregroundColor(isPaired ? .green : .secondary)
    }
    
    if isPaired {
        VStack(alignment: .leading, spacing: 4) {
            Text("Paired Device")
                .font(.caption)
                .foregroundColor(.secondary)
            Text(pairedDeviceName)
                .font(.subheadline)
            Text("Paired on \(pairedAt)")
                .font(.caption)
                .foregroundColor(.secondary)
        }
    }
    
    Button(action: {
        showPairingQR = true
    }) {
        Label("Generate Pairing QR Code", systemImage: "qrcode")
    }
}
.sheet(isPresented: $showPairingQR) {
    PairingQRView(
        userId: settingsManager.userId,
        deviceId: settingsManager.deviceId
    )
}

// Add these state variables at the top of SettingsView
@State private var showPairingQR = false
@AppStorage("isPaired") private var isPaired: Bool = false
@AppStorage("pairedDeviceName") private var pairedDeviceName: String = ""
@AppStorage("pairedAt") private var pairedAt: String = ""
```

---

## Success Criteria

✅ **Pairing button appears in Settings**  
✅ **QR code is generated with all required fields**  
✅ **QR code is scannable and contains valid JSON**  
✅ **Pairing code is displayed for manual entry**  
✅ **Timer shows expiration countdown**  
✅ **QR can be regenerated**  
✅ **UI follows existing app design patterns**

---

## Testing

1. Open CloudSync app → Settings
2. Tap "Generate Pairing QR Code"
3. Verify QR code appears
4. Verify pairing code is shown (XXXX-YYYY format)
5. Scan QR with any QR reader to verify JSON content
6. Check that all fields are present and valid
7. Wait 15 minutes to verify expiration and auto-regeneration

---

## Notes

- **User ID and Device ID**: These are currently stored in `SettingsManager` or `UserDefaults`. Make sure to access them correctly.
- **Expiration**: The 15-minute expiration is a security feature. Android app should validate this timestamp.
- **Manual Entry Fallback**: The pairing code serves as a backup if QR scanning fails.
- **Backend Validation**: Optionally add a backend call to verify pairing, but QR generation works offline.
- **SwiftUI**: Use existing patterns from `SettingsView` and `ContentView` for consistency.

---

## Additional Enhancements (Optional)

- Add haptic feedback when QR is generated
- Show "Copied" toast when pairing code is tapped
- Add a "Unpair" button if already paired
- Store pairing history with timestamps
- Add NFC pairing as alternative (iOS 13+)
