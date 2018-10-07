package villealla.com.arinvaders.WorldEntities

import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import villealla.com.arinvaders.Static.ShipType
import kotlin.math.absoluteValue

class Mothership(type: ShipType, speed: Int, localPosition: Vector3, earthNode: Node, observer: IonDeath, val iMinionSpawner: IMinionSpawner) : Ship(type = type, speed = speed, localPosition = localPosition, earthNode = earthNode, observer = observer) {

    init {

        startMinionSpawner()
    }

    private fun startMinionSpawner() {
        spawnerThread = Thread(Runnable {

            while (true) {
                try {
                    Thread.sleep(rGen.nextLong().absoluteValue % 3000 + 4000)
                } catch (e: InterruptedException) {
                    break
                }

                iMinionSpawner.spawnMinion(this.localPosition)
            }
        })

        spawnerThread.start()
    }


    private lateinit var spawnerThread: Thread

    override fun dispose() {
        spawnerThread.interrupt()

        super.dispose()
    }

}

interface IMinionSpawner {
    fun spawnMinion(localPosition: Vector3)
}