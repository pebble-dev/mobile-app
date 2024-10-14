import 'dart:async';
import 'dart:convert';

import 'package:cobble/domain/api/auth/oauth_token.dart';
import 'package:cobble/domain/secure_storage.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:stream_transform/stream_transform.dart';

class SecureStorage {
  final FlutterSecureStorage _secureStorage;

  late StreamController<SecureStorage> _secureStorageUpdateStream;
  late Stream<SecureStorage> secureStorageUpdateStream;

  SecureStorage(this._secureStorage) {
    _secureStorageUpdateStream = StreamController<SecureStorage>.broadcast();

    secureStorageUpdateStream = _secureStorageUpdateStream.stream;
  }

  Future<OAuthToken?> getToken() async {
    final tokenJson = await _secureStorage.read(key: "token");
    if (tokenJson == null) {
        return null;
    }else {
        return OAuthToken.fromJson(jsonDecode(tokenJson));
    }
  }

  Future<void> setToken(OAuthToken? value) async {
    await _secureStorage.write(
        key: "token",
        value: value != null ? jsonEncode(value.toJson()) : null,
    );
    _secureStorageUpdateStream.add(this);
  }
}

final secureStorageProvider =
  Provider<SecureStorage>((ref) => SecureStorage(ref.watch(flutterSecureStorageProvider)));

final tokenProvider =
  _createSecureStorageItemProvider<Future<OAuthToken?>>((secureStorage) => secureStorage.getToken());

StreamProvider<T> _createSecureStorageItemProvider<T>(
    T Function(SecureStorage secureStorage) mapper,
    ) {
  return StreamProvider<T>((ref) {
    final secureStorage = ref.watch(secureStorageProvider);

    return secureStorage.secureStorageUpdateStream
        .startWith(secureStorage)
        .map(mapper)
        .distinct();
  });
}
