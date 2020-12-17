import 'package:hooks_riverpod/all.dart';
import 'package:path/path.dart';
import 'package:sqflite/sqflite.dart';

import 'dao/timeline_pin_dao.dart';

void createAllCobbleTables(Database db) async {
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

void _upgradeDb(Database db, int oldVersion, int newVersion) async {
  if (oldVersion < 2) {
    await db.execute("UPDATE $tableTimelinePins SET type = lower(type)");
    await db.execute("UPDATE $tableTimelinePins "
        "SET layout = 'calendarPin' "
        "WHERE layout = 'CALENDAR_PIN'");
  }
}

final databaseProvider = FutureProvider.autoDispose<Database>((key) async {
  final dbFolder = await getDatabasesPath();
  final dbPath = join(dbFolder, "cobble.db");

  final db = await openDatabase(dbPath,
      version: 2,
      onCreate: (db, name) => createAllCobbleTables(db),
      onUpgrade: (db, oldVersion, newVersion) =>
          _upgradeDb(db, oldVersion, newVersion));

  // Note: DB is never closed because closing will cause errors in background
  // code. See https://github.com/tekartik/sqflite/issues/558.

  return db;
});
