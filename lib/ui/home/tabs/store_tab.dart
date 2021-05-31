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
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:package_info/package_info.dart';
import 'package:url_launcher/url_launcher.dart';
import 'package:webview_flutter/webview_flutter.dart';
import 'package:cobble/infrastructure/pigeons/pigeons.g.dart';
import 'package:cobble/domain/apps/app_manager.dart';

class StoreTab extends HookWidget implements CobbleScreen {
  final String baseUrl = "https://store-beta.rebble.io";
  final Completer<WebViewController> _controller =
      Completer<WebViewController>();
  final TextEditingController searchController = TextEditingController();

  @override
  Widget build(BuildContext context) {
    final indexTab = useState<int>(0);
    final pageTitle = useState<String>("Loading");
    final backButton = useState<bool>(false);
    final searchBar = useState<bool>(false);

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

    List<String> tabNames = ["Watchfaces", "Apps"];
    List<String> tabUrls = ["$baseUrl/faces/", "$baseUrl/apps/"];

    //rootUrl is used for the initial url and to block going back too far in history, since this is all done with a single WebView
    //it also gives us some flexibility in terms of navigation
    final rootUrl = useState<String>("${tabUrls[0]}?$baseAttrs");

    void onEachPageLoad() async {
      WebViewController controller = await _controller.future;
      String current = await controller.currentUrl() ?? rootUrl.value;
      bool canGoBack = await controller.canGoBack();
      backButton.value = canGoBack && rootUrl.value != current;
    }

    void handleMethod(String method, Map data) async {
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
          final uriWrapper = StringWrapper();
          uriWrapper.value = data["pbw_file"];

          final appInfo = await control.getAppInfo(uriWrapper);
          appManager.beginAppInstall(data["pbw_file"], appInfo, json.encode(data));
          break;
        case "setVisibleApp":
          // I don't see the use for this
          break;
      }
    }

    void _goBack() async {
      WebViewController controller = await _controller.future;
      String current = await controller.currentUrl() ?? rootUrl.value;
      if (rootUrl.value != current) controller.goBack();
    }

    void setWebviewUrl(String url) async {
      WebViewController controller = await _controller.future;
      controller.loadUrl("$url?$baseAttrs");
    }

    void performSearch() {
      setWebviewUrl(
          "${tabUrls[indexTab.value]}search?query=${searchController.text}&$baseAttrs");
      searchBar.value = false;
      searchController.clear();
    }

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
                      onSubmitted: (String value) => performSearch(),
                    ),
                  ),
                  IconButton(
                    icon: Icon(RebbleIcons.caret_right),
                    onPressed: () => performSearch(),
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
        onChanged: (int? newValue) {
          indexTab.value = newValue!;
          rootUrl.value = "${tabUrls[newValue]}?$baseAttrs";
          setWebviewUrl(tabUrls[newValue]);
          // This would be changed anyway, but it looked ugly when it jumped from the previous title
          pageTitle.value = tabNames[newValue];
        },
        selectedItemBuilder: (BuildContext context) {
          return [0, 1].map<Widget>((int value) {
            return Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                SizedBox(width: 25.0),
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
                      if (pageTitle.value != tabNames[value]) ...[
                        SizedBox(height: 4),
                        Text(
                          tabNames[value],
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
            child: Text(tabNames[value]),
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
          if (url[30] != "?")
            url = url.substring(0, 29) + "?" + url.substring(30, url.length);
          Uri uri = Uri.parse(url);
          if (uri.isScheme("pebble-method-call-js-frame")) {
            Map args = json.decode(uri.queryParameters["args"]!);
            handleMethod(args["methodName"], args["data"]);
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
