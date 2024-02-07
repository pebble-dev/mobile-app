import 'package:cobble/domain/db/models/locker_app.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:sqflite/sqflite.dart';
import 'package:sqflite_common/sqlite_api.dart';
import 'package:uuid_type/uuid_type.dart';

import '../cobble_database.dart';

class LockerCacheDao {
  final Future<Database> _dbFuture;

  LockerCacheDao(this._dbFuture);

  Future<void> insertOrUpdate(LockerApp app) async {
    final db = await _dbFuture;

    db.insert(tableLocker, app.toMap(),
        conflictAlgorithm: ConflictAlgorithm.replace);
  }

  Future<List<LockerApp>> getAll() async {
    final db = await _dbFuture;

    final receivedApps = await db.query(tableLocker);

    return receivedApps.map((e) => LockerApp.fromMap(e)).toList();
  }

  Future<LockerApp?> get(String appstoreId) async {
    final db = await _dbFuture;

    final receivedApps = await db.query(tableLocker, where: "id = ?", whereArgs: [appstoreId]);
    if (receivedApps.isNotEmpty) {
      return LockerApp.fromMap(receivedApps.first);
    } else {
      return null;
    }
  }

  Future<void> clear() async {
    final db = await _dbFuture;

    await db.delete(tableLocker);
  }

  Future<void> markForDeletion(String appstoreId) async {
    final db = await _dbFuture;
    await db.update(tableLocker, {"markedForDeletion": 1}, where: "id = ?", whereArgs: [appstoreId]);
  }

  Future<void> markForDeletionByUuid(Uuid uuid) async {
    final db = await _dbFuture;
    await db.update(tableLocker, {"markedForDeletion": 1}, where: "uuid = ?", whereArgs: [uuid.toString()]);
  }
}

final AutoDisposeProvider<LockerCacheDao> lockerCacheDaoProvider = Provider.autoDispose<LockerCacheDao>((ref) {
  final dbFuture = ref.watch(databaseProvider.future);
  return LockerCacheDao(dbFuture);
});

const tableLocker = "locker";