// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'RpcRequest.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RpcRequest _$RpcRequestFromJson(Map<String, dynamic> json) => RpcRequest(
      (json['requestId'] as num).toInt(),
      json['input'] as Map<String, dynamic>,
      json['type'] as String,
    );

Map<String, dynamic> _$RpcRequestToJson(RpcRequest instance) =>
    <String, dynamic>{
      'requestId': instance.requestId,
      'type': instance.type,
      'input': instance.input,
    };
