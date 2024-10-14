package io.rebble.cobble

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import io.rebble.cobble.shared.ui.view.MainView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.plus

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
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.hasExtra("navigationPath")) {
            navHostController?.navigate(intent.getStringExtra("navigationPath")!!)
        }
    }
}