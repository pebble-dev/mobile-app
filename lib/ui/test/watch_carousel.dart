import 'dart:math';

import 'package:flutter/cupertino.dart';
import 'package:cobble/ui/common/icons/watch_icon.dart';
import 'package:cobble/ui/setup/first_run_page.dart';

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
