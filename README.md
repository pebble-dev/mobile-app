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

4. Install flutter on your machine. To make builds reproducible, we use exact flutter version in pubspec.yml. Thus we recommend you use [FVM](https://fvm.app/docs/getting_started/installation) to install flutter. After you install FVM, just run `fvm install` command in the
 project folder and you will automatically get the required flutter version. 
5. [Setup flutter in the IDE of your choice](https://flutter.dev/docs/get-started/editor). Be sure to also configure it with [FVM Flutter path](https://fvm.app/docs/getting_started/configuration#ide).
6. Open this repo in the IDE set up in step 5

If you do not have an IDE, from step 5, you'll instead:

1. `fvm flutter pub get`
2. Launch an emulator: `fvm flutter emulators --launch Pixel_2_API_30`
3. `fvm flutter run`

### Host-specific instructions: Ubuntu and similar

To install FVM on Ubuntu, try something like:

1. `sudo snap install flutter --classic`
2. `flutter`
3. `flutter config --no-analytics # if you want`
4. `dart --disable-analytics # if you want`
5. `dart pub global activate fvm`
6. `export PATH="$PATH":"$HOME/.pub-cache/bin"`

If you don't have Android Studio installed, and you want an emulator, do:

1. `sudo snap install android-studio --classic` (hey, what's 900MB between friends?)
2. Launch `android-studio`.  Update everything in sight (hey, what's 400MB between friends?)
3. `sudo apt-get install qemu-kvm libvirt-daemon-system libvirt-clients bridge-utils` (hey, what's 140MB between friends?)
4. Hit 'More actions...', then 'AVD Manager'.  Then 'Create Virtual Device'. 
Then choose a device (I chose Pixel 2), then download R (hey, what's 620MB
between friends?), then hit finish.

## Building mappings

To build all the mappings in this project (such as entity <> map mapping for SQL), you have to
run the following command:

`fvm flutter pub run build_runner build --delete-conflicting-outputs`

## Building pigeons

Type safe communication between Flutter and native code is performed 
using [Pigeon](https://pub.dev/packages/pigeon). To add new communication interfaces, edit
[pigeons/pigeons.dart](pigeons/pigeons.dart) file and then re-compile interface
with the following command:

```
fvm flutter pub run pigeon \
  --input pigeons/pigeons.dart \
  --dart_out lib/infrastructure/pigeons/pigeons.g.dart \
  --java_out ./android/app/src/main/kotlin/io/rebble/cobble/pigeons/Pigeons.java \
  --java_package "io.rebble.cobble.pigeons" \
  --objc_header_out ./ios/Runner/Pigeon/Pigeons.h \
  --objc_source_out ./ios/Runner/Pigeon/Pigeons.m
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

## Using Navigator

We are using iOS-style tabbed navigation, where each tab has its own stack of screens. In practice
this means there might be multiple stacks (1 main stack and one each for tab) but only 1 stack is
active. In order to push page on an active stack import `CobbleNavigator` extension and then call
`context.push(SomeScreen())`. `SomeScreen` widget should also implement interface `CobbleScreen` and
use `CobbleScaffold.page` or `CobbleScaffold.tab`, which takes care of title and back button in 
navigation bar.

## Custom Cobble components

A lot of components were refactored in custom Widgets, like CobbleCard, CobbleTile, CobbleButton, etc.
and these components should serve you as building blocks upon which to build your UI. They are 
showcased in WidgetLibrary screen and in golden (aka snapshot) tests. All golden images (how widgets 
should look) are included in /test/components/goldens.

## Using localization

To use localized string, add it to all `.json` files in `/lang`, start build_runner to generate 
localized models (see [Building mappings](#building-mappings) above) and then use it as 
`tr.canBeNested.yourKey`. Generator also supports named  and positional parameters:  
`"key": "fixed value, named parameter -> {named}, positional parameter -> {}` and generates 
function instead of string. Use this function similar to string:  
`tr.canBeNested.yourKey('positional', named: 'named param')`.

App's localization is stored in /lang directory, one `.json` file for one language. Structure of 
these `.json` files is then converted to localized model with a help of `ModelGenerator`. Model
is in turn used to load and parse correct `.json` file at app's startup. Refer to 
[build.yaml](build.yaml) and [CobbleLocalizationDelegate](lib/localization/localization_delegate.dart)
for more info.
