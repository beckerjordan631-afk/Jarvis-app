package com.jarvis.assistant

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jarvis.assistant.ui.screens.ChatScreen
import com.jarvis.assistant.ui.screens.HudScreen
import com.jarvis.assistant.ui.theme.JarvisBlack
import com.jarvis.assistant.ui.theme.JarvisTextPrimary
import com.jarvis.assistant.ui.theme.JarvisTheme
import com.jarvis.assistant.ui.theme.MonoFont
import com.jarvis.assistant.viewmodel.JarvisViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: JarvisViewModel by viewModels()

    private val requestMicPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* handled reactively via hasMicPermission() in Compose */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            JarvisTheme {
                var hasMicPermission by remember { mutableStateOf(hasMicPermission()) }
                var showChat by remember { mutableStateOf(false) }
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                if (!hasMicPermission) {
                    PermissionScreen {
                        requestMicPermission.launch(Manifest.permission.RECORD_AUDIO)
                        hasMicPermission = hasMicPermission()
                    }
                } else if (showChat) {
                    ChatScreen(
                        messages = uiState.messages,
                        onSend = { text -> viewModel.handleUserUtterance(text) },
                        onBack = { showChat = false }
                    )
                } else {
                    HudScreen(
                        uiState = uiState,
                        onMicPress = { viewModel.startListening() },
                        onMicRelease = { viewModel.stopListening() },
                        onOpenChat = { showChat = true }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshBattery()
    }

    private fun hasMicPermission(): Boolean =
        ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
}

@androidx.compose.runtime.Composable
private fun PermissionScreen(onRequest: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().background(JarvisBlack),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Text(
                "JARVIS benötigt Mikrofonzugriff, um dir zuzuhören.",
                color = JarvisTextPrimary,
                fontFamily = MonoFont,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = 16.dp))
            Button(onClick = onRequest) {
                Text("Zugriff erlauben")
            }
        }
    }
}
