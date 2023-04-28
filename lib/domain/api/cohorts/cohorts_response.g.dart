// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'cohorts_response.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CohortsResponse _$CohortsResponseFromJson(Map<String, dynamic> json) =>
    CohortsResponse(
      fw: CohortsFirmwares.fromJson(json['fw'] as Map<String, dynamic>),
    );

Map<String, dynamic> _$CohortsResponseToJson(CohortsResponse instance) =>
    <String, dynamic>{
      'fw': instance.fw,
    };
