import 'package:cobble/domain/api/auth/oauth.dart';
import 'package:cobble/domain/api/boot/boot.dart';
import 'package:cobble/domain/api/no_token_exception.dart';
import 'package:cobble/infrastructure/datasources/preferences.dart';
import 'package:cobble/infrastructure/datasources/secure_storage.dart';
import 'package:cobble/infrastructure/datasources/web_services/cohorts.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

final cohortsServiceProvider = FutureProvider<CohortsService>((ref) async {
  final boot = await (await ref.watch(bootServiceProvider.future)).config; //TODO: add cohorts to boot config
  final token = await (await ref.watch(tokenProvider.future));
  final oauth = await ref.watch(oauthClientProvider.future);
  final prefs = await ref.watch(preferencesProvider.future);
  if (token == null) {
    throw NoTokenException("Service requires a token but none was found in storage");
  }
  return CohortsService("https://cohorts.rebble.io", prefs, oauth, token);
});