import '../../infrastructure/pigeons/pigeons.g.dart';

class PbwAppInfoParsed {
  bool? isValid;
  String? uuid;
  String? shortName;
  String? longName;
  String? companyName;
  int? versionCode;
  String? versionLabel;
  Map<String, int>? appKeys;
  List<String>? capabilities;
  List<WatchResource>? resources;
  String? sdkVersion;
  List<String>? targetPlatforms;
  WatchappInfo? watchapp;

  PbwAppInfoParsed(PbwAppInfo pigeon) {
    isValid = pigeon.isValid;
    uuid = pigeon.uuid;
    shortName = pigeon.shortName;
    longName = pigeon.longName;
    companyName = pigeon.companyName;
    versionCode = pigeon.versionCode;
    versionLabel = pigeon.versionLabel;
    appKeys = pigeon.appKeys.cast<String, int>();
    capabilities = pigeon.capabilities.cast<String>();
    resources = pigeon.resources.map((e) => WatchResource.decode(e)).toList();
    sdkVersion = pigeon.sdkVersion;
    targetPlatforms = pigeon.targetPlatforms.cast<String>();
    watchapp = pigeon.watchapp;
  }

  @override
  String toString() {
    return 'PbwAppInfoParsed{isValid: $isValid, uuid: $uuid, shortName: $shortName, longName: $longName, companyName: $companyName, versionCode: $versionCode, versionLabel: $versionLabel, appKeys: $appKeys, capabilities: $capabilities, resources: $resources, sdkVersion: $sdkVersion, targetPlatforms: $targetPlatforms, watchapp: $watchapp}';
  }
}
