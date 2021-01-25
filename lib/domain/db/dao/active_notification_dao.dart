import 'package:cobble/domain/db/models/active_notification.dart';
import 'package:hooks_riverpod/all.dart';
import 'package:sqflite/sqflite.dart';
import 'package:uuid_type/uuid_type.dart';

import '../cobble_database.dart';

const tableActiveNotifications = "active_notif";

class ActiveNotificationDao {
  Future<Database> _dbFuture;

  ActiveNotificationDao(this._dbFuture);

  Future<void> insertOrUpdateActiveNotification(ActiveNotification notif) async {
    final db = await _dbFuture;

    db.insert(tableActiveNotifications, notif.toMap(),
        conflictAlgorithm: ConflictAlgorithm.replace);
  }

  Future<ActiveNotification> getActiveNotifByPinId(Uuid id) async {
    final db = await _dbFuture;

    final receivedActiveNotifs = (await db.query(
      tableActiveNotifications,
      where: "pinId = ?",
      whereArgs: [id.toString()],
    ));

    if (receivedActiveNotifs.isEmpty) {
      return null;
    }

    return ActiveNotification.fromMap(receivedActiveNotifs.first);
  }

  Future<ActiveNotification> getActiveNotifByNotifMeta(int notifId, String packageId, String tagId) async {
    final db = await _dbFuture;

    final receivedActiveNotifs = (await db.query(
      tableActiveNotifications,
      where: "notifId = ? AND packageId = ? AND tagId = ?",
      whereArgs: [notifId, packageId, tagId],
    ));

    if (receivedActiveNotifs.isEmpty) {
      return null;
    }

    return ActiveNotification.fromMap(receivedActiveNotifs.first);
  }

  Future<List<ActiveNotification>> getAllActiveNotifs() async {
    final db = await _dbFuture;

    return (await db.query(tableActiveNotifications))
        .map((e) => ActiveNotification.fromMap(e))
        .toList();
  }

  Future<void> delete(Uuid pinId) async {
    final db = await _dbFuture;

    await db.delete(tableActiveNotifications,
        where: "pinId = ?", whereArgs: [pinId.toString()]);
  }
}

final activeNotifDaoProvider = Provider.autoDispose((ref) {
  final dbFuture = ref.watch(databaseProvider.future);
  return ActiveNotificationDao(dbFuture);
});