# Rebble app

A multi platform watch companion app for Pebble/RebbleOS devices

# Development

## Building the app
1. Checkout this repo
2. [Generate new Github token with `read:packages` permission](https://github.com/settings/tokens). This is required to fetch libpebblecommons from Github packages repository.
3. Create `local.properties` file in `android` folder. Write following to the file:

    ```
    GITHUB_ACTOR=<YOUR GITHUB USERNAME>
    GITHUB_TOKEN=<GENERATED TOKEN>
    ```

4. [Install flutter on your machine](https://flutter.dev/docs/get-started/install)
5. [Setup flutter in the IDE of your choice](https://flutter.dev/docs/get-started/editor)
6. Open this repo in the IDE set up in step 5

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
  --dart_out lib/infrastructure/pigeons/pigeons.g.dart \
  --java_out ./android/app/src/main/java/io/rebble/cobble/pigeons/Pigeons.java \
  --java_package "io.rebble.cobble.pigeons"
```

# Architecture

See [Wiki](https://github.com/pebble-dev/mobile-app/wiki) for more info on app architecture.