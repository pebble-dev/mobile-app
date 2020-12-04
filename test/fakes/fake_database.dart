import 'package:cobble/domain/db/cobble_database.dart';
import 'package:sqflite/sqflite.dart';
import 'package:sqflite_common_ffi/sqflite_ffi.dart';

Future<Database> createTestCobbleDatabase() async {
  sqfliteFfiInit();

  await databaseFactoryFfi.deleteDatabase(inMemoryDatabasePath);

  final db = await databaseFactoryFfi.openDatabase(inMemoryDatabasePath);
  createAllCobbleTables(db);
  return db;
}
