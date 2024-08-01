package io.rebble.cobble

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import io.rebble.cobble.shared.ui.view.MainView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.plus

class MainActivity : AppCompatActivity() {
    lateinit var coroutineScope: CoroutineScope
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
            MainView()
        }
    }
}