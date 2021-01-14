enum TimelineIcon {
  notificationReminder,
  hockeyGame,
  payBill,
  notificationLinkedIn,
  notificationGoogleInbox,
  genericQuestion,
  notificationFlag,
  genericSms,
  watchDisconnected,
  tvShow,
  basketball,
  genericWarning,
  lightRain,
  notificationFacebook,
  incomingPhoneCall,
  notificationGoogleMessenger,
  notificationTelegram,
  notificationFacetime,
  arrowDown,
  notificationOutlook,
  noEvents,
  audioCassette,
  sunset,
  notificationTwitter,
  sunrise,
  heavyRain,
  notificationMailbox,
  americanFootball,
  carRental,
  cricketGame,
  notificationWeChat,
  notificationGeneric,
  notificationSkype,
  cloudyDay,
  duringPhoneCallCentered,
  notificationLine,
  hotelReservation,
  notificationFacebookMessenger,
  notificationLighthouse,
  timelineEmptyCalendar,
  notificationIosPhotos,
  resultDeleted,
  notificationGmail,
  timelineMissedCall,
  sleep,
  resultMute,
  notificationAmazon,
  thumbsUp,
  scheduledFlight,
  settings,
  partlyCloudy,
  stocksEvent,
  notificationGoogleMaps,
  rewardGood,
  notificationYahooMail,
  birthdayEvent,
  genericEmail,
  resultDismissed,
  notificationGooglePhotos,
  tideIsHigh,
  notificationViber,
  lightSnow,
  newsEvent,
  genericConfirmation,
  timelineSports,
  notificationSlack,
  checkInternetConnection,
  activity,
  notificationHipChat,
  notificationInstagram,
  timelineBaseball,
  rewardBad,
  reachedFitnessGoal,
  daySeparator,
  timelineCalendar,
  rainingAndSnowing,
  radioShow,
  dismissedPhoneCall,
  arrowUp,
  rewardAverage,
  musicEvent,
  notificationSnapchat,
  notificationBlackberryMessenger,
  notificationWhatsapp,
  location,
  soccerGame,
  resultFailed,
  resultUnmute,
  scheduledEvent,
  timelineWeather,
  timelineSun,
  notificationGoogleHangouts,
  duringPhoneCall,
  notificationKik,
  resultUnmuteAlt,
  movieEvent,
  glucoseMonitor,
  resultSent,
  alarmClock,
  heavySnow,
  dinnerReservation,
  notificationKakaoTalk,
}

extension ProtocolExtension on TimelineIcon {
  int toProtocolNumber() {
    switch (this) {
      case TimelineIcon.notificationReminder:
        return 3;
      case TimelineIcon.hockeyGame:
        return 30;
      case TimelineIcon.payBill:
        return 38;
      case TimelineIcon.notificationLinkedIn:
        return 115;
      case TimelineIcon.notificationGoogleInbox:
        return 61;
      case TimelineIcon.genericQuestion:
        return 63;
      case TimelineIcon.notificationFlag:
        return 4;
      case TimelineIcon.genericSms:
        return 45;
      case TimelineIcon.watchDisconnected:
        return 48;
      case TimelineIcon.tvShow:
        return 73;
      case TimelineIcon.basketball:
        return 74;
      case TimelineIcon.genericWarning:
        return 28;
      case TimelineIcon.lightRain:
        return 32;
      case TimelineIcon.notificationFacebook:
        return 11;
      case TimelineIcon.incomingPhoneCall:
        return 78;
      case TimelineIcon.notificationGoogleMessenger:
        return 76;
      case TimelineIcon.notificationTelegram:
        return 7;
      case TimelineIcon.notificationFacetime:
        return 110;
      case TimelineIcon.arrowDown:
        return 99;
      case TimelineIcon.notificationOutlook:
        return 64;
      case TimelineIcon.noEvents:
        return 57;
      case TimelineIcon.audioCassette:
        return 12;
      case TimelineIcon.sunset:
        return 85;
      case TimelineIcon.notificationTwitter:
        return 6;
      case TimelineIcon.sunrise:
        return 84;
      case TimelineIcon.heavyRain:
        return 52;
      case TimelineIcon.notificationMailbox:
        return 60;
      case TimelineIcon.americanFootball:
        return 20;
      case TimelineIcon.carRental:
        return 24;
      case TimelineIcon.cricketGame:
        return 26;
      case TimelineIcon.notificationWeChat:
        return 71;
      case TimelineIcon.notificationGeneric:
        return 1;
      case TimelineIcon.notificationSkype:
        return 68;
      case TimelineIcon.cloudyDay:
        return 25;
      case TimelineIcon.duringPhoneCallCentered:
        return 95;
      case TimelineIcon.notificationLine:
        return 67;
      case TimelineIcon.hotelReservation:
        return 31;
      case TimelineIcon.notificationFacebookMessenger:
        return 10;
      case TimelineIcon.notificationLighthouse:
        return 81;
      case TimelineIcon.timelineEmptyCalendar:
        return 96;
      case TimelineIcon.notificationIosPhotos:
        return 114;
      case TimelineIcon.resultDeleted:
        return 43;
      case TimelineIcon.notificationGmail:
        return 9;
      case TimelineIcon.timelineMissedCall:
        return 2;
      case TimelineIcon.sleep:
        return 101;
      case TimelineIcon.resultMute:
        return 46;
      case TimelineIcon.notificationAmazon:
        return 111;
      case TimelineIcon.thumbsUp:
        return 97;
      case TimelineIcon.scheduledFlight:
        return 54;
      case TimelineIcon.settings:
        return 83;
      case TimelineIcon.partlyCloudy:
        return 37;
      case TimelineIcon.stocksEvent:
        return 42;
      case TimelineIcon.notificationGoogleMaps:
        return 112;
      case TimelineIcon.rewardGood:
        return 103;
      case TimelineIcon.notificationYahooMail:
        return 72;
      case TimelineIcon.birthdayEvent:
        return 23;
      case TimelineIcon.genericEmail:
        return 19;
      case TimelineIcon.resultDismissed:
        return 51;
      case TimelineIcon.notificationGooglePhotos:
        return 113;
      case TimelineIcon.tideIsHigh:
        return 50;
      case TimelineIcon.notificationViber:
        return 70;
      case TimelineIcon.lightSnow:
        return 33;
      case TimelineIcon.newsEvent:
        return 36;
      case TimelineIcon.genericConfirmation:
        return 55;
      case TimelineIcon.timelineSports:
        return 17;
      case TimelineIcon.notificationSlack:
        return 116;
      case TimelineIcon.checkInternetConnection:
        return 44;
      case TimelineIcon.activity:
        return 100;
      case TimelineIcon.notificationHipChat:
        return 77;
      case TimelineIcon.notificationInstagram:
        return 59;
      case TimelineIcon.timelineBaseball:
        return 22;
      case TimelineIcon.rewardBad:
        return 102;
      case TimelineIcon.reachedFitnessGoal:
        return 66;
      case TimelineIcon.daySeparator:
        return 56;
      case TimelineIcon.timelineCalendar:
        return 21;
      case TimelineIcon.rainingAndSnowing:
        return 65;
      case TimelineIcon.radioShow:
        return 39;
      case TimelineIcon.dismissedPhoneCall:
        return 75;
      case TimelineIcon.arrowUp:
        return 98;
      case TimelineIcon.rewardAverage:
        return 104;
      case TimelineIcon.musicEvent:
        return 35;
      case TimelineIcon.notificationSnapchat:
        return 69;
      case TimelineIcon.notificationBlackberryMessenger:
        return 58;
      case TimelineIcon.notificationWhatsapp:
        return 5;
      case TimelineIcon.location:
        return 82;
      case TimelineIcon.soccerGame:
        return 41;
      case TimelineIcon.resultFailed:
        return 62;
      case TimelineIcon.resultUnmute:
        return 86;
      case TimelineIcon.scheduledEvent:
        return 40;
      case TimelineIcon.timelineWeather:
        return 14;
      case TimelineIcon.timelineSun:
        return 16;
      case TimelineIcon.notificationGoogleHangouts:
        return 8;
      case TimelineIcon.duringPhoneCall:
        return 49;
      case TimelineIcon.notificationKik:
        return 80;
      case TimelineIcon.resultUnmuteAlt:
        return 94;
      case TimelineIcon.movieEvent:
        return 34;
      case TimelineIcon.glucoseMonitor:
        return 29;
      case TimelineIcon.resultSent:
        return 47;
      case TimelineIcon.alarmClock:
        return 13;
      case TimelineIcon.heavySnow:
        return 53;
      case TimelineIcon.dinnerReservation:
        return 27;
      case TimelineIcon.notificationKakaoTalk:
        return 79;
      default:
        throw Exception("Unknown icon $this");
    }
  }
}
