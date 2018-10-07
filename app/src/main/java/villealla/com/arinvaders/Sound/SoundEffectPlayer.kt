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

enum class SoundEffects(val effectName: Int, val volumeLevel: Float = 1f, var id: Int = 0, val effectId: String = "", val priority: Int = 2) {
    LASER(R.raw.laser, 0.75f),
    EXPLOSION(R.raw.bomb, 0.5f),
    EARTH_HIT_1(R.raw.scream_1, 0.1f, effectId = "planetEffect"),
    EARTH_HIT_2(R.raw.scream_2, 0.4f, effectId = "planetEffect"),
    EARTH_HIT_3(R.raw.scream_3, 0.4f, effectId = "planetEffect"),
    SHIP_HIT(R.raw.ship_hit, 0.5f),
    SHIP_LASER(R.raw.ship_laser, 0.5f)

}

object SoundEffectPlayer {

    private val soundPool: SoundPool
    private val random = ThreadLocalRandom.current()
    private val earthEffects = mutableListOf<SoundEffects>()

    init {
        val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_GAME)
                .build()

        soundPool = SoundPool.Builder()
                .setMaxStreams(6)
                .setAudioAttributes(audioAttributes)
                .build()

        initEarthEffects()
    } // end init

    fun loadAllEffects(context: Context) {
        SoundEffects.values().forEach {
            it.id = soundPool.load(context, it.effectName, 1)
        }
    }

    fun playEffect(soundEffect: SoundEffects) {
        if (soundEffect.id != 0)
            soundPool.play(soundEffect.id, soundEffect.volumeLevel, soundEffect.volumeLevel, soundEffect.priority, 0, 1f)
    }

    // it seems a bit clumsy and unnecessary, as the effects enum is static.
    // a better solution should be thought of...
    private fun initEarthEffects() {
        SoundEffects.values().forEach {

            if (it.effectId == "planetEffect") {
                earthEffects.add(it)
            }
        }
    } // end initEarthEffects

    fun randomEarthEffect(): SoundEffects {

        return earthEffects[random.nextInt(earthEffects.size)]
    }

} // end class