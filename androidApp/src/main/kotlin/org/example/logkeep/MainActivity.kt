package org.example.logkeep

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.delay
import kotlin.time.Duration

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        println("LogStuff: activity oncreate")
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            App()
        }

        lifecycleScope.launch {
            repeat(5) {
                Logger.logDebug("MainActivity", "log count: $it")
                kotlinx.coroutines.delay(500L)
            }
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}