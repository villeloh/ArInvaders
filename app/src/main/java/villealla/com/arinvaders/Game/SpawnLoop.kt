package villealla.com.arinvaders.Game

import android.os.Handler
import android.util.Log
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import villealla.com.arinvaders.Sound.SoundEffectPlayer
import villealla.com.arinvaders.Static.Configuration
import villealla.com.arinvaders.WorldEntities.Planet
import villealla.com.arinvaders.WorldEntities.Ship
import java.util.*

/*
* What is expected of this class:
* spawn wave of ships in a certain amount of time span
* * delay between each ship spawn is randomized
* a delay between each wave
* use current wave number to increase game difficulty
* should give the game ability to stop, pause, restart and continue controls
* minimum coupling
* should be a concrete class
* @author Sinan Sakaoğlu
* */
class SpawnLoop(var waveNumber: Int = 0, val earthNode: Node, val mainHandler: Handler) {

    companion object {
        const val DELAY_BETWEEN_WAVES_SEC: Long = 2
        const val DEFAULT_MIN_SPAWN_DIST = 2F
        const val DEFAULT_MAX_SPAWN_DIST = 2.5F
    }

    private val rGen = Random(System.currentTimeMillis())

    private var shipsInScene = mutableMapOf<String, Ship>()

    private var totalKillCount = 0
    private var waveKillCount = 0

    private var pausedwaveKillCount = -1
    private var pausedspawnedShipCount = -1

    private lateinit var sessionThread: Thread
    private lateinit var waveThread: Thread
    private var isSessionRunning = false

    private var spawnedShipCount = 0
    private var totalShipsToSpawn = 0

    //responsible for starting a wave
    private fun startWave(wave: Int) {

        //reset wave variables
        spawnedShipCount = 0
        waveKillCount = 0

        waveThread = Thread(Runnable {

            calculateTotalShipsToSpawn(wave)
            while (isSessionRunning && spawnedShipCount < totalShipsToSpawn) {

                // adding the ship to the scene must be done in the ui thread because thats what the jvm wants
                mainHandler.post {
                    val newShip = Ship(localPosition = randomCoord(), earthNode = earthNode, observer = onShipDeath, speed = rGen.nextInt(20) + 10)
                    shipsInScene[newShip.name] = newShip
                }
                spawnedShipCount++

            }

        })
        waveThread.start()
    } // end startWave

    private fun calculateTotalShipsToSpawn(wave: Int) {

        totalShipsToSpawn = wave * 2 + 5
        updateUIShipsInWave(totalShipsToSpawn, totalShipsToSpawn)
    }

    private fun updateUIShipsInWave(shipsLeft: Int, totalShipsToSpawn: Int) {

        val message = mainHandler.obtainMessage()
        message.what = Configuration.MESSAGE_SHIPS_LEFT_IN_WAVE
        message.data.putString(Configuration.MESSAGE_SHIPS_LEFT_IN_WAVE.toString(), "$shipsLeft / $totalShipsToSpawn")
        mainHandler.sendMessage(message)
    }

    //responsible for starting and ending all waves
    private fun createSessionThread() {

        sessionThread = Thread(Runnable {

            while (isSessionRunning) {

                //previous wave has ended
                if (shipsInScene.isEmpty()) {

                    // Add a delay between each wave
                    if (waveNumber != 0)
                        try {
                            Thread.sleep(DELAY_BETWEEN_WAVES_SEC * 1000)
                        } catch (ex: InterruptedException) {
                        }

                    //Update ui & notify user about new wave
                    waveNumber++
                    updateUIWaveNum(waveNumber)

                    Log.d(Configuration.DEBUG_TAG, "Starting wave: $waveNumber")
                    startWave(waveNumber)
                }

                try {
                    Thread.sleep(1000)
                } catch (ex: InterruptedException) {
                }
            } // end while-loop
        }) // end sessionThread
    } // end createSessionThread

    private fun updateUIWaveNum(num: Int) {
        val message = mainHandler.obtainMessage()
        message.what = Configuration.MESSAGE_WAVE_NUMBER
        message.data.putString(Configuration.MESSAGE_WAVE_NUMBER.toString(), num.toString())
        mainHandler.sendMessage(message)
    }

    fun start() {
        isSessionRunning = true

        createSessionThread()

        sessionThread.start()
    }

    fun stop() {
        isSessionRunning = false
        sessionThread.interrupt()

        //remove all ships from scene
        shipsInScene.forEach { name, ship ->
            ship.dispose()
        }

        shipsInScene.clear()
    } // end stop

    fun pause() {
        isSessionRunning = false

        sessionThread.interrupt()
        waveThread.interrupt()

        //stop all attack animations
        shipsInScene.forEach { name, ship ->
            ship.pauseAttack()
        }
        Log.d(Configuration.DEBUG_TAG, "All animations paused")
    } // end pause

    fun resume() {
        isSessionRunning = true

        start()

        //resume all attack animations
        shipsInScene.forEach { name, ship ->
            ship.resumeAttack()
        }
        Log.d(Configuration.DEBUG_TAG, "All animations resumed")
    } // end resume

    val onShipDeath = object : Ship.IonDeath {

        override fun onDeath(ship: Ship, reachedEarth: Boolean) {

            //do damage to earth, update ui, update ship list/wave thread
            if (reachedEarth) {
                SoundEffectPlayer.playEffect(SoundEffectPlayer.randomEarthEffect())
                Planet.instance.killPeople(ship.dmg)

                //notify ui thread about this change

                val message = mainHandler.obtainMessage()
                message.what = Configuration.MESSAGE_PEOPLE_ALIVE
                message.data.putString(Configuration.MESSAGE_PEOPLE_ALIVE.toString(), Planet.instance.people().toString())
                mainHandler.sendMessage(message)

            } else {
                //ship killed by player

                totalKillCount++

                //notify ui thread about this change

                val message = mainHandler.obtainMessage()
                message.what = Configuration.MESSAGE_KILL_COUNT
                message.data.putString(Configuration.MESSAGE_KILL_COUNT.toString(), totalKillCount.toString())
                mainHandler.sendMessage(message)
            }

            waveKillCount++


            updateUIShipsInWave(totalShipsToSpawn - waveKillCount, totalShipsToSpawn)

            shipsInScene.remove(ship.name)
        } // end onDeath
    } // end onShipDeath

    private fun randomCoord(minDist: Float = DEFAULT_MIN_SPAWN_DIST,
                            maxDist: Float = DEFAULT_MAX_SPAWN_DIST): Vector3 {
        var sign = if (rGen.nextBoolean()) 1 else -1

        //this formula distributes coordinates evenly
        val x = (rGen.nextFloat() * (maxDist - minDist) + minDist) * sign

        sign = if (rGen.nextBoolean()) 1 else -1

        val z = (rGen.nextFloat() * (maxDist - minDist) + minDist) * sign

        val y = rGen.nextFloat() * maxDist

        // Log.d(Configuration.DEBUG_TAG, "coordinates : x=$x, y=$y z=$z")

        return Vector3(x, y, z)
    } // end randomCoord

} // end class