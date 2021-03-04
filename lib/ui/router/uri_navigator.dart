import 'package:cobble/ui/screens/install_prompt.dart';
import 'package:flutter/cupertino.dart';

import '../../infrastructure/pigeons/pigeons.g.dart';

class UriNavigator implements IntentCallbacks {
  NavigatorState _navigatorState;

  UriNavigator(this._navigatorState);

  void init() {
    IntentCallbacks.setup(this);
    IntentControl().notifyFlutterReadyForIntents();
  }

  @override
  void openUri(StringWrapper arg) async {
    String uri = arg.value;

    AppInstallControl control = AppInstallControl();

    final uriWrapper = StringWrapper();
    uriWrapper.value = uri;

    final pbwResult = await control.getAppInfo(uriWrapper);
    _navigatorState
        .push(CupertinoPageRoute(builder: (_) => InstallPrompt(pbwResult)));
  }
}
