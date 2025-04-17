package com.longkd.simplemediarecord.audio_recorder.recorder.di

import android.content.Context
import com.longkd.simplemediarecord.audio_recorder.recorder.AudioInputDeviceHandler
import com.longkd.simplemediarecord.audio_recorder.recorder.DefaultAudioRecorderFactory
import com.longkd.simplemediarecord.audio_recorder.recorder.DefaultTimerHandler
import com.longkd.simplemediarecord.audio_recorder.recorder.itf.AudioRecorderFactory
import com.longkd.simplemediarecord.audio_recorder.recorder.itf.TimerHandler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AudioRecorderModule {
    @Provides
    @Singleton
    fun provideTimerHandler(): TimerHandler {
        return DefaultTimerHandler()
    }

    @Provides
    @Singleton
    fun provideAudioRecorderFactory(
        @ApplicationContext context: Context,
        @Named("audioFilePath") audioFilePath: String
    ): AudioRecorderFactory {
        return DefaultAudioRecorderFactory(context, audioFilePath)
    }

    @Provides
    @Singleton
    fun provideAudioInputDeviceHandler(@ApplicationContext context: Context) =
        AudioInputDeviceHandler(context)

    @Provides
    @Named("audioFilePath")
    fun provideAudioFilePath(@ApplicationContext context: Context): String {
        return context.filesDir.absolutePath + "/demo.3gp"
    }
}