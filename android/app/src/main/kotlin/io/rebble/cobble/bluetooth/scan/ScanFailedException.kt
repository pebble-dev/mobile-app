package io.rebble.cobble.bluetooth.scan

class ScanFailedException(errorCode: Int) : Exception("Scan failed: $errorCode")