package io.rebble.cobble

import android.content.Intent
import android.media.MediaCodec
import android.media.MediaCodecList
import android.media.MediaFormat
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import io.rebble.cobble.shared.AndroidPlatformContext
import io.rebble.cobble.shared.Logging
import io.rebble.cobble.shared.PlatformContext
import io.rebble.cobble.shared.ui.nav.Routes
import io.rebble.cobble.shared.ui.view.MainView
import io.rebble.cobble.shared.util.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.plus
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class MainActivity : AppCompatActivity() {
    lateinit var coroutineScope: CoroutineScope
    var navHostController: NavHostController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val injectionComponent = (applicationContext as CobbleApplication).component
        val activityComponent = injectionComponent.createActivitySubcomponentFactory()
                .create(this)
        coroutineScope = lifecycleScope + injectionComponent.createExceptionHandler()
        val jobScheduler = injectionComponent.createAndroidJobScheduler()
        jobScheduler.scheduleStartupJobs()

        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            navHostController = rememberNavController()
            MainView(navHostController!!)
            handleIntent(intent)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        val uri = intent.data
        if (intent.scheme == "content" && uri != null) {
            if (uri.path?.endsWith("pbw") == true) {
                Logging.d("Received pbw install intent")
                val fileSize = contentResolver.openAssetFileDescriptor(uri, "r").use {
                    it?.length
                }
                if (fileSize == null || fileSize > 10_000_000) {
                    Logging.e("Invalid PBW file size, ignoring")
                    return
                }
                val cachedUri = cacheIncomingPbw(uri)
                navHostController?.navigate("${Routes.DIALOG_APP_INSTALL}?uri=$cachedUri")
            }
        } else {
            if (intent.hasExtra("navigationPath")) {
                navHostController?.navigate(intent.getStringExtra("navigationPath")!!)
            }
        }
    }

    private fun cacheIncomingPbw(uri: Uri): Uri {
        val cached = applicationContext.cacheDir.resolve("local-${Clock.System.now().toEpochMilliseconds()}.pbw")
        cached.deleteOnExit()
        applicationContext.contentResolver.openInputStream(uri).use { input ->
            cached.outputStream().use { output ->
                input!!.copyTo(output)
            }
        }
        return cached.toUri()
    }
}