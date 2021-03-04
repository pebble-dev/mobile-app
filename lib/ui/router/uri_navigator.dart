import 'package:cobble/ui/router/cobble_navigator.dart';
import 'package:cobble/ui/screens/install_prompt.dart';
import 'package:flutter/cupertino.dart';

import '../../infrastructure/pigeons/pigeons.g.dart';

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
    String uri = arg.value;

    AppInstallControl control = AppInstallControl();

    final uriWrapper = StringWrapper();
    uriWrapper.value = uri;

    final pbwResult = await control.getAppInfo(uriWrapper);
    _context.push(InstallPrompt(pbwResult));
  }
}
