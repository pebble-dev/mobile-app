import 'package:json_annotation/json_annotation.dart';

part 'user.g.dart';

@JsonSerializable(fieldRename: FieldRename.snake)
class User {
  final Map<String, dynamic>? bootOverrides;
  final bool hasTimeline;
  final bool isSubscribed;
  final bool isWizard;
  final String name;
  final List<String> scopes;
  final int timelineTtl;
  final int uid;

  User({required this.hasTimeline, required this.isSubscribed,
    required this.isWizard, required this.name,
    required this.scopes, required this.timelineTtl,
    required this.uid, required this.bootOverrides});
  factory User.fromJson(Map<String, dynamic> json) => _$UserFromJson(json);
  Map<String, dynamic> toJson() => _$UserToJson(this);

  @override
  String toString() => toJson().toString();
}