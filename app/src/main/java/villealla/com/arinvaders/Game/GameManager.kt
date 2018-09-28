package villealla.com.arinvaders.Game

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import villealla.com.arinvaders.ShipManager
import villealla.com.arinvaders.Sound.Maestro
import villealla.com.arinvaders.WorldEntities.Planet

/*
* Class for managing the player and game-related operations.
*
* */

class GameManager private constructor() {

    // I guess this is the right way to communicate between
    // Activities and regular classes... at least now AS
    // shuts up about it
    interface BridgeActivity {

        fun updateKillCount(newValue: Int)
        fun updateWaveNumber(newValue: Int)
        fun updateNumberLeftInWave(newValue: Int)
        fun updatePeopleLeftOnPlanet(newValue: Long)
    }

    init {
        // do stuff when GameManager.instance is assigned
    }

    private object Holder { val INSTANCE = GameManager() }

    // I don't claim to understand this, but it makes this class a Singleton
    companion object {
        val instance: GameManager by lazy { Holder.INSTANCE }
    }

    lateinit var mainActRef: BridgeActivity
    private val shipManager = ShipManager.instance
    var score = 0

    fun setMainActivity(activity: Activity) {
        mainActRef = activity as BridgeActivity
        Log.d("JOOH", "mainActRef: " + mainActRef.toString())
    }

    fun startGameSession() {

        mainActRef.updateKillCount(0)
        mainActRef.updatePeopleLeftOnPlanet(Planet.instance.people())
        // no need to set waveNumber or numberLeftInWave, as SpawnLoop does it
        shipManager.spawnLoop.start()
    }

    fun endGameSession() {

        Maestro.stopMusic() // we can do this here... need to start in MainActivity due to context issues though
        shipManager.spawnLoop.stop()
    }

} // end class