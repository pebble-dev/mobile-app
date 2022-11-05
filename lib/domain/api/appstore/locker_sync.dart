import 'dart:developer';

import 'package:cobble/domain/api/appstore/locker_entry.dart';
import 'package:cobble/domain/api/no_token_exception.dart';
import 'package:cobble/domain/db/dao/locker_cache_dao.dart';
import 'package:cobble/domain/db/models/locker_app.dart';
import 'package:cobble/infrastructure/datasources/web_services/appstore.dart';
import 'package:flutter/foundation.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:state_notifier/state_notifier.dart';

import 'appstore.dart';

class LockerSync extends StateNotifier<List<LockerEntry>?> {
  final Future<AppstoreService> appstoreFuture;
  final LockerCacheDao lockerCacheDao;

  LockerSync(this.appstoreFuture, this.lockerCacheDao) : super(null);

  Future<void> refresh() async {
    try {
      final appstore = await appstoreFuture;
      final locker = await appstore.locker;
      await lockerCacheDao.clear();
      await Future.forEach(locker.map(LockerApp.fromApi), lockerCacheDao.insertOrUpdate);
      if (mounted) {
        state = locker;
      }
    }on NoTokenException catch (e) {
      if (kDebugMode) {
        log("Refresh skipped due to no auth", error: e);
      }
    }
  }
}

final lockerSyncProvider = AutoDisposeStateNotifierProvider((ref) {
  final appstoreFuture = ref.watch(appstoreServiceProvider.future);
  final lockerCacheDao = ref.watch(lockerCacheDaoProvider);
  return LockerSync(appstoreFuture, lockerCacheDao);
});