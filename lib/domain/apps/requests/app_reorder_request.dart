import 'package:uuid_type/uuid_type.dart';

class AppReorderRequest {
  final Uuid uuid;
  final int newPosition;

  AppReorderRequest(this.uuid, this.newPosition);
}
