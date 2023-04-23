import 'dart:io';

import 'package:cobble/domain/api/no_token_exception.dart';
import 'package:cobble/domain/logging.dart';

import 'web_services/cohorts.dart';

class Firmwares {
  final CohortsService cohorts;

  Firmwares(this.cohorts);

  Future<bool> doesFirmwareNeedUpdate(String hardware, FirmwareType type, DateTime timestamp) async {
    final firmwares = (await cohorts.getCohorts({CohortsSelection.fw, CohortsSelection.linkedServices}, hardware)).fw;
    switch (type) {
      case FirmwareType.normal:
        return firmwares.normal?.timestamp.isAfter(timestamp) == true;
      case FirmwareType.recovery:
        return firmwares.recovery?.timestamp.isAfter(timestamp) == true;
      default:
        throw ArgumentError("Unknown firmware type: $type");
    }
  }

  Future<File> getFirmwareFor(String hardware, FirmwareType type) async {
    try {
      final firmwares = (await cohorts.getCohorts({CohortsSelection.fw, CohortsSelection.linkedServices}, hardware)).fw;
      final firmware = type == FirmwareType.normal ? firmwares.normal : firmwares.recovery;
      if (firmware != null) {
        final url = firmware.url;
        final HttpClient client = HttpClient();
        final request = await client.getUrl(Uri.parse(url));
        final response = await request.close();
        if (response.statusCode == HttpStatus.ok) {
          final directory = await Directory.systemTemp.createTemp();
          final file = File(directory.path+"/$hardware-${type == FirmwareType.normal ? "normal" : "recovery"}.bin");
          await response.pipe(file.openWrite());
          return file;
        }
      }
    } on NoTokenException {
      Log.w("No token when trying to get firmware, falling back to local firmware");
    }
    //TODO: local firmware fallback
    throw Exception("No firmware found");
  }
}

enum FirmwareType {
  normal,
  recovery,
}