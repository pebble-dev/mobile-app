import 'package:cobble/infrastructure/pigeons/pigeons.g.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

final permissionControlProvider = Provider<PermissionControl>((ref) => PermissionControl());
final permissionCheckProvider = Provider<PermissionCheck>((ref) => PermissionCheck());
