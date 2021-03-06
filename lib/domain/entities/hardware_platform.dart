import 'package:cobble/ui/common/icons/watch_icon.dart';

enum PebbleHardwarePlatform {
  unknown,
  pebbleOneEv1,
  pebbleOneEv2,
  pebbleOneEv2_3,
  pebbleOneEv2_4,
  pebbleOnePointFive,
  pebbleOnePointZero,
  pebbleSnowyEvt2,
  pebbleSnowyDvt,
  pebbleBobbySmiles,
  pebbleOneBigboard2,
  pebbleOneBigboard,
  pebbleSnowyBigboard,
  pebbleSnowyBigboard2,
  pebbleSpaldingEvt,
  pebbleSpaldingPvt,
  pebbleSpaldingBigboard,
  pebbleSilkEvt,
  pebbleSilk,
  pebbleSilkBigboard,
  pebbleSilkBigboard2Plus,
  pebbleRobertEvt,
  pebbleRobertBigboard,
  pebbleRobertBigboard2
}

PebbleHardwarePlatform pebbleHardwarePlatformFromNumber(int? number) {
  switch (number) {
    case 1:
      return PebbleHardwarePlatform.pebbleOneEv1;
    case 2:
      return PebbleHardwarePlatform.pebbleOneEv2;
    case 3:
      return PebbleHardwarePlatform.pebbleOneEv2_3;
    case 4:
      return PebbleHardwarePlatform.pebbleOneEv2_4;
    case 5:
      return PebbleHardwarePlatform.pebbleOnePointFive;
    case 6:
      return PebbleHardwarePlatform.pebbleOnePointZero;
    case 7:
      return PebbleHardwarePlatform.pebbleSnowyEvt2;
    case 8:
      return PebbleHardwarePlatform.pebbleSnowyDvt;
    case 10:
      return PebbleHardwarePlatform.pebbleBobbySmiles;
    case 254:
      return PebbleHardwarePlatform.pebbleOneBigboard2;
    case 255:
      return PebbleHardwarePlatform.pebbleOneBigboard;
    case 253:
      return PebbleHardwarePlatform.pebbleSnowyBigboard;
    case 252:
      return PebbleHardwarePlatform.pebbleSnowyBigboard2;
    case 9:
      return PebbleHardwarePlatform.pebbleSpaldingEvt;
    case 11:
      return PebbleHardwarePlatform.pebbleSpaldingPvt;
    case 251:
      return PebbleHardwarePlatform.pebbleSpaldingBigboard;
    case 12:
      return PebbleHardwarePlatform.pebbleSilkEvt;
    case 14:
      return PebbleHardwarePlatform.pebbleSilk;
    case 250:
      return PebbleHardwarePlatform.pebbleSilkBigboard;
    case 248:
      return PebbleHardwarePlatform.pebbleSilkBigboard2Plus;
    case 13:
      return PebbleHardwarePlatform.pebbleRobertEvt;
    case 249:
      return PebbleHardwarePlatform.pebbleRobertBigboard;
    case 247:
      return PebbleHardwarePlatform.pebbleRobertBigboard2;
    default:
      return PebbleHardwarePlatform.unknown;
  }
}

extension PebbleHardwareData on PebbleHardwarePlatform {
  WatchType getWatchType() {
    switch (this) {
      case PebbleHardwarePlatform.unknown:
        return WatchType.basalt;
      case PebbleHardwarePlatform.pebbleOneEv1:
        return WatchType.aplite;
      case PebbleHardwarePlatform.pebbleOneEv2:
        return WatchType.aplite;
      case PebbleHardwarePlatform.pebbleOneEv2_3:
        return WatchType.aplite;
      case PebbleHardwarePlatform.pebbleOneEv2_4:
        return WatchType.aplite;
      case PebbleHardwarePlatform.pebbleOnePointFive:
        return WatchType.aplite;
      case PebbleHardwarePlatform.pebbleOnePointZero:
        return WatchType.aplite;
      case PebbleHardwarePlatform.pebbleSnowyEvt2:
        return WatchType.basalt;
      case PebbleHardwarePlatform.pebbleSnowyDvt:
        return WatchType.basalt;
      case PebbleHardwarePlatform.pebbleBobbySmiles:
        return WatchType.basalt;
      case PebbleHardwarePlatform.pebbleOneBigboard2:
        return WatchType.aplite;
      case PebbleHardwarePlatform.pebbleOneBigboard:
        return WatchType.aplite;
      case PebbleHardwarePlatform.pebbleSnowyBigboard:
        return WatchType.basalt;
      case PebbleHardwarePlatform.pebbleSnowyBigboard2:
        return WatchType.basalt;
      case PebbleHardwarePlatform.pebbleSpaldingEvt:
        return WatchType.chalk;
      case PebbleHardwarePlatform.pebbleSpaldingPvt:
        return WatchType.chalk;
      case PebbleHardwarePlatform.pebbleSpaldingBigboard:
        return WatchType.chalk;
      case PebbleHardwarePlatform.pebbleSilkEvt:
        return WatchType.diorite;
      case PebbleHardwarePlatform.pebbleSilk:
        return WatchType.diorite;
      case PebbleHardwarePlatform.pebbleSilkBigboard:
        return WatchType.diorite;
      case PebbleHardwarePlatform.pebbleSilkBigboard2Plus:
        return WatchType.diorite;
      case PebbleHardwarePlatform.pebbleRobertEvt:
        return WatchType.emery;
      case PebbleHardwarePlatform.pebbleRobertBigboard:
        return WatchType.emery;
      case PebbleHardwarePlatform.pebbleRobertBigboard2:
        return WatchType.emery;
      default:
        throw Exception("Unknown hardware platform $this");
    }
  }
}

enum WatchType { aplite, basalt, chalk, diorite, emery }

PebbleWatchModel watchModelFromNumber(int? number) {
  switch (number) {
    case 1:
      return PebbleWatchModel.classic_black;
    case 2:
      return PebbleWatchModel.classic_white;
    case 3:
      return PebbleWatchModel.classic_red;
    case 4:
      return PebbleWatchModel.classic_orange;
    case 5:
      return PebbleWatchModel.classic_red;
    case 6:
      return PebbleWatchModel.classic_steel_silver;
    case 7:
      return PebbleWatchModel.classic_steel_gunmetal;
    case 8:
      return PebbleWatchModel.classic_fly_blue;
    case 9:
      return PebbleWatchModel.classic_fresh_green;
    case 10:
      return PebbleWatchModel.classic_hot_pink;
    case 11:
      return PebbleWatchModel.time_white;
    case 12:
      return PebbleWatchModel.time_black;
    case 13:
      return PebbleWatchModel.time_red;
    case 14:
      return PebbleWatchModel.time_steel_silver;
    case 15:
      return PebbleWatchModel.time_steel_gunmetal;
    case 16:
      return PebbleWatchModel.time_steel_gold;
    case 17:
      return PebbleWatchModel.time_round_silver_14;
    case 18:
      return PebbleWatchModel.time_round_black_14;
    case 19:
      return PebbleWatchModel.time_round_silver_20;
    case 20:
      return PebbleWatchModel.time_round_black_20;
    case 21:
      return PebbleWatchModel.time_round_rose_gold_14;
    case 22:
      return PebbleWatchModel.time_round_rainbow_silver_14;
    case 23:
      return PebbleWatchModel.time_round_rainbow_black_20;
    case 24:
      return PebbleWatchModel.pebble_2_se_black;
    case 25:
      return PebbleWatchModel.pebble_2_hr_black;
    case 26:
      return PebbleWatchModel.pebble_2_se_white;
    case 27:
      return PebbleWatchModel.pebble_2_hr_lime;
    case 28:
      return PebbleWatchModel.pebble_2_hr_flame;
    case 29:
      return PebbleWatchModel.pebble_2_hr_white;
    case 30:
      return PebbleWatchModel.pebble_2_hr_aqua;
    case 31:
      return PebbleWatchModel.time_2_gunmetal;
    case 32:
      return PebbleWatchModel.time_2_silver;
    case 33:
      return PebbleWatchModel.time_2_gold;
    case 34:
      return PebbleWatchModel.time_round_black_silver_polish_20;
    case 35:
      return PebbleWatchModel.time_round_black_gold_polish_20;
    default:
      return PebbleWatchModel.rebble_logo;
  }
}
