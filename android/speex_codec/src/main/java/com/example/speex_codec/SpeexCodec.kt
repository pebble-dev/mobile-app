package com.example.speex_codec

import android.media.MediaCodec
import java.nio.ByteBuffer

class SpeexCodec(private val sampleRate: Long, private val bitRate: Int): AutoCloseable {
    init {
        initNative()
    }
    private val speexDecBits: Long = initSpeexBits()
    private val speexDecState: Long = initDecState(sampleRate, bitRate)

    /**
     * Decode a frame of audio data.
     * @param encodedFrame The encoded frame to decode.
     * @param decodedFrame The buffer to store the decoded frame in.
     *
     */
    fun decodeFrame(encodedFrame: ByteArray, decodedFrame: ByteBuffer, hasHeaderByte: Boolean = true): SpeexDecodeResult {
        return SpeexDecodeResult.fromInt(decode(encodedFrame, decodedFrame, hasHeaderByte))
    }

    override fun close() {
        destroySpeexBits(speexDecBits)
        destroyDecState(speexDecState)
    }

    private external fun initNative()
    private external fun decode(encodedFrame: ByteArray, decodedFrame: ByteBuffer, hasHeaderByte: Boolean): Int
    private external fun initSpeexBits(): Long
    private external fun initDecState(sampleRate: Long, bitRate: Int): Long
    private external fun destroySpeexBits(speexBits: Long)
    private external fun destroyDecState(decState: Long)

    companion object {
        // Used to load the 'speex_codec' library on application startup.
        init {
            System.loadLibrary("speex_codec")
        }
    }
}

enum class SpeexDecodeResult {
    Success,
    EndOfStream,
    CorruptStream;

    companion object {
        fun fromInt(value: Int) = when (value) {
            0 -> Success
            -1 -> EndOfStream
            -2 -> CorruptStream
            else -> throw IllegalArgumentException("Invalid value for SpeexDecodeResult")
        }
    }
}