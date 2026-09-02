package com.jarvis.assistant.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jarvis.assistant.ui.components.HudTopBar
import com.jarvis.assistant.ui.components.JarvisOrb
import com.jarvis.assistant.ui.components.VitalsPanel
import com.jarvis.assistant.ui.components.stateColor
import com.jarvis.assistant.ui.components.stateLabel
import com.jarvis.assistant.ui.theme.JarvisBlack
import com.jarvis.assistant.ui.theme.JarvisBlue
import com.jarvis.assistant.ui.theme.JarvisTextSecondary
import com.jarvis.assistant.ui.theme.MonoFont
import com.jarvis.assistant.viewmodel.JarvisState
import com.jarvis.assistant.viewmodel.JarvisUiState

@Composable
fun HudScreen(
    uiState: JarvisUiState,
    onMicPress: () -> Unit,
    onMicRelease: () -> Unit,
    onOpenChat: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(JarvisBlack)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            HudTopBar(state = uiState.state, batteryLevel = uiState.batteryLevel)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    JarvisOrb(state = uiState.state)
                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = stateLabel(uiState.state),
                        color = stateColor(uiState.state),
                        fontFamily = MonoFont,
                        fontSize = 13.sp,
                        letterSpacing = 3.sp
                    )
                    if (uiState.partialTranscript.isNotBlank()) {
                        Text(
                            text = "\"${uiState.partialTranscript}\"",
                            color = JarvisTextSecondary,
                            fontFamily = MonoFont,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(top = 10.dp, start = 24.dp, end = 24.dp)
                        )
                    }
                    uiState.errorMessage?.let {
                        Text(
                            text = it,
                            color = com.jarvis.assistant.ui.theme.JarvisRed,
                            fontFamily = MonoFont,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 10.dp)
                        )
                    }
                }
            }

            VitalsPanel(memoryCount = uiState.memoryCount, lastCommand = uiState.lastCommand)

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onOpenChat) {
                    Icon(Icons.Filled.Chat, contentDescription = "Chat öffnen", tint = JarvisTextSecondary)
                }

                MicButton(state = uiState.state, onPress = onMicPress, onRelease = onMicRelease)

                Spacer(modifier = Modifier.size(48.dp))
            }
        }
    }
}

@Composable
private fun MicButton(state: JarvisState, onPress: () -> Unit, onRelease: () -> Unit) {
    val listening = state == JarvisState.LISTENING
    Box(
        modifier = Modifier
            .size(76.dp)
            .background(if (listening) JarvisBlue else Color(0xFF10151F), CircleShape)
            .clickable {
                if (listening) onRelease() else onPress()
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (listening) Icons.Filled.MicOff else Icons.Filled.Mic,
            contentDescription = "Mikrofon",
            tint = Color.White,
            modifier = Modifier.size(32.dp)
        )
    }
}
