package villealla.com.arinvaders

import android.os.Handler

/*
* Manages the lifecycle of the spawned space ships.
* @author Ville Lohkovuori
* */

class SpawnLoop {

    // can be set from outside if needed
    var shipTypeToSpawn: ShipType = ShipType.UFO
    var numOfShipsInWave = ShipManager.DEFAULT_NUM_OF_SHIPS_IN_WAVE

    private val shipManager = ShipManager.instance
    private val spawnHandler = Handler()

    // it needs to be an object literal in order to be able to refer to itself with 'this',
    // and for us to be able to stop it later with stopLoop()
    private val shipSpawner = object: Runnable {
        override fun run() {

            shipManager.spawnWaveOfShips(numOfShipsInWave, shipTypeToSpawn)
            spawnHandler.postDelayed(this, ShipManager.DEFAULT_WAVE_LENGTH_MS)
        }
    }

    fun start() {

        // spawn the first wave right away
        // NOTE: it seems we now get a double amount of ships in the first wave. this should be fixed asap
        shipManager.spawnWaveOfShips(numOfShipsInWave, shipTypeToSpawn)
        spawnHandler.post(shipSpawner)
    }

    fun stop() {

        spawnHandler.removeCallbacks(shipSpawner)
    }
} // end class