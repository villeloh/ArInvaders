package villealla.com.arinvaders.Sound

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import villealla.com.arinvaders.R

enum class SoundEffects(val effectName: Int, val volumeLevel: Float = 1f, var id: Int = 0) {
    LASER(R.raw.laser, 0.5f),
    EXPLOSION(R.raw.bomb)
}

object SoundEffectPlayer {

    private val soundPool: SoundPool

    init {

        val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_GAME)
                .build()

        soundPool = SoundPool.Builder()
                .setMaxStreams(20)
                .setAudioAttributes(audioAttributes)
                .build()

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


}