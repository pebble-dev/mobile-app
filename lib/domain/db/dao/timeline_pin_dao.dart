import 'package:cobble/domain/db/cobble_database.dart';
import 'package:cobble/domain/db/models/timeline_pin.dart';
import 'package:hooks_riverpod/all.dart';
import 'package:sqflite/sqflite.dart';
import 'package:sqflite_common/sqlite_api.dart';

class TimelinePinDao {
  Future<Database> dbFuture;

  TimelinePinDao(this.dbFuture);

  void insertOrUpdateTimelinePin(TimelinePin pin) async {
    final db = await dbFuture;

    db.insert(TABLE_TIMELINE_PINS, pin.toMap(),
        conflictAlgorithm: ConflictAlgorithm.replace);
  }

  Future<List<TimelinePin>> getAllPins() async {
    final db = await dbFuture;

    return (await db.query(TABLE_TIMELINE_PINS))
        .map((e) => TimelinePin.fromMap(e))
        .toList();
  }
}

final timelinePinDaoProvider = Provider.autoDispose((ref) {
  final dbFuture = ref.watch(databaseProvider.future);
  return TimelinePinDao(dbFuture);
});

const TABLE_TIMELINE_PINS = "timeline_pin";
