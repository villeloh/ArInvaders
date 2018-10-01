package villealla.com.arinvaders.Game

import android.os.Handler
import android.os.Looper
import com.google.ar.sceneform.Node
import villealla.com.arinvaders.Sound.Maestro
import villealla.com.arinvaders.SpawnLoop
import villealla.com.arinvaders.Static.Configuration
import villealla.com.arinvaders.WorldEntities.Planet

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

    private object Holder { val INSTANCE = GameManager() }

    // makes the class into a singleton
    companion object {
        val instance: GameManager by lazy { Holder.INSTANCE }
    }

    lateinit var earthNode: Node

    lateinit var gameLoop: SpawnLoop
    var mainHandler = Handler(Looper.getMainLooper())
    var gameState = GameState.UNINITIALIZED

    fun startGameSession() {

        gameLoop = SpawnLoop(earthNode = earthNode, mainHandler = mainHandler)

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

        var message = mainHandler.obtainMessage()
        message.what = Configuration.MESSAGE_PEOPLE_ALIVE
        message.data.putString(Configuration.MESSAGE_PEOPLE_ALIVE.toString(), Planet.instance.people().toString())
        mainHandler.sendMessage(message)

        message = mainHandler.obtainMessage()
        message.what = Configuration.MESSAGE_KILL_COUNT
        message.data.putString(Configuration.MESSAGE_KILL_COUNT.toString(), "0")
        mainHandler.sendMessage(message)
    }

    fun endGameSession() {

        Maestro.stopMusic()
        gameLoop.stop()
        gameState = GameState.STOPPED
    }

} // end class