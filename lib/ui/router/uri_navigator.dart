import 'dart:io';

import 'package:cobble/ui/home/tabs/store_tab.dart';
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
    Uri uri = Uri.parse(arg.value!);
    if (uri.isScheme("pebble") && uri.host == 'appstore') {
      String id = uri.pathSegments[0];
      // TODO: We currently set up a minified StoreTab() for this, but it would be
      // better if we just used the StoreTab() that already exists and navigated
      // directly to the app from it (ie. handleRequest('navigate', { 'url': '/application/$id' }))
      Navigator.of(_context).pushNamed('/appstore', arguments: AppstoreArguments(id));
    }

    if (Platform.isAndroid && !uri.isScheme("content")) {
      // Only content URIs are supported
      return;
    }

    AppInstallControl control = AppInstallControl();

    final uriWrapper = StringWrapper();
    uriWrapper.value = uri.toString();

    final pbwResult = await control.getAppInfo(uriWrapper);
    _context.push(InstallPrompt(uri.toString(), pbwResult));
  }
}

void useUriNavigator(BuildContext context) {
  useEffect(() {
    final navigator = UriNavigator(context);
    return navigator.cancel;
  }, []);
}
