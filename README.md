# Rebble app

A multi platform watch companion app for Pebble/RebbleOS devices

# Development

## Building pigeons

Type safe communication between Flutter and native code is performed 
using [Pigeon](https://pub.dev/packages/pigeon). To add new communication interfaces, edit
[pigeons/pigeons.dart](pigeons/pigeons.dart) file and then re-compile interface
with the following command:

```
flutter pub run pigeon \
  --input pigeons/pigeons.dart \
  --dart_out lib/infrastructure/pigeons/pigeons.dart \
  --java_out ./android/app/src/main/java/io/rebble/fossil/pigeons/Pigeons.java \
  --java_package "io.rebble.fossil.pigeons"
```