package com.sotospeak.controller

import com.sotospeak.dto.*
import com.sotospeak.service.AuthService
import com.sotospeak.service.EmailVerificationService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService,
    private val emailVerificationService: EmailVerificationService
) {
    @PostMapping("/register")
    fun register(@Valid @RequestBody request: RegisterRequest): ResponseEntity<RegisterResponse> {
        return ResponseEntity.ok(authService.register(request))
    }

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<AuthResponse> {
        return ResponseEntity.ok(authService.login(request))
    }

    /** Подтверждение email по ссылке из письма — публичный, отдаёт HTML-страницу. */
    @GetMapping("/verify-email", produces = [MediaType.TEXT_HTML_VALUE])
    fun verifyEmail(@RequestParam token: String): ResponseEntity<String> {
        if (!emailVerificationService.enabled) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        }
        val success = emailVerificationService.confirm(token)
        val (title, text) = if (success) {
            "Почта подтверждена!" to "Теперь можно войти в So to speak."
        } else {
            "Ссылка недействительна" to "Токен истёк или уже использован — запросите новое письмо в приложении."
        }
        return ResponseEntity.ok(
            """<!DOCTYPE html><html lang="ru"><head><meta charset="utf-8"><title>$title</title>
                |<meta name="viewport" content="width=device-width, initial-scale=1"></head>
                |<body style="font-family:sans-serif;background:#EEF3FF;display:flex;min-height:100vh;align-items:center;justify-content:center;margin:0">
                |<div style="background:#fff;border-radius:22px;padding:40px;max-width:420px;text-align:center">
                |<h1 style="color:#2D3561;font-size:24px">$title</h1>
                |<p style="color:#58609A">$text</p></div></body></html>""".trimMargin()
        )
    }

    /** Повторная отправка письма — всегда 200 (anti-enumeration). */
    @PostMapping("/resend-verification")
    fun resendVerification(@Valid @RequestBody request: ResendVerificationRequest): ResponseEntity<Void> {
        if (!emailVerificationService.enabled) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        }
        emailVerificationService.resend(request.email)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/oauth/{provider}")
    fun oauthLogin(
        @PathVariable provider: String,
        @Valid @RequestBody request: OAuthRequest
    ): ResponseEntity<AuthResponse> {
        return ResponseEntity.ok(authService.oauthLogin(provider, request))
    }

    @PostMapping("/refresh")
    fun refresh(@Valid @RequestBody request: RefreshTokenRequest): ResponseEntity<AuthResponse> {
        return ResponseEntity.ok(authService.refreshToken(request))
    }
}
