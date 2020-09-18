import 'dart:math';

import 'package:flutter/cupertino.dart';
import 'package:fossil/ui/common/icons/WatchIcon.dart';
import 'package:fossil/ui/setup/FirstRunPage.dart';

class WatchCarousel extends StatelessWidget {
  @override
  build(BuildContext context) {
    List<PebbleWatchModel> values = PebbleWatchModel.values;
    return ListView.builder(
        scrollDirection: Axis.horizontal,
        itemBuilder: (context, index) {
          PebbleWatchModel model = values[Random().nextInt(values.length)];

          return CarouselIcon(
            icon: PebbleWatchIcon(model, size: 96.0),
          );
        });
  }
}
