import 'package:flutter/material.dart';

class RebbleTheme {
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

  static final appTheme = ThemeData(
    brightness: Brightness.dark,
    colorScheme: RebbleTheme.colorScheme,
    primaryColor: RebbleTheme.colorScheme.primary,
    accentColor: RebbleTheme.colorScheme.secondary,
    backgroundColor: RebbleTheme.colorScheme.background,
    buttonColor: RebbleTheme.colorScheme.primary,

    appBarTheme: AppBarTheme(
      brightness: Brightness.dark,
      color: RebbleTheme.colorScheme.surface,
    ),
    /*bottomNavigationBarTheme: BottomNavigationBarThemeData(
      backgroundColor: RebbleTheme.colorScheme.surface,
    ),*/
    floatingActionButtonTheme: FloatingActionButtonThemeData(
      backgroundColor: RebbleTheme.colorScheme.primary,
      foregroundColor: Colors.white,
    ),
    iconTheme: IconThemeData(color: RebbleTheme.colorScheme.secondary),

    // This makes the visual density adapt to the platform that you run
    // the app on. For desktop platforms, the controls will be smaller and
    // closer together (more dense) than on mobile platforms.
    visualDensity: VisualDensity.adaptivePlatformDensity,
  );
}
