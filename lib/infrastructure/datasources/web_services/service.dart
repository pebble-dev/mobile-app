import 'package:cobble/infrastructure/datasources/web_services/rest_client.dart';
import 'package:flutter/foundation.dart';

abstract class Service {
  @protected
  late final RESTClient client;
  Service(String baseUrl) {
    client = RESTClient(Uri.parse(baseUrl));
  }
}