package com.dicoding.picodiploma.loginwithanimation.di

import android.content.Context
import com.dheril.dicodingstoryapp.data.UserPreference
import com.dheril.dicodingstoryapp.data.UserRepository
import com.dheril.dicodingstoryapp.data.dataStore
import com.dheril.dicodingstoryapp.data.database.StoryDatabase
import com.dheril.dicodingstoryapp.data.remote.retrofit.ApiConfig

object Injection {
    fun provideRepository(context: Context): UserRepository {
        val database = StoryDatabase.getDatabase(context)
        val pref = UserPreference.getInstance(context.dataStore)
        val apiService = ApiConfig.getApiService()
        return UserRepository.getInstance(pref, apiService, database)
    }
}