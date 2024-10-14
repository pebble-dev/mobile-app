import 'package:cobble/infrastructure/backgroundcomm/RpcRequest.dart';
import 'package:json_annotation/json_annotation.dart';
import 'package:uuid_type/uuid_type.dart';

part 'app_reorder_request.g.dart';

String _uuidToString(Uuid uuid) => uuid.toString();

@JsonSerializable()
class AppReorderRequest extends SerializableRpcRequest {
  @JsonKey(fromJson: Uuid.parse, toJson: _uuidToString)
  final Uuid uuid;
  final int newPosition;

  AppReorderRequest(this.uuid, this.newPosition);

  @override
  String toString() {
    return 'AppReorderRequest{uuid: $uuid, newPosition: $newPosition}';
  }

  factory AppReorderRequest.fromJson(Map<String, dynamic> json) => _$AppReorderRequestFromJson(json);
  @override
  Map<String, dynamic> toJson() => _$AppReorderRequestToJson(this);
}
