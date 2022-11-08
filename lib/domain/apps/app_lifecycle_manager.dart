import 'package:cobble/infrastructure/pigeons/pigeons.g.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:uuid_type/uuid_type.dart';

class AppLifecycleManager {
  final appLifecycleControl = AppLifecycleControl();

  Future<void> openApp(Uuid uuid) async {
    final uuidWrapper = StringWrapper();
    uuidWrapper.value = uuid.toString();

    await appLifecycleControl.openAppOnTheWatch(uuidWrapper);
  }
}

final appLifecycleManagerProvider = Provider<AppLifecycleManager>((ref) {
  return AppLifecycleManager();
});
