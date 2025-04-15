package com.longkd.simplemediarecord.recorder

interface AudioRecorder {

    fun start()

    fun pause()

    fun resume()

    /**
     * Stop the recording, kill the recorder, make sure file is saved, clean
     * resources up. It is a suspend function because it may take a while to
     * finish all these tasks, incl. waiting for any threads to finish,
     * and the Service can only be safely killed after it finishes
     */
    suspend fun stop()
}