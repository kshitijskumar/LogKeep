package io.kshitij.logkeep

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalUuidApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        println("LogStuff: activity oncreate")
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            App()
        }

        lifecycleScope.launch {
            val errorEntry = (0..50).random()
            repeat(50) {
                val tag = listOf("MainActivity", "SecondActivity").random()
                if (it != errorEntry) {
                    Logger.logDebug(tag, "log count: $it - ${Uuid.random()}")
                } else {
                    try {
                        throw NullPointerException("Something somewhere went wrong")
                    } catch (e: Exception) {
                        Logger.logDebug(tag, "log count: $it - ${Uuid.random()}", e)
                    }
                }
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