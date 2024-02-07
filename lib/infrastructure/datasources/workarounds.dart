import 'package:cobble/infrastructure/datasources/preferences.dart';
import 'package:cobble/infrastructure/pigeons/pigeons.g.dart';
import 'package:flutter/material.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:rxdart/rxdart.dart';

class Workaround {
  final String name;
  final bool disabled;

  Workaround(this.name, this.disabled);
}

final neededWorkaroundsProvider = StreamProvider<List<Workaround>>((ref) {
  final preferencesData = ref.watch(preferencesProvider);
  if (!(preferencesData is AsyncData)) {
    return Stream<List<Workaround>>.empty();
  }

  final preferences = preferencesData.data!.value;

  fetchControls() async {
    final workaroundControl = WorkaroundsControl();
    final workarounds = await workaroundControl.getNeededWorkarounds();

    return (workarounds.value!.cast<String>());
  }

  return Stream.fromFuture(fetchControls()).switchMap((workarounds) {
    final streams = workarounds.map((workaround) {
      return preferences.preferencesUpdateStream.startWith(preferences).map((event) {
        return Workaround(workaround, event.isWorkaroundDisabled(workaround));
      });
    });

    return ZipStream(streams, (e) => e as List<Workaround>);
  });
});
