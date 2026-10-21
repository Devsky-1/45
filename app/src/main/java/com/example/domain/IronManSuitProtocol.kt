package com.example.domain

import androidx.compose.ui.graphics.Color

enum class StarkArmorModel(
    val codeName: String,
    val designation: String,
    val primaryColorHex: Long,
    val accentColorHex: Long,
    val description: String,
    val weapons: String
) {
    MARK_III(
        codeName = "MARK III",
        designation = "Aero-Tactical Heavy Suit",
        primaryColorHex = 0xFFFF1744, // Stark Hot-Rod Crimson
        accentColorHex = 0xFFFFD54F,  // Gold Titanium
        description = "Gold-Titanium alloy with twin micro-repulsors & shoulder flares.",
        weapons = "Repulsors • Forearm Missiles • Micro-Flares"
    ),
    MARK_VII(
        codeName = "MARK VII",
        designation = "Rapid Pod Deployment Unit",
        primaryColorHex = 0xFF00F0FF, // High-Tech Cyan
        accentColorHex = 0xFFFF3D00,  // Thruster Orange
        description = "Designed for rapid mid-air pod deployment and high-altitude interception.",
        weapons = "Heavy Repulsor Cannons • Laser Cartridges • High-Yield Thrusters"
    ),
    MARK_XLIV_HULKBUSTER(
        codeName = "MARK XLIV",
        designation = "Veronica Orbital Support Cage",
        primaryColorHex = 0xFFFF9100, // Heavy Industrial Amber
        accentColorHex = 0xFFFF1744,  // Deep Crimson
        description = "Modular exo-frame deployed from low-Earth orbital satellite Veronica.",
        weapons = "Jackhammer Gauntlet • Chemical Sedatives • Hydraulic Repulsor Clamps"
    ),
    MARK_L(
        codeName = "MARK L",
        designation = "Bleeding Edge Nanotech Matrix",
        primaryColorHex = 0xFF00E5FF, // Luminous Arc Cyan
        accentColorHex = 0xFF76FF03,  // Emerald Overdrive
        description = "Fluid nano-particle particles capable of infinite shape re-configuration.",
        weapons = "Nano-Shields • Kinetic Energy Blades • Twin Ion Cannons"
    ),
    MARK_LXXXV(
        codeName = "MARK LXXXV",
        designation = "Apex Nanotech Battle Armor",
        primaryColorHex = 0xFFFFD700, // Regal Stark Gold
        accentColorHex = 0xFFFF1744,  // Stark Crimson
        description = "Advanced nano-alloy incorporating the Lightning Refocuser & Gauntlet matrix.",
        weapons = "Lightning Refocuser • Micro-Plasma Cannons • Holo-Shields"
    );

    val primaryColor: Color get() = Color(primaryColorHex)
    val accentColor: Color get() = Color(accentColorHex)
}

data class ArmorSuitStatus(
    val selectedArmor: StarkArmorModel = StarkArmorModel.MARK_LXXXV,
    val powerAllocationRepulsors: Int = 35,
    val powerAllocationThrusters: Int = 45,
    val powerAllocationShields: Int = 20,
    val isArcReactorOverclocked: Boolean = false,
    val arcReactorOutputGigawatts: Float = 3.2f,
    val activeProtocolName: String = "STANDBY PROTOCOL",
    val isTargetLocked: Boolean = false,
    val targetDistanceMeters: Int = 142,
    val repulsorChargeProgress: Float = 0f,
    val isRepulsorFiring: Boolean = false,
    val activeDroneUnitsCount: Int = 0,
    val isVeronicaDeployed: Boolean = false,
    val isHousePartyActive: Boolean = false
)
