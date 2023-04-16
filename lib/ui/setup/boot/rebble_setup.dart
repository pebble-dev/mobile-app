import 'package:cobble/domain/api/auth/oauth.dart';
import 'package:cobble/infrastructure/pigeons/pigeons.g.dart';
import 'package:cobble/ui/common/components/cobble_button.dart';
import 'package:cobble/ui/common/icons/fonts/rebble_icons.dart';
import 'package:cobble/ui/router/cobble_navigator.dart';
import 'package:cobble/ui/router/cobble_scaffold.dart';
import 'package:cobble/ui/router/cobble_screen.dart';
import 'package:cobble/ui/setup/boot/rebble_setup_fail.dart';
import 'package:cobble/ui/setup/boot/rebble_setup_success.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:url_launcher/url_launcher.dart';
import 'package:logging/logging.dart';

class RebbleSetup extends HookWidget implements CobbleScreen {
  static final IntentControl lifecycleControl = IntentControl();
  static final Logger _logger = Logger('RebbleSetup');

  const RebbleSetup({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final oauthClient = useProvider(oauthClientProvider);

    return CobbleScaffold.page(
      title: "Activate Rebble services",
      child: Column(
        children: <Widget>[
          Text(
              "Rebble Web Services provides the app store, timeline integration, timeline weather, and voice dictation"),
          oauthClient.when(
            data: (oauth) {
              final authoriseUri = oauth.generateAuthoriseWebviewUrl();
              return CobbleButton(
                label: "SIGN IN TO REBBLE SERVICES",
                onPressed: () => canLaunchUrl(authoriseUri).then((value) async {
                  if (value) {
                    if (await launchUrl(authoriseUri, mode: LaunchMode.externalApplication)) {
                        final result = await lifecycleControl.waitForOAuth();
                        await closeInAppWebView();
                        if (result.code != null && result.state != null) {
                          try {
                            await oauth.requestTokenFromCode(result.code!, result.state!);
                            context.pushReplacement(RebbleSetupSuccess());
                          } catch (e) {
                            _logger.warning("OAuth error: ${e.toString()}");
                            context.pushReplacement(RebbleSetupFail());
                          }
                        }else {
                            if (kDebugMode) {
                              print("oauth error: ${result.error ?? "null"}");
                            }
                            context.pushReplacement(RebbleSetupFail());
                        }
                    }else {
                        context.pushReplacement(RebbleSetupFail());
                    }
                  }
                }),
              );
            },
            loading: () {
              return ElevatedButton(
                child: Text("SIGN IN TO REBBLE SERVICES"),
                onPressed: null,
              );
            },
            error: (e, stack) {
              print(e);
              return Row(
              children: [
                const Icon(RebbleIcons.warning),
                Text("Services currently unavailable"),
              ],
            );
            },
          ),
          CobbleButton(
            outlined: false,
            label: "SKIP",
            onPressed: () => context.pushReplacement(RebbleSetupSuccess()),
          )
        ],
      ),
    );
  }
}
