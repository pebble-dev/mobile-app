package io.rebble.cobble.handlers

/**
 * Message handler that handles receiving and sending messages from/to watch.
 *
 * All handlers are initialized when watch is connected, so their init methods can
 * send on-connect packets to the watch
 */
interface PebbleMessageHandler