import 'package:flutter/material.dart';
import 'package:webview_flutter/webview_flutter.dart';

class StoreTab extends StatefulWidget {
  @override
  State<StatefulWidget> createState() => new _StoreTabState();
}

class _StoreTabState extends State<StoreTab>{
  @override
  Widget build(BuildContext context) {
    return WebView(
      onWebViewCreated: (c) {
        c.loadUrl("https://store-beta.rebble.io/?native=true&platform=android");
      },
    );
  }
}