enum TimelineIcon {
  NOTIFICATION_REMINDER,
  HOCKEY_GAME,
  PAY_BILL,
  NOTIFICATION_LINKEDIN,
  NOTIFICATION_GOOGLE_INBOX,
  GENERIC_QUESTION,
  NOTIFICATION_FLAG,
  GENERIC_SMS,
  WATCH_DISCONNECTED,
  TV_SHOW,
  BASKETBALL,
  GENERIC_WARNING,
  LIGHT_RAIN,
  NOTIFICATION_FACEBOOK,
  INCOMING_PHONE_CALL,
  NOTIFICATION_GOOGLE_MESSENGER,
  NOTIFICATION_TELEGRAM,
  NOTIFICATION_FACETIME,
  ARROW_DOWN,
  NOTIFICATION_OUTLOOK,
  NO_EVENTS,
  AUDIO_CASSETTE,
  SUNSET,
  NOTIFICATION_TWITTER,
  SUNRISE,
  HEAVY_RAIN,
  NOTIFICATION_MAILBOX,
  AMERICAN_FOOTBALL,
  CAR_RENTAL,
  CRICKET_GAME,
  NOTIFICATION_WECHAT,
  NOTIFICATION_GENERIC,
  NOTIFICATION_SKYPE,
  CLOUDY_DAY,
  DURING_PHONE_CALL_CENTERED,
  NOTIFICATION_LINE,
  HOTEL_RESERVATION,
  NOTIFICATION_FACEBOOK_MESSENGER,
  NOTIFICATION_LIGHTHOUSE,
  TIMELINE_EMPTY_CALENDAR,
  NOTIFICATION_IOS_PHOTOS,
  RESULT_DELETED,
  NOTIFICATION_GMAIL,
  TIMELINE_MISSED_CALL,
  SLEEP,
  RESULT_MUTE,
  NOTIFICATION_AMAZON,
  THUMBS_UP,
  SCHEDULED_FLIGHT,
  SETTINGS,
  PARTLY_CLOUDY,
  STOCKS_EVENT,
  NOTIFICATION_GOOGLE_MAPS,
  REWARD_GOOD,
  NOTIFICATION_YAHOO_MAIL,
  BIRTHDAY_EVENT,
  GENERIC_EMAIL,
  RESULT_DISMISSED,
  NOTIFICATION_GOOGLE_PHOTOS,
  TIDE_IS_HIGH,
  NOTIFICATION_VIBER,
  LIGHT_SNOW,
  NEWS_EVENT,
  GENERIC_CONFIRMATION,
  TIMELINE_SPORTS,
  NOTIFICATION_SLACK,
  CHECK_INTERNET_CONNECTION,
  ACTIVITY,
  NOTIFICATION_HIPCHAT,
  NOTIFICATION_INSTAGRAM,
  TIMELINE_BASEBALL,
  REWARD_BAD,
  REACHED_FITNESS_GOAL,
  DAY_SEPARATOR,
  TIMELINE_CALENDAR,
  RAINING_AND_SNOWING,
  RADIO_SHOW,
  DISMISSED_PHONE_CALL,
  ARROW_UP,
  REWARD_AVERAGE,
  MUSIC_EVENT,
  NOTIFICATION_SNAPCHAT,
  NOTIFICATION_BLACKBERRY_MESSENGER,
  NOTIFICATION_WHATSAPP,
  LOCATION,
  SOCCER_GAME,
  RESULT_FAILED,
  RESULT_UNMUTE,
  SCHEDULED_EVENT,
  TIMELINE_WEATHER,
  TIMELINE_SUN,
  NOTIFICATION_GOOGLE_HANGOUTS,
  DURING_PHONE_CALL,
  NOTIFICATION_KIK,
  RESULT_UNMUTE_ALT,
  MOVIE_EVENT,
  GLUCOSE_MONITOR,
  RESULT_SENT,
  ALARM_CLOCK,
  HEAVY_SNOW,
  DINNER_RESERVATION,
  NOTIFICATION_KAKAOTALK,
}

extension ProtocolExtension on TimelineIcon {
  int toProtocolNumber() {
    switch (this) {
      case TimelineIcon.NOTIFICATION_REMINDER:
        return 3;
      case TimelineIcon.HOCKEY_GAME:
        return 30;
      case TimelineIcon.PAY_BILL:
        return 38;
      case TimelineIcon.NOTIFICATION_LINKEDIN:
        return 115;
      case TimelineIcon.NOTIFICATION_GOOGLE_INBOX:
        return 61;
      case TimelineIcon.GENERIC_QUESTION:
        return 63;
      case TimelineIcon.NOTIFICATION_FLAG:
        return 4;
      case TimelineIcon.GENERIC_SMS:
        return 45;
      case TimelineIcon.WATCH_DISCONNECTED:
        return 48;
      case TimelineIcon.TV_SHOW:
        return 73;
      case TimelineIcon.BASKETBALL:
        return 74;
      case TimelineIcon.GENERIC_WARNING:
        return 28;
      case TimelineIcon.LIGHT_RAIN:
        return 32;
      case TimelineIcon.NOTIFICATION_FACEBOOK:
        return 11;
      case TimelineIcon.INCOMING_PHONE_CALL:
        return 78;
      case TimelineIcon.NOTIFICATION_GOOGLE_MESSENGER:
        return 76;
      case TimelineIcon.NOTIFICATION_TELEGRAM:
        return 7;
      case TimelineIcon.NOTIFICATION_FACETIME:
        return 110;
      case TimelineIcon.ARROW_DOWN:
        return 99;
      case TimelineIcon.NOTIFICATION_OUTLOOK:
        return 64;
      case TimelineIcon.NO_EVENTS:
        return 57;
      case TimelineIcon.AUDIO_CASSETTE:
        return 12;
      case TimelineIcon.SUNSET:
        return 85;
      case TimelineIcon.NOTIFICATION_TWITTER:
        return 6;
      case TimelineIcon.SUNRISE:
        return 84;
      case TimelineIcon.HEAVY_RAIN:
        return 52;
      case TimelineIcon.NOTIFICATION_MAILBOX:
        return 60;
      case TimelineIcon.AMERICAN_FOOTBALL:
        return 20;
      case TimelineIcon.CAR_RENTAL:
        return 24;
      case TimelineIcon.CRICKET_GAME:
        return 26;
      case TimelineIcon.NOTIFICATION_WECHAT:
        return 71;
      case TimelineIcon.NOTIFICATION_GENERIC:
        return 1;
      case TimelineIcon.NOTIFICATION_SKYPE:
        return 68;
      case TimelineIcon.CLOUDY_DAY:
        return 25;
      case TimelineIcon.DURING_PHONE_CALL_CENTERED:
        return 95;
      case TimelineIcon.NOTIFICATION_LINE:
        return 67;
      case TimelineIcon.HOTEL_RESERVATION:
        return 31;
      case TimelineIcon.NOTIFICATION_FACEBOOK_MESSENGER:
        return 10;
      case TimelineIcon.NOTIFICATION_LIGHTHOUSE:
        return 81;
      case TimelineIcon.TIMELINE_EMPTY_CALENDAR:
        return 96;
      case TimelineIcon.NOTIFICATION_IOS_PHOTOS:
        return 114;
      case TimelineIcon.RESULT_DELETED:
        return 43;
      case TimelineIcon.NOTIFICATION_GMAIL:
        return 9;
      case TimelineIcon.TIMELINE_MISSED_CALL:
        return 2;
      case TimelineIcon.SLEEP:
        return 101;
      case TimelineIcon.RESULT_MUTE:
        return 46;
      case TimelineIcon.NOTIFICATION_AMAZON:
        return 111;
      case TimelineIcon.THUMBS_UP:
        return 97;
      case TimelineIcon.SCHEDULED_FLIGHT:
        return 54;
      case TimelineIcon.SETTINGS:
        return 83;
      case TimelineIcon.PARTLY_CLOUDY:
        return 37;
      case TimelineIcon.STOCKS_EVENT:
        return 42;
      case TimelineIcon.NOTIFICATION_GOOGLE_MAPS:
        return 112;
      case TimelineIcon.REWARD_GOOD:
        return 103;
      case TimelineIcon.NOTIFICATION_YAHOO_MAIL:
        return 72;
      case TimelineIcon.BIRTHDAY_EVENT:
        return 23;
      case TimelineIcon.GENERIC_EMAIL:
        return 19;
      case TimelineIcon.RESULT_DISMISSED:
        return 51;
      case TimelineIcon.NOTIFICATION_GOOGLE_PHOTOS:
        return 113;
      case TimelineIcon.TIDE_IS_HIGH:
        return 50;
      case TimelineIcon.NOTIFICATION_VIBER:
        return 70;
      case TimelineIcon.LIGHT_SNOW:
        return 33;
      case TimelineIcon.NEWS_EVENT:
        return 36;
      case TimelineIcon.GENERIC_CONFIRMATION:
        return 55;
      case TimelineIcon.TIMELINE_SPORTS:
        return 17;
      case TimelineIcon.NOTIFICATION_SLACK:
        return 116;
      case TimelineIcon.CHECK_INTERNET_CONNECTION:
        return 44;
      case TimelineIcon.ACTIVITY:
        return 100;
      case TimelineIcon.NOTIFICATION_HIPCHAT:
        return 77;
      case TimelineIcon.NOTIFICATION_INSTAGRAM:
        return 59;
      case TimelineIcon.TIMELINE_BASEBALL:
        return 22;
      case TimelineIcon.REWARD_BAD:
        return 102;
      case TimelineIcon.REACHED_FITNESS_GOAL:
        return 66;
      case TimelineIcon.DAY_SEPARATOR:
        return 56;
      case TimelineIcon.TIMELINE_CALENDAR:
        return 21;
      case TimelineIcon.RAINING_AND_SNOWING:
        return 65;
      case TimelineIcon.RADIO_SHOW:
        return 39;
      case TimelineIcon.DISMISSED_PHONE_CALL:
        return 75;
      case TimelineIcon.ARROW_UP:
        return 98;
      case TimelineIcon.REWARD_AVERAGE:
        return 104;
      case TimelineIcon.MUSIC_EVENT:
        return 35;
      case TimelineIcon.NOTIFICATION_SNAPCHAT:
        return 69;
      case TimelineIcon.NOTIFICATION_BLACKBERRY_MESSENGER:
        return 58;
      case TimelineIcon.NOTIFICATION_WHATSAPP:
        return 5;
      case TimelineIcon.LOCATION:
        return 82;
      case TimelineIcon.SOCCER_GAME:
        return 41;
      case TimelineIcon.RESULT_FAILED:
        return 62;
      case TimelineIcon.RESULT_UNMUTE:
        return 86;
      case TimelineIcon.SCHEDULED_EVENT:
        return 40;
      case TimelineIcon.TIMELINE_WEATHER:
        return 14;
      case TimelineIcon.TIMELINE_SUN:
        return 16;
      case TimelineIcon.NOTIFICATION_GOOGLE_HANGOUTS:
        return 8;
      case TimelineIcon.DURING_PHONE_CALL:
        return 49;
      case TimelineIcon.NOTIFICATION_KIK:
        return 80;
      case TimelineIcon.RESULT_UNMUTE_ALT:
        return 94;
      case TimelineIcon.MOVIE_EVENT:
        return 34;
      case TimelineIcon.GLUCOSE_MONITOR:
        return 29;
      case TimelineIcon.RESULT_SENT:
        return 47;
      case TimelineIcon.ALARM_CLOCK:
        return 13;
      case TimelineIcon.HEAVY_SNOW:
        return 53;
      case TimelineIcon.DINNER_RESERVATION:
        return 27;
      case TimelineIcon.NOTIFICATION_KAKAOTALK:
        return 79;
      default:
        throw Exception("Unknown icon $this");
    }
  }
}
