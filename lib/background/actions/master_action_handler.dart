import 'package:cobble/domain/db/dao/timeline_pin_dao.dart';
import 'package:cobble/domain/timeline/timeline_action_response.dart';
import 'package:cobble/infrastructure/pigeons/pigeons.dart';
import 'package:hooks_riverpod/all.dart';
import 'package:uuid_type/uuid_type.dart';

class MasterActionHandler implements ActionHandler {
  final TimelinePinDao _dao;
  final Map<Uuid, ActionHandler> handlers = {};

  MasterActionHandler(this._dao);

  @override
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

    return targetHandler.handleTimelineAction(trigger);
  }
}

abstract class ActionHandler {
  Future<TimelineActionResponse> handleTimelineAction(ActionTrigger trigger);
}

final masterActionHandlerProvider = Provider<MasterActionHandler>((ref) {
  return MasterActionHandler(
    ref.read(timelinePinDaoProvider),
  );
});
