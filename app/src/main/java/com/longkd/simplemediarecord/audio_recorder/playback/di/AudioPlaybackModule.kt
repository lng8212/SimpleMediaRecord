package com.longkd.simplemediarecord.audio_recorder.playback.di

import android.content.Context
import com.longkd.simplemediarecord.audio_recorder.playback.DefaultAudioDeviceHandler
import com.longkd.simplemediarecord.audio_recorder.playback.DefaultAudioFocusHandler
import com.longkd.simplemediarecord.audio_recorder.playback.DefaultAudioPlayer
import com.longkd.simplemediarecord.audio_recorder.playback.itf.AudioDeviceHandler
import com.longkd.simplemediarecord.audio_recorder.playback.itf.AudioFocusHandler
import com.longkd.simplemediarecord.audio_recorder.playback.itf.AudioPlayer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AudioPlaybackModule {

    @Provides
    @Singleton
    fun provideAudioFocusHandler(@ApplicationContext context: Context): AudioFocusHandler {
        return DefaultAudioFocusHandler(context)
    }

    @Provides
    @Singleton
    fun provideAudioDeviceHandler(@ApplicationContext context: Context): AudioDeviceHandler {
        return DefaultAudioDeviceHandler(context)
    }

    @Provides
    @Singleton
    fun provideAudioPlayer(
        @ApplicationContext context: Context,
        audioFocusHandler: AudioFocusHandler,
        audioDeviceHandler: AudioDeviceHandler
    ): AudioPlayer {
        return DefaultAudioPlayer(audioFocusHandler, audioDeviceHandler)
    }
}