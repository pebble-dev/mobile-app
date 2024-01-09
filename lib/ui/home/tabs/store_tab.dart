import 'dart:async';
import 'dart:collection';
import 'dart:convert';
import 'dart:io';
import 'package:cobble/domain/api/appstore/locker_sync.dart';
import 'package:cobble/domain/api/auth/auth.dart';
import 'package:cobble/domain/api/auth/oauth_token.dart';
import 'package:cobble/domain/connection/connection_state_provider.dart';
import 'package:cobble/domain/entities/hardware_platform.dart';
import 'package:cobble/infrastructure/datasources/web_services/auth.dart';
import 'package:cobble/localization/localization.dart';
import 'package:cobble/ui/common/components/cobble_button.dart';
import 'package:cobble/ui/common/icons/fonts/rebble_icons.dart';
import 'package:cobble/ui/common/icons/watch_icon.dart';
import 'package:cobble/ui/router/cobble_scaffold.dart';
import 'package:cobble/ui/router/cobble_screen.dart';
import 'package:cobble/ui/theme/with_cobble_theme.dart';
import 'package:flutter/material.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:package_info/package_info.dart';
import 'package:url_launcher/url_launcher.dart';
import 'package:webview_flutter/webview_flutter.dart';
import 'package:cobble/infrastructure/pigeons/pigeons.g.dart';
import 'package:path/path.dart' as path;
import 'package:uuid_type/uuid_type.dart';

class _TabConfig {
  final String label;
  final Uri url;

  _TabConfig(this.label, this.url);
}

class StoreTab extends HookWidget implements CobbleScreen {
  final _config = [
    _TabConfig(tr.storePage.faces, Uri.parse("https://store-beta.rebble.io/faces")),
    _TabConfig(tr.storePage.apps, Uri.parse("https://store-beta.rebble.io/apps")),
  ];

  @override
  Widget build(BuildContext context) {
    // Those are args from outside that tell us what application to display
    final args = ModalRoute.of(context)!.settings.arguments as AppstoreArguments;
    final appUrl = "https://store-beta.rebble.io/app/";

    final indexTab = useState<int>(0);
    final pageTitle = useState<String>("Loading");
    final backButton = useState<bool>(false);
    final searchBar = useState<bool>(false);
    final attrs = useState<Map<String, String>>(HashMap());
    final baseUrl = useState<Uri>(_config[0].url);

    final Completer<WebViewController> _controller =
      useMemoized(() => Completer<WebViewController>());
    final searchController = useTextEditingController();

    final locker_sync = useProvider(lockerSyncProvider);

    final connectionState = useProvider(connectionStateProvider.state);

    final _auth = useProvider(authServiceProvider.future);

    void handleRequest(String methodName, Map data) async {
      WebViewController controller = await _controller.future;
      data['methodName'] = methodName;
      controller.runJavascript("PebbleBridge.handleRequest(${json.encode(data)})");
    }

    void onEachPageLoad() async {
      WebViewController controller = await _controller.future;
      String current = await controller.currentUrl() ?? baseUrl.value.toString();
      bool canGoBack = await controller.canGoBack();
      backButton.value = canGoBack && baseUrl.value != Uri.parse(current);
    }

    void handleResponse(Map data, int? callback) async {
      WebViewController controller = await _controller.future;
      Map<String, dynamic> response = HashMap();
      response['data'] = data;
      response['callbackId'] = callback;
      controller.runJavascript("PebbleBridge.handleResponse(${json.encode(response)})");
    }

    void installApp(Uuid uuid, int? callback) async {
      Map<String, dynamic> data = HashMap();
      data['added_to_locker'] = false;

      try {
        await locker_sync.addToLocker(uuid);
        data['added_to_locker'] = true;
        handleResponse(data, callback);
      } on Exception {
        handleResponse(data, callback);
      }
    }

    // TODO: When we use up to date hooks riverpod, use callback like so:
    // void Function(String, Map) _handleMethod = useCallback<void Function()>((method, data) async {
    void handleMethod(String method, Map data, int? callback) async {
      switch (method) {
        case "setNavBarTitle":
          // the title is set once per page load, and at the start of every page load, so we attach a hook for that here
          onEachPageLoad();
          pageTitle.value = data["title"];
          break;
        case "openURL":
          launchURL(data["url"]);
          break;
        case "loadAppToDeviceAndLocker":
          installApp(Uuid.parse(data["uuid"]), callback);
          break;
        case "setVisibleApp":
          // In the original app, this was used for displaying sharing button on the app view
          break;
      }
    }

    void _goBack() async {
      WebViewController controller = await _controller.future;
      String current = await controller.currentUrl() ?? baseUrl.value.toString();
      if (baseUrl.value != Uri.parse(current)) controller.goBack();
    }

    Future<void> _setBaseAttrs() async {
      attrs.value.addAll({ 'native': 'true', 'inApp': 'true', 'jsv': '0', 'platform': Platform.operatingSystem });
      PackageInfo packageInfo = await PackageInfo.fromPlatform();
      attrs.value.addAll({ 'app_version': 'packageInfo.version', 'release_id': packageInfo.buildNumber });
      final currentWatch = connectionState.currentConnectedWatch;
      if (currentWatch != null) {
        attrs.value['pebble_color'] = currentWatch.model.index.toString();
        WatchType watchType =
            currentWatch.runningFirmware.hardwarePlatform.getWatchType();
        attrs.value['hardware'] = watchType.toString().split('.').last;
        if (currentWatch.serial != null)
          attrs.value['pid'] = currentWatch.serial!;
      }
    }

    Future<void> _setAuthCookie() async {
      AuthService auth = await _auth;
      OAuthToken token = auth.token;
      if (auth != null) {
        WebViewCookie accessTokenCookie = new WebViewCookie(name: 'access_token', value: token.accessToken, domain: 'store-beta.rebble.io');
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookie(accessTokenCookie);
      }
    }

    void _setWebviewUrl(Uri url) async {
      WebViewController controller = await _controller.future;
      controller.loadUrl(url.toString());
    }

    void _performSearch() {
      Map<String, String> searchAttrs = HashMap();
      searchAttrs['query'] = searchController.text;
      searchAttrs['section'] = indexTab.value == 0 ? 'watchfaces' : 'watchapps';
      handleRequest('search', searchAttrs);
      searchBar.value = false;
      searchController.clear();
    }
    
    void _setIndexTab(int newValue) {
      indexTab.value = newValue;
      baseUrl.value = _config[newValue].url.replace(queryParameters: attrs.value);
      _setWebviewUrl(baseUrl.value);
      // This would be changed anyway, but it looked ugly when it jumped from the previous title
    }

    Future<bool> initialSetup() async {
      await _setBaseAttrs();
      await _setAuthCookie();
      return true;
    }

    useEffect(() {
        initialSetup()
          .whenComplete(() {
            if (args == null) {
              _setIndexTab(indexTab.value);
            } else {
              Uri appUri = Uri.parse(appUrl + args.id).replace(queryParameters: attrs.value);
              _setWebviewUrl(appUri);
            }
          });
      },
      [connectionState],
    );

    return CobbleScaffold.tab(
      leading: backButton.value
          ? IconButton(
              onPressed: () => _goBack(),
              icon: Icon(RebbleIcons.caret_left),
              tooltip: MaterialLocalizations.of(context).backButtonTooltip,
            )
          : null,
      actions: args == null
        ? [
        IconButton(
            icon: Icon(RebbleIcons.search),
            onPressed: () {
              searchBar.value = true;
            },
            tooltip: MaterialLocalizations.of(context).searchFieldLabel,
          ),
      ] : [],
      bottomAppBar: searchBar.value
          ? PreferredSize(
              preferredSize: Size.fromHeight(57),
              child: Row(
                children: [
                  IconButton(
                    icon: Icon(RebbleIcons.x_close),
                    onPressed: () {
                      searchBar.value = false;
                    },
                    tooltip:
                        MaterialLocalizations.of(context).closeButtonTooltip,
                  ),
                  Expanded(
                    child: TextField(
                      controller: searchController,
                      decoration: InputDecoration(
                        hintText: tr.storePage.searchBar,
                        border: InputBorder.none,
                        contentPadding: EdgeInsets.all(19),
                      ),
                      textInputAction: TextInputAction.search,
                      autofocus: true,
                      maxLines: 1,
                      expands: false,
                      onSubmitted: (String value) => _performSearch(),
                    ),
                  ),
                  IconButton(
                    icon: Icon(RebbleIcons.caret_right),
                    onPressed: () => _performSearch(),
                    tooltip: MaterialLocalizations.of(context).searchFieldLabel,
                  )
                ],
              ),
            )
          : null,
      titleWidget: args == null
        ? DropdownButton<int>(
          value: indexTab.value,
          icon: Icon(RebbleIcons.dropdown),
          iconSize: 25,
          underline: Container(
            height: 0,
          ),
          onChanged: (int? newValue) => _setIndexTab(newValue!),
          selectedItemBuilder: (BuildContext context) {
            return [0, 1].map<Widget>((int value) {
              return Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  SizedBox(width: 25.0), //Offset to appear centered
                  Container(
                    width: 125,
                    height: 57,
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        Text(
                          pageTitle.value,
                          overflow: TextOverflow.ellipsis,
                        ),
                        if (pageTitle.value != _config[value].label) ...[
                          SizedBox(height: 4),
                          Text(
                            _config[value].label,
                            style: context.theme.appBarTheme.textTheme!.headline6!
                                .copyWith(
                              fontSize: 14,
                              color: context.scheme!.muted,
                            ),
                          ),
                        ],
                      ],
                    ),
                  ),
                ],
              );
            }).toList();
          },
          items: <int>[0, 1].map<DropdownMenuItem<int>>((int value) {
            return DropdownMenuItem<int>(
              value: value,
              child: Text(_config[value].label),
            );
          }).toList(),
        )
        : Text(
          pageTitle.value,
          overflow: TextOverflow.ellipsis,
        ),
      child: WebView(
        initialUrl: baseUrl.value.toString(),
        javascriptMode: JavascriptMode.unrestricted,
        onWebViewCreated: (WebViewController webViewController) {
          _controller.complete(webViewController);
        },
        navigationDelegate: (NavigationRequest request) {
          // TODO: Most likely needs different handling on iOS device, I don't have one though so I can't test this
          String url = request.url;
          if (url[30] != "?")
            url = url.substring(0, 29) + "?" + url.substring(30, url.length);
          Uri uri = Uri.parse(url);
          if (uri.isScheme("pebble-method-call-js-frame")) {
            Map args = json.decode(uri.queryParameters["args"]!);
            handleMethod(args["methodName"], args["data"], args["callbackId"]);
          }
          // We don't actually want to open any other website
          return NavigationDecision.prevent;
        },
      ),
    );
  }

  launchURL(String url) async {
    if (await canLaunch(url)) {
      await launch(url);
    } else {
      throw 'Could not launch $url';
    }
  }
}

class AppstoreArguments {
  final String id;

  AppstoreArguments(this.id);
}
