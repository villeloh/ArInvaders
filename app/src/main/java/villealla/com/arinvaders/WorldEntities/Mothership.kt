package villealla.com.arinvaders.WorldEntities

import android.os.Handler
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import villealla.com.arinvaders.Static.ShipType
import kotlin.math.absoluteValue

/*
* A class for controlling the Mothership type UFOs, which have
* unique behavior (spawning the smaller ships).
* @author Sinan Sakaoğlu
* */
class Mothership(type: ShipType,
                 speed: Int,
                 localPosition: Vector3,
                 earthNode: Node,
                 observer: IonDeath,
                 val iMinionSpawner: IMinionSpawner,
                 mainHandler: Handler,
                 dmg: Long = Ship.randomizedDmgValue(ShipType.MOTHERSHIP.dmgScaleValue))
    : Ship(type = type,
        speed = speed,
        localPosition = localPosition,
        earthNode = earthNode,
        observer = observer,
        mainHandler = mainHandler,
        dmg = dmg) {

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
    } // end startMinionSpawner


    private lateinit var spawnerThread: Thread

    override fun dispose() {
        spawnerThread.interrupt()

        super.dispose()
    }

} // end class

interface IMinionSpawner {
    fun spawnMinion(localPosition: Vector3)
}