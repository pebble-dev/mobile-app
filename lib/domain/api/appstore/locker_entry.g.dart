// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'locker_entry.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

LockerEntry _$LockerEntryFromJson(Map<String, dynamic> json) => LockerEntry(
      id: json['id'] as String,
      uuid: json['uuid'] as String,
      userToken: json['user_token'] as String,
      title: json['title'] as String,
      type: json['type'] as String,
      category: json['category'] as String,
      version: json['version'] as String?,
      hearts: json['hearts'] as int,
      isConfigurable: json['is_configurable'] as bool,
      isTimelineEnabled: json['is_timeline_enabled'] as bool,
      links: LockerEntryLinks.fromJson(json['links'] as Map<String, dynamic>),
      developer: LockerEntryDeveloper.fromJson(
          json['developer'] as Map<String, dynamic>),
      hardwarePlatforms: (json['hardware_platforms'] as List<dynamic>)
          .map((e) => LockerEntryPlatform.fromJson(e as Map<String, dynamic>))
          .toList(),
      compatibility: LockerEntryCompatibility.fromJson(
          json['compatibility'] as Map<String, dynamic>),
      companions: (json['companions'] as Map<String, dynamic>).map(
        (k, e) => MapEntry(
            k,
            e == null
                ? null
                : LockerEntryCompanionApp.fromJson(e as Map<String, dynamic>)),
      ),
      pbw: json['pbw'] == null
          ? null
          : LockerEntryPBW.fromJson(json['pbw'] as Map<String, dynamic>),
    );

Map<String, dynamic> _$LockerEntryToJson(LockerEntry instance) =>
    <String, dynamic>{
      'id': instance.id,
      'uuid': instance.uuid,
      'user_token': instance.userToken,
      'title': instance.title,
      'type': instance.type,
      'category': instance.category,
      'version': instance.version,
      'hearts': instance.hearts,
      'is_configurable': instance.isConfigurable,
      'is_timeline_enabled': instance.isTimelineEnabled,
      'links': instance.links,
      'developer': instance.developer,
      'hardware_platforms': instance.hardwarePlatforms,
      'compatibility': instance.compatibility,
      'companions': instance.companions,
      'pbw': instance.pbw,
    };

LockerEntryLinks _$LockerEntryLinksFromJson(Map<String, dynamic> json) =>
    LockerEntryLinks(
      json['remove'] as String,
      json['href'] as String,
      json['share'] as String,
    );

Map<String, dynamic> _$LockerEntryLinksToJson(LockerEntryLinks instance) =>
    <String, dynamic>{
      'remove': instance.remove,
      'href': instance.href,
      'share': instance.share,
    };

LockerEntryDeveloper _$LockerEntryDeveloperFromJson(
        Map<String, dynamic> json) =>
    LockerEntryDeveloper(
      json['id'] as String,
      json['name'] as String,
      json['contact_email'] as String,
    );

Map<String, dynamic> _$LockerEntryDeveloperToJson(
        LockerEntryDeveloper instance) =>
    <String, dynamic>{
      'id': instance.id,
      'name': instance.name,
      'contact_email': instance.contactEmail,
    };

LockerEntryPlatform _$LockerEntryPlatformFromJson(Map<String, dynamic> json) =>
    LockerEntryPlatform(
      json['sdk_version'] as String,
      json['pebble_process_info_flags'] as int,
      json['name'] as String,
      json['description'] as String,
      LockerEntryPlatformImages.fromJson(
          json['images'] as Map<String, dynamic>),
    );

Map<String, dynamic> _$LockerEntryPlatformToJson(
        LockerEntryPlatform instance) =>
    <String, dynamic>{
      'sdk_version': instance.sdkVersion,
      'pebble_process_info_flags': instance.pebbleProcessInfoFlags,
      'name': instance.name,
      'description': instance.description,
      'images': instance.images,
    };

LockerEntryPlatformImages _$LockerEntryPlatformImagesFromJson(
        Map<String, dynamic> json) =>
    LockerEntryPlatformImages(
      json['icon'] as String,
      json['list'] as String,
      json['screenshot'] as String,
    );

Map<String, dynamic> _$LockerEntryPlatformImagesToJson(
        LockerEntryPlatformImages instance) =>
    <String, dynamic>{
      'icon': instance.icon,
      'list': instance.list,
      'screenshot': instance.screenshot,
    };

LockerEntryCompatibility _$LockerEntryCompatibilityFromJson(
        Map<String, dynamic> json) =>
    LockerEntryCompatibility(
      ios: LockerEntryCompatibilityPhonePlatformDetails.fromJson(
          json['ios'] as Map<String, dynamic>),
      android: LockerEntryCompatibilityPhonePlatformDetails.fromJson(
          json['android'] as Map<String, dynamic>),
      aplite: LockerEntryCompatibilityWatchPlatformDetails.fromJson(
          json['aplite'] as Map<String, dynamic>),
      basalt: LockerEntryCompatibilityWatchPlatformDetails.fromJson(
          json['basalt'] as Map<String, dynamic>),
      chalk: LockerEntryCompatibilityWatchPlatformDetails.fromJson(
          json['chalk'] as Map<String, dynamic>),
      diorite: LockerEntryCompatibilityWatchPlatformDetails.fromJson(
          json['diorite'] as Map<String, dynamic>),
      emery: LockerEntryCompatibilityWatchPlatformDetails.fromJson(
          json['emery'] as Map<String, dynamic>),
    );

Map<String, dynamic> _$LockerEntryCompatibilityToJson(
        LockerEntryCompatibility instance) =>
    <String, dynamic>{
      'ios': instance.ios,
      'android': instance.android,
      'aplite': instance.aplite,
      'basalt': instance.basalt,
      'chalk': instance.chalk,
      'diorite': instance.diorite,
      'emery': instance.emery,
    };

LockerEntryCompatibilityPhonePlatformDetails
    _$LockerEntryCompatibilityPhonePlatformDetailsFromJson(
            Map<String, dynamic> json) =>
        LockerEntryCompatibilityPhonePlatformDetails(
          json['supported'] as bool,
          json['min_js_version'] as int?,
        );

Map<String, dynamic> _$LockerEntryCompatibilityPhonePlatformDetailsToJson(
        LockerEntryCompatibilityPhonePlatformDetails instance) =>
    <String, dynamic>{
      'supported': instance.supported,
      'min_js_version': instance.minJsVersion,
    };

LockerEntryCompatibilityWatchPlatformDetails
    _$LockerEntryCompatibilityWatchPlatformDetailsFromJson(
            Map<String, dynamic> json) =>
        LockerEntryCompatibilityWatchPlatformDetails(
          json['supported'] as bool,
          LockerEntryFirmwareVersion.fromJson(
              json['firmware'] as Map<String, dynamic>),
        );

Map<String, dynamic> _$LockerEntryCompatibilityWatchPlatformDetailsToJson(
        LockerEntryCompatibilityWatchPlatformDetails instance) =>
    <String, dynamic>{
      'supported': instance.supported,
      'firmware': instance.firmware,
    };

LockerEntryFirmwareVersion _$LockerEntryFirmwareVersionFromJson(
        Map<String, dynamic> json) =>
    LockerEntryFirmwareVersion(
      major: json['major'] as int,
      minor: json['minor'] as int?,
      patch: json['patch'] as int?,
    );

Map<String, dynamic> _$LockerEntryFirmwareVersionToJson(
        LockerEntryFirmwareVersion instance) =>
    <String, dynamic>{
      'major': instance.major,
      'minor': instance.minor,
      'patch': instance.patch,
    };

LockerEntryCompanionApp _$LockerEntryCompanionAppFromJson(
        Map<String, dynamic> json) =>
    LockerEntryCompanionApp(
      json['id'] as int,
      json['icon'] as String,
      json['name'] as String,
      json['url'] as String,
      json['required'] as bool,
      json['pebblekit_version'] as String,
    );

Map<String, dynamic> _$LockerEntryCompanionAppToJson(
        LockerEntryCompanionApp instance) =>
    <String, dynamic>{
      'id': instance.id,
      'icon': instance.icon,
      'name': instance.name,
      'url': instance.url,
      'required': instance.required,
      'pebblekit_version': instance.pebblekitVersion,
    };

LockerEntryPBW _$LockerEntryPBWFromJson(Map<String, dynamic> json) =>
    LockerEntryPBW(
      json['file'] as String,
      json['icon_resource_id'] as int,
      json['release_id'] as String,
    );

Map<String, dynamic> _$LockerEntryPBWToJson(LockerEntryPBW instance) =>
    <String, dynamic>{
      'file': instance.file,
      'icon_resource_id': instance.iconResourceId,
      'release_id': instance.releaseId,
    };
