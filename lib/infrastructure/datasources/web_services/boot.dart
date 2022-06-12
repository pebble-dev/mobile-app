import 'dart:async';

import 'package:cobble/domain/api/boot/auth_config.dart';
import 'package:cobble/domain/api/boot/base_url_entry.dart';
import 'package:cobble/domain/api/boot/boot_config.dart';
import 'package:cobble/infrastructure/datasources/web_services/service.dart';
import 'package:flutter/foundation.dart';

const _confLifetime = Duration(hours: 1);

final _offlineBootConfig = BootConfig(
    auth: AuthConfig(
      base: BaseURLEntry("http://auth.test/api"),
      authoriseUrl: "http://auth.test:8086/oauth/authorise",
      refreshUrl: "http://auth.test:8086/oauth/token",
    ),
);

class BootService extends Service {
  BootConfig? _conf;
  DateTime? _confAge;
  String? token;

  BootService(String baseUrl) : super(baseUrl);

  Future<BootConfig> get config async {
    if (_conf == null || _confAge == null ||
        DateTime.now().difference(_confAge!) >= _confLifetime) {
      _confAge = DateTime.now();
      try {
        BootConfig bootConfig = await reqBootConfig();
        _conf = bootConfig;
        return bootConfig;
      } catch (e) {
        if (kDebugMode) {
          print("Error getting boot config: $e");
        }
        return _offlineBootConfig;
      }
    } else {
      return _conf!;
    }
  }

  Future<BootConfig> reqBootConfig() async {
    return client.getSerialized(BootConfig.fromJson, "cobble");
  }
}