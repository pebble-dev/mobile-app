package io.rebble.cobble.shared.js

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import io.rebble.cobble.shared.domain.common.PebbleDevice
import io.rebble.libpebblecommon.metadata.pbw.appinfo.PbwAppInfo
import io.rebble.libpebblecommon.util.runBlocking
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import org.junit.Before
import org.junit.Test
import java.io.File

class WebViewJsRunnerTest {
    private lateinit var context: Context
    private val json = Json { ignoreUnknownKeys = true }
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
    }

    // Copies from the android_assets of the test to the sdcard so the WebView can access it
    private fun assetsToSdcard(file: String): String {
        val sdcardPath = context.getExternalFilesDir(null)!!.absolutePath
        val testPath = "$sdcardPath/test"
        File(testPath).mkdir()
        val testFile = "$testPath/$file"
        val assetManager = InstrumentationRegistry.getInstrumentation().getContext().getAssets()
        val inputStream = assetManager.open(file)
        val outputStream = File(testFile).outputStream()
        inputStream.copyTo(outputStream)
        inputStream.close()
        outputStream.close()
        return testFile
    }

    @Test
    fun test() =
        runBlocking {
            val appInfo: PbwAppInfo =
                json.decodeFromString(
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
            val printTestPath = assetsToSdcard("print_test.js")
            val webViewJsRunner =
                WebViewJsRunner(
                    context,
                    PebbleDevice(null, "dummy"),
                    coroutineScope,
                    appInfo,
                    printTestPath
                )
            webViewJsRunner.start()
            delay(1000)
            webViewJsRunner.stop()
        }
}