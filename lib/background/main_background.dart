import 'package:cobble/infrastructure/pigeons/pigeons.dart';
import 'package:flutter/widgets.dart';

void main_background() {
  print('Background hello!');

  WidgetsFlutterBinding.ensureInitialized();

  BackgroundControl().notifyFlutterBackgroundStarted();
  print("Background initialized");
}
