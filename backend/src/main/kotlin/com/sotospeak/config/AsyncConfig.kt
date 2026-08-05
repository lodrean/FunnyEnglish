package com.sotospeak.config

import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync

/** Асинхронная отправка писем (EmailService.sendVerification) — SMTP-таймауты не растягивают register. */
@Configuration
@EnableAsync
class AsyncConfig
