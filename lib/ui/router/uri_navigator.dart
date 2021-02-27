import 'package:cobble/domain/logging.dart';
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
  void openUri(StringWrapper arg) {
    String uri = arg.value;
    Log.d("Got uri: $uri");
  }
}