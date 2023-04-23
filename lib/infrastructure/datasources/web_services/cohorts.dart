import 'dart:io';

import 'package:cobble/domain/api/auth/oauth.dart';
import 'package:cobble/domain/api/auth/oauth_token.dart';
import 'package:cobble/domain/api/cohorts/cohorts_response.dart';
import 'package:cobble/infrastructure/datasources/preferences.dart';
import 'package:cobble/infrastructure/datasources/web_services/service.dart';
import 'package:package_info_plus/package_info_plus.dart';
import 'package:device_info_plus/device_info_plus.dart';

const _cacheLifetime = Duration(hours: 1);

class CohortsService extends Service {
  CohortsService(String baseUrl, this._prefs, this._oauth, this._token)
      : super(baseUrl);
  final OAuthToken _token;
  final OAuthClient _oauth;
  final Preferences _prefs;

  final Map<String, CohortsResponse> _cachedCohorts = {};
  DateTime? _cacheAge;

  Future<CohortsResponse> getCohorts(Set<CohortsSelection> select, String hardware) async {
    if (_cachedCohorts[hardware] == null || _cacheAge == null ||
        DateTime.now().difference(_cacheAge!) >= _cacheLifetime) {
      _cacheAge = DateTime.now();
      final tokenCreationDate = _prefs.getOAuthTokenCreationDate();
      if (tokenCreationDate == null) {
        throw StateError("token creation date null when token exists");
      }

      final packageInfo = await PackageInfo.fromPlatform();
      final mobilePlatform = Platform.operatingSystem;
      final mobileVersion = Platform.operatingSystemVersion;
      final mobileHardware = (Platform.isAndroid ? (await DeviceInfoPlugin().androidInfo).model : (await DeviceInfoPlugin().iosInfo).model) ?? "unknown";
      final mobileAppVersion = "Cobble-" + packageInfo.version + "+" + packageInfo.buildNumber;

      final token = await _oauth.ensureNotStale(_token, tokenCreationDate);
      CohortsResponse cohorts = await client.getSerialized(
        CohortsResponse.fromJson,
        "cohort",
        params: {
          "select": select.map((e) => e.value).join(","),
          "hardware": hardware,
          "mobilePlatform": mobilePlatform,
          "mobileVersion": mobileVersion,
          "mobileHardware": mobileHardware,
          "pebbleAppVersion": mobileAppVersion,
        },
        token: token.accessToken,
      );
      _cachedCohorts[hardware] = cohorts;
      return cohorts;
    } else {
      return _cachedCohorts[hardware]!;
    }
  }
}

enum CohortsSelection {
  fw,
  pipelineApi,
  linkedServices,
  healthInsights;

  String get value {
    switch (this) {
      case CohortsSelection.fw:
        return "fw";
      case CohortsSelection.pipelineApi:
        return "pipeline-api";
      case CohortsSelection.linkedServices:
        return "linked-services";
      case CohortsSelection.healthInsights:
        return "health-insights";
    }
  }

  @override
  String toString() => value;

  static CohortsSelection fromString(String value) {
    switch (value) {
      case "fw":
        return CohortsSelection.fw;
      case "pipeline-api":
        return CohortsSelection.pipelineApi;
      case "linked-services":
        return CohortsSelection.linkedServices;
      case "health-insights":
        return CohortsSelection.healthInsights;
      default:
        throw Exception("Unknown cohorts selection: $value");
    }
  }
}
