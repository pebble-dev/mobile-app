import 'package:cobble/ui/common/components/circle_container.dart';
import 'package:cobble/ui/common/icons/watch_icon.dart';
import 'package:cobble/ui/home/home_page.dart';
import 'package:cobble/ui/router/cobble_navigator.dart';
import 'package:cobble/ui/router/cobble_scaffold.dart';
import 'package:cobble/ui/router/cobble_screen.dart';
import 'package:cobble/ui/setup/pair_page.dart';
import 'package:flutter/material.dart';

import '../common/icons/fonts/rebble_icons.dart';

class CarouselIcon extends StatelessWidget {
  CarouselIcon({this.icon});

  final Widget icon;

  @override
  Widget build(BuildContext context) {
    return CircleContainer(
      diameter: 128.0,
      margin: EdgeInsets.symmetric(horizontal: 8.0),
      child: Center(child: icon),
    );
  }
}

class FirstRunPage extends StatefulWidget implements CobbleScreen {
  @override
  State<StatefulWidget> createState() => new _FirstRunPageState();
}

class _FirstRunPageState extends State<FirstRunPage> {
  static List<Widget> _iconScrollerSet = [
    CarouselIcon(
        icon: PebbleWatchIcon.classic(PebbleWatchColor.Red, size: 96.0)),
    CarouselIcon(icon: PebbleWatchIcon.time(PebbleWatchColor.Red, size: 96.0)),
    CarouselIcon(
        icon: PebbleWatchIcon.timeRound(PebbleWatchColor.White,
            bodyStrokeColor: Colors.black, size: 96.0)),
    CarouselIcon(
        icon: PebbleWatchIcon.pebbleTwo(PebbleWatchColor.White,
            buttonsColor: PebbleWatchColor.Aqua,
            bezelColor: PebbleWatchColor.Aqua,
            size: 96.0)),
  ];
  List<Widget> _iconScroller = _iconScrollerSet;

  ScrollController _scrollController = new ScrollController();
  bool _doneScrollChange = false;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((timeStamp) {
      if (!_doneScrollChange) {
        _doneScrollChange = true;
        _scrollController.animateTo(
            _scrollController.position.maxScrollExtent * 4096,
            duration: Duration(seconds: 5 * 4096),
            curve: Curves.linear);
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    return CobbleScaffold.page(
      child: Stack(
        children: <Widget>[
          Positioned(
            top: (MediaQuery.of(context).size.height / 5),
            width: MediaQuery.of(context).size.width,
            child: Column(
              children: <Widget>[
                CircleContainer(
                  child: Image(
                    image: AssetImage("images/app_large.png"),
                  ),
                  diameter: 120,
                  color: Theme.of(context).primaryColor,
                  padding: EdgeInsets.all(20),
                ),
                SizedBox(height: 16.0), // spacer
                Container(
                    margin: EdgeInsets.symmetric(vertical: 8),
                    child: Text("Welcome to Rebble!",
                        style: Theme.of(context).textTheme.headline4)),
                SizedBox(height: 24.0), // spacer
                SingleChildScrollView(
                  scrollDirection: Axis.horizontal,
                  controller: _scrollController
                    ..addListener(() {
                      if (_scrollController.position.pixels >
                          _scrollController.position.maxScrollExtent * 0.9) {
                        setState(() {
                          _iconScroller += _iconScrollerSet;
                        });
                      }
                      _doneScrollChange = false;
                    }),
                  child: Row(children: _iconScroller),
                ),
              ],
            ),
          ),
          Positioned(
            width: MediaQuery.of(context).size.width,
            bottom: 0,
            child: Container(
              margin: EdgeInsets.fromLTRB(16, 16, 16, 16),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: <Widget>[
                  FlatButton(
                    child: Text("SKIP"),
                    onPressed: () => context.pushAndRemoveAllBelow(HomePage()),
                  ),
                  FloatingActionButton.extended(
                    icon: Text("LET'S GET STARTED"),
                    label: Icon(RebbleIcons.caret_right),
                    backgroundColor: Theme.of(context).primaryColor,
                    onPressed: () => context.push(
                      PairPage(
                        fromLanding: true,
                      ),
                    ),
                  ),
                ],
              ),
            ),
          )
        ],
      ),
    );
  }
}
