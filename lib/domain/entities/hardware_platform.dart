import 'package:cobble/ui/common/icons/watch_icon.dart';

enum PebbleHardwarePlatform {
  UNKNOWN,
  PEBBLE_ONE_EV1,
  PEBBLE_ONE_EV2,
  PEBBLE_ONE_EV2_3,
  PEBBLE_ONE_EV2_4,
  PEBBLE_ONE_POINT_FIVE,
  PEBBLE_TWO_POINT_ZERO,
  PEBBLE_SNOWY_EVT2,
  PEBBLE_SNOWY_DVT,
  PEBBLE_BOBBY_SMILES,
  PEBBLE_ONE_BIGBOARD_2,
  PEBBLE_ONE_BIGBOARD,
  PEBBLE_SNOWY_BIGBOARD,
  PEBBLE_SNOWY_BIGBOARD_2,
  PEBBLE_SPALDING_EVT,
  PEBBLE_SPALDING_PVT,
  PEBBLE_SPALDING_BIGBOARD,
  PEBBLE_SILK_EVT,
  PEBBLE_SILK,
  PEBBLE_SILK_BIGBOARD,
  PEBBLE_SILK_BIGBOARD_2_PLUS,
  PEBBLE_ROBERT_EVT,
  PEBBLE_ROBERT_BIGBOARD,
  PEBBLE_ROBERT_BIGBOARD_2
}

PebbleHardwarePlatform pebbleHardwarePlatformFromNumber(int number) {
  switch (number) {
    case 1:
      return PebbleHardwarePlatform.PEBBLE_ONE_EV1;
    case 2:
      return PebbleHardwarePlatform.PEBBLE_ONE_EV2;
    case 3:
      return PebbleHardwarePlatform.PEBBLE_ONE_EV2_3;
    case 4:
      return PebbleHardwarePlatform.PEBBLE_ONE_EV2_4;
    case 5:
      return PebbleHardwarePlatform.PEBBLE_ONE_POINT_FIVE;
    case 6:
      return PebbleHardwarePlatform.PEBBLE_TWO_POINT_ZERO;
    case 7:
      return PebbleHardwarePlatform.PEBBLE_SNOWY_EVT2;
    case 8:
      return PebbleHardwarePlatform.PEBBLE_SNOWY_DVT;
    case 10:
      return PebbleHardwarePlatform.PEBBLE_BOBBY_SMILES;
    case 254:
      return PebbleHardwarePlatform.PEBBLE_ONE_BIGBOARD_2;
    case 255:
      return PebbleHardwarePlatform.PEBBLE_ONE_BIGBOARD;
    case 253:
      return PebbleHardwarePlatform.PEBBLE_SNOWY_BIGBOARD;
    case 252:
      return PebbleHardwarePlatform.PEBBLE_SNOWY_BIGBOARD_2;
    case 9:
      return PebbleHardwarePlatform.PEBBLE_SPALDING_EVT;
    case 11:
      return PebbleHardwarePlatform.PEBBLE_SPALDING_PVT;
    case 251:
      return PebbleHardwarePlatform.PEBBLE_SPALDING_BIGBOARD;
    case 12:
      return PebbleHardwarePlatform.PEBBLE_SILK_EVT;
    case 14:
      return PebbleHardwarePlatform.PEBBLE_SILK;
    case 250:
      return PebbleHardwarePlatform.PEBBLE_SILK_BIGBOARD;
    case 248:
      return PebbleHardwarePlatform.PEBBLE_SILK_BIGBOARD_2_PLUS;
    case 13:
      return PebbleHardwarePlatform.PEBBLE_ROBERT_EVT;
    case 249:
      return PebbleHardwarePlatform.PEBBLE_ROBERT_BIGBOARD;
    case 247:
      return PebbleHardwarePlatform.PEBBLE_ROBERT_BIGBOARD_2;
    default:
      return PebbleHardwarePlatform.UNKNOWN;
  }
}

extension PebbleHardwareData on PebbleHardwarePlatform {
  WatchType getWatchType() {
    switch (this) {
      case PebbleHardwarePlatform.UNKNOWN:
        return WatchType.BASALT;
      case PebbleHardwarePlatform.PEBBLE_ONE_EV1:
        return WatchType.APLITE;
      case PebbleHardwarePlatform.PEBBLE_ONE_EV2:
        return WatchType.APLITE;
      case PebbleHardwarePlatform.PEBBLE_ONE_EV2_3:
        return WatchType.APLITE;
      case PebbleHardwarePlatform.PEBBLE_ONE_EV2_4:
        return WatchType.APLITE;
      case PebbleHardwarePlatform.PEBBLE_ONE_POINT_FIVE:
        return WatchType.APLITE;
      case PebbleHardwarePlatform.PEBBLE_TWO_POINT_ZERO:
        return WatchType.APLITE;
      case PebbleHardwarePlatform.PEBBLE_SNOWY_EVT2:
        return WatchType.BASALT;
      case PebbleHardwarePlatform.PEBBLE_SNOWY_DVT:
        return WatchType.BASALT;
      case PebbleHardwarePlatform.PEBBLE_BOBBY_SMILES:
        return WatchType.BASALT;
      case PebbleHardwarePlatform.PEBBLE_ONE_BIGBOARD_2:
        return WatchType.APLITE;
      case PebbleHardwarePlatform.PEBBLE_ONE_BIGBOARD:
        return WatchType.APLITE;
      case PebbleHardwarePlatform.PEBBLE_SNOWY_BIGBOARD:
        return WatchType.BASALT;
      case PebbleHardwarePlatform.PEBBLE_SNOWY_BIGBOARD_2:
        return WatchType.BASALT;
      case PebbleHardwarePlatform.PEBBLE_SPALDING_EVT:
        return WatchType.CHALK;
      case PebbleHardwarePlatform.PEBBLE_SPALDING_PVT:
        return WatchType.CHALK;
      case PebbleHardwarePlatform.PEBBLE_SPALDING_BIGBOARD:
        return WatchType.CHALK;
      case PebbleHardwarePlatform.PEBBLE_SILK_EVT:
        return WatchType.DIORITE;
      case PebbleHardwarePlatform.PEBBLE_SILK:
        return WatchType.DIORITE;
      case PebbleHardwarePlatform.PEBBLE_SILK_BIGBOARD:
        return WatchType.DIORITE;
      case PebbleHardwarePlatform.PEBBLE_SILK_BIGBOARD_2_PLUS:
        return WatchType.DIORITE;
      case PebbleHardwarePlatform.PEBBLE_ROBERT_EVT:
        return WatchType.EMERY;
      case PebbleHardwarePlatform.PEBBLE_ROBERT_BIGBOARD:
        return WatchType.EMERY;
      case PebbleHardwarePlatform.PEBBLE_ROBERT_BIGBOARD_2:
        return WatchType.EMERY;
      default:
        throw Exception("Unknown hardware platform $this");
    }
  }
}

enum WatchType { APLITE, BASALT, CHALK, DIORITE, EMERY }

PebbleWatchModel watchModelFromNumber(int number) {
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
