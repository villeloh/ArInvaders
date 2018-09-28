package villealla.com.arinvaders.Sound

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import villealla.com.arinvaders.R
import java.util.concurrent.ThreadLocalRandom

enum class SoundEffects(val effectName: Int, val volumeLevel: Float = 1f, var id: Int = 0, val effectId: String = "") {
    LASER(R.raw.laser, 0.75f),
    EXPLOSION(R.raw.bomb),
    EARTH_HIT_1(R.raw.scream_1, 0.1f, 0, "planetEffect"),
    EARTH_HIT_2(R.raw.scream_2, 0.4f, 0, "planetEffect"),
    EARTH_HIT_3(R.raw.scream_3, 0.4f, 0, "planetEffect")
}

object SoundEffectPlayer {

    private val soundPool: SoundPool
    private val random = ThreadLocalRandom.current()
    private val earthEffects = mutableListOf<SoundEffects>()
    private var earthEffectsSize = 0

    init {

        val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_GAME)
                .build()

        soundPool = SoundPool.Builder()
                .setMaxStreams(20)
                .setAudioAttributes(audioAttributes)
                .build()

        initEarthEffects()
    }

    fun loadAllEffects(context: Context) {
        SoundEffects.values().forEach {
            it.id = soundPool.load(context, it.effectName, 1)
        }
    }

    fun playEffect(soundEffect: SoundEffects) {
        if (soundEffect.id != 0)
            soundPool.play(soundEffect.id, soundEffect.volumeLevel, soundEffect.volumeLevel, 1, 0, 1f)
    }

    // it seems a bit clumsy and unnecessary, as the effects enum is static.
    // maybe you can think of a better solution
    private fun initEarthEffects() {
        var i = 0
        SoundEffects.values().forEach {

            if (it.effectId == "planetEffect") {
                earthEffects.add(it)
                i += 1
            }
        }
        earthEffectsSize = earthEffects.size
    }

    fun randomEarthEffect(): SoundEffects {

        return earthEffects[random.nextInt(earthEffectsSize)]
    }

}