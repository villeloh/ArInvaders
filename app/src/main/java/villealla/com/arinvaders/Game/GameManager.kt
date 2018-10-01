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
*
* */

class GameManager private constructor() {


    init {
        // do stuff when GameManager.instance is assigned
    }

    private object Holder { val INSTANCE = GameManager() }

    // I don't claim to understand this, but it makes this class a Singleton
    companion object {
        val instance: GameManager by lazy { Holder.INSTANCE }
    }

    lateinit var earthNode: Node


    lateinit var gameLoop: SpawnLoop
    var mainHandler = Handler(Looper.getMainLooper())

    fun startGameSession() {
        gameLoop = SpawnLoop(earthNode = earthNode, mainHandler = mainHandler)

        resetUI()

        gameLoop.start()
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

        Maestro.stopMusic() // we can do this here... need to start in MainActivity due to context issues though
        //shipManager.spawnLoop.stop()
        gameLoop.stop()
    }

} // end class