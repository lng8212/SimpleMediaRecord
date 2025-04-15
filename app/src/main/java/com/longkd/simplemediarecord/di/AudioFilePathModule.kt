package com.longkd.simplemediarecord.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AudioFilePathModule {

    @Provides
    @Singleton
    fun provideAudioFilePath(
        @ApplicationContext context: Context
    ): String = context.filesDir.absolutePath + "/demo.3gp"
}