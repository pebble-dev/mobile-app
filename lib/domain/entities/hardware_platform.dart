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

enum WatchModel {
  Unknown,
  TintinBlack,
  TintinWhite,
  TintinRed,
  TintinOrange,
  TintinGrey,
  BiancaSilver,
  BiancaBlack,
  TintinBlue,
  TintinGreen,
  TintinPink,
  SnowyWhite,
  SnowyBlack,
  SnowyRed,
  BobbySilver,
  BobbyBlack,
  BobbyGold,
}

WatchModel watchModelFromNumber(int number) {
  switch (number) {
    case 1:
      return WatchModel.TintinBlack;
    case 2:
      return WatchModel.TintinWhite;
    case 3:
      return WatchModel.TintinRed;
    case 4:
      return WatchModel.TintinOrange;
    case 5:
      return WatchModel.TintinGrey;
    case 6:
      return WatchModel.BiancaSilver;
    case 7:
      return WatchModel.BiancaBlack;
    case 8:
      return WatchModel.TintinBlue;
    case 9:
      return WatchModel.TintinGreen;
    case 10:
      return WatchModel.TintinPink;
    case 11:
      return WatchModel.SnowyWhite;
    case 12:
      return WatchModel.SnowyBlack;
    case 13:
      return WatchModel.SnowyRed;
    case 14:
      return WatchModel.BobbySilver;
    case 15:
      return WatchModel.BobbyBlack;
    case 16:
      return WatchModel.BobbyGold;
    default:
      return WatchModel.Unknown;
  }
}
