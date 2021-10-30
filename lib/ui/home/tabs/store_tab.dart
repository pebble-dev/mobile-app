import 'dart:async';
import 'dart:convert';
import 'dart:io';
import 'package:cobble/domain/connection/connection_state_provider.dart';
import 'package:cobble/domain/entities/hardware_platform.dart';
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
import 'package:cobble/domain/apps/app_manager.dart';
import 'package:path/path.dart' as path;

class _TabConfig {
  final String label;
  final String url;

  _TabConfig(this.label, this.url);
}

class StoreTab extends HookWidget implements CobbleScreen {
  final _config = [
    _TabConfig("Watchfaces", "https://store-beta.rebble.io/faces/"),
    _TabConfig("Apps", "https://store-beta.rebble.io/apps/"),
  ];

  @override
  Widget build(BuildContext context) {
    final indexTab = useState<int>(0);
    final pageTitle = useState<String>("Loading");
    final backButton = useState<bool>(false);
    final searchBar = useState<bool>(false);

    final Completer<WebViewController> _controller =
      Completer<WebViewController>();
    final searchController = useTextEditingController();

    final appManager = useProvider(appManagerProvider);
    AppInstallControl control = AppInstallControl();

    String baseAttrs = "native=true&inApp=true&jsv=0";

    // Even if we aren't connected, local storage should remember the default preferences
    final connectionState = useProvider(connectionStateProvider.state);
    final currentWatch = connectionState.currentConnectedWatch;
    if (currentWatch != null) {
      baseAttrs += "&pebble_color=${currentWatch.model.index}";
      WatchType watchType =
          currentWatch.runningFirmware.hardwarePlatform.getWatchType();
      baseAttrs += "&hardware=${watchType.toString().split('.').last}";
      baseAttrs += "&pid=${currentWatch.serial}";
    }

    // TODO: as part of rebble integration, add uid (user id) and mid (phone id) to baseAttrs

    () async {
      PackageInfo packageInfo = await PackageInfo.fromPlatform();
      baseAttrs += "&app_version=${packageInfo.version}";
      baseAttrs += "&release_id=${packageInfo.buildNumber}";
    };

    baseAttrs += "&platform=${Platform.operatingSystem}";

    //rootUrl is used for the initial url and to block going back too far in history, since this is all done with a single WebView, where we don't get to remove history
    //it also gives us some flexibility in terms of navigation
    final rootUrl = useState<String>("${_config[0].url}?$baseAttrs");
    
    void onEachPageLoad() async {
      WebViewController controller = await _controller.future;
      String current = await controller.currentUrl() ?? rootUrl.value;
      bool canGoBack = await controller.canGoBack();
      backButton.value = canGoBack && rootUrl.value != current;
    }
    
    void Function(String, Map) _handleMethod = useCallback<void Function()>((method, data) async {
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
          // TODO: Implement this on kotlin side, so it works on iOS as well
          // Uri? uri = Uri.tryParse(data["pbw_file"]);
          // if (uri != null) {
          //   String? filePath = await _downloadPbw(uri);
          //   if (filePath != null) {
          //     String fileUrl = "file://${filePath}";
          //     final uriWrapper = StringWrapper();
          //     uriWrapper.value = fileUrl;

          //     final appInfo = await control.getAppInfo(uriWrapper);
          //     appManager.beginAppInstall(fileUrl, appInfo);
          //     // TODO: Fill out the rest of the metadata provided by the data map
          //     // Needed for the locker: appstoreId (id in the json), list_image (faces), icon_image (apps), api endpoints for interacting with the store item from the locker
          //   }
          // }
          break;
        case "setVisibleApp":
          // I don't see the use for this, unless we decide to fetch metadata for pbws installed from the outside (we can easily match with uuid)
          break;
      }
    }, []);

    void _goBack() async {
      WebViewController controller = await _controller.future;
      String current = await controller.currentUrl() ?? rootUrl.value;
      if (rootUrl.value != current) controller.goBack();
    }

    void _setWebviewUrl(String url) async {
      WebViewController controller = await _controller.future;
      controller.loadUrl("$url?$baseAttrs");
    }

    void _performSearch() {
      _setWebviewUrl(
          "${_config[indexTab.value].url}search?query=${searchController.text}&$baseAttrs");
      searchBar.value = false;
      searchController.clear();
    }
    
    void _setIndexTab(int newValue)  {
      indexTab.value = newValue;
      rootUrl.value = "${_config[newValue].url}?$baseAttrs";
      _setWebviewUrl(rootUrl.value);
      // This would be changed anyway, but it looked ugly when it jumped from the previous title
      pageTitle.value = _config[newValue].label;
    }

    useEffect(() {
        // When the rootUrl changes, reset the webview to the default values
        _setWebviewUrl(rootUrl.value);
        _setIndexTab(0);
      },
      [rootUrl.value],
    );

    return CobbleScaffold.tab(
      leading: backButton.value
          ? IconButton(
              onPressed: () => _goBack(),
              icon: Icon(RebbleIcons.caret_left),
              tooltip: MaterialLocalizations.of(context).backButtonTooltip,
            )
          : null,
      actions: [
        IconButton(
          icon: Icon(RebbleIcons.search),
          onPressed: () {
            searchBar.value = true;
          },
          tooltip: MaterialLocalizations.of(context).searchFieldLabel,
        ),
      ],
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
                        hintText: 'Search for something...',
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
      titleWidget: DropdownButton<int>(
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
      ),
      child: WebView(
        initialUrl: rootUrl.value,
        javascriptMode: JavascriptMode.unrestricted,
        onWebViewCreated: (WebViewController webViewController) {
          _controller.complete(webViewController);
        },
        navigationDelegate: (NavigationRequest request) {
          String url = request.url;
          // This is an annoying difference between the ios and android urls, so we normalize that to make parsing easier
          if (url[30] != "?")
            url = url.substring(0, 29) + "?" + url.substring(30, url.length);
          Uri uri = Uri.parse(url);
          if (uri.isScheme("pebble-method-call-js-frame")) {
            Map args = json.decode(uri.queryParameters["args"]!);
            _handleMethod(args["methodName"], args["data"]);
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
