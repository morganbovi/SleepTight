package com.apkrocket.sleeptight.audio

import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

/** Produces one continuous stream of [-1.0, 1.0] samples at [sampleRate] Hz. Stateful across calls. */
interface Generator {
    fun nextSample(): Double
}

private fun Double.clamp1() = coerceIn(-1.0, 1.0)

/** A basic one-pole low-pass filter, reused as a building block by several generators. */
private class OnePoleLowPass(private var alpha: Double) {
    var y = 0.0
        private set

    fun setAlpha(newAlpha: Double) {
        alpha = newAlpha
    }

    fun process(x: Double): Double {
        y += alpha * (x - y)
        return y
    }
}

/** Full-spectrum hiss. The raw material every other texture below is built from. */
class WhiteNoiseGenerator(private val rnd: Random = Random(System.nanoTime())) : Generator {
    override fun nextSample(): Double = rnd.nextDouble(-1.0, 1.0)
}

/** Paul Kellet's "economy" pink-noise filter: -3dB/octave rolloff, softer than white noise. */
class PinkNoiseGenerator(private val rnd: Random = Random(System.nanoTime())) : Generator {
    private var b0 = 0.0
    private var b1 = 0.0
    private var b2 = 0.0
    private var b3 = 0.0
    private var b4 = 0.0
    private var b5 = 0.0
    private var b6 = 0.0

    override fun nextSample(): Double {
        val white = rnd.nextDouble(-1.0, 1.0)
        b0 = 0.99886 * b0 + white * 0.0555179
        b1 = 0.99332 * b1 + white * 0.0750759
        b2 = 0.96900 * b2 + white * 0.1538520
        b3 = 0.86650 * b3 + white * 0.3104856
        b4 = 0.55000 * b4 + white * 0.5329522
        b5 = -0.7616 * b5 - white * 0.0168980
        val pink = b0 + b1 + b2 + b3 + b4 + b5 + b6 + white * 0.5362
        b6 = white * 0.115926
        return (pink * 0.11).clamp1()
    }
}

/** Brownian (red) noise: a leaky integration of white noise, deep and rumbly. */
class BrownNoiseGenerator(private val rnd: Random = Random(System.nanoTime())) : Generator {
    private var lastOut = 0.0

    override fun nextSample(): Double {
        val white = rnd.nextDouble(-1.0, 1.0)
        lastOut = (lastOut + 0.02 * white) / 1.02
        return (lastOut * 3.5).clamp1()
    }
}

/** Broadband hiss (rain sheet) plus sparse decaying transients (individual droplets). */
class RainGenerator(private val sampleRate: Int, private val rnd: Random = Random(System.nanoTime())) : Generator {
    private val dcTracker = OnePoleLowPass(0.002)
    private val smoother = OnePoleLowPass(0.35)
    private var dropEnvelope = 0.0

    override fun nextSample(): Double {
        val white = rnd.nextDouble(-1.0, 1.0)
        val low = dcTracker.process(white)
        val highPassed = white - low
        val hiss = smoother.process(highPassed) * 1.2

        var sample = hiss * 0.5
        if (rnd.nextDouble() < 0.0008 * (44100.0 / sampleRate)) {
            dropEnvelope = 0.6 + rnd.nextDouble() * 0.4
        }
        if (dropEnvelope > 0.001) {
            sample += rnd.nextDouble(-1.0, 1.0) * dropEnvelope * 0.7
            dropEnvelope *= 0.965
        }
        return sample.clamp1()
    }
}

/** Slow swelling/receding brown noise with a touch of foam hiss at each crest. */
class OceanWavesGenerator(private val sampleRate: Int, private val rnd: Random = Random(System.nanoTime())) : Generator {
    private var lastOut = 0.0
    private var phase = 0.0
    private var phaseInc = phaseIncFor(7.5)

    private fun phaseIncFor(waveDurationSeconds: Double) = (2.0 * PI) / (sampleRate * waveDurationSeconds)

    override fun nextSample(): Double {
        val white = rnd.nextDouble(-1.0, 1.0)
        lastOut = (lastOut + 0.02 * white) / 1.02
        val base = (lastOut * 3.0).clamp1()

        phase += phaseInc
        if (phase > 2 * PI) {
            phase -= 2 * PI
            phaseInc = phaseIncFor(6.0 + rnd.nextDouble() * 5.0)
        }
        var envelope = (sin(phase - PI / 2.0) + 1.0) / 2.0
        envelope = 0.35 + 0.65 * envelope

        val foam = white * 0.15 * envelope * envelope
        return (base * envelope * 0.9 + foam).clamp1()
    }
}

/** Filtered noise with a slowly wandering cutoff (gusts) and gentle amplitude swell. */
class WindGenerator(private val sampleRate: Int, private val rnd: Random = Random(System.nanoTime())) : Generator {
    private val filter = OnePoleLowPass(0.1)
    private var alpha = 0.1
    private var targetAlpha = 0.1
    private var samplesUntilRetarget = sampleRate
    private var gustPhase = 0.0

    override fun nextSample(): Double {
        val white = rnd.nextDouble(-1.0, 1.0)

        alpha += (targetAlpha - alpha) * 0.0005
        filter.setAlpha(alpha)
        samplesUntilRetarget--
        if (samplesUntilRetarget <= 0) {
            targetAlpha = 0.03 + rnd.nextDouble() * 0.25
            samplesUntilRetarget = (sampleRate * (1.0 + rnd.nextDouble())).toInt()
        }

        val filtered = filter.process(white)
        gustPhase += (2.0 * PI) / (sampleRate * 4.0)
        val ampLfo = 0.6 + 0.4 * sin(gustPhase)
        return (filtered * 2.2 * ampLfo).clamp1()
    }
}

/** Low brown-noise rumble punctuated by randomly-timed decaying "pop" crackles. */
class CampfireGenerator(private val rnd: Random = Random(System.nanoTime())) : Generator {
    private var lastOut = 0.0
    private var popEnvelope = 0.0

    override fun nextSample(): Double {
        val white = rnd.nextDouble(-1.0, 1.0)
        lastOut = (lastOut + 0.02 * white) / 1.02
        val base = (lastOut * 2.5).clamp1() * 0.35

        if (rnd.nextDouble() < 0.0015) {
            popEnvelope = 0.5 + rnd.nextDouble() * 0.5
        }
        var pop = 0.0
        if (popEnvelope > 0.001) {
            pop = rnd.nextDouble(-1.0, 1.0) * popEnvelope
            popEnvelope *= 0.85
        }
        return (base + pop * 0.8).clamp1()
    }
}

/** A synthesized "lub-dub" heartbeat: two damped low sine pulses per cardiac cycle. */
class HeartbeatGenerator(private val sampleRate: Int, private val rnd: Random = Random(System.nanoTime())) : Generator {
    private var phaseSamples = 0
    private var periodSeconds = 0.9

    override fun nextSample(): Double {
        val t = phaseSamples / sampleRate.toDouble()

        var sample = 0.0
        val tLub = t
        if (tLub in 0.0..0.15) {
            sample += sin(2 * PI * 70 * tLub) * exp(-tLub * 18) * 1.0
        }
        val dubStart = periodSeconds * 0.32
        val tDub = t - dubStart
        if (tDub in 0.0..0.15) {
            sample += sin(2 * PI * 55 * tDub) * exp(-tDub * 20) * 0.7
        }

        phaseSamples++
        if (phaseSamples >= (periodSeconds * sampleRate).toInt()) {
            phaseSamples = 0
            periodSeconds = 0.83 + rnd.nextDouble() * 0.15
        }
        return (sample * 0.9).clamp1()
    }
}

fun createGenerator(type: SoundType, sampleRate: Int): Generator = when (type) {
    SoundType.WHITE_NOISE -> WhiteNoiseGenerator()
    SoundType.PINK_NOISE -> PinkNoiseGenerator()
    SoundType.BROWN_NOISE -> BrownNoiseGenerator()
    SoundType.RAIN -> RainGenerator(sampleRate)
    SoundType.OCEAN_WAVES -> OceanWavesGenerator(sampleRate)
    SoundType.WIND -> WindGenerator(sampleRate)
    SoundType.CAMPFIRE -> CampfireGenerator()
    SoundType.HEARTBEAT -> HeartbeatGenerator(sampleRate)
}
