package com.funnyenglish.app

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import coil3.compose.LocalPlatformContext
import com.funnyenglish.app.di.InitializeCoil
import com.funnyenglish.app.di.provideAppConfig
import io.github.aakira.napier.Antilog
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.LogLevel
import io.github.aakira.napier.Napier
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Custom logger that writes to both console and file
 */
class FileAndConsoleLogger : Antilog() {
    private val logDir = File(System.getProperty("user.home"), ".funnyenglish/logs")
    private val logFile: File
    
    init {
        logDir.mkdirs()
        logFile = File(logDir, "app-${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))}.log")
    }
    
    fun getLogFilePath(): String = logFile.absolutePath
    
    override fun performLog(
        priority: LogLevel,
        tag: String?,
        throwable: Throwable?,
        message: String?
    ) {
        // Console output
        val consoleOutput = buildString {
            append("[${priority.name}] ")
            tag?.let { append("[$it] ") }
            message?.let { append(it) }
            throwable?.let { append(" - ${it.message}") }
        }
        println(consoleOutput)
        
        // File output with timestamp
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        val fileLine = buildString {
            append("[$timestamp] ${priority.name}")
            tag?.let { append(" [$it]") }
            append(": ")
            message?.let { append(it) }
            throwable?.let { append(" - ${it.stackTraceToString()}") }
            append("\n")
        }
        
        try {
            logFile.appendText(fileLine)
        } catch (e: Exception) {
            println("[ERROR] Failed to write to log file: ${e.message}")
        }
    }
}

fun main() {
    // Initialize logging
    val logger = FileAndConsoleLogger()
    Napier.base(logger)
    
    val appConfig = provideAppConfig()
    
    Napier.i("=== FunnyEnglish Desktop Started ===")
    Napier.i("API Base URL: ${appConfig.baseUrl}")
    Napier.i("Network Logs Enabled: ${appConfig.enableNetworkLogs}")
    Napier.i("Log file: ${logger.getLogFilePath()}")
    
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "FunnyEnglish"
        ) {
            InitializeCoil()
            App()
        }
    }
    
    Napier.i("=== FunnyEnglish Desktop Closed ===")
}
