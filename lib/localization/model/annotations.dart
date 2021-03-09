import 'package:json_annotation/json_annotation.dart';

class Model extends JsonSerializable {
  const Model()
      : super(
          fieldRename: FieldRename.snake,
          createToJson: false,
          nullable: false,
          disallowUnrecognizedKeys: true,
        );
}

class Field extends JsonKey {
  const Field()
      : super(
          required: true,
          disallowNullValue: true,
        );
}
