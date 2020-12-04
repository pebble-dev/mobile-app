enum TimelineGameState { PRE_GAME, IN_GAME }

extension ProtocolExtension on TimelineGameState {
  int toProtocolNumber() {
    switch (this) {
      case TimelineGameState.PRE_GAME:
        return 0;
      case TimelineGameState.IN_GAME:
        return 1;
      default:
        throw Exception("Unknown game state $this");
    }
  }
}
