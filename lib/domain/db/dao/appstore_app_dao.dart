import 'package:cobble/domain/db/models/appstore_app.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:sqflite/sqflite.dart';
import 'package:sqflite_common/sqlite_api.dart';
import 'package:uuid_type/uuid_type.dart';

import '../cobble_database.dart';

class AppstoreAppDao {
  Future<Database> _dbFuture;

  AppstoreAppDao(this._dbFuture);

  Future<void> insertOrUpdatePackage(AppstoreApp appstoreApp) async {
    final db = await _dbFuture;

    db.insert(tableAppstoreApps, appstoreApp.toMap(),
        conflictAlgorithm: ConflictAlgorithm.replace);
  }

  Future<AppstoreApp?> getPackage(Uuid itemId) async {
    final db = await _dbFuture;

    final list = (await db.query(tableAppstoreApps,
            where: "uuid = ?", whereArgs: [itemId.toString()]))
        .map((e) => AppstoreApp.fromMap(e))
        .toList();

    if (!list.isEmpty) {
      return list.first;
    } else {
      return null;
    }
  }

  Future<void> delete(Uuid itemId) async {
    final db = await _dbFuture;
    await db
        .delete(tableAppstoreApps, where: "uuid = ?", whereArgs: [itemId.toString()]);
  }
}

final AutoDisposeProvider<AppstoreAppDao> appstoreAppDaoProvider = Provider.autoDispose((ref) {
  final dbFuture = ref.watch(databaseProvider.future);
  return AppstoreAppDao(dbFuture);
});

const tableAppstoreApps = "appstore_app";
