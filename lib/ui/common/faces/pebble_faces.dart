import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:cobble/domain/entities/hardware_platform.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:flutter_svg_provider/flutter_svg_provider.dart';
import 'package:cobble/ui/common/icons/watch_icon.dart';
import 'package:cobble/ui/common/faces/kickstart_faces.dart';
import 'dart:math';
import 'package:intl/intl.dart';

class _FaceLayer {
  _FaceLayer({this.svg, this.angle, this.alignment, this.child});
  final String? svg;
  final double? angle;
  final Alignment? alignment;
  final Widget? child;
}

class _WatchFace extends StatelessWidget {
  _WatchFace(this.layers, {this.size = const Size(92, 92), this.prefix = ''});
  final List<_FaceLayer> layers;
  final Size size;
  final String prefix;
  @override
  Widget build(BuildContext context) {
    return SizedBox(
      width: size.width,
      height: size.height,
      child: Stack(
        children: layers.map((e) {
          if (e.svg != null)
            return Transform.rotate(
              child: Image(image: Svg(prefix + e.svg!, size: size)),
              angle: e.angle ?? 0,
            );
          else
            return Container(child: e.child, alignment: e.alignment);
        }).toList(),
      ),
    );
  }
}

class PebbleWatchFace extends HookWidget {
  PebbleWatchFace(this.watchface, this.model, {this.width = 92.0});

  final DefaultWatchface watchface;
  final PebbleWatchModel model;
  final double width;

  @override
  Widget build(BuildContext context) {
    double height = width * (7 / 6);
    DateTime now = DateTime.now();

    String leadingZero(int value) {
      return (value < 10 ? "0" : "") + value.toString();
    }

    Widget dateIndicator(Color color, EdgeInsets margin) {
      return Container(
        margin: margin,
        child: Text(
          "${DateFormat.E().format(now).toUpperCase()} ${leadingZero(now.day)}",
          style: TextStyle(
            fontWeight: FontWeight.bold,
            color: color,
            fontSize: width * 0.08,
          ),
        ),
      );
    }

    Widget textContainer(String text,
        {double fontSize = 0.1,
        Color color = const Color(0xFFFFFFFF),
        String? fontFamily,
        FontWeight? fontWeight,
        double top = 0,
        double bottom = 0,
        double left = 0,
        double right = 0}) {
      return Container(
        margin: EdgeInsets.fromLTRB(
            width * left, width * top, width * right, width * bottom),
        child: Text(
          text,
          style: TextStyle(
            fontWeight: fontWeight,
            fontFamily: fontFamily,
            color: color,
            fontSize: width * fontSize,
          ),
        ),
      );
    }

    // TODO: make those actually update in real time
    DateTime midnight = DateTime(now.year, now.month, now.day);
    int secondsSinceMidnight = now.difference(midnight).inSeconds;
    double hoursAngle = ((pi / 6) * (secondsSinceMidnight / 3600)) % (2 * pi);
    double minutesAngle = ((pi / 30) * (secondsSinceMidnight / 60)) % (2 * pi);
    double secondsAngle = ((pi / 30) * secondsSinceMidnight) % (2 * pi);
    PebbleWatchLine line = watchLineFromNumber(model.index);

    Widget? roundTicToc(model) {
      switch (model) {
        case PebbleWatchModel.time_round_silver_14:
          return _WatchFace(
            [
              _FaceLayer(svg: 'background.svg'),
              _FaceLayer(
                child: dateIndicator(
                    Color(0xFF545454), EdgeInsets.only(bottom: width * 0.14)),
                alignment: Alignment.bottomCenter,
              ),
              _FaceLayer(svg: 'hour.svg', angle: hoursAngle),
              _FaceLayer(svg: 'minute.svg', angle: minutesAngle),
              // _FaceLayer(svg: 'second.svg', angle: secondsAngle), // This was apparently removed from firmware
              _FaceLayer(svg: 'overlay.svg'),
            ],
            size: Size(width, width),
            prefix: 'images/tictoc/time_round/silver_14/',
          );
        case PebbleWatchModel.time_round_silver_20:
          return _WatchFace(
            [
              _FaceLayer(svg: 'background.svg'),
              _FaceLayer(svg: 'hour.svg', angle: hoursAngle),
              _FaceLayer(svg: 'minute.svg', angle: minutesAngle),
              _FaceLayer(svg: 'overlay.svg'),
            ],
            size: Size(width, width),
            prefix: 'images/tictoc/time_round/silver_20/',
          );
        case PebbleWatchModel.time_round_rose_gold_14:
          return _WatchFace(
            [
              _FaceLayer(svg: 'background.svg'),
              _FaceLayer(svg: 'hour.svg', angle: hoursAngle),
              _FaceLayer(svg: 'minute.svg', angle: minutesAngle),
              _FaceLayer(svg: 'overlay.svg'),
            ],
            size: Size(width, width),
            prefix: 'images/tictoc/time_round/gold_14/',
          );
        case PebbleWatchModel.time_round_black_14:
          return _WatchFace(
            [
              _FaceLayer(svg: 'background.svg'),
              _FaceLayer(
                child: dateIndicator(
                    Color(0xFFFFFFFF), EdgeInsets.only(bottom: width * 0.14)),
                alignment: Alignment.bottomCenter,
              ),
              _FaceLayer(svg: 'hour.svg', angle: hoursAngle),
              _FaceLayer(svg: 'minute.svg', angle: minutesAngle),
              _FaceLayer(svg: 'overlay.svg'),
            ],
            size: Size(width, width),
            prefix: 'images/tictoc/time_round/black_14/',
          );
        case PebbleWatchModel.time_round_black_20:
          return _WatchFace(
            [
              _FaceLayer(svg: 'background.svg'),
              _FaceLayer(
                child: dateIndicator(
                    Color(0xFFFFFFFF), EdgeInsets.only(left: width * 0.14)),
                alignment: Alignment.centerLeft,
              ),
              _FaceLayer(svg: 'hour.svg', angle: hoursAngle),
              _FaceLayer(svg: 'minute.svg', angle: minutesAngle),
              _FaceLayer(svg: 'second.svg', angle: secondsAngle),
              _FaceLayer(svg: 'overlay.svg'),
            ],
            size: Size(width, width),
            prefix: 'images/tictoc/time_round/black_20/',
          );
      }
    }

    switch (watchface) {
      case DefaultWatchface.tictoc:
        if (line == PebbleWatchLine.time ||
            line == PebbleWatchLine.time_steel) {
          return _WatchFace(
            [
              _FaceLayer(svg: 'background.svg'),
              _FaceLayer(svg: 'hour.svg', angle: hoursAngle),
              _FaceLayer(svg: 'minute.svg', angle: minutesAngle),
            ],
            size: Size(width, height),
            prefix: 'images/tictoc/time/',
          );
        } else if (line == PebbleWatchLine.classic ||
            line == PebbleWatchLine.steel ||
            line == PebbleWatchLine.pebble_2) {
          return _WatchFace(
            [
              _FaceLayer(svg: 'background.svg'),
              _FaceLayer(
                child: textContainer(DateFormat.MMMMd().format(now),
                    fontSize: 0.14, bottom: 0.5, left: 0.05),
                alignment: Alignment.bottomLeft,
              ),
              _FaceLayer(
                child: textContainer(DateFormat.Hm().format(now),
                    fontWeight: FontWeight.bold,
                    fontSize: 0.32,
                    bottom: 0.12,
                    left: 0.05),
                alignment: Alignment.bottomLeft,
              ),
            ],
            size: Size(width, height),
            prefix: 'images/tictoc/classic/',
          );
        } else if (line == PebbleWatchLine.time_round) {
          final result = roundTicToc(model);
          if (result != null) return result;
        }
        break;
      case DefaultWatchface.kickstart:
        // TODO: implement actual values from pebble health into KickstartActivityPainter
        // and handle whether or not the person has health enabled
        if (line == PebbleWatchLine.time ||
            line == PebbleWatchLine.time_steel) {
          return _WatchFace(
            [
              _FaceLayer(svg: 'images/kickstart/small_color.svg'),
              _FaceLayer(
                child: SizedBox(
                  height: height,
                  width: width,
                  child: ClipRect(
                    child: new CustomPaint(
                      painter: new KickstartActivityPainter(
                          0.4, 0.2, KickstartType.small_color),
                    ),
                  ),
                ),
              ),
              _FaceLayer(
                child: textContainer(DateFormat.Hm().format(now),
                    fontFamily: 'Kickstart', fontSize: 0.48, top: 0.28),
                alignment: Alignment.topCenter,
              ),
              _FaceLayer(
                child: textContainer(
                    "10,323", // TODO: replace with the actual step count
                    color: Color(0xFF00AA52),
                    fontFamily: 'Kickstart',
                    fontSize: 0.26,
                    bottom: 0.14),
                alignment: Alignment.bottomCenter,
              ),
            ],
            size: Size(width, height),
          );
        } else if (line == PebbleWatchLine.time_round) {
          return _WatchFace(
            [
              _FaceLayer(svg: 'images/kickstart/round.svg'),
              _FaceLayer(
                child: SizedBox(
                  height: width,
                  width: width,
                  child: ClipRect(
                    child: new CustomPaint(
                      painter: new KickstartActivityPainter(
                          0.4, 0.2, KickstartType.round),
                    ),
                  ),
                ),
              ),
              _FaceLayer(
                child: textContainer(DateFormat.Hm().format(now),
                    fontFamily: 'Kickstart', fontSize: 0.38, top: 0.24),
                alignment: Alignment.topCenter,
              ),
              _FaceLayer(
                child: textContainer(
                    "10,323", // TODO: replace with the actual step count
                    fontFamily: 'Kickstart',
                    color: Color(0xFF00AA52),
                    fontSize: 0.22,
                    bottom: 0.14),
                alignment: Alignment.bottomCenter,
              ),
            ],
            size: Size(width, width),
          );
        } else if (line == PebbleWatchLine.classic ||
            line == PebbleWatchLine.steel) {
          return _WatchFace(
            [
              _FaceLayer(svg: 'images/kickstart/small_bw.svg'),
              _FaceLayer(
                child: SizedBox(
                  height: height,
                  width: width,
                  child: ClipRect(
                    child: new CustomPaint(
                      painter: new KickstartActivityPainter(
                          0.4, 0.2, KickstartType.small_bw),
                    ),
                  ),
                ),
              ),
              _FaceLayer(
                child: textContainer(DateFormat.Hm().format(now),
                    fontFamily: 'LecoBold', fontSize: 0.22, top: 0.30),
                alignment: Alignment.topCenter,
              ),
              _FaceLayer(
                child: textContainer(
                    "10,323", // TODO: replace with the actual step count
                    fontWeight: FontWeight.bold,
                    fontSize: 0.12,
                    bottom: 0.31,
                    right: 0.14),
                alignment: Alignment.bottomRight,
              ),
            ],
            size: Size(width, height),
          );
        } else if (line == PebbleWatchLine.pebble_2) {
          return _WatchFace(
            [
              _FaceLayer(svg: 'images/kickstart/small_bw_hr.svg'),
              _FaceLayer(
                child: SizedBox(
                  height: height,
                  width: width,
                  child: ClipRect(
                    child: new CustomPaint(
                      painter: new KickstartActivityPainter(
                          0.4, 0.2, KickstartType.small_bw),
                    ),
                  ),
                ),
              ),
              _FaceLayer(
                child: textContainer(DateFormat.Hm().format(now),
                    fontFamily: 'LecoBold', fontSize: 0.22, top: 0.20),
                alignment: Alignment.topCenter,
              ),
              _FaceLayer(
                child: textContainer(
                    "120 BPM", // TODO: replace with the actual heartrate
                    fontWeight: FontWeight.bold,
                    fontSize: 0.12,
                    bottom: 0.39,
                    right: 0.14),
                alignment: Alignment.bottomRight,
              ),
              _FaceLayer(
                child: textContainer(
                    "10,323", // TODO: replace with the actual step count
                    fontWeight: FontWeight.bold,
                    fontSize: 0.12,
                    bottom: 0.19,
                    right: 0.14),
                alignment: Alignment.bottomRight,
              ),
            ],
            size: Size(width, height),
          );
        } else if (line == PebbleWatchLine.time_2) {
          return _WatchFace(
            [
              _FaceLayer(svg: 'images/kickstart/big.svg'),
              _FaceLayer(
                child: SizedBox(
                  height: width * (57 / 50),
                  width: width,
                  child: ClipRect(
                    child: new CustomPaint(
                      painter: new KickstartActivityPainter(
                          0.4, 0.2, KickstartType.big),
                    ),
                  ),
                ),
              ),
              _FaceLayer(
                child: textContainer(DateFormat.Hm().format(now),
                    fontFamily: 'Kickstart', fontSize: 0.52),
                alignment: Alignment.topCenter,
              ),
              _FaceLayer(
                child: textContainer(
                    "120 BPM", // TODO: replace with the actual heartrate
                    color: Color(0xFFFF0000),
                    fontFamily: 'Kickstart',
                    fontSize: 0.26,
                    bottom: 0.29,
                    right: 0.12),
                alignment: Alignment.bottomRight,
              ),
              _FaceLayer(
                child: textContainer(
                    "10,323", // TODO: replace with the actual step count
                    color: Color(0xFF00AA52),
                    fontFamily: 'Kickstart',
                    fontSize: 0.26,
                    bottom: 0.07,
                    right: 0.12),
                alignment: Alignment.bottomRight,
              ),
            ],
            size: Size(width, width * (57 / 50)),
          );
        }
        break;
    }
    return Container();
  }
}

enum DefaultWatchface {
  tictoc,
  kickstart,
}
