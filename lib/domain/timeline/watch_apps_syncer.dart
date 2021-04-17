import 'package:cobble/domain/apps/app_compatibility.dart';
import 'package:cobble/domain/connection/connection_state_provider.dart';
import 'package:cobble/domain/db/dao/app_dao.dart';
import 'package:cobble/domain/db/models/next_sync_action.dart';
import 'package:cobble/domain/entities/hardware_platform.dart';
import 'package:cobble/domain/timeline/blob_status.dart';
import 'package:cobble/infrastructure/datasources/preferences.dart';
import 'package:cobble/infrastructure/pigeons/pigeons.g.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

import '../logging.dart';

/// Sync controller that handles synchronization between internal database and
/// watch
///
/// Should only be used from background isolate
class WatchAppsSyncer {
  final AppDao appDao;
  final AppInstallControl appInstallControl;
  final ConnectionCallbacksStateNotifier connectionStateProvider;
  final Future<Preferences> preferences;

  WatchAppsSyncer(this.appDao, this.appInstallControl,
      this.connectionStateProvider, this.preferences);

  Future<bool> syncAppDatabaseWithWatch() async {
    Log.d('Syncing apps');
    final status = await _performSync();

    switch (status) {
      case statusSuccess:
        Log.d("App Sync OK");
        return true;
      case statusInvalidOperation:
      case statusInvalidDatabaseId:
      case statusInvalidData:
      case statusDataStale:
      case statusNotSupported:
      case statusLocked:
        Log.e("App Sync failed due to a bug in the sync engine: $status");
        return false;
      case statusDatabaseFull:
        Log.w("App Sync database is full");
        return false;
      case statusGeneralFailure:
      case statusTryLater:
      case statusWatchDisconnected:
      default:
        Log.w("App Sync failed ($status)...");
        // We have no idea what has gone wrong
        return false;
    }
  }

  Future<int?> _performSync() async {
    final connectedWatch = connectionStateProvider.state.currentConnectedWatch;
    if (connectedWatch == null) {
      return statusWatchDisconnected;
    }

    final connectedWatchType =
        connectedWatch.runningFirmware.hardwarePlatform.getWatchType();

    try {
      final appsToDelete = await appDao.getAllPackagesWithPendingDelete();
      for (final appToDelete in appsToDelete) {
        final StringWrapper idWrapper = StringWrapper();
        idWrapper.value = appToDelete.uuid.toString();

        final res = await appInstallControl.removeAppFromBlobDb(idWrapper);

        if (res.value != statusSuccess) {
          return res.value;
        }

        await appDao.delete(appToDelete.uuid);
      }

      final appsToUpload = await appDao.getAllPackagesWithPendingUpload();
      for (final appToSync in appsToUpload) {
        Log.d('Pending app $appToSync');

        if (!appToSync.isCompatibleWith(connectedWatchType)) {
          Log.d("App not compatible. Skipping...");
          continue;
        }

        final StringWrapper uuidWrapper = StringWrapper();
        uuidWrapper.value = appToSync.uuid.toString();
        Log.d('Inserting app');
        final res = await appInstallControl.insertAppIntoBlobDb(uuidWrapper);

        if (res.value != statusSuccess) {
          return res.value;
        }

        await appDao.setSyncAction(
          appToSync.uuid,
          NextSyncAction.Nothing,
        );
      }
    } catch (e) {
      Log.e(e.toString());
      // Log error to native
      return statusInvalidData;
    }

    final preferences = await this.preferences;

    await preferences.reload();
    if (preferences.isAppReorderPending()) {
      final allApps = await appDao.getAllInstalledApps();
      final uuidStrings = allApps.map((e) => e.uuid.toString()).toList();
      final listWrapper = ListWrapper();
      listWrapper.value = uuidStrings;

      final statusWrapper =
          await appInstallControl.sendAppOrderToWatch(listWrapper);
      final status = statusWrapper.value;

      if (status != statusSuccess) {
        Log.e("Reorder failed: $status");
        return status;
      }

      await preferences.setAppReorderPending(false);
    }

    return statusSuccess;
  }

  Future<bool> clearAllAppsFromWatchAndResync() async {
    final res = await appInstallControl.removeAllApps();

    if (res.value != statusSuccess) {
      Log.d("App clearing failed  ${res.value}");
      return false;
    }

    await appDao.resetSyncStatus();
    return syncAppDatabaseWithWatch();
  }
}

final AutoDisposeProvider<WatchAppsSyncer> watchAppSyncerProvider =
Provider.autoDispose<WatchAppsSyncer>((ref) {
  final appDao = ref.watch(appDaoProvider);
  final appInstallControl = ref.watch(appInstallControlProvider);
  final connectionState = ref.watch(connectionStateProvider);
  final preferences = ref.read(preferencesProvider.future);

  return WatchAppsSyncer(
    appDao,
    appInstallControl,
    connectionState,
    preferences,
  );
});

final appInstallControlProvider = Provider((ref) => AppInstallControl());
