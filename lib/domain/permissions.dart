import 'package:cobble/infrastructure/pigeons/pigeons.g.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

final permissionControlProvider = Provider((ref) => PermissionControl());
final permissionCheckProvider = Provider((ref) => PermissionCheck());
