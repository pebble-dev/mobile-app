import '../../infrastructure/pigeons/pigeons.g.dart';

extension PbwAppInfoExtension on PbwAppInfo {
  Map<String, int> appKeysCast() {
    return appKeys!.cast<String, int>();
  }

  List<String> capabilitiesCast() {
    return capabilities?.cast<String>() ?? [];
  }

  List<WatchResource> resourcesCast() {
    return resources?.map((e) => WatchResource.decode(e!)).toList() ?? [];
  }

  List<String> targetPlatformsCast() {
    return targetPlatforms!.cast<String>();
  }
}