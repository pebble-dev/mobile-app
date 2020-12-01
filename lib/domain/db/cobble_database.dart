import 'package:hooks_riverpod/all.dart';
import 'package:path/path.dart';
import 'package:sqflite/sqflite.dart';

import 'dao/timeline_pin_dao.dart';

void createAllCobbleTables(Database db) async {
  await db.execute("""
    CREATE TABLE $TABLE_TIMELINE_PINS(
      itemId TEXT PRIMARY KEY NOT NULL,
      parentId TEXT NOT NULL,
      backingId TEXT,
      timestamp INT NOT NULL,
      duration INT NOT NULL,
      type TEXT NTO NULL,
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

final databaseProvider = FutureProvider.autoDispose<Database>((key) async {
  final dbFolder = await getDatabasesPath();
  final dbPath = join(dbFolder, "cobble.db");

  final db = await openDatabase(dbPath,
      version: 1, onCreate: (db, name) => createAllCobbleTables(db));

  key.onDispose(() {
    db.close();
  });

  return db;
});
