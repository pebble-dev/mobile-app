import 'package:cobble/domain/api/cohorts/cohorts_firmware.dart';
import 'package:json_annotation/json_annotation.dart';

part 'cohorts_firmwares.g.dart';

@JsonSerializable(disallowUnrecognizedKeys: true)
class CohortsFirmwares {
  final CohortsFirmware? normal;
  final CohortsFirmware? recovery;

  CohortsFirmwares({required this.normal, required this.recovery});
  factory CohortsFirmwares.fromJson(Map<String, dynamic> json) => _$CohortsFirmwaresFromJson(json);
  Map<String, dynamic> toJson() => _$CohortsFirmwaresToJson(this);

  @override
  String toString() => toJson().toString();
}