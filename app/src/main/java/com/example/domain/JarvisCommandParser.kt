package com.example.domain

import java.util.regex.Pattern

sealed interface ParsedJarvisCommand {
    data class Flashlight(val enable: Boolean) : ParsedJarvisCommand
    data class Timer(val seconds: Int, val label: String) : ParsedJarvisCommand
    data class Weather(val location: String) : ParsedJarvisCommand
    data class Reminder(val title: String, val dueTime: String, val priority: String = "NORMAL") : ParsedJarvisCommand
    data class Note(val title: String, val content: String) : ParsedJarvisCommand
    data class Diagnostic(val component: String = "ALL") : ParsedJarvisCommand
    data class Protocol(val protocolName: String) : ParsedJarvisCommand
    object DailyBriefing : ParsedJarvisCommand
    object ClearHistory : ParsedJarvisCommand
    data class GeneralQuery(val query: String) : ParsedJarvisCommand
}

object JarvisCommandParser {

    fun parse(input: String): ParsedJarvisCommand {
        val text = input.trim().lowercase()

        // Flashlight intents (English, Hindi & Hinglish)
        if (text.contains("flashlight on") || text.contains("turn on flashlight") || text.contains("torch on") ||
            text.contains("enable lights") || text.contains("illuminate") || text.contains("torch chalu") ||
            text.contains("torch jalao") || text.contains("light jalao") || text.contains("light on") ||
            text.contains("flashlight chalu") || text.contains("टॉर्च चालू") || text.contains("टॉर्च जलाओ") ||
            text.contains("लाइट जलाओ") || text.contains("लाइट ऑन")) {
            return ParsedJarvisCommand.Flashlight(true)
        }
        if (text.contains("flashlight off") || text.contains("turn off flashlight") || text.contains("torch off") ||
            text.contains("disable lights") || text.contains("torch band") || text.contains("torch bujhao") ||
            text.contains("light band") || text.contains("flashlight band") || text.contains("टॉर्च बंद") ||
            text.contains("टॉर्च बुझाओ") || text.contains("लाइट बंद")) {
            return ParsedJarvisCommand.Flashlight(false)
        }

        // Clean slate / Clear history (English, Hindi & Hinglish)
        if (text.contains("clean slate") || text.contains("clear history") || text.contains("purge logs") ||
            text.contains("history saaf") || text.contains("chat delete") || text.contains("sab clear") ||
            text.contains("हिस्ट्री साफ") || text.contains("सब मिटाओ")) {
            return ParsedJarvisCommand.ClearHistory
        }

        // Protocols
        if (text.contains("protocol")) {
            val name = when {
                text.contains("house party") -> "House Party Protocol"
                text.contains("stealth") -> "Stealth Mode"
                text.contains("defense") || text.contains("sentry") -> "Perimeter Defense Grid"
                text.contains("overclock") || text.contains("power") -> "Arc Reactor Overclock"
                else -> "Standard Operational Protocol"
            }
            return ParsedJarvisCommand.Protocol(name)
        }

        // Timers: e.g. "set a timer for 5 minutes", "timer 30 seconds", "5 minute ka timer lagao", "टाइमर लगाओ"
        if (text.contains("timer") || text.contains("countdown") || text.contains("टाइमर") || text.contains("minute ka timer")) {
            val seconds = extractTimerSeconds(text)
            if (seconds > 0) {
                val label = extractTimerLabel(text)
                return ParsedJarvisCommand.Timer(seconds, label)
            }
        }

        // Reminders: e.g. "remind me to call Pepper at 5 PM", "reminder buy groceries", "yaad dilana 5 baje"
        if (text.startsWith("remind") || text.contains("set a reminder") || text.contains("reminder") ||
            text.contains("yaad dilana") || text.contains("yaad dila do") || text.contains("रिमाइंडर") || text.contains("याद दिलाना")) {
            val (title, due) = extractReminderDetails(input)
            return ParsedJarvisCommand.Reminder(title = title, dueTime = due)
        }

        // Notes: e.g. "take a note buy milk", "note Stark tech idea", "note banao", "likh lo"
        if (text.startsWith("take a note") || text.startsWith("note:") || text.startsWith("new note") ||
            text.startsWith("write note") || text.contains("note banao") || text.contains("note likho") ||
            text.contains("likh lo") || text.contains("नोट बनाओ") || text.contains("नोट लिखो")) {
            val content = input
                .replace(Regex("(?i)^(take a note|new note|write note|note banao|note likho|likh lo|नोट बनाओ|नोट लिखो|note:?)\\s*(that|to|about|ki|yeh)?\\s*"), "")
                .trim()
            val title = if (content.length > 25) content.take(25) + "..." else content
            return ParsedJarvisCommand.Note(title = if (title.isBlank()) "Quick Note" else title, content = content)
        }

        // Diagnostics / System checks (English, Hindi & Hinglish)
        if (text.contains("diagnostic") || text.contains("system status") || text.contains("telemetry check") ||
            text.contains("hardware scan") || text.contains("battery kitni") || text.contains("battery level") ||
            text.contains("status batao") || text.contains("kaisa chal raha") || text.contains("सिस्टम स्टेटस") ||
            text.contains("बैटरी कितनी")) {
            return ParsedJarvisCommand.Diagnostic("ALL")
        }

        // Daily Briefing / Greetings
        if (text == "good morning" || text.contains("daily briefing") || text.contains("morning report") ||
            text.contains("status briefing") || text.contains("aaj ka report") || text.contains("shubh prabhat") ||
            text.contains("namaste") || text.contains("शुभ प्रभात") || text.contains("नमस्ते") || text.contains("aaj ka haal")) {
            return ParsedJarvisCommand.DailyBriefing
        }

        // Weather: e.g. "weather", "weather in Mumbai", "mausam kaisa hai", "मौसम कैसा है"
        if (text.contains("weather") || text.contains("forecast") || text.contains("temperature") ||
            text.contains("atmospheric") || text.contains("mausam") || text.contains("barish") ||
            text.contains("मौसम") || text.contains("बारिश")) {
            val location = extractLocation(text)
            return ParsedJarvisCommand.Weather(location)
        }

        return ParsedJarvisCommand.GeneralQuery(input)
    }

    private fun extractTimerSeconds(text: String): Int {
        var totalSec = 0
        // Minutes match
        val minPattern = Pattern.compile("(\\d+)\\s*(min|minute|minutes|m\\b)")
        val minMatcher = minPattern.matcher(text)
        if (minMatcher.find()) {
            val mins = minMatcher.group(1)?.toIntOrNull() ?: 0
            totalSec += mins * 60
        }

        // Seconds match
        val secPattern = Pattern.compile("(\\d+)\\s*(sec|second|seconds|s\\b)")
        val secMatcher = secPattern.matcher(text)
        if (secMatcher.find()) {
            val secs = secMatcher.group(1)?.toIntOrNull() ?: 0
            totalSec += secs
        }

        // Hour match
        val hrPattern = Pattern.compile("(\\d+)\\s*(hour|hours|hr|hrs|h\\b)")
        val hrMatcher = hrPattern.matcher(text)
        if (hrMatcher.find()) {
            val hrs = hrMatcher.group(1)?.toIntOrNull() ?: 0
            totalSec += hrs * 3600
        }

        if (totalSec == 0) {
            // Check standalone number: "timer 5" -> assume minutes
            val numPattern = Pattern.compile("timer\\s+(\\d+)")
            val numMatcher = numPattern.matcher(text)
            if (numMatcher.find()) {
                val num = numMatcher.group(1)?.toIntOrNull() ?: 0
                totalSec = num * 60
            }
        }

        return if (totalSec > 0) totalSec else 300 // default 5m
    }

    private fun extractTimerLabel(text: String): String {
        val forIndex = text.indexOf(" for ")
        return if (forIndex != -1 && forIndex + 5 < text.length) {
            text.substring(forIndex + 5).capitalizeFirstLetter()
        } else {
            "Countdown Timer"
        }
    }

    private fun extractReminderDetails(input: String): Pair<String, String> {
        val clean = input.replace(Regex("(?i)^(remind me to|set a reminder to|set reminder for|remind me)\\s*"), "").trim()
        val atIndex = clean.lastIndexOf(" at ")
        val tomorrowIndex = clean.lastIndexOf(" tomorrow")

        var title = clean
        var dueTime = "Today"

        if (atIndex != -1) {
            title = clean.substring(0, atIndex).trim()
            dueTime = clean.substring(atIndex + 4).trim()
        } else if (tomorrowIndex != -1) {
            title = clean.substring(0, tomorrowIndex).trim()
            dueTime = "Tomorrow"
        }

        if (title.isBlank()) title = "Important Task"
        return Pair(title.capitalizeFirstLetter(), dueTime)
    }

    private fun extractLocation(text: String): String {
        val inIndex = text.indexOf(" in ")
        val forIndex = text.indexOf(" for ")
        return when {
            inIndex != -1 && inIndex + 4 < text.length -> text.substring(inIndex + 4).trim().capitalizeFirstLetter()
            forIndex != -1 && forIndex + 5 < text.length -> text.substring(forIndex + 5).trim().capitalizeFirstLetter()
            else -> "Local City"
        }
    }

    private fun String.capitalizeFirstLetter(): String {
        return if (isEmpty()) this else this.substring(0, 1).uppercase() + this.substring(1)
    }
}
