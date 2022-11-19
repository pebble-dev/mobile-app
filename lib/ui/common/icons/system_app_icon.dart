import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:cobble/ui/common/icons/comp_icon.dart';
import 'package:cobble/ui/common/icons/fonts/rebble_icons.dart';
import 'package:uuid_type/uuid_type.dart';

class _AppIcon extends StatelessWidget {
  _AppIcon(this.stroke, this.fill,
      {this.size = 48.0,
      this.backgroundColor = AppBackgroundColor.Orange});
  final IconData stroke;
  final IconData fill;
  final double size;
  final Color backgroundColor;
  @override
  Widget build(BuildContext context) {
    return Container(
      child: Center(
        child: CompIcon(stroke, fill),
      ),
      width: size,
      height: size,
      decoration: BoxDecoration(
        color: backgroundColor,
        borderRadius: BorderRadius.all(Radius.circular(size / 6)),
      ),
    );
  }
}

class SystemAppIcon extends StatelessWidget {
  SystemAppIcon(this.appid,
      {this.size = 48.0});

  final Uuid appid;
  final double size;

  @override
  Widget build(BuildContext context) {
    switch (appid.toString()) {
      case '1f03293d-47af-4f28-b960-f2b02a6dd757': // Music app
        return _AppIcon(RebbleIcons.music_note, RebbleIcons.music_note_background, size: size, backgroundColor: AppBackgroundColor.Yellow);
      case 'b2cae818-10f8-46df-ad2b-98ad2254a3c1': // Notifications app
        return _AppIcon(RebbleIcons.notification, RebbleIcons.notification_background, size: size, backgroundColor: AppBackgroundColor.Red);
      case '': // Calendar app
        return _AppIcon(RebbleIcons.calendar, RebbleIcons.calendar_background, size: size, backgroundColor: AppBackgroundColor.Orange);
      case '07e0d9cb-8957-4bf7-9d42-35bf47caadfe': // Settings app
        return _AppIcon(RebbleIcons.settings, RebbleIcons.settings_background, size: size, backgroundColor: AppBackgroundColor.Blue);
      case '36d8c6ed-4c83-4fa1-a9e2-8f12dc941f8c': // Health app
        return _AppIcon(RebbleIcons.health_heart, RebbleIcons.health_heart_background, size: size, backgroundColor: AppBackgroundColor.Pink);
      case '67a32d95-ef69-46d4-a0b9-854cc62f97f9': // Alarms app
        return _AppIcon(RebbleIcons.notification_megaphone, RebbleIcons.notification_megaphone_background, size: size, backgroundColor: AppBackgroundColor.DarkGreen);
      case '0863fc6a-66c5-4f62-ab8a-82ed00a98b5d': // SMS app
        return _AppIcon(RebbleIcons.sms_messages, RebbleIcons.sms_messages_background, size: size, backgroundColor: AppBackgroundColor.DarkGreen);
      case '18e443ce-38fd-47c8-84d5-6d0c775fbe55': // Watchfaces app
        return _AppIcon(RebbleIcons.watch_faces, RebbleIcons.watch_faces_background, size: size, backgroundColor: AppBackgroundColor.DarkGreen);
      default:
        return _AppIcon(RebbleIcons.unknown_app, RebbleIcons.unknown_app_background, size: size);
    }
  }
}

class AppBackgroundColor {
  static const Color Pink = Color(0xFFFF3F8F);
  static const Color Red = Color(0xFFD33434);
  static const Color Orange = Color(0xFFFA5521);
  static const Color Yellow = Color(0xFFFFFF00);
  static const Color Green = Color(0xFF00FFAA);
  static const Color DarkGreen = Color(0xFF00A982);
  static const Color Blue = Color(0xFF00C3FD);
  static const Color DarkBlue = Color(0xFF008DFF);
  static const Color Purple = Color(0xFF6B1D97);
}

