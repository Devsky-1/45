package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assistant
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.JarvisViewModel
import com.example.ui.screens.AssistantCustomizationScreen
import com.example.ui.screens.LockScreenOverlay
import com.example.ui.screens.MainAssistantScreen
import com.example.ui.screens.MemoryMatrixScreen
import com.example.ui.screens.SystemDiagnosticsScreen
import com.example.ui.theme.JarvisCardBg
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanLight
import com.example.ui.theme.JarvisObsidian
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: JarvisViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Enable show when locked and screen turn-on for Assistant
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        setContent {
            MyApplicationTheme {
                JarvisApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun JarvisApp(viewModel: JarvisViewModel) {
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val isLockScreenActive by viewModel.isLockScreenActive.collectAsStateWithLifecycle()

    // Dynamic runtime microphone permission launcher
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            bottomBar = {
                if (!isLockScreenActive) {
                    NavigationBar(
                        containerColor = JarvisCardBg,
                        contentColor = JarvisCyan,
                        tonalElevation = 8.dp,
                        modifier = Modifier
                            .windowInsetsPadding(WindowInsets.navigationBars)
                            .testTag("main_navigation_bar")
                    ) {
                        NavigationBarItem(
                            selected = selectedTab == 0,
                            onClick = { viewModel.setSelectedTab(0) },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Assistant,
                                    contentDescription = "Core HUD"
                                )
                            },
                            label = {
                                Text(
                                    text = "ASSISTANT",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF001F24),
                                selectedTextColor = JarvisCyanLight,
                                indicatorColor = JarvisCyan,
                                unselectedIconColor = JarvisTextMuted,
                                unselectedTextColor = JarvisTextMuted
                            ),
                            modifier = Modifier.testTag("nav_core_hud")
                        )

                        NavigationBarItem(
                            selected = selectedTab == 1,
                            onClick = { viewModel.setSelectedTab(1) },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = "Customize"
                                )
                            },
                            label = {
                                Text(
                                    text = "STUDIO",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF001F24),
                                selectedTextColor = JarvisCyanLight,
                                indicatorColor = JarvisCyan,
                                unselectedIconColor = JarvisTextMuted,
                                unselectedTextColor = JarvisTextMuted
                            ),
                            modifier = Modifier.testTag("nav_studio_customize")
                        )

                        NavigationBarItem(
                            selected = selectedTab == 2,
                            onClick = { viewModel.setSelectedTab(2) },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Lock Screen"
                                )
                            },
                            label = {
                                Text(
                                    text = "LOCK",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF001F24),
                                selectedTextColor = JarvisCyanLight,
                                indicatorColor = JarvisCyan,
                                unselectedIconColor = JarvisTextMuted,
                                unselectedTextColor = JarvisTextMuted
                            ),
                            modifier = Modifier.testTag("nav_lock_screen")
                        )

                        NavigationBarItem(
                            selected = selectedTab == 3,
                            onClick = { viewModel.setSelectedTab(3) },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = "Diagnostics"
                                )
                            },
                            label = {
                                Text(
                                    text = "STATUS",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF001F24),
                                selectedTextColor = JarvisCyanLight,
                                indicatorColor = JarvisCyan,
                                unselectedIconColor = JarvisTextMuted,
                                unselectedTextColor = JarvisTextMuted
                            ),
                            modifier = Modifier.testTag("nav_telemetry")
                        )

                        NavigationBarItem(
                            selected = selectedTab == 4,
                            onClick = { viewModel.setSelectedTab(4) },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Memory,
                                    contentDescription = "Memory"
                                )
                            },
                            label = {
                                Text(
                                    text = "MEMORY",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF001F24),
                                selectedTextColor = JarvisCyanLight,
                                indicatorColor = JarvisCyan,
                                unselectedIconColor = JarvisTextMuted,
                                unselectedTextColor = JarvisTextMuted
                            ),
                            modifier = Modifier.testTag("nav_memory")
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (selectedTab) {
                    0 -> MainAssistantScreen(viewModel = viewModel)
                    1 -> AssistantCustomizationScreen(viewModel = viewModel)
                    2 -> LockScreenOverlay(viewModel = viewModel, onDismiss = { viewModel.setSelectedTab(0) })
                    3 -> SystemDiagnosticsScreen(viewModel = viewModel)
                    4 -> MemoryMatrixScreen(viewModel = viewModel)
                }
            }
        }

        // Fullscreen lock screen ambient overlay modal if activated
        AnimatedVisibility(
            visible = isLockScreenActive,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            LockScreenOverlay(
                viewModel = viewModel,
                onDismiss = { viewModel.toggleLockScreenSimulator(false) }
            )
        }
    }
}
