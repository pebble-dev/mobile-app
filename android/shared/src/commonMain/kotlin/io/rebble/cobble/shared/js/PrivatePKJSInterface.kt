package io.rebble.cobble.shared.js

interface PrivatePKJSInterface {
    fun privateLog(message: String)
    fun logInterceptedSend()
    fun logInterceptedRequest()
    fun getVersionCode(): Int
}