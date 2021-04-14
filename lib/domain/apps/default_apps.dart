import 'package:cobble/domain/db/dao/app_dao.dart';
import 'package:cobble/domain/db/models/app.dart';
import 'package:cobble/domain/db/models/next_sync_action.dart';
import 'package:cobble/localization/localization.dart';
import 'package:uuid_type/uuid_type.dart';

Future<void> populate_system_apps(AppDao dao) async {
  await _add_system_app(
    dao,
    "07e0d9cb-8957-4bf7-9d42-35bf47caadfe",
    tr.systemApps.settings,
  );

  await _add_system_app(
    dao,
    "1f03293d-47af-4f28-b960-f2b02a6dd757",
    tr.systemApps.music,
  );

  await _add_system_app(
    dao,
    "b2cae818-10f8-46df-ad2b-98ad2254a3c1",
    tr.systemApps.notifications,
  );

  await _add_system_app(
    dao,
    "67a32d95-ef69-46d4-a0b9-854cc62f97f9",
    tr.systemApps.alarms,
  );

  await _add_system_app(
    dao,
    "18e443ce-38fd-47c8-84d5-6d0c775fbe55",
    tr.systemApps.watchfaces,
  );
}

Future<void> _add_system_app(AppDao dao, String uuid, String name) async {
  final nextOrder = await dao.getNumberOfAllInstalledPackages();

  await dao.insertOrUpdatePackage(
    App(
        uuid: Uuid(uuid),
        shortName: name,
        longName: name,
        company: "Pebble",
        appstoreId: null,
        version: "1.0.0",
        isWatchface: false,
        isSystem: true,
        supportedHardware: ["aplite", "basalt", "chalk", "diorite", "emery"],
        nextSyncAction: NextSyncAction.Nothing,
        appOrder: nextOrder),
  );
}
