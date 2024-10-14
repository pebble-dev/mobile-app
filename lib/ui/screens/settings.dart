import 'package:cobble/domain/api/auth/auth.dart';
import 'package:cobble/domain/api/auth/user.dart';
import 'package:cobble/domain/api/boot/boot.dart';
import 'package:cobble/domain/api/no_token_exception.dart';
import 'package:cobble/localization/localization.dart';
import 'package:cobble/ui/common/components/cobble_card.dart';
import 'package:cobble/ui/common/components/cobble_tile.dart';
import 'package:cobble/ui/common/icons/fonts/rebble_icons.dart';
import 'package:cobble/ui/devoptions/dev_options_page.dart';
import 'package:cobble/ui/home/tabs/widget_library.dart';
import 'package:cobble/ui/router/cobble_scaffold.dart';
import 'package:cobble/ui/router/cobble_screen.dart';
import 'package:cobble/ui/screens/about.dart';
import 'package:cobble/ui/screens/calendar.dart';
import 'package:cobble/ui/screens/health.dart';
import 'package:cobble/ui/screens/notifications.dart';
import 'package:cobble/ui/screens/placeholder_screen.dart';
import 'package:cobble/ui/setup/boot/rebble_setup.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:flutter_svg_provider/flutter_svg_provider.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:url_launcher/url_launcher.dart';

class Settings extends HookConsumerWidget implements CobbleScreen {
  const Settings({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final auth = ref.watch(authServiceProvider.future);
    final webviews = ref.watch(bootServiceProvider.future).then((value) async => (await value.config).webviews);
    return CobbleScaffold.tab(
      title: tr.settings.title,
      child: ListView(
        children: [
          FutureBuilder<User>(
            future: auth.then((value) => value.user),
            builder: (context, snap) {
              if (snap.hasError) {
                if (snap.error is NoTokenException) {
                  return CobbleCard.inList(
                    leading: Svg('images/app_icon.svg'),
                    title: tr.settings.signInTitle,
                    onClick: () {
                      Navigator.of(context, rootNavigator: true).push(MaterialPageRoute(builder: (context) => const RebbleSetup()));
                    },
                  );
                } else {
                  return CobbleCard.inList(leading: Svg('images/app_icon.svg'), title: tr.settings.accountError);
                }
              } else if (snap.hasData) {
                final timelineTTL = Duration(minutes: snap.data!.timelineTtl);
                String ttlString;
                if (timelineTTL.inMinutes % 60 == 0) {
                  ttlString = tr.settings.timeline.subtitleEveryHours.replaceAll("\$\$hours\$\$", timelineTTL.inHours.toString());
                } else {
                  ttlString = tr.settings.timeline.subtitleEveryMinutes.replaceAll("\$\$minutes\$\$", timelineTTL.inMinutes.toString());
                }
                return CobbleCard.inList(
                  leading: Svg('images/app_icon.svg'),
                  title: tr.settings.account,
                  subtitle: snap.data!.name,
                  child: Column(
                    children: [
                      CobbleTile.info(
                        leading: RebbleIcons.dictation_microphone,
                        title: tr.settings.subscription.title,
                        subtitle: snap.data!.isSubscribed ? tr.settings.subscription.subtitleSubscribed : tr.settings.subscription.subtitleNotSubscribed,
                      ),
                      CobbleTile.info(
                        leading: RebbleIcons.timeline_pin,
                        title: tr.settings.timeline.title,
                        subtitle: ttlString,
                      ),
                    ],
                  ),
                  actions: [
                    CobbleCardAction(
                      label: tr.settings.signOut,
                      onPressed: () async {
                        (await auth).signOut();
                      },
                    ),
                    CobbleCardAction(
                      label: tr.settings.manageAccount,
                      onPressed: () async {
                        final url = Uri.parse((await webviews).manageAccount);
                        if (await canLaunchUrl(url)) {
                          await launchUrl(url, mode: LaunchMode.externalApplication);
                        }
                      },
                    ),
                  ],
                );
              } else {
                return CobbleCard.inList(leading: Svg('images/app_icon.svg'), title: tr.settings.account, child: const CircularProgressIndicator(),);
              }
            },
          ),
          CobbleTile.navigation(
            leading: RebbleIcons.notification,
            title: tr.settings.notificationsAndMuting,
            navigateTo: Notifications(),
          ),
          CobbleTile.navigation(
            leading: RebbleIcons.health_heart,
            title: tr.settings.health,
            navigateTo: Health(),
          ),
          CobbleTile.navigation(
            leading: RebbleIcons.calendar,
            title: tr.settings.calendar,
            navigateTo: Calendar(),
          ),
          // // TODO: Not yet implemented
          // CobbleTile.navigation(
          //   leading: RebbleIcons.sms_messages,
          //   title: tr.settings.messagesAndCannedReplies,
          //   navigateTo: PlaceholderScreen(),
          // ),
          // CobbleTile.navigation(
          //   leading: RebbleIcons.system_language,
          //   title: tr.settings.languageAndVoice,
          //   navigateTo: PlaceholderScreen(),
          // ),
          // CobbleTile.navigation(
          //   leading: RebbleIcons.analytics,
          //   title: tr.settings.analytics,
          //   navigateTo: PlaceholderScreen(),
          // ),
          CobbleTile.navigation(
            leading: RebbleIcons.about_app,
            title: tr.settings.aboutAndSupport,
            navigateTo: About(),
          ),
          CobbleTile.navigation(
            leading: RebbleIcons.developer_settings,
            title: tr.settings.developerOptions,
            navigateTo: DevOptionsPage(),
          ),
          if (kDebugMode)
            CobbleTile.navigation(
              leading: RebbleIcons.developer_connection_console,
              title: tr.settings.widgetLibrary,
              navigateTo: WidgetLibrary(),
            ),
        ],
      ),
    );
  }
}
