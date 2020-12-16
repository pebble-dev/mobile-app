import 'package:cobble/infrastructure/pigeons/pigeons.dart';
import 'package:hooks_riverpod/all.dart';

final permissionControlProvider = Provider((ref) => PermissionControl());
final permissionCheckProvider = Provider((ref) => PermissionCheck());
