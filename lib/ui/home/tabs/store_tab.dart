import 'package:cobble/ui/router/cobble_scaffold.dart';
import 'package:cobble/ui/router/cobble_screen.dart';
import 'package:flutter/material.dart';
import 'package:webview_flutter/webview_flutter.dart';

class StoreTab extends StatefulWidget implements CobbleScreen {
  @override
  State<StatefulWidget> createState() => new _StoreTabState();
}

class _StoreTabState extends State<StoreTab> {
  @override
  Widget build(BuildContext context) {
    return CobbleScaffold(
      child: WebView(
        initialUrl:
            "https://store-beta.rebble.io/?native=true&platform=android",
        javascriptMode: JavascriptMode.unrestricted,
      ),
    );
  }
}
