package com.sotospeak.exception

/** Неверные email/пароль при логине → 401 (SEC: вместо прежнего 400). */
class InvalidCredentialsException(message: String = "Invalid credentials") : RuntimeException(message)

/** Refresh-токен невалиден/истёк/отозван/уже ротирован → 401 (SEC: вместо прежнего 400). */
class InvalidRefreshTokenException(message: String = "Invalid refresh token") : RuntimeException(message)
