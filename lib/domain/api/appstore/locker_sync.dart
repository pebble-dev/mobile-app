import 'package:cobble/domain/api/appstore/locker_entry.dart';
import 'package:cobble/infrastructure/datasources/web_services/appstore.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:state_notifier/state_notifier.dart';

import 'appstore.dart';

class LockerSync extends StateNotifier<List<LockerEntry>?> {
  final AppstoreService appstore;

  LockerSync(this.appstore) : super(null);

  Future<void> refresh() async {
    state = await appstore.locker;
  }
}

final lockerSyncProvider = FutureProvider((ref) async {
  try {
    final appstore = await ref.watch(appstoreServiceProvider.future);
    return LockerSync(appstore);
  } catch (e) {
    print("Locker error: " + e.toString());
    return null;
  }
});