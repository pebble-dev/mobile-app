import 'dart:convert';

import 'package:cobble/domain/timeline/timeline_action.dart';
import 'package:cobble/domain/timeline/timeline_attribute.dart';

String serializeAttributesToJson(List<TimelineAttribute> attributes) {
  return jsonEncode(attributes.map((e) => e.toJson()).toList());
}

String serializeActionsToJson(List<TimelineAction> actions) {
  return jsonEncode(actions.map((e) => e.toJson()).toList());
}
