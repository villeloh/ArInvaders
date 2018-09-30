package villealla.com.arinvaders

import android.os.Handler
import android.util.Log
import villealla.com.arinvaders.Game.GameManager
import villealla.com.arinvaders.Static.ShipType

/*
* Manages the lifecycle of the spawned space ships.
* @author Ville Lohkovuori
* */

// we need to pass the shipManager instance since otherwise there's a null object reference...
// due to the manager not being fully initialized when it's referred to from here (I guess)
class SpawnLoop(private val shipManager: ShipManager) {

    private var waveNumber = 0

    // can be set from outside if needed
    var shipTypeToSpawn: ShipType = ShipType.UFO
    var numOfShipsInWave = ShipManager.DEFAULT_NUM_OF_SHIPS_IN_WAVE

    private val spawnHandler = Handler()

    // it needs to be an object literal in order to be able to refer to itself with 'this',
    // and for us to be able to stop it later with stop()
    private val shipSpawner = object : Runnable {

        override fun run() {

            val gameManager = GameManager.instance
            waveNumber += 1
            shipManager.spawnWaveOfShips(numOfShipsInWave, shipTypeToSpawn)
            spawnHandler.postDelayed(this, ShipManager.DEFAULT_WAVE_LENGTH_MS)

            Log.d("JOOH", "gameManager in shipSpawner: " + gameManager.toString())
            Log.d("JOOH", "mainActRef in shipSpawner: " + gameManager.mainActRef.toString())

            gameManager.mainActRef.updateWaveNumber(waveNumber) // spaghetti code... ugh -.-
            gameManager.mainActRef.updateNumberLeftInWave(numOfShipsInWave)
        }
    }

    fun getWaveNumber(): Int {
        return waveNumber
    }

    fun start() {

        spawnHandler.post(shipSpawner)
    }

    fun stop() {

        spawnHandler.removeCallbacks(shipSpawner)
    }
}