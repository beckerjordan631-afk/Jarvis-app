package com.jarvis.assistant.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jarvis.assistant.ui.theme.JarvisBlueBright
import com.jarvis.assistant.ui.theme.JarvisGreen
import com.jarvis.assistant.ui.theme.JarvisPanel
import com.jarvis.assistant.ui.theme.JarvisTextSecondary
import com.jarvis.assistant.ui.theme.MonoFont
import com.jarvis.assistant.viewmodel.JarvisState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HudTopBar(state: JarvisState, batteryLevel: Int) {
    val timeFmt = remember { SimpleDateFormat("HH:mm", Locale.GERMANY) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("JARVIS", color = JarvisBlueBright, fontFamily = MonoFont, fontWeight = FontWeight.Bold, fontSize = 16.sp, letterSpacing = 2.sp)
        Text(timeFmt.format(Date()), color = JarvisTextSecondary, fontFamily = MonoFont, fontSize = 13.sp)
        Text("$batteryLevel%", color = if (batteryLevel < 20) com.jarvis.assistant.ui.theme.JarvisRed else JarvisTextSecondary, fontFamily = MonoFont, fontSize = 13.sp)
        Text(stateLabel(state), color = stateColor(state), fontFamily = MonoFont, fontSize = 13.sp)
    }
}

@Composable
fun VitalsPanel(memoryCount: Int, lastCommand: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .background(JarvisPanel, RoundedCornerShape(10.dp))
            .padding(16.dp)
    ) {
        Text("SYSTEM VITALS", color = JarvisTextSecondary, fontFamily = MonoFont, fontSize = 11.sp, letterSpacing = 2.sp)
        Row(modifier = Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            VitalItem(label = "MEMORY", value = "$memoryCount")
            VitalItem(label = "MIKROFON", value = "BEREIT")
            VitalItem(label = "KI-STATUS", value = "AKTIV")
        }
        Text(
            "LETZTER BEFEHL",
            color = JarvisTextSecondary,
            fontFamily = MonoFont,
            fontSize = 11.sp,
            letterSpacing = 2.sp,
            modifier = Modifier.padding(top = 14.dp)
        )
        Text(
            lastCommand,
            color = JarvisGreen,
            fontFamily = MonoFont,
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun VitalItem(label: String, value: String) {
    Column {
        Text(label, color = JarvisTextSecondary, fontFamily = MonoFont, fontSize = 10.sp)
        Text(value, color = JarvisBlueBright, fontFamily = MonoFont, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

fun stateLabel(state: JarvisState): String = when (state) {
    JarvisState.IDLE -> "BEREIT"
    JarvisState.LISTENING -> "HÖRT ZU"
    JarvisState.THINKING -> "DENKT NACH"
    JarvisState.SPEAKING -> "SPRICHT"
}

@Composable
fun stateColor(state: JarvisState) = when (state) {
    JarvisState.IDLE -> JarvisTextSecondary
    JarvisState.LISTENING -> JarvisBlueBright
    JarvisState.THINKING -> JarvisGreen
    JarvisState.SPEAKING -> JarvisBlueBright
}
