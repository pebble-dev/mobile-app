
import 'package:cobble/domain/db/cobble_database.dart';
import 'package:cobble/domain/db/models/notification_channel.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:sqflite/sqflite.dart';

const tableChannels = "notif_channel";

class NotificationChannelDao {
  Future<Database> _dbFuture;

  NotificationChannelDao(this._dbFuture);

  Future<void> insertOrUpdateNotificationChannel(NotificationChannel channel) async {
    final db = await _dbFuture;

    db.insert(tableChannels, channel.toMap(),
        conflictAlgorithm: ConflictAlgorithm.replace);
  }

  Future<NotificationChannel?> getNotifChannelByIds(String channelId, String packageId) async {
    final db = await _dbFuture;

    final receivedChannels = (await db.query(
      tableChannels,
      where: "channelId = ? AND packageId = ?",
      whereArgs: [channelId, packageId],
    ));

    if (receivedChannels.isEmpty) {
      return null;
    }

    return NotificationChannel.fromMap(receivedChannels.first);
  }

  Future<List<NotificationChannel>> getNotifChannelsByPackage(String packageId) async {
    final db = await _dbFuture;

    final receivedChannels = (await db.query(
      tableChannels,
      where: "packageId = ?",
      whereArgs: [packageId],
    ));

    if (receivedChannels.isEmpty) {
      return [];
    }

    return receivedChannels.map((e) => NotificationChannel.fromMap(e)).toList();
  }

  Future<int> deleteNotifChannelByIds(String channelId, String packageId) async {
    final db = await _dbFuture;

    return db.delete(
      tableChannels,
      where: "channelId = ? AND packageId = ?",
      whereArgs: [channelId, packageId]
    );
  }
}

final AutoDisposeProvider<NotificationChannelDao> notifChannelDaoProvider = Provider.autoDispose((ref) {
  final dbFuture = ref.watch(databaseProvider!.future);
  return NotificationChannelDao(dbFuture);
});