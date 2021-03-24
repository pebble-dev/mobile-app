import 'package:cobble/ui/theme/cobble_scheme.dart';
import 'package:flutter/material.dart';

extension WithCobbleTheme on BuildContext {
  ThemeData get theme => Theme.of(this);
  TextTheme get textTheme => this.theme.textTheme;
  CobbleSchemeData? get scheme => CobbleScheme.of(this);
}
