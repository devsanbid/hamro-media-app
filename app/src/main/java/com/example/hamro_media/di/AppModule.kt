package com.example.hamro_media.di

import android.content.Context
import com.example.hamro_media.repository.AuthRepository
import com.example.hamro_media.repository.AuthRepositoryImpl
import com.example.hamro_media.repository.PostRepository
import com.example.hamro_media.repository.PostRepositoryImpl

object AppModule {
    fun provideAuthRepository(): AuthRepository {
        return AuthRepositoryImpl()
    }
    
    fun providePostRepository(context: Context): PostRepository {
        return PostRepositoryImpl(context = context)
    }
}