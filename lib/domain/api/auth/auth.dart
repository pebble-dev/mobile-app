import 'package:cobble/domain/api/auth/oauth.dart';
import 'package:cobble/domain/api/auth/user.dart';
import 'package:cobble/domain/api/boot/boot.dart';
import 'package:cobble/domain/api/no_token_exception.dart';
import 'package:cobble/infrastructure/datasources/preferences.dart';
import 'package:cobble/infrastructure/datasources/secure_storage.dart';
import 'package:cobble/infrastructure/datasources/web_services/auth.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

final authServiceProvider = FutureProvider<AuthService>((ref) async {
  final boot = await (await ref.watch(bootServiceProvider.future)).config;
  final token = await (await ref.watch(tokenProvider.last));
  final oauth = await ref.watch(oauthClientProvider.future);
  final prefs = await ref.watch(preferencesProvider.future);
  if (token == null) {
    throw NoTokenException("Service requires a token but none was found in storage");
  }
  return AuthService(boot.auth.base, prefs, oauth, token);
});

final authUserProvider = FutureProvider<User?>((ref) async {
  try {
    final auth = await ref.watch(authServiceProvider.future);
    return await auth.user;
  } on NoTokenException {
    return null;
  }
});
