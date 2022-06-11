import 'package:json_annotation/json_annotation.dart';

part 'base_url_entry.g.dart';

@JsonSerializable()
class BaseURLEntry {
  final String base;

  BaseURLEntry(this.base);
  factory BaseURLEntry.fromJson(Map<String, dynamic> json) => _$BaseURLEntryFromJson(json);
}