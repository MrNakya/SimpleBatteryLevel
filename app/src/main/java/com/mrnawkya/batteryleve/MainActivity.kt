package com.mrnawkya.batteryleve

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

// Визначення мов
enum class Language(val displayName: String) {
    ENGLISH("English"),
    RUSSIAN("Русский"),
    UKRAINIAN("Українська")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                BatteryApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatteryApp() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var language by remember { mutableStateOf(Language.UKRAINIAN) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text("Меню", modifier = Modifier.padding(16.dp), fontSize = 20.sp)
                HorizontalDivider()
                Language.entries.forEach { lang ->
                    NavigationDrawerItem(
                        label = { Text(lang.displayName) },
                        selected = lang == language,
                        onClick = {
                            language = lang
                            scope.launch { drawerState.close() }
                        }
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Battery Info") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            // Використовуємо текст замість Іконки, щоб уникнути помилок
                            Text("☰", fontSize = 24.sp)
                        }
                    }
                )
            }
        ) { padding ->
            BatteryScreen(language, Modifier.padding(padding))
        }
    }
}

@Composable
fun BatteryScreen(language: Language, modifier: Modifier) {
    val context = LocalContext.current
    var charge by remember { mutableStateOf(0) }

    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context, i: Intent) {
                val l = i.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val s = i.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                if (l != -1 && s != -1) charge = (l * 100 / s.toFloat()).toInt()
            }
        }
        context.registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        onDispose { context.unregisterReceiver(receiver) }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "$charge%", fontSize = 80.sp, color = if (charge < 20) Color.Red else Color.Green)
        Text(text = when(language) {
            Language.ENGLISH -> "Battery Level"
            Language.RUSSIAN -> "Заряд батареи"
            Language.UKRAINIAN -> "Заряд батареї"
        }, fontSize = 24.sp)
    }
}