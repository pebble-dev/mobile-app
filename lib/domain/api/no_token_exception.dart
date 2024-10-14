class NoTokenException extends StateError {
  @override
  toString() => "NoTokenException: $message";

  NoTokenException(String message) : super(message);
}