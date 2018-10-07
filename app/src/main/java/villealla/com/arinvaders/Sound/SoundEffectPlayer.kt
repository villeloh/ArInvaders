package villealla.com.arinvaders.Sound

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import villealla.com.arinvaders.R
import java.util.concurrent.ThreadLocalRandom

/*
* Responsible for sound effects.
* @author Sinan Sakaoğlu, Ville Lohkovuori
* */

enum class SoundEffects(val effectName: Int, val volumeLevel: Float = 1f, var id: Int = 0, val effectId: String = "") {
    LASER(R.raw.laser, 0.75f),
    EXPLOSION(R.raw.bomb),
    EARTH_HIT_1(R.raw.scream_1, 0.1f, 0, "planetEffect"),
    EARTH_HIT_2(R.raw.scream_2, 0.4f, 0, "planetEffect"),
    EARTH_HIT_3(R.raw.scream_3, 0.4f, 0, "planetEffect"),
    EARTH_HIT_4(R.raw.nuke, 0.35f, 0, "planetEffect"),
}

object SoundEffectPlayer {

    private val rGen = ThreadLocalRandom.current()

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
                .setMaxStreams(5)
                .setAudioAttributes(audioAttributes)
                .build()

        initEarthEffects()
    } // end init

    fun loadAllEffects(context: Context) {
        SoundEffects.values().forEach {
            it.id = soundPool.load(context, it.effectName, 1)
        }
    }

    fun playEffect(soundEffect: SoundEffects, loop: Boolean = false) {

        // a little variation in the sound level / pitch serves to add some realism
        var sign = if (rGen.nextBoolean()) 1 else -1
        val modVol = 1.0f + sign * rGen.nextFloat() * 0.2f // 0.8 -- 1.2

        sign = if (rGen.nextBoolean()) 1 else -1
        val modifiedPitch = 1.0f + sign * rGen.nextFloat() * 0.2f // 0.8 -- 1.2

        val modifiedVolume = soundEffect.volumeLevel * modVol

        val loopNum = if (loop) 1 else 0

        if (soundEffect.id != 0)
            soundPool.play(soundEffect.id, modifiedVolume, modifiedVolume, 1, loopNum, modifiedPitch)
    } // end playEffect

    // it seems a bit clumsy and unnecessary, as the effects enum is static.
    // a better solution should be thought of...
    private fun initEarthEffects() {
        SoundEffects.values().forEach {

            if (it.effectId == "planetEffect") {
                earthEffects.add(it)
            }
        }
        earthEffectsSize = earthEffects.size
    } // end initEarthEffects

    fun randomEarthEffect(): SoundEffects {

        return earthEffects[random.nextInt(earthEffectsSize)]
    }

} // end class