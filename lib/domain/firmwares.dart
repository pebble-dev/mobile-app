

import 'package:cobble/infrastructure/datasources/firmwares.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

import 'api/cohorts/cohorts.dart';

final firmwaresProvider = FutureProvider((ref) async {
  final cohorts = await ref.watch(cohortsServiceProvider.future);
  return Firmwares(cohorts);
});