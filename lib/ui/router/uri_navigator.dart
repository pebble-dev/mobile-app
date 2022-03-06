import 'dart:io';

import 'package:cobble/ui/router/cobble_navigator.dart';
import 'package:cobble/ui/screens/install_prompt.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter_hooks/flutter_hooks.dart';

import '../../infrastructure/pigeons/pigeons.g.dart';

/// If user opens any URI with Cobble,
/// this navigator will receive notification and handle that URI.
class UriNavigator implements IntentCallbacks {
  BuildContext _context;

  final _intentControl = IntentControl();

  UriNavigator(this._context) {
    IntentCallbacks.setup(this);
    _intentControl.notifyFlutterReadyForIntents();
  }

  void cancel() {
    _intentControl.notifyFlutterNotReadyForIntents();
  }

  @override
  void openUri(StringWrapper arg) async {
    String uri = arg.value!;

    if (Platform.isAndroid && !uri.startsWith("content://")) {
      // Only content URIs are supported
      return;
    }

    AppInstallControl control = AppInstallControl();

    final uriWrapper = StringWrapper();
    uriWrapper.value = uri;

    final pbwResult = await control.getAppInfo(uriWrapper);
    _context.push(InstallPrompt(uri, pbwResult));
  }
}

void useUriNavigator(BuildContext context) {
  useEffect(() {
    final navigator = UriNavigator(context);
    return navigator.cancel;
  }, []);
}
