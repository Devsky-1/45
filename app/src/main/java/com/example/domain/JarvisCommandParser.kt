package com.example.domain

import java.util.Locale
import java.util.regex.Pattern

sealed interface ParsedJarvisCommand {
    data class Flashlight(val enable: Boolean) : ParsedJarvisCommand
    data class Timer(val seconds: Int, val label: String) : ParsedJarvisCommand
    data class Weather(val location: String) : ParsedJarvisCommand
    data class Reminder(val title: String, val dueTime: String, val priority: String = "NORMAL") : ParsedJarvisCommand
    data class Note(val title: String, val content: String) : ParsedJarvisCommand
    data class Diagnostic(val component: String = "ALL") : ParsedJarvisCommand
    data class Protocol(val protocolName: String) : ParsedJarvisCommand
    data class OpenApp(val appName: String, val packageName: String? = null) : ParsedJarvisCommand
    object GoHome : ParsedJarvisCommand
    data class MathCalculation(val expression: String, val result: String) : ParsedJarvisCommand
    object StopSpeaking : ParsedJarvisCommand
    object TimeQuery : ParsedJarvisCommand
    object DateQuery : ParsedJarvisCommand
    object DailyBriefing : ParsedJarvisCommand
    object ClearHistory : ParsedJarvisCommand
    data class GeneralQuery(val query: String) : ParsedJarvisCommand
}

object JarvisCommandParser {

    fun parse(input: String): ParsedJarvisCommand {
        val text = input.trim().lowercase()

        // Immediate Stop / Silence commands
        if (text == "stop" || text == "cancel" || text == "shut up" || text == "be quiet" ||
            text == "chup" || text == "ruko" || text == "band karo" || text == "रुको" || text == "चुप रहो" ||
            text.startsWith("stop speaking") || text.startsWith("stop talking")) {
            return ParsedJarvisCommand.StopSpeaking
        }

        // Open Apps commands
        if (text.startsWith("open ") || text.startsWith("launch ") || text.contains(" kholo") || text.contains(" खोलो")) {
            when {
                text.contains("youtube") || text.contains("यू ट्यूब") ->
                    return ParsedJarvisCommand.OpenApp("YouTube", "com.google.android.youtube")
                text.contains("chrome") || text.contains("browser") || text.contains("क्रोम") ->
                    return ParsedJarvisCommand.OpenApp("Chrome", "com.android.chrome")
                text.contains("camera") || text.contains("कैमरा") ->
                    return ParsedJarvisCommand.OpenApp("Camera", "android.media.action.IMAGE_CAPTURE")
                text.contains("settings") || text.contains("setting") || text.contains("सेटिंग") ->
                    return ParsedJarvisCommand.OpenApp("Settings", "android.settings.SETTINGS")
                text.contains("calculator") || text.contains("कैलकुलेटर") ->
                    return ParsedJarvisCommand.OpenApp("Calculator", "com.google.android.calculator")
                text.contains("maps") || text.contains("map") || text.contains("नक्शा") ->
                    return ParsedJarvisCommand.OpenApp("Maps", "com.google.android.apps.maps")
                else -> {
                    val app = text.replace(Regex("(?i)^(open|launch|kholo|खोलो)\\s*"), "").trim()
                    if (app.isNotBlank()) return ParsedJarvisCommand.OpenApp(app)
                }
            }
        }

        // Go Home command
        if (text == "go home" || text == "home screen" || text == "open home" || text == "return home" ||
            text == "home" || text == "ghar jao" || text == "home jao" || text == "होम स्क्रीन" || text == "होम जाओ") {
            return ParsedJarvisCommand.GoHome
        }

        // Direct Math Calculation ("What is 25 times 4?" -> "100.")
        val mathCmd = tryParseMath(text)
        if (mathCmd != null) {
            return mathCmd
        }

        // Time Query
        if (text.contains("what time") || text.contains("current time") || text.contains("tell me the time") ||
            text.contains("time kya hai") || text.contains("time kya hua") || text.contains("kitne baje") ||
            text.contains("समय क्या है") || text.contains("टाइम क्या है") || text == "time") {
            return ParsedJarvisCommand.TimeQuery
        }

        // Date Query
        if (text.contains("what date") || text.contains("today's date") || text.contains("what is the date") ||
            text.contains("date kya hai") || text.contains("aaj ki date") || text.contains("आज की तारीख") ||
            text == "date" || text == "aaj konsa din hai") {
            return ParsedJarvisCommand.DateQuery
        }

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

    private fun tryParseMath(rawText: String): ParsedJarvisCommand.MathCalculation? {
        val clean = rawText.lowercase()
            .replace("what is", "")
            .replace("what's", "")
            .replace("calculate", "")
            .replace("solve", "")
            .replace("tell me", "")
            .replace("how much is", "")
            .replace("kitna hoga", "")
            .replace("kya hoga", "")
            .trim()

        // Replace textual operator words with mathematical symbols
        val normalized = clean
            .replace("multiplied by", "*")
            .replace("times", "*")
            .replace("into", "*")
            .replace("guna", "*")
            .replace("गुणा", "*")
            .replace("divided by", "/")
            .replace("divide by", "/")
            .replace("over", "/")
            .replace("bhag", "/")
            .replace("भाग", "/")
            .replace("plus", "+")
            .replace("add", "+")
            .replace("jod", "+")
            .replace("जोड़", "+")
            .replace("minus", "-")
            .replace("ghata", "-")
            .replace("घटाओ", "-")
            .replace("x", "*")
            .trim()

        val mathRegex = Regex("^([0-9]+(?:\\.[0-9]+)?)\\s*([\\+\\-\\*/])\\s*([0-9]+(?:\\.[0-9]+)?)$")
        val match = mathRegex.find(normalized) ?: return null

        val num1 = match.groupValues[1].toDoubleOrNull() ?: return null
        val op = match.groupValues[2]
        val num2 = match.groupValues[3].toDoubleOrNull() ?: return null

        val calcResult: Double = when (op) {
            "+" -> num1 + num2
            "-" -> num1 - num2
            "*" -> num1 * num2
            "/" -> {
                if (num2 == 0.0) return ParsedJarvisCommand.MathCalculation(rawText, "Undefined division by zero, sir.")
                num1 / num2
            }
            else -> return null
        }

        // Format: integer results formatted cleanly as "100." per Jarvis style, decimals to 2-4 digits
        val formatted = if (calcResult % 1.0 == 0.0) {
            "${calcResult.toLong()}."
        } else {
            val rounded = String.format(Locale.getDefault(), "%.4f", calcResult).trimEnd('0').trimEnd('.')
            "$rounded."
        }

        return ParsedJarvisCommand.MathCalculation(expression = rawText, result = formatted)
    }
}
