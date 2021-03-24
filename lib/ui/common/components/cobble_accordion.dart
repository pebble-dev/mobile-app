import 'package:flutter/material.dart';
import 'package:flutter_hooks/flutter_hooks.dart';

class CobbleAccordion extends HookWidget {
  final bool initialExpanded;
  final Widget Function(
    VoidCallback onTap,
    double heightFactor,
  ) headerBuilder;
  final Widget child;

  const CobbleAccordion({
    Key? key,
    required this.headerBuilder,
    required this.child,
    this.initialExpanded = false,
  })  : assert(headerBuilder != null),
        assert(child != null),
        assert(initialExpanded != null),
        super(key: key);

  @override
  Widget build(BuildContext context) {
    final expanded = useState(initialExpanded);
    final controller = useAnimationController(
      duration: Duration(milliseconds: 200),
      initialValue: initialExpanded ? 1 : 0,
    );
    final heightFactor = useAnimation(controller.drive(
      CurveTween(curve: Curves.easeIn),
    ));

    useEffect(() {
      if (expanded.value) {
        controller.forward();
      } else {
        controller.reverse();
      }
      return null;
    }, [expanded.value]);

    return AnimatedBuilder(
      animation: controller.view,
      builder: (context, child) => Column(
        children: [
          headerBuilder(
            () {
              expanded.value = !expanded.value;
            },
            heightFactor,
          ),
          ClipRect(
            child: Align(
              heightFactor: heightFactor,
              alignment: Alignment.bottomCenter,
              child: child,
            ),
          ),
        ],
      ),
      child: child,
    );
  }
}
