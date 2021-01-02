import 'package:cobble/domain/timeline/timeline_attribute.dart';
import 'package:cobble/domain/timeline/timeline_serializer.dart';
import 'package:cobble/infrastructure/pigeons/pigeons.g.dart';

class TimelineActionResponse {
  final bool success;
  final List<TimelineAttribute> attributes;

  TimelineActionResponse(this.success, {this.attributes = const []});

  ActionResponsePigeon toPigeon() {
    final pigeon = ActionResponsePigeon();

    pigeon.success = success;
    pigeon.attributesJson = serializeAttributesToJson(attributes);

    return pigeon;
  }
}
