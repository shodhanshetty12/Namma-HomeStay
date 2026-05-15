package com.namma.homestay.data

import android.content.Context
import com.namma.homestay.data.firestore.FirestoreNammaRepository
import com.namma.homestay.data.repo.FakeNammaRepository
import com.namma.homestay.data.repo.NammaRepository

object AppContainer {
    private var repositoryInstance: NammaRepository? = null

    fun getRepository(context: Context): NammaRepository {
        return repositoryInstance ?: synchronized(this) {
            val instance = if (AppConfig.useFakeBackend) {
                FakeNammaRepository(hostId = AppConfig.hostId)
            } else {
                FirestoreNammaRepository(context.applicationContext)
            }
            repositoryInstance = instance
            instance
        }
    }
}
