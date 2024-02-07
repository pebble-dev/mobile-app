import 'package:cobble/infrastructure/datasources/preferences.dart';
import 'package:cobble/infrastructure/datasources/web_services/boot.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

final bootServiceProvider = FutureProvider<BootService>(
  (ref) async => BootService(await ref.watch(bootUrlProvider.last) ?? ""),
);