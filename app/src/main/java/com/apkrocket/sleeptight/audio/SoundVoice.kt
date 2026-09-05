package com.apkrocket.sleeptight.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.math.max

private const val SAMPLE_RATE = 44100
private const val CHUNK_SAMPLES = 2048

/**
 * Streams one continuously-synthesized [SoundType] to its own [AudioTrack] on a dedicated
 * thread, so it loops forever with no seams and no bundled audio asset.
 */
class SoundVoice(private val type: SoundType) {
    private var audioTrack: AudioTrack? = null
    private var thread: Thread? = null

    @Volatile
    private var running = false

    fun start() {
        if (running) return
        running = true

        val minBuffer = AudioTrack.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val bufferSize = max(minBuffer, CHUNK_SAMPLES * 2) * 4

        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(SAMPLE_RATE)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()
        // Loudness is controlled entirely by the device's STREAM_MUSIC volume, not here.
        track.setVolume(1f)
        audioTrack = track

        thread = Thread({
            val generator = createGenerator(type, SAMPLE_RATE)
            val buffer = ShortArray(CHUNK_SAMPLES)
            track.play()
            while (running) {
                for (i in buffer.indices) {
                    buffer[i] = (generator.nextSample() * Short.MAX_VALUE).toInt().toShort()
                }
                track.write(buffer, 0, buffer.size)
            }
        }, "SoundVoice-${type.name}").apply {
            isDaemon = true
            priority = Thread.MAX_PRIORITY
            start()
        }
    }

    fun stop() {
        running = false
        thread?.join(300)
        thread = null
        audioTrack?.let {
            runCatching {
                it.stop()
                it.flush()
                it.release()
            }
        }
        audioTrack = null
    }
}
