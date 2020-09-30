package io.rebble.fossil.bluetooth.scan

class ScanFailedException(errorCode: Int) : Exception("Scan failed: $errorCode")