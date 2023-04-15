import 'dart:io';

import 'package:cobble/domain/api/appstore/locker_entry.dart';
import 'package:cobble/domain/api/no_token_exception.dart';
import 'package:cobble/domain/db/dao/locker_cache_dao.dart';
import 'package:cobble/domain/db/models/locker_app.dart';
import 'package:cobble/infrastructure/datasources/web_services/appstore.dart';
import 'package:flutter/foundation.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:path_provider/path_provider.dart';
import 'package:state_notifier/state_notifier.dart';
import 'package:uuid_type/uuid_type.dart';
import 'package:logging/logging.dart';

import '../../logging.dart';
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
    final docsDir = (await getApplicationDocumentsDirectory()).parent; // .parent escapes from 'flutter specific' dir
    final appDir = Directory(docsDir.path + "/files/apps");
    if (!await appDir.exists()) {
      appDir.create(recursive: true);
    }

    try {
      final appstore = await appstoreFuture;
      final locker = await appstore.locker;

      final currentCache = await lockerCacheDao.getAll();
      for (var current in currentCache) {
        if (locker.indexWhere((updated) => current.id == updated.id) != -1 && current.markedForDeletion) {
          await appstore.removeFromLocker(current.uuid.toString());
        }
      }

      for (var nw in locker) {
        final uuid = nw.uuid.toLowerCase();
        final appFile = File(appDir.path + "/" + uuid + ".pbw");
        final currentI = currentCache.indexWhere((element) => element.id == nw.id);
        if (!await appFile.exists() || (currentI != -1 && currentCache[currentI].version != nw.version)) {
          Log.d("Downloading app $uuid as it doesn't exist/has update...");
          final fd = appFile.openWrite();
          try {
            final uri = nw.pbw?.file.isNotEmpty == true ? Uri.parse(nw.pbw!.file) : null;
            if (uri == null) {
              Log.e("No PBW for $uuid, skipping");
              continue;
            }
            final req = await _client.getUrl(uri);
            final res = await req.close();
            if (res.statusCode == 200) {
              await res.pipe(fd);
            } else {
              Log.e("Error downloading PBW for $uuid: ${res.statusCode}, skipping");
              continue;
            }
          } finally {
            await fd.close();
          }
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
    refresh();
  }

  Future<void> removeFromLocker(Uuid uuid) async {
    await lockerCacheDao.markForDeletionByUuid(uuid); // done locally and actioned upon refresh for offline-first
    refresh();
  }
}

final lockerSyncProvider = AutoDisposeStateNotifierProvider((ref) {
  final appstoreFuture = ref.watch(appstoreServiceProvider.future);
  final lockerCacheDao = ref.watch(lockerCacheDaoProvider);
  return LockerSync(appstoreFuture, lockerCacheDao);
});