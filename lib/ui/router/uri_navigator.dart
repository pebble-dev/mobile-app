import 'package:cobble/domain/entities/pbw_app_info_parsed.dart';
import 'package:flutter/cupertino.dart';

import '../../infrastructure/pigeons/pigeons.g.dart';

class UriNavigator implements IntentCallbacks {
  BuildContext _buildContext;

  UriNavigator(this._buildContext);

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

    final out = await control.getAppInfo(uriWrapper);
    if (!out.isValid) {
      print('Not valid :(');
    } else {
      final appInfo = PbwAppInfoParsed(out);

      print('Got $appInfo');
    }
  }
}