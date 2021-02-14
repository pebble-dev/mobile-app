package io.rebble.cobble.transport.bluetooth.scan

class ScanFailedException(errorCode: Int) : Exception("Scan failed: $errorCode")