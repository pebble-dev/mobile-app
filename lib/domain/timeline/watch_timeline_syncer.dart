import 'package:cobble/domain/db/dao/timeline_pin_dao.dart';
import 'package:cobble/domain/db/models/next_sync_action.dart';
import 'package:cobble/domain/timeline/blob_status.dart';
import 'package:cobble/infrastructure/pigeons/pigeons.dart';
import 'package:hooks_riverpod/all.dart';

/// Sync controller that handles synchronization between internal database and
/// watch

class WatchTimelineSyncer {
  final TimelinePinDao timelinePinDao;
  final TimelineControl timelineControl = TimelineControl();

  WatchTimelineSyncer(this.timelinePinDao);

  Future<int> syncPinDatabaseWithWatch() async {
    try {
      final pinsToDelete = await timelinePinDao.getAllPinsWithPendingDelete();
      for (final pinToDelete in pinsToDelete) {
        final StringWrapper idWrapper = StringWrapper();
        idWrapper.value = pinToDelete.itemId.toString();

        final res = await timelineControl.removePin(idWrapper);

        if (res.value != statusSuccess) {
          return res.value;
        }

        await timelinePinDao.delete(pinToDelete.itemId);
      }

      final pinsToUpload = await timelinePinDao.getAllPinsWithPendingUpload();
      for (final pinToSync in pinsToUpload) {
        final res = await timelineControl.addPin(pinToSync.toPigeon());

        if (res.value != statusSuccess) {
          return res.value;
        }

        await timelinePinDao.setSyncAction(
            pinToSync.itemId, NextSyncAction.Nothing);
      }
    } catch (e) {
      // Log error to native
      return statusInvalidData;
    }

    return statusSuccess;
  }

  Future<int> removeAllPins() async {
    final res = await timelineControl.removeAllPins();

    if (res.value != statusSuccess) {
      return res.value;
    }

    await timelinePinDao.deleteAll();
    return statusSuccess;
  }
}

final watchTimelineSyncerProvider =
Provider.autoDispose<WatchTimelineSyncer>((ref) {
  final timelinePinDao = ref.watch(timelinePinDaoProvider);

  return WatchTimelineSyncer(timelinePinDao);
});
