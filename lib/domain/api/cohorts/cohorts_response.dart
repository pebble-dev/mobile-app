import 'package:cobble/domain/api/cohorts/cohorts_firmwares.dart';
import 'package:json_annotation/json_annotation.dart';

part 'cohorts_response.g.dart';

@JsonSerializable(fieldRename: FieldRename.snake, disallowUnrecognizedKeys: false)
class CohortsResponse {
  final CohortsFirmwares fw;

  CohortsResponse({required this.fw});
  factory CohortsResponse.fromJson(Map<String, dynamic> json) => _$CohortsResponseFromJson(json);
  Map<String, dynamic> toJson() => _$CohortsResponseToJson(this);

  @override
  String toString() => toJson().toString();
}