import 'package:json_annotation/json_annotation.dart';
import 'package:uuid_type/uuid_type.dart';

// We use json_serializable package to automatically generate entity <> map
// mapping for SQL insertion. Custom converters here help with some custom
// classes

class NumberDateTimeConverter implements JsonConverter<DateTime?, int?> {
  const NumberDateTimeConverter();

  @override
  DateTime? fromJson(int? json) {
    if (json == null) {
      return null;
    }
    return DateTime.fromMillisecondsSinceEpoch(json, isUtc: true);
  }

  @override
  int? toJson(DateTime? object) {
    if (object == null) {
      return null;
    }

    return object.millisecondsSinceEpoch;
  }
}

class UuidConverter implements JsonConverter<Uuid?, String?> {
  const UuidConverter();

  @override
  Uuid? fromJson(String? json) {
    if (json == null) {
      return null;
    }
    return Uuid.parse(json);
  }

  @override
  String? toJson(Uuid? object) {
    if (object == null) {
      return null;
    }
    return object.toString();
  }
}

class NonNullUuidConverter implements JsonConverter<Uuid, String> {
  const NonNullUuidConverter();

  @override
  Uuid fromJson(String json) {
    return Uuid.parse(json);
  }

  @override
  String toJson(Uuid object) {
    return object.toString();
  }
}

class BooleanNumberConverter implements JsonConverter<bool, int> {
  const BooleanNumberConverter();

  @override
  bool fromJson(int json) {
    return json == 1;
  }

  @override
  int toJson(bool object) {
    return object ? 1 : 0;
  }
}

class CommaSeparatedListConverter
    implements JsonConverter<List<String>, String> {
  const CommaSeparatedListConverter();

  @override
  List<String> fromJson(String json) {
    return json.split(",");
  }

  @override
  String toJson(List<String> object) {
    return object.join(",");
  }
}
