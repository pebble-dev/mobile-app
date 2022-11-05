import 'package:cobble/domain/api/appstore/locker_entry.dart';
import 'package:cobble/domain/db/converters/sql_json_converters.dart';
import 'package:json_annotation/json_annotation.dart';
import 'package:uuid_type/uuid_type.dart';
import 'package:collection/collection.dart';

part 'locker_app.g.dart';

@NonNullUuidConverter()
@JsonSerializable()
class LockerApp {
  final String id;
  final Uuid uuid;
  final String version;
  final String? apliteIcon;
  final String? basaltIcon;
  final String? chalkIcon;
  final String? dioriteIcon;

  LockerApp({required this.id,
    required this.uuid,
    required this.version,
    this.apliteIcon,
    this.basaltIcon,
    this.chalkIcon,
    this.dioriteIcon});

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
      version: entry.version!,
      apliteIcon: entry.hardwarePlatforms.firstWhereOrNull((element) => element.name == "aplite")?.images.icon,
      basaltIcon: entry.hardwarePlatforms.firstWhereOrNull((element) => element.name == "basalt")?.images.icon,
      chalkIcon: entry.hardwarePlatforms.firstWhereOrNull((element) => element.name == "chalk")?.images.icon,
      dioriteIcon: entry.hardwarePlatforms.firstWhereOrNull((element) => element.name == "diorite")?.images.icon,
    );
  }
}