enum TimelineGameState { preGame, inGame }

extension ProtocolExtension on TimelineGameState {
  int toProtocolNumber() {
    switch (this) {
      case TimelineGameState.preGame:
        return 0;
      case TimelineGameState.inGame:
        return 1;
      default:
        throw Exception("Unknown game state $this");
    }
  }
}
