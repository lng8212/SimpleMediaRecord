package com.longkd.simplemediarecord.audio_recorder.di

import android.content.Context
import com.longkd.simplemediarecord.audio_recorder.AudioFocusHandler
import com.longkd.simplemediarecord.audio_recorder.DefaultAudioFocusHandler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CommonModule {
    @Provides
    @Singleton
    fun provideAudioFocusHandler(@ApplicationContext context: Context): AudioFocusHandler {
        return DefaultAudioFocusHandler(context)
    }
}