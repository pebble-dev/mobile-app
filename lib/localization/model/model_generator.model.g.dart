// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'model_generator.model.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Language _$LanguageFromJson(Map<String, dynamic> json) {
  $checkKeys(
    json,
    allowedKeys: const [
      'common',
      'first_run',
      'timeline_attribute',
      'timeline_sync',
      'recurrence',
      'splash_page',
      'home_page',
      'about_page',
      'watches_page',
      'alerting_apps',
      'alerting_apps_filter',
      'more_setup_page',
      'pair_page',
      'setup',
      'health',
      'notifications',
      'settings',
      'system_apps',
      'calendar',
      'locker_page'
    ],
    requiredKeys: const [
      'common',
      'first_run',
      'timeline_attribute',
      'timeline_sync',
      'recurrence',
      'splash_page',
      'home_page',
      'about_page',
      'watches_page',
      'alerting_apps',
      'alerting_apps_filter',
      'more_setup_page',
      'pair_page',
      'setup',
      'health',
      'notifications',
      'settings',
      'system_apps',
      'calendar',
      'locker_page'
    ],
    disallowNullValues: const [
      'common',
      'first_run',
      'timeline_attribute',
      'timeline_sync',
      'recurrence',
      'splash_page',
      'home_page',
      'about_page',
      'watches_page',
      'alerting_apps',
      'alerting_apps_filter',
      'more_setup_page',
      'pair_page',
      'setup',
      'health',
      'notifications',
      'settings',
      'system_apps',
      'calendar',
      'locker_page'
    ],
  );
  return Language(
    LanguageCommon.fromJson(json['common'] as Map<String, dynamic>),
    LanguageFirstRun.fromJson(json['first_run'] as Map<String, dynamic>),
    LanguageTimelineAttribute.fromJson(
        json['timeline_attribute'] as Map<String, dynamic>),
    LanguageTimelineSync.fromJson(
        json['timeline_sync'] as Map<String, dynamic>),
    LanguageRecurrence.fromJson(json['recurrence'] as Map<String, dynamic>),
    LanguageSplashPage.fromJson(json['splash_page'] as Map<String, dynamic>),
    LanguageHomePage.fromJson(json['home_page'] as Map<String, dynamic>),
    LanguageAboutPage.fromJson(json['about_page'] as Map<String, dynamic>),
    LanguageWatchesPage.fromJson(json['watches_page'] as Map<String, dynamic>),
    LanguageAlertingApps.fromJson(
        json['alerting_apps'] as Map<String, dynamic>),
    LanguageAlertingAppsFilter.fromJson(
        json['alerting_apps_filter'] as Map<String, dynamic>),
    LanguageMoreSetupPage.fromJson(
        json['more_setup_page'] as Map<String, dynamic>),
    LanguagePairPage.fromJson(json['pair_page'] as Map<String, dynamic>),
    LanguageSetup.fromJson(json['setup'] as Map<String, dynamic>),
    LanguageHealth.fromJson(json['health'] as Map<String, dynamic>),
    LanguageNotifications.fromJson(
        json['notifications'] as Map<String, dynamic>),
    LanguageSettings.fromJson(json['settings'] as Map<String, dynamic>),
    LanguageSystemApps.fromJson(json['system_apps'] as Map<String, dynamic>),
    LanguageCalendar.fromJson(json['calendar'] as Map<String, dynamic>),
    LanguageLockerPage.fromJson(json['locker_page'] as Map<String, dynamic>),
  );
}

LanguageAboutPage _$LanguageAboutPageFromJson(Map<String, dynamic> json) {
  $checkKeys(
    json,
    allowedKeys: const [
      'title',
      'about',
      'community',
      'support',
      'help_center',
      'help_center_subtitle',
      'email_us',
      'email_us_subtitle',
      'discord_server',
      'discord_server_subtitle',
      'reddit',
      'reddit_subtitle',
      'discord',
      'discord_subtitle',
      'twitter',
      'twitter_subtitle',
      'source_code',
      'licenses',
      'version_string'
    ],
    requiredKeys: const [
      'title',
      'about',
      'community',
      'support',
      'help_center',
      'help_center_subtitle',
      'email_us',
      'email_us_subtitle',
      'discord_server',
      'discord_server_subtitle',
      'reddit',
      'reddit_subtitle',
      'discord',
      'discord_subtitle',
      'twitter',
      'twitter_subtitle',
      'source_code',
      'licenses',
      'version_string'
    ],
    disallowNullValues: const [
      'title',
      'about',
      'community',
      'support',
      'help_center',
      'help_center_subtitle',
      'email_us',
      'email_us_subtitle',
      'discord_server',
      'discord_server_subtitle',
      'reddit',
      'reddit_subtitle',
      'discord',
      'discord_subtitle',
      'twitter',
      'twitter_subtitle',
      'source_code',
      'licenses',
      'version_string'
    ],
  );
  return LanguageAboutPage(
    json['title'] as String,
    json['about'] as String,
    json['community'] as String,
    json['support'] as String,
    json['help_center'] as String,
    json['help_center_subtitle'] as String,
    json['email_us'] as String,
    json['email_us_subtitle'] as String,
    json['discord_server'] as String,
    json['discord_server_subtitle'] as String,
    json['reddit'] as String,
    json['reddit_subtitle'] as String,
    json['discord'] as String,
    json['discord_subtitle'] as String,
    json['twitter'] as String,
    json['twitter_subtitle'] as String,
    json['source_code'] as String,
    json['licenses'] as String,
    json['version_string'] as String,
  );
}

LanguageAlertingApps _$LanguageAlertingAppsFromJson(Map<String, dynamic> json) {
  $checkKeys(
    json,
    allowedKeys: const ['title', 'subtitle', 'muted_today', 'alerted_today'],
    requiredKeys: const ['title', 'subtitle', 'muted_today', 'alerted_today'],
    disallowNullValues: const [
      'title',
      'subtitle',
      'muted_today',
      'alerted_today'
    ],
  );
  return LanguageAlertingApps(
    json['title'] as String,
    json['subtitle'] as String,
    json['muted_today'] as String,
    json['alerted_today'] as String,
  );
}

LanguageAlertingAppsFilter _$LanguageAlertingAppsFilterFromJson(
    Map<String, dynamic> json) {
  $checkKeys(
    json,
    allowedKeys: const ['title', 'app_name', 'app_source'],
    requiredKeys: const ['title', 'app_name', 'app_source'],
    disallowNullValues: const ['title', 'app_name', 'app_source'],
  );
  return LanguageAlertingAppsFilter(
    json['title'] as String,
    json['app_name'] as String,
    LanguageAlertingAppsFilterAppSource.fromJson(
        json['app_source'] as Map<String, dynamic>),
  );
}

LanguageAlertingAppsFilterAppSource
    _$LanguageAlertingAppsFilterAppSourceFromJson(Map<String, dynamic> json) {
  $checkKeys(
    json,
    allowedKeys: const ['all', 'phone', 'watch'],
    requiredKeys: const ['all', 'phone', 'watch'],
    disallowNullValues: const ['all', 'phone', 'watch'],
  );
  return LanguageAlertingAppsFilterAppSource(
    json['all'] as String,
    json['phone'] as String,
    json['watch'] as String,
  );
}

LanguageCalendar _$LanguageCalendarFromJson(Map<String, dynamic> json) {
  $checkKeys(
    json,
    allowedKeys: const ['title', 'toggle_title', 'toggle_subtitle', 'choose'],
    requiredKeys: const ['title', 'toggle_title', 'toggle_subtitle', 'choose'],
    disallowNullValues: const [
      'title',
      'toggle_title',
      'toggle_subtitle',
      'choose'
    ],
  );
  return LanguageCalendar(
    json['title'] as String,
    json['toggle_title'] as String,
    json['toggle_subtitle'] as String,
    json['choose'] as String,
  );
}

LanguageCommon _$LanguageCommonFromJson(Map<String, dynamic> json) {
  $checkKeys(
    json,
    allowedKeys: const ['skip', 'title', 'yes', 'no'],
    requiredKeys: const ['skip', 'title', 'yes', 'no'],
    disallowNullValues: const ['skip', 'title', 'yes', 'no'],
  );
  return LanguageCommon(
    json['skip'] as String,
    json['title'] as String,
    json['yes'] as String,
    json['no'] as String,
  );
}

LanguageFirstRun _$LanguageFirstRunFromJson(Map<String, dynamic> json) {
  $checkKeys(
    json,
    allowedKeys: const ['title', 'fab'],
    requiredKeys: const ['title', 'fab'],
    disallowNullValues: const ['title', 'fab'],
  );
  return LanguageFirstRun(
    json['title'] as String,
    json['fab'] as String,
  );
}

LanguageHealth _$LanguageHealthFromJson(Map<String, dynamic> json) {
  $checkKeys(
    json,
    allowedKeys: const [
      'title',
      'subtitle',
      'description',
      'track_me',
      'activity',
      'sleep',
      'sync',
      'database'
    ],
    requiredKeys: const [
      'title',
      'subtitle',
      'description',
      'track_me',
      'activity',
      'sleep',
      'sync',
      'database'
    ],
    disallowNullValues: const [
      'title',
      'subtitle',
      'description',
      'track_me',
      'activity',
      'sleep',
      'sync',
      'database'
    ],
  );
  return LanguageHealth(
    json['title'] as String,
    json['subtitle'] as String,
    json['description'] as String,
    json['track_me'] as String,
    LanguageHealthActivity.fromJson(json['activity'] as Map<String, dynamic>),
    LanguageHealthSleep.fromJson(json['sleep'] as Map<String, dynamic>),
    LanguageHealthSync.fromJson(json['sync'] as Map<String, dynamic>),
    LanguageHealthDatabase.fromJson(json['database'] as Map<String, dynamic>),
  );
}

LanguageHealthActivity _$LanguageHealthActivityFromJson(
    Map<String, dynamic> json) {
  $checkKeys(
    json,
    allowedKeys: const ['title', 'subtitle'],
    requiredKeys: const ['title', 'subtitle'],
    disallowNullValues: const ['title', 'subtitle'],
  );
  return LanguageHealthActivity(
    json['title'] as String,
    json['subtitle'] as String,
  );
}

LanguageHealthDatabase _$LanguageHealthDatabaseFromJson(
    Map<String, dynamic> json) {
  $checkKeys(
    json,
    allowedKeys: const [
      'title',
      'manage',
      'delete',
      'backup',
      'restore',
      'perm_delete',
      'permanently_delete'
    ],
    requiredKeys: const [
      'title',
      'manage',
      'delete',
      'backup',
      'restore',
      'perm_delete',
      'permanently_delete'
    ],
    disallowNullValues: const [
      'title',
      'manage',
      'delete',
      'backup',
      'restore',
      'perm_delete',
      'permanently_delete'
    ],
  );
  return LanguageHealthDatabase(
    json['title'] as String,
    json['manage'] as String,
    json['delete'] as String,
    json['backup'] as String,
    json['restore'] as String,
    json['perm_delete'] as String,
    LanguageHealthDatabasePermanentlyDelete.fromJson(
        json['permanently_delete'] as Map<String, dynamic>),
  );
}

LanguageHealthDatabasePermanentlyDelete
    _$LanguageHealthDatabasePermanentlyDeleteFromJson(
        Map<String, dynamic> json) {
  $checkKeys(
    json,
    allowedKeys: const ['title', 'description', 'positive', 'negative'],
    requiredKeys: const ['title', 'description', 'positive', 'negative'],
    disallowNullValues: const ['title', 'description', 'positive', 'negative'],
  );
  return LanguageHealthDatabasePermanentlyDelete(
    json['title'] as String,
    json['description'] as String,
    json['positive'] as String,
    json['negative'] as String,
  );
}

LanguageHealthSleep _$LanguageHealthSleepFromJson(Map<String, dynamic> json) {
  $checkKeys(
    json,
    allowedKeys: const ['title', 'subtitle'],
    requiredKeys: const ['title', 'subtitle'],
    disallowNullValues: const ['title', 'subtitle'],
  );
  return LanguageHealthSleep(
    json['title'] as String,
    json['subtitle'] as String,
  );
}

LanguageHealthSync _$LanguageHealthSyncFromJson(Map<String, dynamic> json) {
  $checkKeys(
    json,
    allowedKeys: const ['title', 'subtitle', 'sign_out', 'switch_account'],
    requiredKeys: const ['title', 'subtitle', 'sign_out', 'switch_account'],
    disallowNullValues: const [
      'title',
      'subtitle',
      'sign_out',
      'switch_account'
    ],
  );
  return LanguageHealthSync(
    json['title'] as String,
    json['subtitle'] as String,
    json['sign_out'] as String,
    json['switch_account'] as String,
  );
}

LanguageHomePage _$LanguageHomePageFromJson(Map<String, dynamic> json) {
  $checkKeys(
    json,
    allowedKeys: const [
      'testing',
      'health',
      'locker',
      'store',
      'watches',
      'settings'
    ],
    requiredKeys: const [
      'testing',
      'health',
      'locker',
      'store',
      'watches',
      'settings'
    ],
    disallowNullValues: const [
      'testing',
      'health',
      'locker',
      'store',
      'watches',
      'settings'
    ],
  );
  return LanguageHomePage(
    json['testing'] as String,
    json['health'] as String,
    json['locker'] as String,
    json['store'] as String,
    json['watches'] as String,
    json['settings'] as String,
  );
}

LanguageLockerPage _$LanguageLockerPageFromJson(Map<String, dynamic> json) {
  $checkKeys(
    json,
    allowedKeys: const [
      'apply',
      'permissions',
      'face_settings',
      'app_settings',
      'not_compatible',
      'delete',
      'my_faces',
      'my_apps',
      'get_faces',
      'get_apps',
      'incompatible_faces',
      'incompatible_apps'
    ],
    requiredKeys: const [
      'apply',
      'permissions',
      'face_settings',
      'app_settings',
      'not_compatible',
      'delete',
      'my_faces',
      'my_apps',
      'get_faces',
      'get_apps',
      'incompatible_faces',
      'incompatible_apps'
    ],
    disallowNullValues: const [
      'apply',
      'permissions',
      'face_settings',
      'app_settings',
      'not_compatible',
      'delete',
      'my_faces',
      'my_apps',
      'get_faces',
      'get_apps',
      'incompatible_faces',
      'incompatible_apps'
    ],
  );
  return LanguageLockerPage(
    json['apply'] as String,
    json['permissions'] as String,
    json['face_settings'] as String,
    json['app_settings'] as String,
    json['not_compatible'] as String,
    json['delete'] as String,
    json['my_faces'] as String,
    json['my_apps'] as String,
    json['get_faces'] as String,
    json['get_apps'] as String,
    json['incompatible_faces'] as String,
    json['incompatible_apps'] as String,
  );
}

LanguageMoreSetupPage _$LanguageMoreSetupPageFromJson(
    Map<String, dynamic> json) {
  $checkKeys(
    json,
    allowedKeys: const ['title', 'fab', 'content'],
    requiredKeys: const ['title', 'fab', 'content'],
    disallowNullValues: const ['title', 'fab', 'content'],
  );
  return LanguageMoreSetupPage(
    json['title'] as String,
    json['fab'] as String,
    json['content'] as String,
  );
}

LanguageNotifications _$LanguageNotificationsFromJson(
    Map<String, dynamic> json) {
  $checkKeys(
    json,
    allowedKeys: const ['title', 'enabled', 'choose_apps', 'silence'],
    requiredKeys: const ['title', 'enabled', 'choose_apps', 'silence'],
    disallowNullValues: const ['title', 'enabled', 'choose_apps', 'silence'],
  );
  return LanguageNotifications(
    json['title'] as String,
    json['enabled'] as String,
    json['choose_apps'] as String,
    LanguageNotificationsSilence.fromJson(
        json['silence'] as Map<String, dynamic>),
  );
}

LanguageNotificationsSilence _$LanguageNotificationsSilenceFromJson(
    Map<String, dynamic> json) {
  $checkKeys(
    json,
    allowedKeys: const ['title', 'description', 'notifications', 'calls'],
    requiredKeys: const ['title', 'description', 'notifications', 'calls'],
    disallowNullValues: const [
      'title',
      'description',
      'notifications',
      'calls'
    ],
  );
  return LanguageNotificationsSilence(
    json['title'] as String,
    json['description'] as String,
    json['notifications'] as String,
    json['calls'] as String,
  );
}

LanguagePairPage _$LanguagePairPageFromJson(Map<String, dynamic> json) {
  $checkKeys(
    json,
    allowedKeys: const ['title', 'search_again', 'status'],
    requiredKeys: const ['title', 'search_again', 'status'],
    disallowNullValues: const ['title', 'search_again', 'status'],
  );
  return LanguagePairPage(
    json['title'] as String,
    LanguagePairPageSearchAgain.fromJson(
        json['search_again'] as Map<String, dynamic>),
    LanguagePairPageStatus.fromJson(json['status'] as Map<String, dynamic>),
  );
}

LanguagePairPageSearchAgain _$LanguagePairPageSearchAgainFromJson(
    Map<String, dynamic> json) {
  $checkKeys(
    json,
    allowedKeys: const ['ble', 'classic'],
    requiredKeys: const ['ble', 'classic'],
    disallowNullValues: const ['ble', 'classic'],
  );
  return LanguagePairPageSearchAgain(
    json['ble'] as String,
    json['classic'] as String,
  );
}

LanguagePairPageStatus _$LanguagePairPageStatusFromJson(
    Map<String, dynamic> json) {
  $checkKeys(
    json,
    allowedKeys: const ['recovery', 'new_device'],
    requiredKeys: const ['recovery', 'new_device'],
    disallowNullValues: const ['recovery', 'new_device'],
  );
  return LanguagePairPageStatus(
    json['recovery'] as String,
    json['new_device'] as String,
  );
}

LanguageRecurrence _$LanguageRecurrenceFromJson(Map<String, dynamic> json) {
  $checkKeys(
    json,
    allowedKeys: const ['unknown', 'daily', 'weekly', 'monthly', 'yearly'],
    requiredKeys: const ['unknown', 'daily', 'weekly', 'monthly', 'yearly'],
    disallowNullValues: const [
      'unknown',
      'daily',
      'weekly',
      'monthly',
      'yearly'
    ],
  );
  return LanguageRecurrence(
    json['unknown'] as String,
    json['daily'] as String,
    json['weekly'] as String,
    json['monthly'] as String,
    json['yearly'] as String,
  );
}

LanguageSettings _$LanguageSettingsFromJson(Map<String, dynamic> json) {
  $checkKeys(
    json,
    allowedKeys: const [
      'title',
      'account',
      'subscription',
      'timeline',
      'sign_out',
      'manage_account',
      'notifications_and_muting',
      'health',
      'calendar',
      'messages_and_canned_replies',
      'language_and_voice',
      'analytics',
      'about_and_support',
      'developer_options',
      'widget_library'
    ],
    requiredKeys: const [
      'title',
      'account',
      'subscription',
      'timeline',
      'sign_out',
      'manage_account',
      'notifications_and_muting',
      'health',
      'calendar',
      'messages_and_canned_replies',
      'language_and_voice',
      'analytics',
      'about_and_support',
      'developer_options',
      'widget_library'
    ],
    disallowNullValues: const [
      'title',
      'account',
      'subscription',
      'timeline',
      'sign_out',
      'manage_account',
      'notifications_and_muting',
      'health',
      'calendar',
      'messages_and_canned_replies',
      'language_and_voice',
      'analytics',
      'about_and_support',
      'developer_options',
      'widget_library'
    ],
  );
  return LanguageSettings(
    json['title'] as String,
    json['account'] as String,
    LanguageSettingsSubscription.fromJson(
        json['subscription'] as Map<String, dynamic>),
    LanguageSettingsTimeline.fromJson(json['timeline'] as Map<String, dynamic>),
    json['sign_out'] as String,
    json['manage_account'] as String,
    json['notifications_and_muting'] as String,
    json['health'] as String,
    json['calendar'] as String,
    json['messages_and_canned_replies'] as String,
    json['language_and_voice'] as String,
    json['analytics'] as String,
    json['about_and_support'] as String,
    json['developer_options'] as String,
    json['widget_library'] as String,
  );
}

LanguageSettingsSubscription _$LanguageSettingsSubscriptionFromJson(
    Map<String, dynamic> json) {
  $checkKeys(
    json,
    allowedKeys: const ['title', 'subtitle'],
    requiredKeys: const ['title', 'subtitle'],
    disallowNullValues: const ['title', 'subtitle'],
  );
  return LanguageSettingsSubscription(
    json['title'] as String,
    json['subtitle'] as String,
  );
}

LanguageSettingsTimeline _$LanguageSettingsTimelineFromJson(
    Map<String, dynamic> json) {
  $checkKeys(
    json,
    allowedKeys: const ['title', 'subtitle'],
    requiredKeys: const ['title', 'subtitle'],
    disallowNullValues: const ['title', 'subtitle'],
  );
  return LanguageSettingsTimeline(
    json['title'] as String,
    json['subtitle'] as String,
  );
}

LanguageSetup _$LanguageSetupFromJson(Map<String, dynamic> json) {
  $checkKeys(
    json,
    allowedKeys: const ['success'],
    requiredKeys: const ['success'],
    disallowNullValues: const ['success'],
  );
  return LanguageSetup(
    LanguageSetupSuccess.fromJson(json['success'] as Map<String, dynamic>),
  );
}

LanguageSetupSuccess _$LanguageSetupSuccessFromJson(Map<String, dynamic> json) {
  $checkKeys(
    json,
    allowedKeys: const ['title', 'subtitle', 'welcome', 'fab'],
    requiredKeys: const ['title', 'subtitle', 'welcome', 'fab'],
    disallowNullValues: const ['title', 'subtitle', 'welcome', 'fab'],
  );
  return LanguageSetupSuccess(
    json['title'] as String,
    json['subtitle'] as String,
    json['welcome'] as String,
    json['fab'] as String,
  );
}

LanguageSplashPage _$LanguageSplashPageFromJson(Map<String, dynamic> json) {
  $checkKeys(
    json,
    allowedKeys: const ['title', 'body'],
    requiredKeys: const ['title', 'body'],
    disallowNullValues: const ['title', 'body'],
  );
  return LanguageSplashPage(
    json['title'] as String,
    json['body'] as String,
  );
}

LanguageSystemApps _$LanguageSystemAppsFromJson(Map<String, dynamic> json) {
  $checkKeys(
    json,
    allowedKeys: const [
      'settings',
      'music',
      'notifications',
      'alarms',
      'watchfaces'
    ],
    requiredKeys: const [
      'settings',
      'music',
      'notifications',
      'alarms',
      'watchfaces'
    ],
    disallowNullValues: const [
      'settings',
      'music',
      'notifications',
      'alarms',
      'watchfaces'
    ],
  );
  return LanguageSystemApps(
    json['settings'] as String,
    json['music'] as String,
    json['notifications'] as String,
    json['alarms'] as String,
    json['watchfaces'] as String,
  );
}

LanguageTimelineAttribute _$LanguageTimelineAttributeFromJson(
    Map<String, dynamic> json) {
  $checkKeys(
    json,
    allowedKeys: const ['heading', 'subtitle', 'title', 'paragraph'],
    requiredKeys: const ['heading', 'subtitle', 'title', 'paragraph'],
    disallowNullValues: const ['heading', 'subtitle', 'title', 'paragraph'],
  );
  return LanguageTimelineAttribute(
    LanguageTimelineAttributeHeading.fromJson(
        json['heading'] as Map<String, dynamic>),
    LanguageTimelineAttributeSubtitle.fromJson(
        json['subtitle'] as Map<String, dynamic>),
    LanguageTimelineAttributeTitle.fromJson(
        json['title'] as Map<String, dynamic>),
    LanguageTimelineAttributeParagraph.fromJson(
        json['paragraph'] as Map<String, dynamic>),
  );
}

LanguageTimelineAttributeHeading _$LanguageTimelineAttributeHeadingFromJson(
    Map<String, dynamic> json) {
  $checkKeys(
    json,
    allowedKeys: const ['attendees', 'status', 'recurrence', 'calendar'],
    requiredKeys: const ['attendees', 'status', 'recurrence', 'calendar'],
    disallowNullValues: const ['attendees', 'status', 'recurrence', 'calendar'],
  );
  return LanguageTimelineAttributeHeading(
    json['attendees'] as String,
    json['status'] as String,
    json['recurrence'] as String,
    json['calendar'] as String,
  );
}

LanguageTimelineAttributeParagraph _$LanguageTimelineAttributeParagraphFromJson(
    Map<String, dynamic> json) {
  $checkKeys(
    json,
    allowedKeys: const ['accepted', 'maybe', 'declined'],
    requiredKeys: const ['accepted', 'maybe', 'declined'],
    disallowNullValues: const ['accepted', 'maybe', 'declined'],
  );
  return LanguageTimelineAttributeParagraph(
    json['accepted'] as String,
    json['maybe'] as String,
    json['declined'] as String,
  );
}

LanguageTimelineAttributeSubtitle _$LanguageTimelineAttributeSubtitleFromJson(
    Map<String, dynamic> json) {
  $checkKeys(
    json,
    allowedKeys: const [
      'removed',
      'calendar_muted',
      'accepted',
      'maybe',
      'declined'
    ],
    requiredKeys: const [
      'removed',
      'calendar_muted',
      'accepted',
      'maybe',
      'declined'
    ],
    disallowNullValues: const [
      'removed',
      'calendar_muted',
      'accepted',
      'maybe',
      'declined'
    ],
  );
  return LanguageTimelineAttributeSubtitle(
    json['removed'] as String,
    json['calendar_muted'] as String,
    json['accepted'] as String,
    json['maybe'] as String,
    json['declined'] as String,
  );
}

LanguageTimelineAttributeTitle _$LanguageTimelineAttributeTitleFromJson(
    Map<String, dynamic> json) {
  $checkKeys(
    json,
    allowedKeys: const [
      'accept',
      'maybe',
      'decline',
      'remove',
      'mute_calendar'
    ],
    requiredKeys: const [
      'accept',
      'maybe',
      'decline',
      'remove',
      'mute_calendar'
    ],
    disallowNullValues: const [
      'accept',
      'maybe',
      'decline',
      'remove',
      'mute_calendar'
    ],
  );
  return LanguageTimelineAttributeTitle(
    json['accept'] as String,
    json['maybe'] as String,
    json['decline'] as String,
    json['remove'] as String,
    json['mute_calendar'] as String,
  );
}

LanguageTimelineSync _$LanguageTimelineSyncFromJson(Map<String, dynamic> json) {
  $checkKeys(
    json,
    allowedKeys: const ['watch_full'],
    requiredKeys: const ['watch_full'],
    disallowNullValues: const ['watch_full'],
  );
  return LanguageTimelineSync(
    LanguageTimelineSyncWatchFull.fromJson(
        json['watch_full'] as Map<String, dynamic>),
  );
}

LanguageTimelineSyncWatchFull _$LanguageTimelineSyncWatchFullFromJson(
    Map<String, dynamic> json) {
  $checkKeys(
    json,
    allowedKeys: const ['p0', 'p1'],
    requiredKeys: const ['p0', 'p1'],
    disallowNullValues: const ['p0', 'p1'],
  );
  return LanguageTimelineSyncWatchFull(
    json['p0'] as String,
    json['p1'] as String,
  );
}

LanguageWatchesPage _$LanguageWatchesPageFromJson(Map<String, dynamic> json) {
  $checkKeys(
    json,
    allowedKeys: const ['title', 'status', 'action', 'all_watches', 'fab'],
    requiredKeys: const ['title', 'status', 'action', 'all_watches', 'fab'],
    disallowNullValues: const [
      'title',
      'status',
      'action',
      'all_watches',
      'fab'
    ],
  );
  return LanguageWatchesPage(
    json['title'] as String,
    LanguageWatchesPageStatus.fromJson(json['status'] as Map<String, dynamic>),
    LanguageWatchesPageAction.fromJson(json['action'] as Map<String, dynamic>),
    json['all_watches'] as String,
    json['fab'] as String,
  );
}

LanguageWatchesPageAction _$LanguageWatchesPageActionFromJson(
    Map<String, dynamic> json) {
  $checkKeys(
    json,
    allowedKeys: const ['connect', 'disconnect', 'check_updates', 'forget'],
    requiredKeys: const ['connect', 'disconnect', 'check_updates', 'forget'],
    disallowNullValues: const [
      'connect',
      'disconnect',
      'check_updates',
      'forget'
    ],
  );
  return LanguageWatchesPageAction(
    json['connect'] as String,
    json['disconnect'] as String,
    json['check_updates'] as String,
    json['forget'] as String,
  );
}

LanguageWatchesPageStatus _$LanguageWatchesPageStatusFromJson(
    Map<String, dynamic> json) {
  $checkKeys(
    json,
    allowedKeys: const [
      'nothing_connected',
      'connected',
      'connecting',
      'disconnected',
      'background_service_stopped'
    ],
    requiredKeys: const [
      'nothing_connected',
      'connected',
      'connecting',
      'disconnected',
      'background_service_stopped'
    ],
    disallowNullValues: const [
      'nothing_connected',
      'connected',
      'connecting',
      'disconnected',
      'background_service_stopped'
    ],
  );
  return LanguageWatchesPageStatus(
    json['nothing_connected'] as String,
    json['connected'] as String,
    json['connecting'] as String,
    json['disconnected'] as String,
    json['background_service_stopped'] as String,
  );
}
