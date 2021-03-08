import 'package:cobble/domain/db/models/app.dart';
import 'package:hooks_riverpod/all.dart';
import 'package:sqflite/sqflite.dart';
import 'package:sqflite_common/sqlite_api.dart';

import '../cobble_database.dart';

class AppDao {
  Future<Database> _dbFuture;

  AppDao(this._dbFuture);

  Future<void> insertOrUpdateApp(App app) async {
    final db = await _dbFuture;

    db.insert(tableApps, app.toMap(),
        conflictAlgorithm: ConflictAlgorithm.replace);
  }

  Future<List<App>> getAllInstalledApps() async {
    final db = await _dbFuture;

    final receivedApps = (await db.query(tableApps));

    return receivedApps.map((e) => App.fromMap(e)).toList();
  }
}

final AutoDisposeProvider<AppDao> appDaoProvider = Provider.autoDispose((ref) {
  final dbFuture = ref.watch(databaseProvider.future);
  return AppDao(dbFuture);
});


const tableApps = "app";
