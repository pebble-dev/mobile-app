import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:fossil/ui/Theme.dart';
import 'package:fossil/ui/common/icons/fonts/PebbleWatchIcons.dart';

class _WatchLayer {
  _WatchLayer(this.layer, this.color);
  final IconData layer;
  final Color color;
}

class WatchIcon extends StatelessWidget {
  WatchIcon(this.layers, {this.size = 48.0});
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

class PebbleWatchIcon {
  // First layer = bottom layer, last layer = top layer (draw order)
  static WatchIcon classic(Color bodyColor, {double size = 48.0}) => WatchIcon(
        [
          _WatchLayer(PebbleWatchIcons.classic_body_fill, bodyColor),
          _WatchLayer(PebbleWatchIcons.classic_body_stroke, Colors.black),
          _WatchLayer(PebbleWatchIcons.classic_screen_fill, Colors.white),
          _WatchLayer(PebbleWatchIcons.classic_screen_stroke, Colors.black),
        ],
        size: size,
      );
  static WatchIcon classicSteel(Color bodyColor, {double size = 48.0}) =>
      WatchIcon(
        [
          _WatchLayer(PebbleWatchIcons.classic_steel_body_fill, bodyColor),
          _WatchLayer(PebbleWatchIcons.classic_steel_body_stroke, Colors.black),
          _WatchLayer(PebbleWatchIcons.classic_steel_screen_fill, Colors.white),
          _WatchLayer(
              PebbleWatchIcons.classic_steel_screen_stroke, Colors.black),
        ],
        size: size,
      );
  static WatchIcon time(Color bodyColor, {double size = 48.0}) => WatchIcon(
        [
          _WatchLayer(PebbleWatchIcons.time_body_fill, bodyColor),
          _WatchLayer(PebbleWatchIcons.time_body_stroke, Colors.black),
          _WatchLayer(PebbleWatchIcons.time_screen_fill, Colors.white),
          _WatchLayer(PebbleWatchIcons.time_screen_stroke, Colors.black),
        ],
        size: size,
      );
  static WatchIcon timeSteel(Color bodyColor, {double size = 48.0}) =>
      WatchIcon([
        _WatchLayer(PebbleWatchIcons.time_steel_body_fill, bodyColor),
        _WatchLayer(PebbleWatchIcons.time_steel_body_stroke, Colors.black),
        _WatchLayer(PebbleWatchIcons.time_steel_screen_fill, Colors.white),
        _WatchLayer(PebbleWatchIcons.time_steel_screen_stroke, Colors.black),
      ]);
  static WatchIcon timeRound(Color bodyColor,
          {Color bodyStrokeColor = Colors.black, double size = 48.0}) =>
      WatchIcon(
        [
          _WatchLayer(PebbleWatchIcons.time_round_body_fill, bodyColor),
          _WatchLayer(PebbleWatchIcons.time_round_body_stroke, bodyStrokeColor),
          _WatchLayer(PebbleWatchIcons.time_round_screen_fill, Colors.white),
          _WatchLayer(PebbleWatchIcons.time_round_screen_stroke, Colors.black),
        ],
        size: size,
      );
  static WatchIcon pebbleTwo(Color bodyColor,
          {Color buttonsColor = Colors.black,
          Color bezelColor = Colors.black,
          double size = 48.0}) =>
      WatchIcon(
        [
          _WatchLayer(PebbleWatchIcons.pebble_2_buttons_stroke, buttonsColor),
          _WatchLayer(PebbleWatchIcons.pebble_2_body_fill, bodyColor),
          _WatchLayer(PebbleWatchIcons.pebble_2_body_stroke, Colors.black),
          _WatchLayer(PebbleWatchIcons.pebble_2_screen_fill, Colors.white),
          _WatchLayer(PebbleWatchIcons.pebble_2_screen_stroke, bezelColor)
        ],
        size: size,
      );
  static WatchIcon rebbleLogo({size = 48.0}) => WatchIcon([
        _WatchLayer(PebbleWatchIcons.rebble_logo_body_fill, Colors.white),
        _WatchLayer(PebbleWatchIcons.rebble_logo_body_stroke, Colors.black),
        _WatchLayer(PebbleWatchIcons.rebble_logo_hands, Color(0xFFFA5521)),
      ]);
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

  static const Color Aqua =
      Color(0xFF00A99F); // Button+screen stroke for turq/white P2HR
  static const Color Flame =
      Color(0xFFD33434); // Button color for red/black P2HR
  static const Color Lime =
      Color(0xFF8BE346); // Button color for green/black P2HR
}

enum PebbleWatchModel {
  classic_black,
  classic_white,
  classic_red,
  classic_grey,
  classic_orange,
  classic_fresh_green,
  classic_hot_pink,
  classic_fly_blue,
  //
  classic_steel_silver,
  classic_steel_gunmetal,
  //
  time_black,
  time_white,
  time_red,
  //
  time_steel_silver,
  time_steel_gunmetal,
  time_steel_gold,
  //
  time_round_silver,
  time_round_black,
  time_round_rose_gold,
  time_round_black_silver_polish,
  time_round_black_gold_polish,
  // i actually don't know every color of PTR released, please extend if necessary
  pebble_2_black,
  pebble_2_white,
  pebble_2_flame,
  pebble_2_lime,
  pebble_2_aqua,
  //
  rebble_logo
}

/*const*/ Map<PebbleWatchModel, Widget> watchDictionary = {
  PebbleWatchModel.classic_black:
      PebbleWatchIcon.classic(PebbleWatchColor.Black),
  PebbleWatchModel.classic_white:
      PebbleWatchIcon.classic(PebbleWatchColor.White),
  PebbleWatchModel.classic_red: PebbleWatchIcon.classic(PebbleWatchColor.Red),
  PebbleWatchModel.classic_grey: PebbleWatchIcon.classic(PebbleWatchColor.Grey),
  PebbleWatchModel.classic_orange:
      PebbleWatchIcon.classic(PebbleWatchColor.Orange),
  PebbleWatchModel.classic_fresh_green:
      PebbleWatchIcon.classic(PebbleWatchColor.FreshGreen),
  PebbleWatchModel.classic_hot_pink:
      PebbleWatchIcon.classic(PebbleWatchColor.HotPink),
  PebbleWatchModel.classic_fly_blue:
      PebbleWatchIcon.classic(PebbleWatchColor.FlyBlue),
  //
  PebbleWatchModel.classic_steel_silver:
      PebbleWatchIcon.classicSteel(PebbleWatchColor.Silver),
  PebbleWatchModel.classic_steel_gunmetal:
      PebbleWatchIcon.classicSteel(PebbleWatchColor.Grey),
  //
  PebbleWatchModel.time_black: PebbleWatchIcon.time(PebbleWatchColor.Black),
  PebbleWatchModel.time_white: PebbleWatchIcon.time(PebbleWatchColor.White),
  PebbleWatchModel.time_red: PebbleWatchIcon.time(PebbleWatchColor.Red),
  //
  PebbleWatchModel.time_steel_silver:
      PebbleWatchIcon.timeSteel(PebbleWatchColor.Silver),
  PebbleWatchModel.time_steel_gunmetal:
      PebbleWatchIcon.timeSteel(PebbleWatchColor.Grey),
  PebbleWatchModel.time_steel_gold:
      PebbleWatchIcon.timeSteel(PebbleWatchColor.Gold),
  //
  PebbleWatchModel.time_round_silver:
      PebbleWatchIcon.timeRound(PebbleWatchColor.White),
  PebbleWatchModel.time_round_black:
      PebbleWatchIcon.timeRound(PebbleWatchColor.Black),
  PebbleWatchModel.time_round_rose_gold: PebbleWatchIcon.timeRound(
      PebbleWatchColor.White,
      bodyStrokeColor: PebbleWatchColor.RoseGold),
  PebbleWatchModel.time_round_black_silver_polish: PebbleWatchIcon.timeRound(
      PebbleWatchColor.Black,
      bodyStrokeColor: PebbleWatchColor.Silver),
  PebbleWatchModel.time_round_black_gold_polish: PebbleWatchIcon.timeRound(
      PebbleWatchColor.Black,
      bodyStrokeColor: PebbleWatchColor.Gold),
  //
  PebbleWatchModel.pebble_2_black:
      PebbleWatchIcon.pebbleTwo(PebbleWatchColor.Black),
  PebbleWatchModel.pebble_2_white:
      PebbleWatchIcon.pebbleTwo(PebbleWatchColor.White),
  PebbleWatchModel.pebble_2_flame: PebbleWatchIcon.pebbleTwo(
      PebbleWatchColor.Black,
      buttonsColor: PebbleWatchColor.Flame),
  PebbleWatchModel.pebble_2_lime: PebbleWatchIcon.pebbleTwo(
      PebbleWatchColor.Black,
      buttonsColor: PebbleWatchColor.Lime),
  PebbleWatchModel.pebble_2_aqua: PebbleWatchIcon.pebbleTwo(
      PebbleWatchColor.White,
      buttonsColor: PebbleWatchColor.Aqua,
      bezelColor: PebbleWatchColor.Aqua),
  //
  PebbleWatchModel.rebble_logo: PebbleWatchIcon.rebbleLogo()
};
