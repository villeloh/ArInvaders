package villealla.com.arinvaders

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

    private val shipManager = ShipManager.instance
    private lateinit var gameSession: GameSession

    fun startGameSession() {

        gameSession = GameSession()
        gameSession.start() // does nothing atm
        shipManager.spawnLoop.start()
    }

    fun stopGameSession() {
        shipManager.spawnLoop.stop()
    }

} // end class