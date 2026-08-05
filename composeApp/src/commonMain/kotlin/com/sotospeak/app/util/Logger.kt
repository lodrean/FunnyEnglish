package com.sotospeak.app.util

/**
 * Дедупликация (OpenSpec add-client-logging): единая реализация логгера — в shared.
 * WARN/ERROR дополнительно складываются в remote-очередь для отправки на backend.
 */
typealias Logger = com.sotospeak.shared.util.Logger
