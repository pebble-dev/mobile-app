import 'package:cobble/domain/entities/hardware_platform.dart';

import 'db/models/app.dart';

extension AppCompatibility on App {
  bool isCompatibleWith(WatchType watchType) {
    switch (watchType) {
      case WatchType.aplite:
        return supportedHardware.contains("aplite");
      case WatchType.basalt:
        return supportedHardware.contains("aplite") ||
            supportedHardware.contains("basalt");
      case WatchType.chalk:
        return supportedHardware.contains("chalk");
      case WatchType.diorite:
        return supportedHardware.contains("aplite") ||
            supportedHardware.contains("diorite");
      case WatchType.emery:
        return supportedHardware.contains("aplite") ||
            supportedHardware.contains("basalt") ||
            supportedHardware.contains("diorite") ||
            supportedHardware.contains("emery");
    }
  }
}
