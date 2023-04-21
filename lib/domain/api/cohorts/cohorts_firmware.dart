import 'package:json_annotation/json_annotation.dart';

part 'cohorts_firmware.g.dart';

DateTime _dateTimeFromJson(int json) => DateTime.fromMillisecondsSinceEpoch(json);
int _dateTimeToJson(DateTime dateTime) => dateTime.millisecondsSinceEpoch;

@JsonSerializable(disallowUnrecognizedKeys: true)
class CohortsFirmware {
  final String url;
  @JsonKey(name: 'sha-256')
  final String sha256;
  final String friendlyVersion;
  @JsonKey(fromJson: _dateTimeFromJson, toJson: _dateTimeToJson)
  final DateTime timestamp;
  final String notes;

  CohortsFirmware({required this.url, required this.sha256, required this.friendlyVersion, required this.timestamp, required this.notes});
  factory CohortsFirmware.fromJson(Map<String, dynamic> json) => _$CohortsFirmwareFromJson(json);
  Map<String, dynamic> toJson() => _$CohortsFirmwareToJson(this);
}