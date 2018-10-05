package villealla.com.arinvaders.WorldEntities

import android.util.Log
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import villealla.com.arinvaders.Static.Configuration
import villealla.com.arinvaders.Static.ShipType
import kotlin.math.absoluteValue

class Mothership(type: ShipType, speed: Int, localPosition: Vector3, earthNode: Node, observer: IonDeath, val iMinionSpawner: IMinionSpawner) : Ship(type = type, speed = speed, localPosition = localPosition, earthNode = earthNode, observer = observer) {

    override fun attack(earthPosition: Vector3) {
        Log.d(Configuration.DEBUG_TAG, "Mother : ${this.name}")
        super.attack(earthPosition)
        startMinionSpawner()
    }

    private fun startMinionSpawner() {
        spawnerThread = Thread(Runnable {

            while (true) {
                try {
                    Thread.sleep(rGen.nextLong().absoluteValue % 4000 + 4000)
                } catch (e: InterruptedException) {
                    break
                }

                iMinionSpawner.spawnMinion(this.localPosition)
            }
        })

        spawnerThread.start()
    }


    private lateinit var spawnerThread: Thread


    override fun die() {
        spawnerThread.interrupt()

        super.die()
    }
}

interface IMinionSpawner {
    fun spawnMinion(localPosition: Vector3)
}