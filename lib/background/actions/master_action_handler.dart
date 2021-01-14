import 'package:cobble/background/actions/calendar_action_handler.dart';
import 'package:cobble/domain/calendar/calendar_pin_convert.dart';
import 'package:cobble/domain/db/dao/timeline_pin_dao.dart';
import 'package:cobble/domain/db/models/timeline_pin.dart';
import 'package:cobble/domain/timeline/timeline_action_response.dart';
import 'package:cobble/infrastructure/pigeons/pigeons.g.dart';
import 'package:hooks_riverpod/all.dart';
import 'package:uuid_type/uuid_type.dart';

class MasterActionHandler {
  final TimelinePinDao _dao;
  final Map<Uuid, ActionHandler> handlers = {};

  MasterActionHandler(this._dao, CalendarActionHandler calendarActionHandler) {
    handlers[calendarWatchappId] = calendarActionHandler;
  }

  Future<TimelineActionResponse> handleTimelineAction(
    ActionTrigger trigger,
  ) async {
    final pin = await _dao.getPinById(Uuid(trigger.itemId));
    if (pin == null) {
      return TimelineActionResponse(false);
    }

    final targetHandler = handlers[pin.parentId];
    if (targetHandler == null) {
      return TimelineActionResponse(false);
    }

    return targetHandler.handleTimelineAction(pin, trigger);
  }
}

abstract class ActionHandler {
  Future<TimelineActionResponse> handleTimelineAction(TimelinePin pin,
      ActionTrigger trigger,);
}

final masterActionHandlerProvider = Provider<MasterActionHandler>((ref) {
  return MasterActionHandler(ref.read(timelinePinDaoProvider),
      ref.read(calendarActionHandlerProvider));
});
