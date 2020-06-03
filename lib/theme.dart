import 'package:flutter/material.dart';

class CTheme {
  static final colorScheme = ColorScheme(
    primary: Color(0xFFFA5521),
    primaryVariant: Color(0xFFFA5521),
    secondary: Color(0xFFF9A285),
    secondaryVariant: Color(0xFFF9A285),
    surface: Color(0xFF484848),
    background: Color(0xFF333333),
    error: Color(0xfff44336),
    onPrimary: Colors.white,
    onSecondary: Colors.white,
    onSurface: Colors.white,
    onBackground: Colors.white,
    onError: Colors.white70,
    brightness: Brightness.dark,
  );
  static final buttonTheme = ButtonTheme(
    highlightColor: colorScheme.primary,
    colorScheme: colorScheme,
  );
}