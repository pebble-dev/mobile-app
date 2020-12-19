extension StringExtensions on String {
  String trimWithEllipsis(int maxLength) {
    if (length <= maxLength) {
      return this;
    }

    return this.substring(0, maxLength - 1) + "…";
  }
}
