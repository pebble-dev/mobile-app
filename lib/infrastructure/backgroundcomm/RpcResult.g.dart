// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'RpcResult.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RpcResult _$RpcResultFromJson(Map<String, dynamic> json) => RpcResult(
      json['id'] as int,
      json['successResult'] as String?,
      json['errorResult'] as String?,
    );

Map<String, dynamic> _$RpcResultToJson(RpcResult instance) => <String, dynamic>{
      'id': instance.id,
      'successResult': instance.successResult,
      'errorResult': instance.errorResult,
    };
