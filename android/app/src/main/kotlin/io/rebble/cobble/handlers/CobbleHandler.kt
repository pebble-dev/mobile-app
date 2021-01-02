package io.rebble.cobble.handlers

/**
 * Handler that observes external events and acts on them.
 *
 * All handlers are initialized when watch is connected, so their init methods can
 * send on-connect packets to the watch
 */
interface CobbleHandler