import 'package:flutter/material.dart';
import 'package:fossil/icons/rebble_icons_stroke_icons.dart';
import 'package:fossil/icons/WatchIcon.dart';
import 'package:fossil/setup/PairPage.dart';

class CircleContainer extends StatelessWidget {
  CircleContainer({
    this.child,
    this.diameter,
    this.color,
    this.margin,
    this.padding
  });

  final Widget child;
  final double diameter;
  final Color color;
  final EdgeInsets margin;
  final EdgeInsets padding;
  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: BoxDecoration(
        color: color == null ? Theme.of(context).dividerColor : color,
        shape: BoxShape.circle
      ),
      child: child,
      width: diameter,
      height: diameter,
      margin: margin == null ? EdgeInsets.zero : margin,
      padding: padding == null ? EdgeInsets.zero : padding,
    );
  }

}

class CarouselIcon extends StatelessWidget {
  CarouselIcon({this.icon});
  final Widget icon;
  @override
  Widget build(BuildContext context) {
    return CircleContainer(
      diameter: 128.0,
      margin: EdgeInsets.symmetric(horizontal:8.0),
      child: Center(
          child: icon
      ),
    );
  }

}

class FirstRunPage extends StatefulWidget {
  @override
  State<StatefulWidget> createState() => new _FirstRunPageState();
}

class _FirstRunPageState extends State<FirstRunPage> {
  static final EdgeInsets _iconMargin = EdgeInsets.all(4);
  static List<Widget> _iconScrollerSet = [
    CarouselIcon(icon: PebbleWatchIcon.Classic(PebbleWatchColor.Red, size: 96.0)),
    CarouselIcon(icon: PebbleWatchIcon.Time(PebbleWatchColor.Red, size: 96.0)),
    CarouselIcon(icon: PebbleWatchIcon.Round(PebbleWatchColor.White, bodyStrokeColor: Colors.black, size: 96.0)),
    CarouselIcon(icon: PebbleWatchIcon.Two(PebbleWatchColor.White, PebbleWatchColor.Turquoise, bezelColor: PebbleWatchColor.Turquoise, size: 96.0)),
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
        _scrollController.animateTo(_scrollController.position.maxScrollExtent * 4096, duration: Duration(seconds: 5*4096), curve: Curves.linear);
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Stack(
        children: <Widget>[
          Positioned(
            top: MediaQuery.of(context).size.height / 4,
            width: MediaQuery.of(context).size.width,
            child: Column(
              children: <Widget>[
                CircleContainer(
                  child: Image(image: AssetImage("images/app_large.png"),),
                  diameter: 120,
                  color: Theme.of(context).primaryColor,
                  padding: EdgeInsets.all(20),
                ),
                SizedBox(height: 16.0), // spacer
                Container(margin: EdgeInsets.symmetric(vertical: 8), child: Text("Welcome to Rebble!", style: Theme.of(context).textTheme.headline4)),
                SizedBox(height: 24.0), // spacer
                SingleChildScrollView(
                  scrollDirection: Axis.horizontal,
                  controller: _scrollController..addListener(() {
                    if (_scrollController.position.pixels > _scrollController.position.maxScrollExtent * 0.9) {
                      setState(() {
                        _iconScroller += _iconScrollerSet;
                      });
                    }
                    _doneScrollChange = false;
                  }),
                  child: Row(
                      children: _iconScroller
                  ),
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
                    onPressed: () {
                    },
                  ),
                  FloatingActionButton.extended(
                      icon: Text("LET'S GET STARTED"),
                      label: Icon(RebbleIconsStroke.caret_right),
                      backgroundColor: Theme.of(context).primaryColor,
                      onPressed: () {
                        Navigator.push(context, MaterialPageRoute(builder: (context) => PairPage()));
                      }
                  )
                ],
              ),
            ),
          )
        ],
      ),
    );
  }
}