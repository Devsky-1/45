package com.example.service

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.example.ui.screens.JarvisAssistActivity

class JarvisQuickTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        qsTile?.let { tile ->
            tile.state = Tile.STATE_ACTIVE
            tile.label = "JARVIS Assist"
            tile.subtitle = "Voice & Quick Actions"
            tile.updateTile()
        }
    }

    override fun onClick() {
        super.onClick()

        JarvisFloatingOverlayService.activateFromWakeWord(this, null)
    }
}
