import 'package:flutter/material.dart';
import 'package:flutter/scheduler.dart';
import 'package:flutter_hooks/flutter_hooks.dart';

Brightness? usePlatformBrightness() {
  return use(_DetectBrightness());
}

class _DetectBrightness extends Hook<Brightness?> {
  @override
  HookState<Brightness?, Hook<Brightness?>> createState() {
    return _DetectBrightnessState();
  }
}

class _DetectBrightnessState extends HookState<Brightness?, _DetectBrightness>
    with WidgetsBindingObserver {
  Brightness? brightness;

  _DetectBrightnessState() {
    this.brightness = SchedulerBinding.instance!.window.platformBrightness;
  }

  @override
  void initHook() {
    WidgetsBinding.instance!.addObserver(this);
    super.initHook();
  }

  void dispose() {
    WidgetsBinding.instance!.removeObserver(this);
    super.dispose();
  }

  @override
  Brightness? build(BuildContext context) => brightness;

  @override
  void didChangePlatformBrightness() {
    setState(() {
      brightness = SchedulerBinding.instance!.window.platformBrightness;
    });
    super.didChangePlatformBrightness();
  }
}
