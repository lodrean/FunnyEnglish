package com.sotospeak.core.domain.repository

import com.sotospeak.core.domain.model.GuestSession
import com.sotospeak.core.domain.model.GuestTestProgress

interface GuestProgressRepository {
    fun getSession(): GuestSession?
    fun saveSession(session: GuestSession)
    fun clearSession()
    fun addTestProgress(progress: GuestTestProgress)
    fun getTestProgress(testId: String): GuestTestProgress?
    fun hasProgress(): Boolean
}
