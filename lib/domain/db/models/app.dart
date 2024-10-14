import 'package:cobble/domain/db/converters/sql_json_converters.dart';
import 'package:cobble/domain/db/models/next_sync_action.dart';
import 'package:json_annotation/json_annotation.dart';
import 'package:uuid_type/uuid_type.dart';

part 'app.g.dart';

@JsonSerializable()
@NonNullUuidConverter()
@BooleanNumberConverter()
@CommaSeparatedListConverter()
class App {
  /// UUID of the app
  final Uuid uuid;

  /// Short name of the app (as displayed on the watch)
  final String shortName;

  /// Full name of the app
  final String longName;

  /// Company that made the app
  final String company;

  /// ID of the app store entry, if app was downloaded from the app store.
  /// Null otherwise.
  final String? appstoreId;

  /// Version of the app
  final String version;

  /// Whether app is a watchapp or a watchface.
  final bool isWatchface;

  /// Whether app is a system app that cannot be uninstalled
  final bool isSystem;

  /// List of supported hardware codenames
  /// (see WatchType enum for list of all entries)
  final List<String> supportedHardware;

  /// Action that should be performed for this app
  /// when the next sync-to-watch is performed
  final NextSyncAction nextSyncAction;

  final int appOrder;

  final String? url;

  final String processInfoFlags;

  final String sdkVersions;

  App(
      {required this.uuid,
      required this.shortName,
      required this.longName,
      required this.company,
      required this.appstoreId,
      required this.version,
      required this.isWatchface,
      required this.isSystem,
      required this.supportedHardware,
      required this.nextSyncAction,
      required this.appOrder,
      required this.processInfoFlags,
      required this.sdkVersions,
      required this.url});

  Map<String, dynamic> toMap() {
    return _$AppToJson(this);
  }

  factory App.fromMap(Map<String, dynamic> map) {
    return _$AppFromJson(map);
  }

  @override
  String toString() {
    return 'App{uuid: $uuid, shortName: $shortName, longName: $longName, company: $company, appstoreId: $appstoreId, version: $version, isWatchface: $isWatchface, supportedHardware: $supportedHardware, processInfoFlags: $processInfoFlags, sdkVersions: $sdkVersions, nextSyncAction: $nextSyncAction, order: $appOrder}';
  }
}
