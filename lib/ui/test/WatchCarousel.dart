import 'package:flutter/cupertino.dart';
import 'package:fossil/ui/common/icons/WatchIcon.dart';
import 'package:fossil/ui/setup/FirstRunPage.dart';

class WatchCarousel extends StatelessWidget {
  @override
  build(BuildContext context) {
    return ListView.builder(
        scrollDirection: Axis.horizontal,
        itemBuilder: (context, index) {
          return CarouselIcon(
            icon: PebbleWatchIcon(model: PebbleWatchModel.time_steel_gold),
          );
        });
  }
}
