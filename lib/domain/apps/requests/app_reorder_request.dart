import 'package:uuid_type/uuid_type.dart';

class AppReorderRequest {
  final Uuid uuid;
  final int newPosition;

  AppReorderRequest(this.uuid, this.newPosition);

  Map<String, dynamic> toMap() {
    return {
      'type': 'AppReorderRequest',
      'uuid': uuid.toString(),
      'newPosition': newPosition,
    };
  }

  factory AppReorderRequest.fromMap(Map<String, dynamic> map) {
    return AppReorderRequest(
      Uuid.parse(map['uuid'] as String),
      map['newPosition'] as int,
    );
  }
}
