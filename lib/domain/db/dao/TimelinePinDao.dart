import 'package:cobble/domain/db/models/TimelinePin.dart';
import 'package:hooks_riverpod/all.dart';
import 'package:sqflite/sqflite.dart';
import 'package:sqflite_common/sqlite_api.dart';

import '../CobbleDatabase.dart';

class TimelinePinDao {
  ProviderReference _ref;

  TimelinePinDao(this._ref);

  void insertOrUpdateTimelinePin(TimelinePin pin) async {
    final db = await _getDb();

    try {
      db.insert(TABLE_TIMELINE_PINS, pin.toMap(),
          conflictAlgorithm: ConflictAlgorithm.replace);
    } finally {
      await db.close();
    }
  }

  Future<List<TimelinePin>> getAllPins() async {
    final db = await _getDb();

    try {
      return (await db.query(TABLE_TIMELINE_PINS))
          .map((e) => TimelinePin.fromMap(e))
          .toList();
    } finally {
      await db.close();
    }
  }

  Future<Database> _getDb() async =>
      await _ref.container.read(databaseProvider.future);
}

final timelinePinDaoProvider = Provider((ref) => TimelinePinDao(ref));

const TABLE_TIMELINE_PINS = "timeline_pin";
