import 'package:cobble/domain/db/dao/timeline_pin_dao.dart';
import 'package:cobble/domain/db/models/next_sync_action.dart';
import 'package:cobble/domain/local_notifications.dart';
import 'package:cobble/domain/timeline/blob_status.dart';
import 'package:cobble/infrastructure/pigeons/pigeons.g.dart';
import 'package:cobble/localization/localization.dart';
import 'package:cobble/util/container_extensions.dart';
import 'package:flutter_local_notifications/flutter_local_notifications.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

import '../logging.dart';

/// Sync controller that handles synchronization between internal database and
/// watch
///
/// Should only be used from background isolate
class WatchTimelineSyncer {
  final TimelinePinDao timelinePinDao;
  final TimelineSyncControl timelineSyncControl;
  final Future<AsyncValue<FlutterLocalNotificationsPlugin>>
      localNotificationsPlugin;
  final TimelineControl timelineControl = TimelineControl();

  WatchTimelineSyncer(
    this.timelinePinDao,
    this.timelineSyncControl,
    this.localNotificationsPlugin,
  );

  Future<bool> syncPinDatabaseWithWatch() async {
    final status = await _performSync();

    switch (status) {
      case statusSuccess:
        Log.d("Timeline Pin Sync OK");
        return true;
      case statusInvalidOperation:
      case statusInvalidDatabaseId:
      case statusInvalidData:
      case statusKeyDoesNotExist:
      case statusDataStale:
      case statusNotSupported:
      case statusLocked:
        Log.e(
            "Timeline Pin Sync failed due to a bug in the sync engine: $status");
        return false;
      case statusDatabaseFull:
        Log.w("Timeline Pin Sync database is full");
        _displayWatchFullWarning();
        return true;
      case statusGeneralFailure:
      case statusTryLater:
      case statusWatchDisconnected:
      default:
        Log.w("Timeline Pin Sync failed ($status). Retrying later...");
        // We have no idea what has gone wrong
        await this.timelineSyncControl.syncTimelineToWatchLater();
        return false;
    }
  }

  Future<int?> _performSync() async {
    try {
      final pinsToDelete = await timelinePinDao.getAllPinsWithPendingDelete();
      for (final pinToDelete in pinsToDelete) {
        final StringWrapper idWrapper = StringWrapper();
        idWrapper.value = pinToDelete.itemId.toString();

        final res = await timelineControl.removePin(idWrapper);

        if (res.value != statusSuccess && res.value != statusKeyDoesNotExist) {
          return res.value;
        }

        if (pinToDelete.nextSyncAction == NextSyncAction.DeleteThenIgnore) {
          await timelinePinDao.setSyncAction(
            pinToDelete.itemId,
            NextSyncAction.Ignore,
          );
        } else {
          await timelinePinDao.delete(pinToDelete.itemId);
        }
      }

      final pinsToUpload = await timelinePinDao.getAllPinsWithPendingUpload();
      for (final pinToSync in pinsToUpload) {
        final res = await timelineControl.addPin(pinToSync.toPigeon());

        if (res.value == statusDatabaseFull) {
          final dateAfterThreeDays = DateTime.now().add(Duration(days: 3));
          if (pinToSync.timestamp!.isAfter(dateAfterThreeDays)) {
            // Any pins after 3 day are just buffer to allow for offline
            // watch operations.
            // No need to trouble the user if we can't fit that buffer onto
            // the watch
            return statusSuccess;
          } else {
            return statusDatabaseFull;
          }
        }
        if (res.value != statusSuccess) {
          return res.value;
        }

        await timelinePinDao.setSyncAction(
          pinToSync.itemId,
          NextSyncAction.Nothing,
        );
      }
    } catch (e) {
      // Log error to native
      return statusInvalidData;
    }

    return statusSuccess;
  }

  Future<bool> clearAllPinsFromWatchAndResync() async {
    final res = await timelineControl.removeAllPins();

    if (res.value != statusSuccess) {
      Log.d("Pin clearing failed  ${res.value}");
      return false;
    }

    await timelinePinDao.resetSyncStatus();
    return syncPinDatabaseWithWatch();
  }

  void _displayWatchFullWarning() async {
    final pluginValue = await localNotificationsPlugin;
    if (pluginValue is AsyncError) {
      Log.e("Notification init failed: ${(pluginValue as AsyncError).error}");
      return;
    }

    final plugin = pluginValue.value!;

    const AndroidNotificationDetails androidPlatformChannelSpecifics =
        AndroidNotificationDetails("WARNINGS", "Warnings",
            channelDescription: "Warnings",
            importance: Importance.defaultImportance,
            priority: Priority.defaultPriority,
            showWhen: false);
    const NotificationDetails platformChannelSpecifics =
        NotificationDetails(android: androidPlatformChannelSpecifics);
    await plugin.show(
      0,
      tr.timelineSync.watchFull.p0,
      tr.timelineSync.watchFull.p1,
      platformChannelSpecifics,
    );
  }
}

final AutoDisposeProvider<WatchTimelineSyncer> watchTimelineSyncerProvider =
    Provider.autoDispose<WatchTimelineSyncer>((ref) {
  final timelinePinDao = ref.watch(timelinePinDaoProvider);
  final timelineSyncControl = ref.watch(timelineSyncControlProvider);
  final localNotificationsPlugin = ref.readUntilFirstSuccessOrError(
    localNotificationsPluginProvider,
  );

  return WatchTimelineSyncer(
    timelinePinDao,
    timelineSyncControl,
    localNotificationsPlugin,
  );
});

final timelineSyncControlProvider = Provider<TimelineSyncControl>((ref) => TimelineSyncControl());
