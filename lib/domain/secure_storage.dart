import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

const _androidOptions = AndroidOptions(encryptedSharedPreferences: true);
final flutterSecureStorageProvider =
  Provider<FlutterSecureStorage>((ref) => const FlutterSecureStorage(aOptions: _androidOptions));