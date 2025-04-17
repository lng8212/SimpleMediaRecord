package com.longkd.simplemediarecord.audio_recorder.playback.di

import android.content.Context
import com.longkd.simplemediarecord.audio_recorder.AudioFocusHandler
import com.longkd.simplemediarecord.audio_recorder.playback.AudioOutputDeviceHandler
import com.longkd.simplemediarecord.audio_recorder.playback.DefaultAudioPlayer
import com.longkd.simplemediarecord.audio_recorder.playback.itf.AudioDeviceHandler
import com.longkd.simplemediarecord.audio_recorder.playback.itf.AudioPlayer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AudioPlaybackModule {

    @Provides
    @Singleton
    @Named("output_handler")
    fun provideAudioDeviceHandler(@ApplicationContext context: Context): AudioDeviceHandler {
        return AudioOutputDeviceHandler(context)
    }

    @Provides
    @Singleton
    fun provideAudioPlayer(
        audioFocusHandler: AudioFocusHandler,
        @Named("output_handler") audioDeviceHandler: AudioDeviceHandler
    ): AudioPlayer {
        return DefaultAudioPlayer(audioFocusHandler, audioDeviceHandler)
    }
}