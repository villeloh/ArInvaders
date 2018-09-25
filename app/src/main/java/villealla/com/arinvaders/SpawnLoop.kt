package villealla.com.arinvaders

import android.os.Handler
import villealla.com.arinvaders.WorldEntities.ShipType

/*
* Manages the lifecycle of the spawned space ships.
* @author Ville Lohkovuori
* */

// we need to pass the shipManager instance since otherwise there's a null object reference...
// due to the manager not being fully initialized when it's referred to from here (I guess)
class SpawnLoop(private val shipManager: ShipManager) {

    // can be set from outside if needed
    var shipTypeToSpawn: ShipType = ShipType.UFO
    var numOfShipsInWave = ShipManager.DEFAULT_NUM_OF_SHIPS_IN_WAVE

    private val spawnHandler = Handler()

    // it needs to be an object literal in order to be able to refer to itself with 'this',
    // and for us to be able to stop it later with stop()
    private val shipSpawner = object : Runnable {
        override fun run() {

            shipManager.spawnWaveOfShips(numOfShipsInWave, shipTypeToSpawn)
            spawnHandler.postDelayed(this, ShipManager.DEFAULT_WAVE_LENGTH_MS)
        }
    }

    fun start() {

        spawnHandler.post(shipSpawner)
    }

    fun stop() {

        spawnHandler.removeCallbacks(shipSpawner)
    }
} // end class