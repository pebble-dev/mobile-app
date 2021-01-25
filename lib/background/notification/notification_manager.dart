
import 'dart:convert';
import 'dart:io';

import 'package:cobble/background/actions/master_action_handler.dart';
import 'package:cobble/domain/db/dao/active_notification_dao.dart';
import 'package:cobble/domain/db/models/active_notification.dart';
import 'package:cobble/domain/db/models/timeline_pin.dart';
import 'package:cobble/domain/db/models/timeline_pin_layout.dart';
import 'package:cobble/domain/db/models/timeline_pin_type.dart';
import 'package:cobble/domain/notification/notification_action.dart';
import 'package:cobble/domain/notification/notification_message.dart';
import 'package:cobble/domain/preferences.dart';
import 'package:cobble/domain/timeline/timeline_action.dart';
import 'package:cobble/domain/timeline/timeline_action_response.dart';
import 'package:cobble/domain/timeline/timeline_attribute.dart';
import 'package:cobble/domain/timeline/timeline_icon.dart';
import 'package:cobble/domain/timeline/timeline_serializer.dart';
import 'package:cobble/infrastructure/pigeons/pigeons.g.dart';
import 'package:hooks_riverpod/all.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:uuid_type/uuid_type.dart';

final Uuid notificationsWatchappId = Uuid("B2CAE818-10F8-46DF-AD2B-98AD2254A3C1");

class NotificationManager implements ActionHandler{
  final NotificationUtils _notificationUtils = NotificationUtils();
  final ActiveNotificationDao _activeNotificationDao;
  final Future<SharedPreferences> _preferencesFuture;

  NotificationManager(this._activeNotificationDao, this._preferencesFuture);

  Future<TimelineIcon> _determineIcon(String packageId) async {
    TimelineIcon icon = TimelineIcon.notificationGeneric;
    List<String> mailPackages = (await _notificationUtils.getMailPackages()).value;
    List<String> smsPackages = (await _notificationUtils.getSMSPackages()).value;
    if (mailPackages.contains(packageId)) {
      icon = TimelineIcon.genericEmail;
    }else if (smsPackages.contains(packageId)) {
      icon = TimelineIcon.genericSms;
    }
    if (Platform.isAndroid) {
      switch (packageId) {
        case "com.google.android.gm.lite":
        case "com.google.android.gm":
          icon = TimelineIcon.notificationGmail;
          break;
        case "com.microsoft.office.outlook":
          icon = TimelineIcon.notificationOutlook;
          break;
        case "com.Slack":
          icon = TimelineIcon.notificationSlack;
          break;
        case "com.snapchat.android":
          icon = TimelineIcon.notificationSnapchat;
          break;
        case "com.twitter.android":
        case "com.twitter.android.lite":
          icon = TimelineIcon.notificationTwitter;
          break;
        case "org.telegram.messenger":
          icon = TimelineIcon.notificationTelegram;
          break;
        case "com.facebook.katana":
        case "com.facebook.lite":
          icon = TimelineIcon.notificationFacebook;
          break;
        case "com.facebook.orca":
          icon = TimelineIcon.notificationFacebookMessenger;
          break;
        case "com.whatsapp":
        case "com.whatsapp.w4b":
          icon = TimelineIcon.notificationWhatsapp;
          break;
      }
    }
    return icon;
  }

  Future<TimelinePin> handleNotification(NotificationPigeon notif) async {
    ActiveNotification old = await _activeNotificationDao.getActiveNotifByNotifMeta(notif.notifId, notif.packageId, notif.tagId);
    Uuid itemId;
    if (old != null && old.pinId != null) {
      itemId = old.pinId;
    }else {
      itemId = Uuid(uuid.v4());
    }
    TimelineAttribute icon = TimelineAttribute.icon(await _determineIcon(notif.packageId));
    TimelineAttribute sender = TimelineAttribute.sender(notif.title);
    TimelineAttribute subject = TimelineAttribute.subtitle(notif.text);
    TimelineAttribute content;
    if (notif.messagesJson.isEmpty) {
      content = TimelineAttribute.body("");
    }else {
      List<Map<String, dynamic>> messages = jsonDecode(notif.messagesJson);
      String contentText = "";
      messages.forEach((el) {
        NotificationMessage message = NotificationMessage.fromJson(el);
        contentText += message.sender + ": ";
        contentText += message.text;
      });
      content = TimelineAttribute.body(contentText);
    }
    List<TimelineAction> actions;
    actions.add(TimelineAction(MetaAction.DISMISS.index, actionTypeDismiss, [
      TimelineAttribute.title("Dismiss")
    ]));

    List<String> disabledActionPkgs = (await _preferencesFuture).getStringList(disabledActionPackagesKey);
    if (disabledActionPkgs == null || !disabledActionPkgs.contains(notif.packageId)) {
      List<Map<String, dynamic>> notifActions = jsonDecode(notif.actionsJson);
      if (notifActions != null) {
        for (int i=0; i<notifActions.length; i++) {
          NotificationAction action = NotificationAction.fromJson(notifActions[i]);
          actions.add(TimelineAction((MetaAction.values.length-1)+i, actionTypeGeneric, [
            TimelineAttribute.title(action.title)
          ]));
        }
      }
    }

    actions.add(TimelineAction(MetaAction.OPEN.index, actionTypeGeneric, [
      TimelineAttribute.title("Open on phone")
    ]));
    actions.add(TimelineAction(MetaAction.MUTE_PKG.index, actionTypeGeneric, [
      TimelineAttribute.title("Mute app")
    ]));
    if (notif.tagId != null) {
      actions.add(TimelineAction(MetaAction.MUTE_TAG.index, actionTypeGeneric, [
        TimelineAttribute.title("Mute tag '${notif.tagName}'")
      ]));
    }

    return TimelinePin(
      itemId: itemId,
      parentId: notificationsWatchappId,
      timestamp: DateTime.now(),
      duration: 0,
      type: TimelinePinType.notification,
      layout: TimelinePinLayout.genericNotification,
      attributesJson: serializeAttributesToJson([icon, sender, subject, content]),
      actionsJson: serializeActionsToJson(actions),

      isAllDay: false,
      isVisible: true,
      isFloating: false,
      persistQuickView: false
    );
  }

  @override
  Future<TimelineActionResponse> handleTimelineAction(TimelinePin pin, ActionTrigger trigger) async {
    switch (trigger.actionId) {
      case 0: // DISMISS
        BooleanWrapper res = await _notificationUtils.dismissNotification(StringWrapper()..value=pin.itemId.toString());
        if (res.value) {
          return TimelineActionResponse(true, attributes: [
            TimelineAttribute.subtitle("Dismissed"),
            TimelineAttribute.largeIcon(TimelineIcon.resultDismissed)
          ]);
        }
        break;
      case 1: // OPEN
        break;
      case 2: // MUTE_PKG
        break;
      case 3: // MUTE_TAG
        break;
      default: // Custom
        break;
    }
  }

  void dismissNotification(Uuid itemId) {
    _notificationUtils.dismissNotification(StringWrapper()..value=itemId.toString());
    _activeNotificationDao.delete(itemId);
  }
}

final notificationManagerProvider = Provider((ref) => NotificationManager(ref.read(activeNotifDaoProvider), ref.read(sharedPreferencesProvider)));

final disabledActionPackagesKey = "disabledActionPackages";

enum MetaAction {
  DISMISS,
  OPEN,
  MUTE_PKG,
  MUTE_TAG
}