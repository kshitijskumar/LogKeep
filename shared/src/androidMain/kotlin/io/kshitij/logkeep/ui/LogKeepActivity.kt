package io.kshitij.logkeep.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import io.kshitij.logkeep.core.LogKeep
import io.kshitij.logkeep.ui.navigation.LogKeepNavViewModel

internal class LogKeepActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        if (LogKeep.sessionRepository == null) {
            finish()
            return
        }
        setContent {
            val navViewModel: LogKeepNavViewModel = viewModel()
            LogKeepNavigationHost(navViewModel = navViewModel)
        }
    }
}
