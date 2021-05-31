import 'package:cobble/domain/db/converters/sql_json_converters.dart';
import 'package:cobble/domain/db/models/next_sync_action.dart';
import 'package:json_annotation/json_annotation.dart';
import 'package:uuid_type/uuid_type.dart';

part 'appstore_app.g.dart';

@JsonSerializable()
@NonNullUuidConverter()
@BooleanNumberConverter()
@CommaSeparatedListConverter()
class AppstoreApp {
  
  /// ID of the app store entry
  final String id;
  
  /// UUID of the app
  final Uuid uuid;

  /// Title of the app as in the store
  final String title;

  /// 
  final List<int>? listImage;

  /// 
  final List<int>? iconImage;

  /// 
  final List<int>? screenshotImage;

  /// Whether app is a watchapp or a watchface.
  final bool isWatchface;

  AppstoreApp(
      {required this.id,
      required this.uuid,
      required this.title,
      required this.listImage,
      required this.iconImage,
      required this.screenshotImage,
      required this.isWatchface});

  Map<String, dynamic> toMap() {
    return _$AppstoreAppToJson(this);
  }

  factory AppstoreApp.fromMap(Map<String, dynamic> map) {
    return _$AppstoreAppFromJson(map);
  }

  @override
  String toString() {
    return 'AppstoreApp{uuid: $id, uuid: $uuid, title: $title, listImage: $listImage, iconImage: $iconImage, screenshotImage: $screenshotImage, isWatchface: $isWatchface}';
  }
}
