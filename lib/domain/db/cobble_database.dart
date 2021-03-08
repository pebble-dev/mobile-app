import 'dart:async';

import 'package:cobble/domain/db/dao/active_notification_dao.dart';
import 'package:cobble/domain/db/dao/app_dao.dart';
import 'package:hooks_riverpod/all.dart';
import 'package:path/path.dart';
import 'package:sqflite/sqflite.dart';

import 'dao/timeline_pin_dao.dart';

Future<void> createTimelinePinsTable(Database db) async {
  await db.execute("""
    CREATE TABLE $tableTimelinePins(
      itemId TEXT PRIMARY KEY NOT NULL,
      parentId TEXT NOT NULL,
      backingId TEXT,
      timestamp INT NOT NULL,
      duration INT NOT NULL,
      type TEXT NOT NULL,
      isVisible INTEGER NOT NULL,
      isFloating INTEGER NOT NULL,
      isAllDay INTEGER NOT NULL,
      persistQuickView INTEGER NOT NULL,
      layout TEXT NOT NULL,
      attributesJson TEXT NOT NULL,
      actionsJson TEXT,
      nextSyncAction TEXT NOT NULL
    )
  """);
}

Future<void> createActiveNotificationsTable(Database db) async {
  await db.execute("""
    CREATE TABLE $tableActiveNotifications(
      pinId TEXT PRIMARY KEY NOT NULL,
      notifId INT NOT NULL,
      packageId TEXT NOT NULL,
      tagId TEXT NOT NULL
    )
  """);
}

Future<void> createAppsTable(Database db) async {
  await db.execute("""
    CREATE TABLE $tableApps(
      uuid TEXT PRIMARY KEY NOT NULL,
      shortName TEXT NOT NULL,
      longName TEXT NOT NULL,
      isWatchface INTEGER NOT NULL,
      company TEXT NOT NULL,
      appstoreId TEXT,
      version TEXT NOT NULL,
      appOrder INTEGER NOT NULL,
      supportedHardware TEXT NOT NULL,
      nextSyncAction TEXT NOT NULL
    )
  """);
}

void _createDb(Database db) async {
  await createTimelinePinsTable(db);
  await createActiveNotificationsTable(db);
  await createAppsTable(db);
}

void _upgradeDb(Database db, int oldVersion, int newVersion) async {
  if (oldVersion < 2) {
    await db.execute("UPDATE $tableTimelinePins SET type = lower(type)");
    await db.execute("UPDATE $tableTimelinePins "
        "SET layout = 'calendarPin' "
        "WHERE layout = 'CALENDAR_PIN'");
  }

  if (oldVersion < 3) {
    createActiveNotificationsTable(db);
    createAppsTable(db);
  }
}

final AutoDisposeFutureProvider<Database> databaseProvider =
    FutureProvider.autoDispose<Database>((key) async {
  final dbFolder = await (getDatabasesPath() as FutureOr<String>);
  final dbPath = join(dbFolder, "cobble.db");

  final db = await openDatabase(dbPath,
      version: 3,
      onCreate: (db, name) {
        _createDb(db);
      },
      onUpgrade: (db, oldVersion, newVersion) =>
          _upgradeDb(db, oldVersion, newVersion));

  // Note: DB is never closed because closing will cause errors in background
  // code. See https://github.com/tekartik/sqflite/issues/558.

  return db;
});
