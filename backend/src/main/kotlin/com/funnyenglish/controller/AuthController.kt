package com.funnyenglish.controller

import com.funnyenglish.dto.*
import com.funnyenglish.service.AuthService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService
) {
    @PostMapping("/register")
    fun register(@Valid @RequestBody request: RegisterRequest): ResponseEntity<AuthResponse> {
        return ResponseEntity.ok(authService.register(request))
    }

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<AuthResponse> {
        return ResponseEntity.ok(authService.login(request))
    }

    @PostMapping("/oauth/{provider}")
    fun oauthLogin(
        @PathVariable provider: String,
        @Valid @RequestBody request: OAuthRequest
    ): ResponseEntity<AuthResponse> {
        return ResponseEntity.ok(authService.oauthLogin(provider, request))
    }
}
