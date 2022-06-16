import 'package:cobble/domain/api/boot/boot.dart';
import 'package:cobble/infrastructure/datasources/secure_storage.dart';
import 'package:cobble/infrastructure/datasources/web_services/auth.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

final authServiceProvider = Provider((ref) async {
  final boot = await (await ref.watch(bootServiceProvider.future)).config;
  final token = await (await ref.watch(tokenProvider.last));
  if (token == null) {
    throw StateError("Service requires a token but none was found in storage");
  }
  return AuthService(boot.auth.base, token);
});