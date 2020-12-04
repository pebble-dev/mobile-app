import 'dart:async';

/// Run provided function and all async tasks that it spawns synchronously
T runBlocking<T>(T Function() callback) => runZoned(() => callback(),
    zoneSpecification: ZoneSpecification(
        scheduleMicrotask: (_, __, ___, microtask) => microtask()));
