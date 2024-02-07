import 'package:json_annotation/json_annotation.dart';

part 'locker_entry.g.dart';

@JsonSerializable(fieldRename: FieldRename.snake)
class LockerEntry {
  final String id;
  final String uuid;
  final String userToken;
  final String title;
  final String type;
  final String category;
  final String? version;
  final int hearts;
  final bool isConfigurable;
  final bool isTimelineEnabled;
  final LockerEntryLinks links;
  final LockerEntryDeveloper developer;
  final List<LockerEntryPlatform> hardwarePlatforms;
  final LockerEntryCompatibility compatibility;
  final Map<String, LockerEntryCompanionApp?> companions;
  final LockerEntryPBW? pbw;

  LockerEntry({
    required this.id,
    required this.uuid,
    required this.userToken,
    required this.title,
    required this.type,
    required this.category,
    this.version,
    required this.hearts,
    required this.isConfigurable,
    required this.isTimelineEnabled,
    required this.links,
    required this.developer,
    required this.hardwarePlatforms,
    required this.compatibility,
    required this.companions,
    this.pbw,
  });

  factory LockerEntry.fromJson(Map<String, dynamic> json) => _$LockerEntryFromJson(json);
  Map<String, dynamic> toJson() => _$LockerEntryToJson(this);

  @override
  String toString() => toJson().toString();
}

@JsonSerializable(fieldRename: FieldRename.snake)
class LockerEntryLinks {
  final String remove;
  final String href;
  final String share;

  LockerEntryLinks(this.remove, this.href, this.share);
  
  factory LockerEntryLinks.fromJson(Map<String, dynamic> json) => _$LockerEntryLinksFromJson(json);
  Map<String, dynamic> toJson() => _$LockerEntryLinksToJson(this);

  @override
  String toString() => toJson().toString();
}

@JsonSerializable(fieldRename: FieldRename.snake)
class LockerEntryDeveloper {
  final String id;
  final String name;
  final String contactEmail;

  LockerEntryDeveloper(this.id, this.name, this.contactEmail);

  factory LockerEntryDeveloper.fromJson(Map<String, dynamic> json) => _$LockerEntryDeveloperFromJson(json);
  Map<String, dynamic> toJson() => _$LockerEntryDeveloperToJson(this);

  @override
  String toString() => toJson().toString();
}

@JsonSerializable(fieldRename: FieldRename.snake)
class LockerEntryPlatform {
  final String sdkVersion;
  final int pebbleProcessInfoFlags;
  final String name;
  final String description;
  final LockerEntryPlatformImages images;

  LockerEntryPlatform(this.sdkVersion, this.pebbleProcessInfoFlags, this.name,
      this.description, this.images);

  factory LockerEntryPlatform.fromJson(Map<String, dynamic> json) => _$LockerEntryPlatformFromJson(json);
  Map<String, dynamic> toJson() => _$LockerEntryPlatformToJson(this);

  @override
  String toString() => toJson().toString();
}

@JsonSerializable(fieldRename: FieldRename.snake)
class LockerEntryPlatformImages {
  final String icon;
  final String list;
  final String screenshot;

  LockerEntryPlatformImages(this.icon, this.list, this.screenshot);

  factory LockerEntryPlatformImages.fromJson(Map<String, dynamic> json) => _$LockerEntryPlatformImagesFromJson(json);
  Map<String, dynamic> toJson() => _$LockerEntryPlatformImagesToJson(this);

  @override
  String toString() => toJson().toString();
}

@JsonSerializable(fieldRename: FieldRename.snake)
class LockerEntryCompatibility {
  final LockerEntryCompatibilityPhonePlatformDetails ios;
  final LockerEntryCompatibilityPhonePlatformDetails android;
  final LockerEntryCompatibilityWatchPlatformDetails aplite;
  final LockerEntryCompatibilityWatchPlatformDetails basalt;
  final LockerEntryCompatibilityWatchPlatformDetails chalk;
  final LockerEntryCompatibilityWatchPlatformDetails diorite;
  final LockerEntryCompatibilityWatchPlatformDetails emery;

  LockerEntryCompatibility({
    required this.ios,
    required this.android,
    required this.aplite,
    required this.basalt,
    required this.chalk,
    required this.diorite,
    required this.emery,
  });

  factory LockerEntryCompatibility.fromJson(Map<String, dynamic> json) => _$LockerEntryCompatibilityFromJson(json);
  Map<String, dynamic> toJson() => _$LockerEntryCompatibilityToJson(this);

  @override
  String toString() => toJson().toString();
}

@JsonSerializable(fieldRename: FieldRename.snake)
class LockerEntryCompatibilityPhonePlatformDetails {
  final bool supported;
  final int? minJsVersion;

  LockerEntryCompatibilityPhonePlatformDetails(
      this.supported, this.minJsVersion);

  factory LockerEntryCompatibilityPhonePlatformDetails.fromJson(Map<String, dynamic> json) => _$LockerEntryCompatibilityPhonePlatformDetailsFromJson(json);
  Map<String, dynamic> toJson() => _$LockerEntryCompatibilityPhonePlatformDetailsToJson(this);

  @override
  String toString() => toJson().toString();
}

@JsonSerializable(fieldRename: FieldRename.snake)
class LockerEntryCompatibilityWatchPlatformDetails {
  final bool supported;
  final LockerEntryFirmwareVersion firmware;

  LockerEntryCompatibilityWatchPlatformDetails(this.supported, this.firmware);

  factory LockerEntryCompatibilityWatchPlatformDetails.fromJson(Map<String, dynamic> json) => _$LockerEntryCompatibilityWatchPlatformDetailsFromJson(json);
  Map<String, dynamic> toJson() => _$LockerEntryCompatibilityWatchPlatformDetailsToJson(this);

  @override
  String toString() => toJson().toString();
}

@JsonSerializable(fieldRename: FieldRename.snake)
class LockerEntryFirmwareVersion {
  final int major;
  final int? minor;
  final int? patch;

  LockerEntryFirmwareVersion({required this.major, this.minor, this.patch});

  factory LockerEntryFirmwareVersion.fromJson(Map<String, dynamic> json) => _$LockerEntryFirmwareVersionFromJson(json);
  Map<String, dynamic> toJson() => _$LockerEntryFirmwareVersionToJson(this);

  @override
  String toString() => toJson().toString();
}

@JsonSerializable(fieldRename: FieldRename.snake)
class LockerEntryCompanionApp {
  final int id;
  final String icon;
  final String name;
  final String url;
  final bool required;
  final String pebblekitVersion;

  LockerEntryCompanionApp(this.id, this.icon, this.name, this.url,
      this.required, this.pebblekitVersion);

  factory LockerEntryCompanionApp.fromJson(Map<String, dynamic> json) => _$LockerEntryCompanionAppFromJson(json);
  Map<String, dynamic> toJson() => _$LockerEntryCompanionAppToJson(this);

  @override
  String toString() => toJson().toString();
}

@JsonSerializable(fieldRename: FieldRename.snake)
class LockerEntryPBW {
  final String file;
  final int iconResourceId;
  final String releaseId;

  LockerEntryPBW(this.file, this.iconResourceId, this.releaseId);
  
  factory LockerEntryPBW.fromJson(Map<String, dynamic> json) => _$LockerEntryPBWFromJson(json);
  Map<String, dynamic> toJson() => _$LockerEntryPBWToJson(this);

  @override
  String toString() => toJson().toString();
}
