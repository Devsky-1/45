package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.JarvisViewModel
import com.example.ui.screens.AssistantCustomizationScreen
import com.example.ui.screens.LockScreenOverlay
import com.example.ui.screens.MainAssistantScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: JarvisViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Enable show when locked and screen turn-on for Voice Assistant
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

        // Start background service for ambient wake word detection if enabled and permitted
        if (viewModel.appearanceConfig.value.wakeWordEnabled &&
            androidx.core.content.ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            try {
                com.example.service.JarvisBackgroundService.start(this)
            } catch (_: Exception) {}
        }
    }
}

@Composable
fun JarvisApp(viewModel: JarvisViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val isLockScreenActive by viewModel.isLockScreenActive.collectAsStateWithLifecycle()

    // Dynamic runtime microphone permission launcher
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted && viewModel.appearanceConfig.value.wakeWordEnabled) {
            try {
                com.example.service.JarvisBackgroundService.start(context)
            } catch (_: Exception) {}
        }
    }

    LaunchedEffect(Unit) {
        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing
        ) { _ ->
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "screen_transition"
            ) { tab ->
                when (tab) {
                    0 -> MainAssistantScreen(
                        viewModel = viewModel,
                        onOpenSettings = { viewModel.setSelectedTab(1) }
                    )
                    1 -> {
                        BackHandler {
                            viewModel.setSelectedTab(0)
                        }
                        AssistantCustomizationScreen(
                            viewModel = viewModel,
                            onNavigateBack = { viewModel.setSelectedTab(0) }
                        )
                    }
                    else -> MainAssistantScreen(
                        viewModel = viewModel,
                        onOpenSettings = { viewModel.setSelectedTab(1) }
                    )
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
