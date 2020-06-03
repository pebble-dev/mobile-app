
import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'file:///D:/AndroidStudioProjects/fossil/fossil/lib/setup/PairPage.dart';
import 'package:fossil/icons/rebble_icons_stroke_only_icons.dart';

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
  CarouselIcon({this.icon, this.margin});
  final IconData icon;
  final EdgeInsets margin;
  @override
  Widget build(BuildContext context) {
    CircleContainer child = CircleContainer(
      child: Icon(icon, size: 92,),
      diameter: 140,
    );

    if (margin != null) {
      return Container(
        margin: margin,
        child: child,
      );
    }else return child;
  }

}

class FirstRunPage extends StatefulWidget {
  @override
  State<StatefulWidget> createState() => new _FirstRunPageState();
}

class _FirstRunPageState extends State<FirstRunPage> {
  static final EdgeInsets _iconMargin = EdgeInsets.all(4);
  static List<Widget> _iconScrollerSet = [
    CarouselIcon(icon: Icons.launch, margin: _iconMargin),
    CarouselIcon(icon: Icons.warning, margin: _iconMargin),
    CarouselIcon(icon: Icons.event, margin: _iconMargin),
    CarouselIcon(icon: Icons.add_a_photo, margin: _iconMargin)];
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
                  diameter: 170,
                  color: Theme.of(context).accentColor,
                  padding: EdgeInsets.all(25),
                ),
                Container(margin: EdgeInsets.symmetric(vertical: 8), child: Text("Welcome to Rebble!", style: Theme.of(context).textTheme.headline4)),
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
              margin: EdgeInsets.fromLTRB(10, 20, 10, 20),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: <Widget>[
                  OutlineButton(
                    child: Text("SKIP"),
                    onPressed: () {
                    },
                  ),
                  FloatingActionButton.extended(
                      label: Row(children: <Widget>[
                        Text("LET'S GET STARTED"),
                        Icon(RebbleIconsStrokeOnly.caret_right)],
                        mainAxisAlignment: MainAxisAlignment.center,
                      ),
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