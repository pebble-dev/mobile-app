import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'dart:ui';
import 'dart:math';

class KickstartActivityPainter extends CustomPainter {
  final double stepPercentage;
  final double monthPercentage;
  final KickstartType type;

  KickstartActivityPainter(
      this.stepPercentage, this.monthPercentage, this.type);

  Path createTypePath(Size size, KickstartType type) {
    double stroke = size.width * 0.07;
    if (type == KickstartType.round) {
      stroke = size.width * 0.08;
      Rect screen = const Offset(0, 0) & Size.square(size.width);
      return Path()..arcTo(screen, -0.5 * pi, 1.9 * pi, false);
    }
    return Path()
      ..moveTo(size.width * 0.5, 0)
      ..lineTo(size.width - stroke, 0)
      ..arcToPoint(Offset(size.width, stroke),
          radius: Radius.circular(stroke), rotation: pi / 2)
      ..lineTo(size.width, size.height - stroke)
      ..arcToPoint(Offset(size.width - stroke, size.height),
          radius: Radius.circular(stroke), rotation: pi / 2)
      ..lineTo(stroke, size.height)
      ..arcToPoint(Offset(0, size.height - stroke),
          radius: Radius.circular(stroke), rotation: pi / 2)
      ..lineTo(0, stroke)
      ..arcToPoint(Offset(stroke, 0),
          radius: Radius.circular(stroke), rotation: pi / 2)
      ..lineTo(size.width * 0.3, 0);
  }

  Tangent getMonthPosition(
      Path originalPath, double monthPercentage, Size size) {
    final totalLength = originalPath
        .computeMetrics()
        .fold(0.0, (double prev, PathMetric metric) => prev + metric.length);

    final length = totalLength * monthPercentage;

    var currentLength = 0.0;

    var metricsIterator = originalPath.computeMetrics().iterator;

    Tangent? position;

    while (metricsIterator.moveNext()) {
      var metric = metricsIterator.current;

      var nextLength = currentLength + metric.length;

      final isLastSegment = nextLength > length;
      if (isLastSegment) {
        final remainingLength = length - currentLength;
        position = metric.getTangentForOffset(remainingLength);
        break;
      }

      currentLength = nextLength;
    }

    return position ?? Tangent.fromAngle(Offset(size.width / 2, 0), -0.5 * pi);
  }

  Path createPathSegment(Path originalPath, double stepPercentage) {
    final totalLength = originalPath
        .computeMetrics()
        .fold(0.0, (double prev, PathMetric metric) => prev + metric.length);

    final length = totalLength * stepPercentage;

    var currentLength = 0.0;

    final path = new Path();

    var metricsIterator = originalPath.computeMetrics().iterator;

    while (metricsIterator.moveNext()) {
      var metric = metricsIterator.current;

      var nextLength = currentLength + metric.length;

      final isLastSegment = nextLength > length;
      if (isLastSegment) {
        final remainingLength = length - currentLength;
        final pathSegment = metric.extractPath(0.0, remainingLength);

        path.addPath(pathSegment, Offset.zero);
        break;
      } else {
        final pathSegment = metric.extractPath(0.0, metric.length);
        path.addPath(pathSegment, Offset.zero);
      }

      currentLength = nextLength;
    }

    return path;
  }

  @override
  void paint(Canvas canvas, Size size) {
    final fullPath = createTypePath(size, type);

    final path = createPathSegment(fullPath, stepPercentage);

    final Paint paint = Paint();
    paint.color = Color(0xFF00AA53);
    if (type == KickstartType.small_bw) paint.color = Color(0xFF808080);
    paint.style = PaintingStyle.stroke;
    paint.strokeCap = StrokeCap.butt;
    paint.strokeWidth = size.width * 0.14;
    if (type == KickstartType.round) paint.strokeWidth = size.width * 0.16;

    canvas.drawPath(path, paint);

    // Draw the yellow month average indicator
    paint.color = Color(0xFFFFFF02);
    if (type == KickstartType.small_bw) paint.color = Color(0xFFFFFFFF);
    paint.strokeCap = StrokeCap.round;
    paint.strokeWidth = size.width * 0.03;

    final position = getMonthPosition(fullPath, monthPercentage, size);
    final angle = position.angle;
    final offset = size.width * 0.09;
    final dx = offset * sin(angle);
    final dy = offset * cos(angle);
    final indicator = Path()
      ..moveTo(position.position.dx + dx, position.position.dy + dy)
      ..lineTo(position.position.dx - dx, position.position.dy - dy);
    canvas.drawPath(indicator, paint);
  }

  @override
  bool shouldRepaint(CustomPainter oldDelegate) => true;
}

enum KickstartType { small_bw, small_color, round, big }
