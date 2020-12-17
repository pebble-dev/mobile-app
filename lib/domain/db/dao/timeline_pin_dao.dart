import 'package:cobble/domain/db/cobble_database.dart';
import 'package:cobble/domain/db/models/next_sync_action.dart';
import 'package:cobble/domain/db/models/timeline_pin.dart';
import 'package:hooks_riverpod/all.dart';
import 'package:sqflite/sqflite.dart';
import 'package:sqflite_common/sqlite_api.dart';
import 'package:uuid_type/uuid_type.dart';

class TimelinePinDao {
  Future<Database> _dbFuture;

  TimelinePinDao(this._dbFuture);

  Future<void> insertOrUpdateTimelinePin(TimelinePin pin) async {
    final db = await _dbFuture;

    db.insert(tableTimelinePins, pin.toMap(),
        conflictAlgorithm: ConflictAlgorithm.replace);
  }

  Future<List<TimelinePin>> getAllPins() async {
    final db = await _dbFuture;

    return (await db.query(tableTimelinePins))
        .map((e) => TimelinePin.fromMap(e))
        .toList();
  }

  Future<List<TimelinePin>> getPinsFromParent(Uuid parentId) async {
    final db = await _dbFuture;

    return (await db.query(
      tableTimelinePins,
      where: "parentId = ?",
      whereArgs: [parentId.toString()],
    ))
        .map((e) => TimelinePin.fromMap(e))
        .toList();
  }

  Future<List<TimelinePin>> getAllPinsWithPendingSyncAction() async {
    final db = await _dbFuture;

    return (await db.query(
      tableTimelinePins,
      where: "nextSyncAction <> \"Nothing\"",
      orderBy: "timestamp ASC"))
        .map((e) => TimelinePin.fromMap(e))
        .toList();
  }

  Future<void> setSyncAction(
      Uuid itemId, NextSyncAction newNextSyncAction) async {
    final db = await _dbFuture;

    await db.update(
        tableTimelinePins,
        {
          "nextSyncAction":
              TimelinePin.nextSyncActionEnumMap()[newNextSyncAction]
        },
        where: "itemId = ?",
        whereArgs: [itemId.toString()]);
  }

  Future<void> delete(Uuid itemId) async {
    final db = await _dbFuture;

    await db.delete(tableTimelinePins,
        where: "itemId = ?", whereArgs: [itemId.toString()]);
  }

  Future<void> deleteAll() async {
    final db = await _dbFuture;
    await db.delete(tableTimelinePins);
  }
}

final timelinePinDaoProvider = Provider.autoDispose((ref) {
  final dbFuture = ref.watch(databaseProvider.future);
  return TimelinePinDao(dbFuture);
});

const tableTimelinePins = "timeline_pin";
