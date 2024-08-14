import 'package:cobble/domain/api/appstore/locker_entry.dart';
import 'package:cobble/domain/db/converters/sql_json_converters.dart';
import 'package:json_annotation/json_annotation.dart';
import 'package:uuid_type/uuid_type.dart';
import 'package:collection/collection.dart';

part 'locker_app.g.dart';

@NonNullUuidConverter()
@JsonSerializable()
@BooleanNumberConverter()
class LockerApp {
  final String id;
  final Uuid uuid;
  final String version;
  final String? apliteIcon;
  final String? basaltIcon;
  final String? chalkIcon;
  final String? dioriteIcon;
  final String? apliteList;
  final String? basaltList;
  final String? chalkList;
  final String? dioriteList;
  final bool markedForDeletion;

  LockerApp({required this.id,
    required this.uuid,
    required this.version,
    this.apliteIcon,
    this.basaltIcon,
    this.chalkIcon,
    this.dioriteIcon,
    this.apliteList,
    this.basaltList,
    this.chalkList,
    this.dioriteList,
    this.markedForDeletion = false});

  Map<String, dynamic> toMap() {
    return _$LockerAppToJson(this);
  }

  factory LockerApp.fromMap(Map<String, dynamic> map) {
    return _$LockerAppFromJson(map);
  }

  factory LockerApp.fromApi(LockerEntry entry) {
    return LockerApp(
      id: entry.id,
      uuid: Uuid.parse(entry.uuid),
      version: entry.version ?? "",
      apliteIcon: entry.hardwarePlatforms.firstWhereOrNull((element) => element.name == "aplite")?.images.icon,
      basaltIcon: entry.hardwarePlatforms.firstWhereOrNull((element) => element.name == "basalt")?.images.icon,
      chalkIcon: entry.hardwarePlatforms.firstWhereOrNull((element) => element.name == "chalk")?.images.icon,
      dioriteIcon: entry.hardwarePlatforms.firstWhereOrNull((element) => element.name == "diorite")?.images.icon,
      apliteList: entry.hardwarePlatforms.firstWhereOrNull((element) => element.name == "aplite")?.images.list,
      basaltList: entry.hardwarePlatforms.firstWhereOrNull((element) => element.name == "basalt")?.images.list,
      chalkList: entry.hardwarePlatforms.firstWhereOrNull((element) => element.name == "chalk")?.images.list,
      dioriteList: entry.hardwarePlatforms.firstWhereOrNull((element) => element.name == "diorite")?.images.list,
    );
  }

  String? getPlatformListImage(String platform) {
    switch (platform) {
      case "aplite":
        return apliteList;
      case "basalt":
        return basaltList;
      case "chalk":
        return chalkList;
      case "diorite":
        return dioriteList;
      default:
        return null;
    }
  }

  String? getPlatformIconImage(String platform) {
    switch (platform) {
      case "aplite":
        return apliteIcon;
      case "basalt":
        return basaltIcon;
      case "chalk":
        return chalkIcon;
      case "diorite":
        return dioriteIcon;
      default:
        return null;
    }
  }

}