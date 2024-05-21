package co.touchlab.skie.phases.header

import co.touchlab.skie.oir.element.OirClass
import co.touchlab.skie.oir.element.renderForwardDeclaration
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.element.SirModule
import co.touchlab.skie.util.cache.writeTextIfDifferent

object GenerateFakeObjCDependenciesPhase : SirPhase {

    context(SirPhase.Context)
    override suspend fun execute() {
        oirProvider.externalClassesAndProtocols
            .groupBy { it.originalSirClass.module }
            // TODO: Replace with two types of external modules (fake and SDK) and handle available SDK modules properly.
            .filterKeys { it is SirModule.External && it.name !in knownSdkModules }
            .mapKeys { it.key as SirModule.External }
            .forEach { (module, types) ->
                generateFakeFramework(module, types)
            }
    }

    context(SirPhase.Context)
    private fun generateFakeFramework(module: SirModule.External, classes: List<OirClass>) {
        generateModuleMap(module)
        generateHeader(module, classes)
    }

    context(SirPhase.Context)
    private fun generateModuleMap(module: SirModule) {
        val moduleMapContent =
            """
            framework module ${module.name} {
                umbrella header "${module.name}.h"
            }
        """.trimIndent()

        skieBuildDirectory.swiftCompiler.fakeObjCFrameworks.moduleMap(module.name).writeTextIfDifferent(moduleMapContent)
    }

    context(SirPhase.Context)
    private fun generateHeader(module: SirModule, classes: List<OirClass>) {
        val foundationImport = "#import <Foundation/NSObject.h>"

        val declarations = classes
            .sortedBy { it.name }
            .joinToString("\n") { it.getHeaderEntry() }

        val headerContent = "$foundationImport\n\n$declarations"

        skieBuildDirectory.swiftCompiler.fakeObjCFrameworks.header(module.name).writeTextIfDifferent(headerContent)
    }
}

private fun OirClass.getHeaderEntry(): String =
    when (kind) {
        OirClass.Kind.Class -> getClassHeaderEntry()
        OirClass.Kind.Protocol -> getProtocolHeaderEntry()
    }

private fun OirClass.getClassHeaderEntry(): String =
    "@interface ${renderForwardDeclaration()} : NSObject @end"

private fun OirClass.getProtocolHeaderEntry(): String =
    "@protocol ${renderForwardDeclaration()} @end"

/*
 This currently has all non-private frameworks from all darwin platforms.

 To get the list again, run:

 ```sh
 cd /Applications/Xcode.app/Contents/Developer/Platforms
 find *.platform/Developer/SDKs/*.sdk/System/Library/Frameworks -name '*.framework' -prune -type d -exec basename {} '.framework' \; | sort | uniq | pbcopy
 ``` // this is to match a comment "opening" in the find command: */
*/
private val knownSdkModules = setOf(
    "AGL",
    "ARKit",
    "AVFAudio",
    "AVFoundation",
    "AVKit",
    "AVRouting",
    "Accelerate",
    "Accessibility",
    "Accounts",
    "ActivityKit",
    "AdAttributionKit",
    "AdServices",
    "AdSupport",
    "AddressBook",
    "AddressBookUI",
    "AppClip",
    "AppIntents",
    "AppKit",
    "AppTrackingTransparency",
    "AppleScriptKit",
    "AppleScriptObjC",
    "ApplicationServices",
    "AssetsLibrary",
    "Assignables",
    "AudioToolbox",
    "AudioUnit",
    "AudioVideoBridging",
    "AuthenticationServices",
    "AutomatedDeviceEnrollment",
    "AutomaticAssessmentConfiguration",
    "Automator",
    "BackgroundAssets",
    "BackgroundTasks",
    "BrowserEngineCore",
    "BrowserEngineKit",
    "BusinessChat",
    "CFNetwork",
    "CalendarStore",
    "CallKit",
    "CarKey",
    "CarPlay",
    "Carbon",
    "Charts",
    "Cinematic",
    "ClassKit",
    "ClockKit",
    "CloudKit",
    "Cocoa",
    "Collaboration",
    "ColorSync",
    "Combine",
    "CompositorServices",
    "Contacts",
    "ContactsUI",
    "CoreAudio",
    "CoreAudioKit",
    "CoreAudioTypes",
    "CoreBluetooth",
    "CoreData",
    "CoreDisplay",
    "CoreFoundation",
    "CoreGraphics",
    "CoreHaptics",
    "CoreImage",
    "CoreLocation",
    "CoreLocationUI",
    "CoreMIDI",
    "CoreMIDIServer",
    "CoreML",
    "CoreMedia",
    "CoreMediaIO",
    "CoreMotion",
    "CoreNFC",
    "CoreServices",
    "CoreSpotlight",
    "CoreTelephony",
    "CoreText",
    "CoreTransferable",
    "CoreVideo",
    "CoreWLAN",
    "CreateML",
    "CreateMLComponents",
    "CryptoKit",
    "CryptoTokenKit",
    "DVDPlayback",
    "DataDetection",
    "DeveloperToolsSupport",
    "DeviceActivity",
    "DeviceCheck",
    "DeviceDiscoveryExtension",
    "DeviceDiscoveryUI",
    "DirectoryService",
    "DiscRecording",
    "DiscRecordingUI",
    "DiskArbitration",
    "DockKit",
    "DriverKit",
    "EventKit",
    "EventKitUI",
    "ExceptionHandling",
    "ExecutionPolicy",
    "ExposureNotification",
    "ExtensionFoundation",
    "ExtensionKit",
    "ExternalAccessory",
    "FamilyControls",
    "FileProvider",
    "FileProviderUI",
    "FinanceKit",
    "FinanceKitUI",
    "FinderSync",
    "ForceFeedback",
    "Foundation",
    "GLKit",
    "GLUT",
    "GSS",
    "GameController",
    "GameKit",
    "GameplayKit",
    "GroupActivities",
    "HealthKit",
    "HealthKitUI",
    "HomeKit",
    "Hypervisor",
    "ICADevices",
    "IOBluetooth",
    "IOBluetoothUI",
    "IOKit",
    "IOSurface",
    "IOUSBHost",
    "IdentityLookup",
    "IdentityLookupUI",
    "ImageCaptureCore",
    "ImageIO",
    "InputMethodKit",
    "InstallerPlugins",
    "InstantMessage",
    "Intents",
    "IntentsUI",
    "JavaNativeFoundation",
    "JavaRuntimeSupport",
    "JavaScriptCore",
    "JournalingSuggestions",
    "Kerberos",
    "Kernel",
    "KernelManagement",
    "LDAP",
    "LatentSemanticMapping",
    "LightweightCodeRequirements",
    "LinkPresentation",
    "LiveCommunicationKit",
    "LocalAuthentication",
    "LocalAuthenticationEmbeddedUI",
    "MLCompute",
    "MailKit",
    "ManagedAppDistribution",
    "ManagedSettings",
    "ManagedSettingsUI",
    "MapKit",
    "MarketplaceKit",
    "Matter",
    "MatterSupport",
    "MediaAccessibility",
    "MediaLibrary",
    "MediaPlayer",
    "MediaSetup",
    "MediaToolbox",
    "Message",
    "MessageUI",
    "Messages",
    "Metal",
    "MetalFX",
    "MetalKit",
    "MetalPerformanceShaders",
    "MetalPerformanceShadersGraph",
    "MetricKit",
    "MobileCoreServices",
    "ModelIO",
    "MultipeerConnectivity",
    "MusicKit",
    "NaturalLanguage",
    "NearbyInteraction",
    "NetFS",
    "Network",
    "NetworkExtension",
    "NotificationCenter",
    "OSAKit",
    "OSLog",
    "OpenAL",
    "OpenCL",
    "OpenDirectory",
    "OpenGL",
    "OpenGLES",
    "PCSC",
    "PDFKit",
    "PHASE",
    "ParavirtualizedGraphics",
    "PassKit",
    "PencilKit",
    "Photos",
    "PhotosUI",
    "PreferencePanes",
    "ProximityReader",
    "PushKit",
    "PushToTalk",
    "QTKit",
    "Quartz",
    "QuartzCore",
    "QuickLook",
    "QuickLookThumbnailing",
    "QuickLookUI",
    "RealityFoundation",
    "RealityKit",
    "ReplayKit",
    "RoomPlan",
    "Ruby",
    "SafariServices",
    "SafetyKit",
    "SceneKit",
    "ScreenCaptureKit",
    "ScreenSaver",
    "ScreenTime",
    "ScriptingBridge",
    "Security",
    "SecurityFoundation",
    "SecurityInterface",
    "SensitiveContentAnalysis",
    "SensorKit",
    "ServiceManagement",
    "SharedWithYou",
    "SharedWithYouCore",
    "ShazamKit",
    "Social",
    "SoundAnalysis",
    "Speech",
    "SpriteKit",
    "StoreKit",
    "SwiftData",
    "SwiftUI",
    "Symbols",
    "SyncServices",
    "System",
    "SystemConfiguration",
    "SystemExtensions",
    "TVMLKit",
    "TVServices",
    "TVUIKit",
    "TWAIN",
    "TabularData",
    "Tcl",
    "ThreadNetwork",
    "TipKit",
    "Tk",
    "Translation",
    "Twitter",
    "UIKit",
    "UniformTypeIdentifiers",
    "UserNotifications",
    "UserNotificationsUI",
    "VideoDecodeAcceleration",
    "VideoSubscriberAccount",
    "VideoToolbox",
    "Virtualization",
    "Vision",
    "VisionKit",
    "WatchConnectivity",
    "WatchKit",
    "WeatherKit",
    "WebKit",
    "WidgetKit",
    "WorkoutKit",
    "_AVKit_SwiftUI",
    "_AdAttributionKit_StoreKit",
    "_AppIntents_AppKit",
    "_AppIntents_SwiftUI",
    "_AppIntents_UIKit",
    "_AuthenticationServices_SwiftUI",
    "_ClockKit_SwiftUI",
    "_CompositorServices_SwiftUI",
    "_CoreData_CloudKit",
    "_CoreLocationUI_SwiftUI",
    "_CoreNFC_UIKit",
    "_DeviceActivity_SwiftUI",
    "_DeviceDiscoveryUI_SwiftUI",
    "_GroupActivities_AppKit",
    "_GroupActivities_SwiftUI",
    "_GroupActivities_UIKit",
    "_HomeKit_SwiftUI",
    "_LocalAuthentication_SwiftUI",
    "_ManagedAppDistribution_SwiftUI",
    "_MapKit_SwiftUI",
    "_MarketplaceKit_UIKit",
    "_MusicKit_SwiftUI",
    "_PassKit_SwiftUI",
    "_PhotosUI_SwiftUI",
    "_QuickLook_SwiftUI",
    "_RealityKit_SwiftUI",
    "_SceneKit_SwiftUI",
    "_SpriteKit_SwiftUI",
    "_StoreKit_SwiftUI",
    "_SwiftData_CoreData",
    "_SwiftData_SwiftUI",
    "_Translation_SwiftUI",
    "_WatchKit_SwiftUI",
    "_WorkoutKit_SwiftUI",
    "iAd",
    "iTunesLibrary",
    "vmnet",
)
