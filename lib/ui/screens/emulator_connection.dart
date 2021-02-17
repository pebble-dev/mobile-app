import 'package:cobble/domain/connection/connection_state_provider.dart';
import 'package:cobble/infrastructure/pigeons/pigeons.g.dart';
import 'package:cobble/ui/common/components/circle_container.dart';
import 'package:cobble/ui/common/icons/fonts/rebble_icons.dart';
import 'package:cobble/ui/home/home_page.dart';
import 'package:cobble/ui/router/cobble_scaffold.dart';
import 'package:cobble/ui/router/cobble_screen.dart';
import 'package:cobble/ui/theme/with_cobble_theme.dart';
import 'package:flutter/material.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:cobble/ui/router/cobble_navigator.dart';

final UiConnectionControl uiConnectionControl = UiConnectionControl();
final ConnectionControl connectionControl = ConnectionControl();

class EmulatorConnection extends HookWidget implements CobbleScreen {
  final bool fromSetup;
  const EmulatorConnection({
    Key key,
    this.fromSetup = false,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final _connectionState = useProvider(connectionStateProvider.state);
    final _hostField =
        useTextEditingController.fromValue(TextEditingValue.empty);
    final _portField =
        useTextEditingController.fromValue(TextEditingValue(text: "8080"));
    return CobbleScaffold.tab(
        title: "Emulator connector",
        child: SingleChildScrollView(
          child: Column(
            children: <Widget>[
              CircleContainer(
                child: Icon(RebbleIcons.developer_connection_console,
                    size: 140.0, color: Colors.black),
                diameter: MediaQuery.of(context).size.height / 3,
                color: context.scheme.danger,
                margin: EdgeInsets.fromLTRB(0, 32, 0, 16),
              ),
              Container(
                  margin: EdgeInsets.symmetric(vertical: 8),
                  child: Text("Emulator connector",
                      style: context.textTheme.headline4)),
              Container(
                margin: EdgeInsets.symmetric(horizontal: 64),
                child: Column(
                  children: [
                    HookBuilder(builder: (context) {
                      if (fromSetup && _connectionState.isEmulator && _connectionState.isConnected) {
                        Future.microtask(() => context.pushAndRemoveAllBelow(HomePage()));
                      }
                      return Row(
                        mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                        children: [
                          Row(
                            children: [
                              Text("Status: ",
                                  style: DefaultTextStyle.of(context)
                                      .style
                                      .copyWith(fontSize: 18)),
                              _connectionState.isConnected
                                  ? Text("Connected",
                                      style: DefaultTextStyle.of(context)
                                          .style
                                          .copyWith(fontSize: 18, fontWeight: FontWeight.bold))
                                  : _connectionState.isConnecting
                                      ? Text("Connecting",
                                          style: DefaultTextStyle.of(context)
                                              .style
                                              .copyWith(
                                                  color: context.scheme.danger,
                                                  fontSize: 18, fontWeight: FontWeight.bold))
                                      : Text(
                                          "Disconnected",
                                          style: DefaultTextStyle.of(context)
                                              .style
                                              .copyWith(
                                                  color: context.scheme.danger,
                                                  fontSize: 18, fontWeight: FontWeight.bold),
                                        ),
                            ],
                          ),
                          Switch(
                              value: _connectionState.isEmulator &&
                                  (_connectionState.isConnecting ||
                                      _connectionState.isConnected),
                              onChanged: (v) {
                                if (v) {
                                  uiConnectionControl.connectToEmulator(
                                      EmulatorConnectionPigeon()
                                        ..host = _hostField.value.text
                                        ..port =
                                            int.parse(_portField.value.text));
                                } else {
                                  connectionControl.disconnect();
                                }
                              }),
                        ],
                      );
                    }),
                    Row(
                      children: [
                        Expanded(
                            flex: 2,
                            child: TextField(
                              decoration: InputDecoration(
                                  filled: true,
                                  labelText: "IP address",
                                  border: UnderlineInputBorder().copyWith(
                                      borderRadius: BorderRadius.all(Radius.circular(4)))),
                              controller: _hostField,
                            )),
                        SizedBox(width: 8),
                        Expanded(
                            flex: 1,
                            child: TextField(
                                decoration: InputDecoration(
                                    filled: true,
                                    labelText: "Port",
                                    border: UnderlineInputBorder().copyWith(
                                        borderRadius: BorderRadius.all(Radius.circular(4)))),
                                controller: _portField,
                                keyboardType: TextInputType.number)),
                      ],
                    )
                  ],
                ),
              )
            ],
          ),
        ));
  }
}
