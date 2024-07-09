package io.rebble.cobble.shared.js

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import io.rebble.libpebblecommon.metadata.pbw.appinfo.PbwAppInfo
import io.rebble.libpebblecommon.util.runBlocking
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import org.junit.Before
import org.junit.Test

class WebViewJsRunnerTest {

    private lateinit var context: Context
    private val json = Json {ignoreUnknownKeys = true}
    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
    }
    @Test
    fun test() = runBlocking {
        val appInfo: PbwAppInfo = json.decodeFromString(
            """
                {
                    "targetPlatforms": [
                        "aplite", 
                        "basalt", 
                        "chalk", 
                        "diorite"
                    ], 
                    "projectType": "native", 
                    "messageKeys": {}, 
                    "companyName": "ttmm", 
                    "enableMultiJS": true, 
                    "versionLabel": "2.12", 
                    "longName": "ttmmbrn", 
                    "shortName": "ttmmbrn", 
                    "name": "ttmmbrn", 
                    "sdkVersion": "3", 
                    "displayName": "ttmmbrn", 
                    "uuid": "c4c60c62-2c22-4ad7-aef4-cad9481da58b", 
                    "appKeys": {}, 
                    "capabilities": [
                        "health", 
                        "location", 
                        "configurable"
                    ], 
                    "watchapp": {
                        "watchface": true
                    },
                    "resources": {
                        "media": []
                    }
                }
            """.trimIndent()
        )
        val printTestPath = "file:///android_asset/print_test.js"
        val webViewJsRunner = WebViewJsRunner(context, appInfo, printTestPath)
        webViewJsRunner.start()
        delay(5000)
    }
}