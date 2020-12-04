# Rebble app

A multi platform watch companion app for Pebble/RebbleOS devices

# Development

## Building the app

1. [Install flutter on your machine](https://flutter.dev/docs/get-started/install)
2. [Setup flutter in the IDE of your choice](https://flutter.dev/docs/get-started/editor)
3. Pull this repo and open it in the IDE set up in step 2

## Building mappings

To build all the mappings in this project (such as entity <> map mapping for SQL), you have to
run the following command:

`flutter pub run build_runner build`

## Building pigeons

Type safe communication between Flutter and native code is performed 
using [Pigeon](https://pub.dev/packages/pigeon). To add new communication interfaces, edit
[pigeons/pigeons.dart](pigeons/pigeons.dart) file and then re-compile interface
with the following command:

```
flutter pub run pigeon \
  --input pigeons/pigeons.dart \
  --dart_out lib/infrastructure/pigeons/pigeons.dart \
  --java_out ./android/app/src/main/java/io/rebble/cobble/pigeons/Pigeons.java \
  --java_package "io.rebble.cobble.pigeons"
```