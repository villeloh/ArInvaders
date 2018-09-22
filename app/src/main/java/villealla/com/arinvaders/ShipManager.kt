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

// pass 'this' / applicationContext as the context from MainActivity
// NOTE: ideally, this class would be a Singleton, but I could think of no good
// way to refer to the right context and earthNode in that case
class ShipManager(private val context: Context, private val earthNode: Node) {

    companion object {

        const val DEFAULT_NUM_OF_SHIPS_IN_WAVE = 15
        const val DEFAULT_MIN_SPAWN_DIST = 2F
        const val DEFAULT_MAX_SPAWN_DIST = 2.5F
    }

    private val shipMap = mutableMapOf<String, Ship>()

    fun spawnShip(ship: Ship) {

        val renderable = ModelRenderable.builder()
                .setSource(context, Uri.parse(ship.type.modelName))
                .build()
        renderable.thenAccept { it ->

            val shipNode = AnimatableNode()
            shipNode.renderable = it
            shipNode.setParent(earthNode)
            shipNode.renderable.isShadowCaster = false
            shipNode.name = ship.id // can be used for destroying ships later

            val spawnCoord = randomCoord()
            shipNode.localPosition = spawnCoord

            shipNode.attack(Vector3(0f,0f,0f))

            // track the ship (for collective operations)
            shipMap[ship.id] = ship
        }
    } // end spawnShip

    // we could add different spawn patterns (chosen by enum perhaps).
    //  without any arguments, spawns the default number of UFOs
    fun spawnWaveOfShips(numOfShips: Int = DEFAULT_NUM_OF_SHIPS_IN_WAVE, ship: Ship = Ship()) {
        for (item in 0..numOfShips) {

            spawnShip(ship)
        }
    }

    fun damageShip(dmg: Int, shipId: String) {

        val ship = shipMap.get(shipId)!! // if it's been hit, it should always exist
        ship.hp -= dmg
        if (ship.hp < 0) ship.hp = 0
    }

    private fun randomCoord(minDist: Float = DEFAULT_MIN_SPAWN_DIST,
                            maxDist: Float = DEFAULT_MAX_SPAWN_DIST): Vector3 {

        val rGen = Random()

        var sign = if (rGen.nextBoolean() == true) 1 else -1

        var x =  rGen.nextFloat() * maxDist // 0 - maxDist
        x = Math.max(x, minDist) * sign // -maxDist - +maxDist, excluding -minDist - +minDist

        sign = if (rGen.nextBoolean() == true) 1 else -1
        var z = rGen.nextFloat() * maxDist
        z = Math.max(z, minDist) * sign

        val y = rGen.nextFloat() * maxDist

        Log.d("XYZ", "$x $y $z")

        return Vector3(x, y, z)
    }

} // end class