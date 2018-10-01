package villealla.com.arinvaders

import android.os.Handler
import android.util.Log
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import villealla.com.arinvaders.Sound.SoundEffectPlayer
import villealla.com.arinvaders.Static.Configuration
import villealla.com.arinvaders.WorldEntities.Planet
import villealla.com.arinvaders.WorldEntities.Ship
import java.util.*
import kotlin.math.abs

/*
* What is expected of this class:
* spawn wave of ships in a certain amount of time span
* * delay between each ship spawn is randomized
* a delay between each wave
* use current wave number to increase game difficulty
* should give the game ability to stop, pause, restart and continue controls
* minimum coupling
* should be a concrete class
* */
class SpawnLoop(var waveNumber: Int = 0, val earthNode: Node, val mainHandler: Handler) {

    companion object {
        const val DELAY_BETWEEN_WAVES_SEC: Long = 2
        const val DEFAULT_MIN_SPAWN_DIST = 2F
        const val DEFAULT_MAX_SPAWN_DIST = 2.5F
    }

    private val rGen = Random(System.currentTimeMillis())

    init {
        initSession()
    }

    private var shipsInScene = mutableMapOf<String, Ship>()

    private var killCount = 0

    private lateinit var sessionThread: Thread
    private var isSessionRunning = false

    //responsible for starting a wave
    private fun startWave(wave: Int) {
        Thread(Runnable {
            var spawnedShipCount = 0
            val totalShipsToSpawn = wave * 2 + 5
            while (isSessionRunning && spawnedShipCount != totalShipsToSpawn) {

                //adding the ship to the scene must be done in the ui thread because thats what the jvm wants
                mainHandler.post {
                    val newShip = Ship(localPosition = randomCoord(), earthNode = earthNode, observer = onShipDeath)
                    shipsInScene[newShip.name] = newShip
                    spawnedShipCount++
                }

                //delay between each ship spawn
                try {
                    Thread.sleep(abs(rGen.nextLong() % 500))
                } catch (ex: InterruptedException) {
                }

            }
        }).start()
    }

    //responsible for starting and ending all waves
    private fun initSession() {

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

                    Log.d(Configuration.DEBUG_TAG, "Starting wave: $waveNumber")
                    startWave(waveNumber)
                }

                try {
                    Thread.sleep(1000)
                } catch (ex: InterruptedException) {
                }

            }

        })
    }

    fun start() {
        isSessionRunning = true
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
    }

    fun pause() {
        isSessionRunning = false
        //stop all attack animations

        shipsInScene.forEach { name, ship ->
            ship.pauseAttack()
        }

    }

    fun resume() {
        //resume all attack animations
        isSessionRunning = true

        shipsInScene.forEach { name, ship ->
            ship.resumeAttack()
        }
    }


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

                killCount++

                //notify ui thread about this change

                val message = mainHandler.obtainMessage()
                message.what = Configuration.MESSAGE_KILL_COUNT
                message.data.putString(Configuration.MESSAGE_KILL_COUNT.toString(), killCount.toString())
                mainHandler.sendMessage(message)

            }



            shipsInScene.remove(ship.name)
        }

    }

    private fun randomCoord(minDist: Float = DEFAULT_MIN_SPAWN_DIST,
                            maxDist: Float = DEFAULT_MAX_SPAWN_DIST): Vector3 {
        var sign = if (rGen.nextBoolean()) 1 else -1

        //this formula distributes coordinates more evenly
        val x = (rGen.nextFloat() * (maxDist - minDist) + minDist) * sign

        sign = if (rGen.nextBoolean()) 1 else -1

        val z = (rGen.nextFloat() * (maxDist - minDist) + minDist) * sign

        val y = rGen.nextFloat() * maxDist

        // Log.d(Configuration.DEBUG_TAG, "coordinates : x=$x, y=$y z=$z")

        return Vector3(x, y, z)
    }


}