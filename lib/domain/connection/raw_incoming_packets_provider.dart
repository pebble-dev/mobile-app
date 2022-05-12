import 'dart:async';
import 'dart:typed_data';

import 'package:cobble/domain/entities/pebble_device.dart';
import 'package:cobble/infrastructure/pigeons/pigeons.g.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

class RawIncomingPacketsProvider implements RawIncomingPacketsCallbacks {
  final _rawIncomingPacketsControl = RawIncomingPacketsControl();

  late StreamController<Uint8List> _streamController;

  RawIncomingPacketsProvider() {
    _streamController = StreamController.broadcast(
      onListen: () {
        RawIncomingPacketsCallbacks.setup(this);
        _rawIncomingPacketsControl.observeIncomingPackets();
      },
      onCancel: () {
        RawIncomingPacketsCallbacks.setup(null);
        _rawIncomingPacketsControl.cancelObservingIncomingPackets();
      },
    );
  }

  @override
  void onPacketReceived(ListWrapper arg) {
    final bytes = Uint8List(arg.value!.length);
    for (int i = 0; i < arg.value!.length; i++) {
      bytes[i] = arg.value![i] as int;
    }

    _streamController.add(bytes);
  }

  Stream<Uint8List> get stream => _streamController.stream;
}

final Provider<Stream<Uint8List>> rawPacketStreamProvider = Provider((ref) => RawIncomingPacketsProvider().stream);

