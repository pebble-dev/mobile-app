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

## Using Cobble theming

App's components are styled through modified Material theme, in theory you should never specify
custom styles in your own component. If you have to, try to use colors that are defined in 
`ThemeData` (accessed by `WithCobbleTheme(context).theme`) or alternatively in 
`CobbleSchemeData` (`WithCobbleTheme(context).scheme`). Scheme is collection of colors, 
created by designer while the theme is higher-level grouping of these colours to provide meaningful 
base styles for components. If you start using Material component which isn't styled properly, 
take a look at Material theme and see if you can set styles there before setting styles directly on
component. There is limited set of text types, as defined by designer, if you need different text 
style, extends these types with `.copyWith` instead of creating your own.
