class SelectableCalendar {
  String name;
  String id;
  bool enabled;
  int color;

  SelectableCalendar(this.name, this.id, this.enabled, this.color);

  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      other is SelectableCalendar &&
          runtimeType == other.runtimeType &&
          name == other.name &&
          id == other.id &&
          enabled == other.enabled &&
          color == other.color;

  @override
  int get hashCode => name.hashCode ^ id.hashCode ^ enabled.hashCode ^ color.hashCode;

  @override
  String toString() {
    return 'SelectableCalendar{name: $name, id: $id, enabled: $enabled, color: $color}';
  }
}
