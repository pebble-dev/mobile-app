class SelectableCalendar {
  String name;
  String id;
  bool enabled;

  SelectableCalendar(this.name, this.id, this.enabled);

  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      other is SelectableCalendar &&
          runtimeType == other.runtimeType &&
          name == other.name &&
          id == other.id &&
          enabled == other.enabled;

  @override
  int get hashCode => name.hashCode ^ id.hashCode ^ enabled.hashCode;

  @override
  String toString() {
    return 'SelectableCalendar{name: $name, id: $id, enabled: $enabled}';
  }
}
