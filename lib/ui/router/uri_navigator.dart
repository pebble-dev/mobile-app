import 'dart:io';

import 'package:cobble/ui/router/cobble_navigator.dart';
import 'package:cobble/ui/screens/install_prompt.dart';
import 'package:cobble/ui/screens/update_prompt.dart';
import 'package:flutter/material.dart';
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
    String uri = arg.value!;

    if (Platform.isAndroid && !uri.startsWith("content://")) {
      // Only content URIs are supported
      return;
    }

    print("Opening URI: $uri");

    if (uri.endsWith("pbw")) {
      AppInstallControl control = AppInstallControl();

      final uriWrapper = StringWrapper();
      uriWrapper.value = uri;

      final pbwResult = await control.getAppInfo(uriWrapper);
      _context.push(InstallPrompt(uri, pbwResult));
    } else if (uri.endsWith("pbz")) {
      FirmwareUpdateControl control = FirmwareUpdateControl();

      final uriWrapper = StringWrapper();
      uriWrapper.value = uri;

      final compat = await control.checkFirmwareCompatible(uriWrapper);
      if (compat.value == true) {
        _context.push(UpdatePrompt(
            uri: uri,
            onSuccess: (screenContext) {
              Navigator.of(screenContext).pop();
            },
            confirmOnSuccess: true
        ));
      } else {
        Navigator.of(_context).push(MaterialPageRoute(
          builder: (context) => AlertDialog(
            title: const Text("Incompatible firmware"),
            content: const Text("This firmware is not compatible with your watch."),
            actions: [
              TextButton(
                onPressed: () {
                  Navigator.of(context).pop();
                },
                child: const Text("OK"),
              )
            ],
          )
        ));
      }
    } else {
      Navigator.of(_context).push(MaterialPageRoute(
        builder: (context) => AlertDialog(
          title: const Text("Unsupported file type"),
          content: const Text("This URI is not supported."),
          actions: [
            TextButton(
              onPressed: () {
                Navigator.of(context).pop();
              },
              child: const Text("OK"),
            )
          ],
        )
      ));
    }
  }
}

void useUriNavigator(BuildContext context) {
  useEffect(() {
    final navigator = UriNavigator(context);
    return navigator.cancel;
  }, []);
}
