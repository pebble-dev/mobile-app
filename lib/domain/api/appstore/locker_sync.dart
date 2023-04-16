import 'dart:io';

import 'package:cobble/domain/api/appstore/locker_entry.dart';
import 'package:cobble/domain/api/no_token_exception.dart';
import 'package:cobble/domain/db/dao/locker_cache_dao.dart';
import 'package:cobble/domain/db/models/locker_app.dart';
import 'package:cobble/infrastructure/datasources/web_services/appstore.dart';
import 'package:flutter/foundation.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:state_notifier/state_notifier.dart';
import 'package:uuid_type/uuid_type.dart';
import 'package:logging/logging.dart';

import 'appstore.dart';

class LockerSync extends StateNotifier<List<LockerEntry>?> {
  final Future<AppstoreService> appstoreFuture;
  final LockerCacheDao lockerCacheDao;
  final Logger _logger = Logger("LockerSync");
  final HttpClient _client = HttpClient();

  LockerSync(this.appstoreFuture, this.lockerCacheDao) : super(null) {
    refresh();
  }

  Future<void> refresh() async {
    try {
      final appstore = await appstoreFuture;
      final locker = await appstore.locker;

      final currentCache = await lockerCacheDao.getAll();
      for (var current in currentCache) {
        if (locker.indexWhere((updated) => current.id == updated.id) != -1 && current.markedForDeletion) {
          await appstore.removeFromLocker(current.uuid.toString());
        }
      }

      await lockerCacheDao.clear();
      await Future.forEach(locker.map(LockerApp.fromApi), lockerCacheDao.insertOrUpdate);
      if (mounted) {
        state = locker;
      }
    } on NoTokenException {
      if (kDebugMode) {
        _logger.warning("Refresh skipped due to no auth");
      }
    }
  }

  Future<void> addToLocker(Uuid uuid) async {
    final appstore = await appstoreFuture;
    await appstore.addToLocker(uuid.toString());
    await refresh();
  }

  Future<void> removeFromLocker(Uuid uuid) async {
    await lockerCacheDao.markForDeletionByUuid(uuid); // done locally and actioned upon refresh for offline-first
    await refresh();
  }
}

final lockerSyncProvider = AutoDisposeStateNotifierProvider((ref) {
  final appstoreFuture = ref.watch(appstoreServiceProvider.future);
  final lockerCacheDao = ref.watch(lockerCacheDaoProvider);
  return LockerSync(appstoreFuture, lockerCacheDao);
});