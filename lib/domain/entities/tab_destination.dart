import 'package:flutter/material.dart';

class TabDestination {
  TabDestination(
      this.icon,
      this.iconFill,
      this.title,
      this.pageBody,
      );

  final String title; // Title of BottomNavigationBarItem and More tab item
  final IconData icon; // Simple stroke icon for BottomNavigationBarItem
  final IconData iconFill; // Composite icon fill for More tab
  final Widget pageBody; // Page content when part of HomePage, e.g. a StoreTab()
}