package villealla.com.arinvaders.Sound

import android.content.Context
import android.media.MediaPlayer
import villealla.com.arinvaders.R

enum class Music(val musicName: Int) {
    BATTLE(R.raw.arcade_war)
}

//Responsible for background music
object Maestro {

    private var mediaPlayer: MediaPlayer? = null

    init {

    }

    fun playMusic(context: Context, music: Music, loop: Boolean) {

        mediaPlayer = MediaPlayer.create(context, music.musicName)
        mediaPlayer!!.isLooping = loop
        mediaPlayer!!.start()
    }

    fun stopMusic() {
        mediaPlayer?.stop()
    }

    fun pauseMusic() {
        if (mediaPlayer != null && mediaPlayer!!.isPlaying)
            mediaPlayer?.pause()
    }

    fun resumeMusic() {
        mediaPlayer?.start()
    }


}