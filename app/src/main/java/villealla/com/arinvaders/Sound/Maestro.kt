package villealla.com.arinvaders.Sound

import android.content.Context
import android.media.MediaPlayer
import villealla.com.arinvaders.R

/* Responsible for background music.
* @author Sinan Sakaoğlu
* */

enum class Music(val musicName: Int) {
    BATTLE(R.raw.arcade_war)
}

object Maestro {

    private var mediaPlayer: MediaPlayer? = null

    fun playMusic(context: Context, music: Music, loop: Boolean) {

        mediaPlayer = MediaPlayer.create(context, music.musicName)
        mediaPlayer!!.isLooping = loop
        mediaPlayer!!.setVolume(0.4f,0.4f)
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

} // end class