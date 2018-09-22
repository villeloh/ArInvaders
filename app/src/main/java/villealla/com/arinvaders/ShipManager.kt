package villealla.com.arinvaders

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import java.util.*

/*
* Controls the collective operations regarding Ships
* (creation as waves, tracking, etc).
* @author Ville Lohkovuori
* */

// pass 'this' as the context from MainActivity
// NOTE: ideally, this class would be a Singleton, but I could think of no good
// way to refer to the right context and earthNode in that case
class ShipManager(val context: Context, val earthNode: Node) {

    companion object {
        const val DEFAULT_SPAWN_DISTANCE_MIN = 1
        const val DEFAULT_SPAWN_DISTANCE_MAX = 2
        const val DEFAULT_NUM_OF_SHIPS_IN_WAVE = 15
    }

    private val shipMap = mutableMapOf<String, Ship>()

    private fun trackShip(shipToTrack: Ship) {

        shipMap[shipToTrack.id] = shipToTrack
    }

    private fun untrackShip(shipToRemove: Ship) {

        shipMap.remove(shipToRemove.id)
    }

    fun spawnShip(ship: Ship) {

        val renderable = ModelRenderable.builder()
                .setSource(context, Uri.parse(ship.type.modelName))
                .build()
        renderable.thenAccept {  it ->

            // we'll need to make our own Node subclass for moving the ships...
            // for now let's just try to create them
            val shipNode = AnimatableNode()
            shipNode.renderable = it
            shipNode.setParent(earthNode)
            shipNode.renderable.isShadowCaster = false
            shipNode.name = ship.id // can be used for destroying ships later

            val spawnCoord = generateRandomCoord()
            shipNode.localPosition = spawnCoord

            shipMap[ship.id] = ship
        }
    } // end spawnShip

    // we could add different spawn patterns (chosen by enum perhaps)
    fun spawnWaveOfShips(numOfShips: Int, ship: Ship) {
        for (item in 0..numOfShips) {

            spawnShip(ship)
        }
    }

    private fun generateRandomCoord(): Vector3 {

        val randomGen = Random()
        val x = (if (randomGen.nextBoolean() == true) 1 else -1) * randomGen.nextFloat() * 0.35
        val y = randomGen.nextFloat() * 0.35
        val z = (if (randomGen.nextBoolean() == true) 1 else -1) * randomGen.nextFloat() * 0.35
        Log.d("XYZ", "$x $y $z")

        return Vector3(x.toFloat(), y.toFloat(), z.toFloat())
    }

} // end class