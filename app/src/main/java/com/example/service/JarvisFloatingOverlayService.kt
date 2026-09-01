package com.example.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.data.repository.AssistantAppearanceConfig
import com.example.data.repository.AssistantColorTheme
import com.example.data.repository.AssistantShape
import com.example.domain.JarvisCoreEngine
import com.example.ui.components.ArcReactorVisualizer
import com.example.ui.components.CurvedPillVisualizer
import com.example.ui.components.JarvisState
import com.example.ui.components.MinimalBubbleVisualizer
import com.example.ui.components.SiriOrbVisualizer
import com.example.ui.components.WaveformRibbonVisualizer
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisGreen
import com.example.ui.theme.JarvisObsidian
import com.example.ui.theme.JarvisRed
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.math.abs

class JarvisFloatingOverlayService : Service(), LifecycleOwner, SavedStateRegistryOwner {

    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private val mainHandler = Handler(Looper.getMainLooper())

    private val isExpandedFlow = MutableStateFlow(false)
    private val isDismissRequested = MutableStateFlow(false)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action

        if (action == ACTION_STOP_OVERLAY) {
            removeFloatingBubble()
            stopSelf()
            return START_NOT_STICKY
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            // Overlay permission is required to draw over home screen & other apps
            stopSelf()
            return START_NOT_STICKY
        }

        val command = intent?.getStringExtra(EXTRA_COMMAND)
        val isWakeTrigger = action == ACTION_WAKE_TRIGGER

        showFloatingOverlay()

        if (isWakeTrigger) {
            isExpandedFlow.value = true
            val engine = JarvisCoreEngine.getInstance(applicationContext)
            if (!command.isNullOrBlank()) {
                engine.setQueryInput(command)
                engine.processUserQuery(command)
            } else {
                engine.toggleVoiceRecognition()
            }
        }

        return START_STICKY
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun showFloatingOverlay() {
        if (floatingView != null) return

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val screenMetrics = resources.displayMetrics
        val screenWidth = screenMetrics.widthPixels
        val screenHeight = screenMetrics.heightPixels

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            // Position near the bottom center initially like Siri
            x = (screenWidth / 2) - 160
            y = screenHeight - 420
        }

        val composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@JarvisFloatingOverlayService)
            setViewTreeSavedStateRegistryOwner(this@JarvisFloatingOverlayService)
            setContent {
                val engine = JarvisCoreEngine.getInstance(applicationContext)
                val config by engine.appearanceConfig.collectAsState()
                val jarvisState by engine.jarvisState.collectAsState()
                val rmsAudio by engine.rmsAudioLevel.collectAsState()
                val queryInput by engine.currentQueryInput.collectAsState()
                val responseText by engine.lastJarvisResponse.collectAsState()
                val isExpanded by isExpandedFlow.collectAsState()

                // Auto collapse capsule 5 seconds after speaking finishes if in STANDBY
                LaunchedEffect(jarvisState) {
                    if (jarvisState == JarvisState.STANDBY && isExpanded) {
                        mainHandler.postDelayed({
                            if (jarvisState == JarvisState.STANDBY) {
                                isExpandedFlow.value = false
                            }
                        }, 5000)
                    } else if (jarvisState != JarvisState.STANDBY) {
                        isExpandedFlow.value = true
                    }
                }

                SiriFloatingWidgetLayout(
                    config = config,
                    state = jarvisState,
                    audioLevel = rmsAudio,
                    queryText = queryInput,
                    responseText = responseText,
                    isExpanded = isExpanded || jarvisState != JarvisState.STANDBY,
                    onShapeClick = {
                        if (jarvisState == JarvisState.SPEAKING) {
                            engine.ttsHelper.stop()
                        } else {
                            engine.toggleVoiceRecognition()
                        }
                    },
                    onToggleTorch = {
                        engine.quickToggleFlashlight()
                    },
                    onCloseClick = {
                        isExpandedFlow.value = false
                        if (jarvisState == JarvisState.LISTENING) {
                            engine.speechHelper.stopListening()
                        }
                        if (jarvisState == JarvisState.SPEAKING) {
                            engine.ttsHelper.stop()
                        }
                    },
                    onDismissEntirely = {
                        removeFloatingBubble()
                        stopSelf()
                    }
                )
            }
        }

        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var isDragging = false

        composeView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isDragging = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = (event.rawX - initialTouchX).toInt()
                    val deltaY = (event.rawY - initialTouchY).toInt()
                    if (abs(deltaX) > 10 || abs(deltaY) > 10) {
                        isDragging = true
                        params.x = initialX + deltaX
                        params.y = initialY + deltaY
                        windowManager?.updateViewLayout(composeView, params)
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!isDragging) {
                        val engine = JarvisCoreEngine.getInstance(applicationContext)
                        if (engine.jarvisState.value == JarvisState.SPEAKING) {
                            engine.ttsHelper.stop()
                        } else {
                            engine.toggleVoiceRecognition()
                        }
                    }
                    true
                }
                else -> false
            }
        }

        try {
            windowManager?.addView(composeView, params)
            floatingView = composeView
        } catch (_: Exception) {
        }
    }

    private fun removeFloatingBubble() {
        if (floatingView != null) {
            try {
                windowManager?.removeView(floatingView)
            } catch (_: Exception) {
            }
            floatingView = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        removeFloatingBubble()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_START_OVERLAY = "com.example.action.START_OVERLAY"
        const val ACTION_STOP_OVERLAY = "com.example.action.STOP_OVERLAY"
        const val ACTION_WAKE_TRIGGER = "com.example.action.WAKE_TRIGGER"
        const val EXTRA_COMMAND = "extra_command"

        fun start(context: Context) {
            val intent = Intent(context, JarvisFloatingOverlayService::class.java).apply {
                action = ACTION_START_OVERLAY
            }
            context.startService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, JarvisFloatingOverlayService::class.java).apply {
                action = ACTION_STOP_OVERLAY
            }
            context.startService(intent)
        }

        fun activateFromWakeWord(context: Context, command: String? = null) {
            val intent = Intent(context, JarvisFloatingOverlayService::class.java).apply {
                action = ACTION_WAKE_TRIGGER
                if (!command.isNullOrBlank()) {
                    putExtra(EXTRA_COMMAND, command)
                }
            }
            context.startService(intent)
        }
    }
}

/**
 * Compact Siri-like Floating UI:
 * Displays ONLY the selected Studio shape and an expandable frosted glass pill.
 * NO FULL-SCREEN BLOCKER!
 */
@Composable
fun SiriFloatingWidgetLayout(
    config: AssistantAppearanceConfig,
    state: JarvisState,
    audioLevel: Float,
    queryText: String,
    responseText: String,
    isExpanded: Boolean,
    onShapeClick: () -> Unit,
    onToggleTorch: () -> Unit,
    onCloseClick: () -> Unit,
    onDismissEntirely: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val infiniteTransition = rememberInfiniteTransition(label = "floating_aura")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    JarvisState.LISTENING -> 400
                    JarvisState.PROCESSING -> 300
                    JarvisState.SPEAKING -> 500
                    else -> 1600
                },
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .widthIn(max = 320.dp)
            .padding(6.dp)
            .animateContentSize()
    ) {
        // 1. Compact Siri-Style Glass Pill (Expands only when listening, processing, or speaking)
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(tween(250)) + expandVertically(tween(250)),
            exit = fadeOut(tween(200)) + shrinkVertically(tween(200))
        ) {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xEE0B1220)
                ),
                border = BorderStroke(
                    (1.5f * config.glowIntensity).dp,
                    Brush.horizontalGradient(config.colorTheme.gradientColors)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("siri_floating_capsule")
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    // Header row: Status indicator + Quick actions + Close button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (state) {
                                            JarvisState.LISTENING -> JarvisCyan
                                            JarvisState.PROCESSING -> JarvisAmber
                                            JarvisState.SPEAKING -> JarvisGreen
                                            else -> config.colorTheme.accentColor
                                        }
                                    )
                            )
                            Text(
                                text = when (state) {
                                    JarvisState.LISTENING -> "LISTENING..."
                                    JarvisState.PROCESSING -> "PROCESSING..."
                                    JarvisState.SPEAKING -> "J.A.R.V.I.S."
                                    else -> config.effectiveWakeWord.uppercase()
                                },
                                color = config.colorTheme.accentColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 0.5.sp
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onToggleTorch,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FlashlightOn,
                                    contentDescription = "Torch",
                                    tint = JarvisTextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            IconButton(
                                onClick = {
                                    if (responseText.isNotBlank()) {
                                        clipboardManager.setText(AnnotatedString(responseText))
                                        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy",
                                    tint = JarvisTextSecondary,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                            IconButton(
                                onClick = onCloseClick,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    tint = JarvisTextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    // Spoken User Query (if active)
                    if (queryText.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "“$queryText”",
                            color = JarvisTextSecondary,
                            fontSize = 12.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Spoken Response Text
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = responseText.ifBlank { "How can I assist you, sir?" },
                        color = JarvisTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 18.sp,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // 2. The Selected Assistant Shape from Studio (Siri Orb, Curved Pill, Arc Reactor, Wave Ribbon, Minimal Bubble)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .scale(pulseScale)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onShapeClick
                )
                .testTag("floating_assistant_shape")
        ) {
            when (config.shape) {
                AssistantShape.SIRI_ORB -> {
                    SiriOrbVisualizer(
                        state = state,
                        colorTheme = config.colorTheme,
                        audioLevel = audioLevel,
                        size = 72.dp,
                        glowIntensity = config.glowIntensity,
                        onClick = onShapeClick
                    )
                }
                AssistantShape.CURVED_PILL -> {
                    CurvedPillVisualizer(
                        state = state,
                        colorTheme = config.colorTheme,
                        audioLevel = audioLevel,
                        width = 175.dp,
                        height = 52.dp,
                        glowIntensity = config.glowIntensity,
                        onClick = onShapeClick
                    )
                }
                AssistantShape.ARC_REACTOR -> {
                    ArcReactorVisualizer(
                        state = state,
                        audioLevel = audioLevel,
                        size = 74.dp,
                        onClick = onShapeClick
                    )
                }
                AssistantShape.WAVEFORM_RIBBON -> {
                    WaveformRibbonVisualizer(
                        state = state,
                        colorTheme = config.colorTheme,
                        audioLevel = audioLevel,
                        glowIntensity = config.glowIntensity,
                        onClick = onShapeClick
                    )
                }
                AssistantShape.MINIMAL_BUBBLE -> {
                    MinimalBubbleVisualizer(
                        state = state,
                        colorTheme = config.colorTheme,
                        audioLevel = audioLevel,
                        size = 66.dp,
                        glowIntensity = config.glowIntensity,
                        onClick = onShapeClick
                    )
                }
            }
        }
    }
}
