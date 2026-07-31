package com.funnyenglish.core.domain.repository

import com.funnyenglish.core.domain.model.GuestSession
import com.funnyenglish.core.domain.model.GuestTestProgress

interface GuestProgressRepository {
    fun getSession(): GuestSession?
    fun saveSession(session: GuestSession)
    fun clearSession()
    fun addTestProgress(progress: GuestTestProgress)
    fun getTestProgress(testId: String): GuestTestProgress?
    fun hasProgress(): Boolean
}
