package villealla.com.arinvaders.Game

import android.os.Handler
import android.os.Looper
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import villealla.com.arinvaders.Sound.Maestro
import villealla.com.arinvaders.Static.Configuration

/*
* Class for managing the player and game-related operations.
* @author Sinan Sakaoğlu
* */

enum class GameState {
    UNINITIALIZED(),
    RUNNING(),
    PAUSED(),
    STOPPED(),
}

class GameManager private constructor() {


    init {
        // do stuff when GameManager.instance is assigned
    }

    private object Holder {
        val INSTANCE = GameManager()
    }

    // makes the class into a singleton
    companion object {
        val instance: GameManager by lazy { Holder.INSTANCE }
    }

    lateinit var earthNode: Node
    lateinit var anchorNode: AnchorNode

    lateinit var gameLoop: SpawnLoop
    var mainHandler = Handler(Looper.getMainLooper())
    var gameState = GameState.UNINITIALIZED

    fun startGameSession(difficulty: String) {

        val multiplier: Float = when (difficulty) {
            "easy" -> 0.5f
            "hard" -> 1.5f
            else -> 1f
        }

        gameLoop = SpawnLoop(multiplier, earthNode = earthNode, mainHandler = mainHandler, anchorNode = anchorNode)

        resetUI()

        gameLoop.start()
        gameState = GameState.RUNNING
    }

    fun pauseGameSession() {

        gameLoop.pause()
        Maestro.pauseMusic()
        gameState = GameState.PAUSED
    }

    fun resumeGameSession() {

        gameLoop.resume()
        Maestro.resumeMusic()
        gameState = GameState.RUNNING
    }

    fun resetUI() {

        val message = mainHandler.obtainMessage()
        message.what = Configuration.MESSAGE_RESET
        mainHandler.sendMessage(message)
    }

    fun endGameSession(exitEarly: Boolean = false) {

        if (gameState == GameState.RUNNING) {
            Maestro.stopMusic()
            gameLoop.stop()
            gameState = GameState.STOPPED

            if (!exitEarly) {
                val message = mainHandler.obtainMessage()
                message.what = Configuration.MESSAGE_GAME_OVER
                mainHandler.sendMessage(message)
            }
        }

    }

} // end class