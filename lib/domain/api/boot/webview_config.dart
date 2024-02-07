import 'package:json_annotation/json_annotation.dart';

part 'webview_config.g.dart';

@JsonSerializable()
class WebviewConfig {
  final String appstoreApplication;
  final String appstoreWatchapps;
  final String appstoreWatchfaces;
  final String manageAccount;

  WebviewConfig({required this.appstoreApplication, required this.appstoreWatchapps, required this.appstoreWatchfaces, required this.manageAccount});

  factory WebviewConfig.fromJson(Map<String, dynamic> json) => _$WebviewConfigFromJson(json);

  Map<String, dynamic> toJson() => _$WebviewConfigToJson(this);

  @override
  String toString() => toJson().toString();
}