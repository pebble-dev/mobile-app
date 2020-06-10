
import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:fossil/icons/pebble_watch_icons_icons.dart';

class _WatchLayer {
  _WatchLayer(this.layer, this.color);
  final IconData layer;
  final Color color;
}

class WatchIcon extends StatelessWidget{
  WatchIcon(this.layers, {this.size = 40.0});
  final List<_WatchLayer> layers;
  final double size;
  @override
  Widget build(BuildContext context) {
    return Stack(
      children: layers.map((e) => Icon(e.layer, color: e.color, size: size,)).toList(),
    );
  }
}

class PebbleWatchIcon { // First layer = bottom layer, last layer = top layer (draw order)
  static WatchIcon Classic(Color bodyColor, {double size = 40.0}) => WatchIcon([
    _WatchLayer(PebbleWatchIcons.classic_body_fill, bodyColor),
    _WatchLayer(PebbleWatchIcons.classic_body_stroke, Colors.black),

    _WatchLayer(PebbleWatchIcons.classic_screen_fill, Colors.white),
    _WatchLayer(PebbleWatchIcons.classic_screen_stroke, Colors.black),
  ], size: size,);
  static WatchIcon Time(Color bodyColor, {double size = 40.0}) => WatchIcon([
    _WatchLayer(PebbleWatchIcons.time_body_fill, bodyColor),
    _WatchLayer(PebbleWatchIcons.time_body_stroke, Colors.black),

    _WatchLayer(PebbleWatchIcons.time_screen_fill, Colors.white),
    _WatchLayer(PebbleWatchIcons.time_screen_stroke, Colors.black),
  ], size: size,);
  static WatchIcon Two(Color bodyColor, Color buttonsColor, {Color bezelColor = Colors.black, double size = 40.0}) => WatchIcon([
    _WatchLayer(PebbleWatchIcons.pebble_2_buttons_stroke, buttonsColor),

    _WatchLayer(PebbleWatchIcons.pebble_2_body_fill, bodyColor),
    _WatchLayer(PebbleWatchIcons.pebble_2_body_stroke, Colors.black),

    _WatchLayer(PebbleWatchIcons.pebble_2_screen_fill, Colors.white),
    _WatchLayer(PebbleWatchIcons.pebble_2_screen_stroke, bezelColor)
  ], size: size,);
}