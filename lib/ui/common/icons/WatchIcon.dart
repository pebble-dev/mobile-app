import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:fossil/ui/common/icons/fonts/PebbleWatchIcons.dart';

class _WatchLayer {
  _WatchLayer(this.layer, this.color);
  final IconData layer;
  final Color color;
}

class _WatchIcon extends StatelessWidget {
  _WatchIcon(this.layers, {this.size = 48.0});
  final List<_WatchLayer> layers;
  final double size;
  @override
  Widget build(BuildContext context) {
    return Stack(
      children: layers
          .map((e) => Icon(
                e.layer,
                color: e.color,
                size: size,
              ))
          .toList(),
    );
  }
}

class PebbleWatchIcon extends StatelessWidget {
  // First layer = bottom layer, last layer = top layer (draw order)
  static _WatchIcon classic(Color bodyColor, {double size = 48.0}) =>
      _WatchIcon(
        [
          _WatchLayer(PebbleWatchIcons.classic_body_fill, bodyColor),
          _WatchLayer(PebbleWatchIcons.classic_body_stroke, Colors.black),
          _WatchLayer(PebbleWatchIcons.classic_screen_fill, Colors.white),
          _WatchLayer(PebbleWatchIcons.classic_screen_stroke, Colors.black),
        ],
        size: size,
      );
  static _WatchIcon classicSteel(Color bodyColor, {double size = 48.0}) =>
      _WatchIcon(
        [
          _WatchLayer(PebbleWatchIcons.classic_steel_body_fill, bodyColor),
          _WatchLayer(PebbleWatchIcons.classic_steel_body_stroke, Colors.black),
          _WatchLayer(PebbleWatchIcons.classic_steel_screen_fill, Colors.white),
          _WatchLayer(
              PebbleWatchIcons.classic_steel_screen_stroke, Colors.black),
        ],
        size: size,
      );
  static _WatchIcon time(Color bodyColor, {double size = 48.0}) => _WatchIcon(
        [
          _WatchLayer(PebbleWatchIcons.time_body_fill, bodyColor),
          _WatchLayer(PebbleWatchIcons.time_body_stroke, Colors.black),
          _WatchLayer(PebbleWatchIcons.time_screen_fill, Colors.white),
          _WatchLayer(PebbleWatchIcons.time_screen_stroke, Colors.black),
        ],
        size: size,
      );
  static _WatchIcon timeSteel(Color bodyColor, {double size = 48.0}) =>
      _WatchIcon([
        _WatchLayer(PebbleWatchIcons.time_steel_body_fill, bodyColor),
        _WatchLayer(PebbleWatchIcons.time_steel_body_stroke, Colors.black),
        _WatchLayer(PebbleWatchIcons.time_steel_screen_fill, Colors.white),
        _WatchLayer(PebbleWatchIcons.time_steel_screen_stroke, Colors.black),
      ], size: size);
  static _WatchIcon timeRound(Color bodyColor,
          {Color bodyStrokeColor = Colors.black, double size = 48.0}) =>
      _WatchIcon(
        [
          _WatchLayer(PebbleWatchIcons.time_round_body_fill, bodyColor),
          _WatchLayer(PebbleWatchIcons.time_round_body_stroke, bodyStrokeColor),
          _WatchLayer(PebbleWatchIcons.time_round_screen_fill, Colors.white),
          _WatchLayer(PebbleWatchIcons.time_round_screen_stroke, Colors.black),
        ],
        size: size,
      );
  static _WatchIcon pebbleTwo(Color bodyColor,
          {Color buttonsColor = Colors.black,
          Color bezelColor = Colors.black,
          double size = 48.0}) =>
      _WatchIcon(
        [
          _WatchLayer(PebbleWatchIcons.pebble_2_buttons_stroke, buttonsColor),
          _WatchLayer(PebbleWatchIcons.pebble_2_body_fill, bodyColor),
          _WatchLayer(PebbleWatchIcons.pebble_2_body_stroke, Colors.black),
          _WatchLayer(PebbleWatchIcons.pebble_2_screen_fill, Colors.white),
          _WatchLayer(PebbleWatchIcons.pebble_2_screen_stroke, bezelColor)
        ],
        size: size,
      );
  static _WatchIcon rebbleLogo({size = 48.0}) => _WatchIcon([
        _WatchLayer(PebbleWatchIcons.rebble_logo_body_fill, Colors.white),
        _WatchLayer(PebbleWatchIcons.rebble_logo_body_stroke, Colors.black),
        _WatchLayer(PebbleWatchIcons.rebble_logo_hands, Color(0xFFFA5521)),
      ], size: size);

  PebbleWatchIcon(this.model, {this.size = 48.0});

  final PebbleWatchModel model;
  final double size;

  @override
  Widget build(BuildContext context) {
    // i'm so sorry for this

    switch (model) {
      case PebbleWatchModel.classic_black:
        return PebbleWatchIcon.classic(PebbleWatchColor.Black, size: size);
      case PebbleWatchModel.classic_white:
        return PebbleWatchIcon.classic(PebbleWatchColor.White, size: size);
      case PebbleWatchModel.classic_red:
        return PebbleWatchIcon.classic(PebbleWatchColor.Red, size: size);
      case PebbleWatchModel.classic_grey:
        return PebbleWatchIcon.classic(PebbleWatchColor.Grey, size: size);
      case PebbleWatchModel.classic_orange:
        return PebbleWatchIcon.classic(PebbleWatchColor.Orange, size: size);
      case PebbleWatchModel.classic_fresh_green:
        return PebbleWatchIcon.classic(PebbleWatchColor.FreshGreen, size: size);
      case PebbleWatchModel.classic_hot_pink:
        return PebbleWatchIcon.classic(PebbleWatchColor.HotPink, size: size);
      case PebbleWatchModel.classic_fly_blue:
        return PebbleWatchIcon.classic(PebbleWatchColor.FlyBlue, size: size);
      //
      case PebbleWatchModel.classic_steel_silver:
        return PebbleWatchIcon.classicSteel(PebbleWatchColor.Silver,
            size: size);
      case PebbleWatchModel.classic_steel_gunmetal:
        return PebbleWatchIcon.classicSteel(PebbleWatchColor.Grey, size: size);
      //
      case PebbleWatchModel.time_black:
        return PebbleWatchIcon.time(PebbleWatchColor.Black, size: size);
      case PebbleWatchModel.time_white:
        return PebbleWatchIcon.time(PebbleWatchColor.White, size: size);
      case PebbleWatchModel.time_red:
        return PebbleWatchIcon.time(PebbleWatchColor.Red, size: size);
      //
      case PebbleWatchModel.time_steel_silver:
        return PebbleWatchIcon.timeSteel(PebbleWatchColor.Silver, size: size);
      case PebbleWatchModel.time_steel_gunmetal:
        return PebbleWatchIcon.timeSteel(PebbleWatchColor.Grey, size: size);
      case PebbleWatchModel.time_steel_gold:
        return PebbleWatchIcon.timeSteel(PebbleWatchColor.Gold, size: size);
      //
      case PebbleWatchModel.time_round_silver_14:
        return PebbleWatchIcon.timeRound(PebbleWatchColor.White, size: size);
      case PebbleWatchModel.time_round_silver_20:
        return PebbleWatchIcon.timeRound(PebbleWatchColor.White, size: size);
      case PebbleWatchModel.time_round_black_14:
        return PebbleWatchIcon.timeRound(PebbleWatchColor.Black, size: size);
      case PebbleWatchModel.time_round_black_20:
        return PebbleWatchIcon.timeRound(PebbleWatchColor.Black, size: size);
      case PebbleWatchModel.time_round_rose_gold_14:
        return PebbleWatchIcon.timeRound(PebbleWatchColor.White,
            bodyStrokeColor: PebbleWatchColor.RoseGold, size: size);
      case PebbleWatchModel.time_round_black_silver_polish_20:
        return PebbleWatchIcon.timeRound(PebbleWatchColor.Black,
            bodyStrokeColor: PebbleWatchColor.Silver, size: size);
      case PebbleWatchModel.time_round_black_gold_polish_20:
        return PebbleWatchIcon.timeRound(PebbleWatchColor.Black,
            bodyStrokeColor: PebbleWatchColor.Gold, size: size);
      case PebbleWatchModel.time_round_rainbow_silver_14:
        return PebbleWatchIcon.timeRound(PebbleWatchColor.White,
            bodyStrokeColor: PebbleWatchColor.Rainbow, size: size);
      case PebbleWatchModel.time_round_rainbow_black_20:
        return PebbleWatchIcon.timeRound(PebbleWatchColor.Black,
            bodyStrokeColor: PebbleWatchColor.Rainbow, size: size);
      //
      case PebbleWatchModel.pebble_2_hr_black:
        return PebbleWatchIcon.pebbleTwo(PebbleWatchColor.Black, size: size);
      case PebbleWatchModel.pebble_2_se_black:
        return PebbleWatchIcon.pebbleTwo(PebbleWatchColor.Black, size: size);
      case PebbleWatchModel.pebble_2_hr_white:
        return PebbleWatchIcon.pebbleTwo(PebbleWatchColor.White, size: size);
      case PebbleWatchModel.pebble_2_se_white:
        return PebbleWatchIcon.pebbleTwo(PebbleWatchColor.White, size: size);
      case PebbleWatchModel.pebble_2_hr_flame:
        return PebbleWatchIcon.pebbleTwo(PebbleWatchColor.Black,
            buttonsColor: PebbleWatchColor.Flame, size: size);
      case PebbleWatchModel.pebble_2_hr_lime:
        return PebbleWatchIcon.pebbleTwo(PebbleWatchColor.Black,
            buttonsColor: PebbleWatchColor.Lime, size: size);
      case PebbleWatchModel.pebble_2_hr_aqua:
        return PebbleWatchIcon.pebbleTwo(PebbleWatchColor.White,
            buttonsColor: PebbleWatchColor.Aqua,
            bezelColor: PebbleWatchColor.Aqua,
            size: size);
      //
      case PebbleWatchModel.rebble_logo:
        return PebbleWatchIcon.rebbleLogo(size: size);
    }
  }
}

class PebbleWatchColor {
  // Defining colors used for different Pebble models
  static const Color Black =
      Color(0xFF444444); // Body color for black and gunmetal watches
  static const Color Red = Color(0xFFD33434); // Body color for red classic/time
  static const Color White = Color(0xFFFFFFFF); // Body color for white watches
  static const Color Grey = Color(0xFFADADAD); // Body color for grey classic
  static const Color Orange =
      Color(0xFFF4953D); // Body color for orange classic
  static const Color HotPink =
      Color(0xFFFF3F8F); // They sure made a lot of these huh
  static const Color FreshGreen =
      Color(0xFFA7EF6F); // Body color for green classics
  static const Color FlyBlue =
      Color(0xFF41CBF7); // Body color for blue classics

  static const Color Silver =
      Color(0xFFE0E0E0); // Body color for silver watches
  static const Color Gold = Color(0xFFEACB7B); // Body color for gold time steel
  static const Color RoseGold =
      Color(0xFFD7A17F); // Body stroke color for gold time round
  static const Color Rainbow = Color(
      0xFF51C4CB); // Unreleased spalding prototype, let's call it an easter egg

  static const Color Aqua =
      Color(0xFF00A99F); // Button+screen stroke for turq/white P2HR
  static const Color Flame =
      Color(0xFFD33434); // Button color for red/black P2HR
  static const Color Lime =
      Color(0xFF8BE346); // Button color for green/black P2HR
}

enum PebbleWatchModel {
  // This enum is meant to match the official Pebble app's configuration, so
  // that the model number the watch reports can be used without additional
  // logic to find the model

  rebble_logo, // COLOR_UNKNOWN 0
  //
  classic_black, // COLOR_BLACK 1
  classic_white, // COLOR_WHITE 2
  classic_red, // COLOR_RED 3
  classic_orange, // COLOR_ORANGE 4
  classic_grey, // COLOR_GRAY 5
  //
  classic_steel_silver, // COLOR_BIANCA_SILVER 6
  classic_steel_gunmetal, // COLOR_BIANCA_BLACK 7
  //
  classic_fly_blue, // COLOR_TINTIN_BLUE 8
  classic_fresh_green, // COLOR_TINTIN_GREEN 9
  classic_hot_pink, // COLOR_TINTIN_PINK 10
  //
  time_white, // COLOR_SNOWY_WHITE 11
  time_black, // COLOR_SNOWY_BLACK 12
  time_red, // COLOR_SNOWY_RED 13
  //
  time_steel_silver, // COLOR_BOBBY_SILVER 14
  time_steel_gunmetal, // COLOR_BOBBY_BLACK 15
  time_steel_gold, // COLOR_BOBBY_GOLD 16
  //
  time_round_silver_14, // COLOR_SPALDING_SILVER_14 17
  time_round_black_14, // COLOR_SPALDING_BLACK_14 18
  time_round_silver_20, // COLOR_SPALDING_SILVER_20 19
  time_round_black_20, // COLOR_SPALDING_BLACK_20 20
  time_round_rose_gold_14, // COLOR_SPALDING_ROSE_GOLD_14 21
  time_round_rainbow_silver_14, // COLOR_SPALDING_SILVER_RAINBOW_14 22
  time_round_rainbow_black_20, // COLOR_SPALDING_BLACK_RAINBOW_20 23
  //
  pebble_2_se_black, // COLOR_SILK_SE_BLACK 24
  pebble_2_hr_black, // COLOR_SILK_HR_BLACK 25
  pebble_2_se_white, // COLOR_SILK_SE_WHITE 26
  pebble_2_hr_lime, // COLOR_SILK_HR_GREEN 27
  pebble_2_hr_flame, // COLOR_SILK_HR_RED 28
  pebble_2_hr_white, // COLOR_SILK_HR_WHITE 29
  pebble_2_hr_aqua, // COLOR_SILK_HR_TURQOISE 30
  //
  time_2_black, // COLOR_ROBERT_BLACK 31
  time_2_silver, // COLOR_ROBERT_SILVER 32
  time_2_gold, // COLOR_ROBERT_GOLD 33
  // these have to go at the bottom for proper enum values
  time_round_black_silver_polish_20, // COLOR_SPALDING_POLISHED_SILVER_20 34
  time_round_black_gold_polish_20, // COLOR_SPALDING_POLISHED_GOLD_20 35
}
