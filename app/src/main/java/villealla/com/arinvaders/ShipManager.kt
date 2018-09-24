package villealla.com.arinvaders

import android.util.Log
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import java.util.*

/*
* Controls the collective operations regarding Ships
* (creation as waves, tracking, etc).
* @author Ville Lohkovuori, Sinan <just put your surname here>
* */

class ShipManager private constructor() {

    private lateinit var earthNode: Node
    lateinit var spawnLoop: SpawnLoop // set from MainActivity atm... I get a weird null object reference without that; fix asap!

    init {
    }

    private object Holder {

        val INSTANCE = ShipManager()
    }

    companion object {

        val instance: ShipManager by lazy { Holder.INSTANCE }
        const val DEFAULT_NUM_OF_SHIPS_IN_WAVE = 15
        const val DEFAULT_MIN_SPAWN_DIST = 2F
        const val DEFAULT_MAX_SPAWN_DIST = 2.5F
        const val DEFAULT_WAVE_LENGTH_MS = 8000L
    }

    private val rGen = Random(System.currentTimeMillis())

    private val shipMap = mutableMapOf<String, Ship>()

    // ugly af, but whatever
    fun setEarthNode(passedNode: Node) {
        earthNode = passedNode
    }

    fun spawnShip(shipType: ShipType) {

        val ship = Ship(type = shipType)

        val spawnCoord = randomCoord()

        ship.node.renderable = Ship.renderables[shipType]
        ship.node.localPosition = spawnCoord
        ship.node.renderable.isShadowCaster = false
        ship.node.name = ship.id // can be used for destroying ships later
        ship.node.setParent(earthNode)

        ship.node.setOnTouchListener {
            hitTestResult, mEvent ->

            ship.onTouchNode(hitTestResult, mEvent)
            true
        }
        ship.attack(Vector3(0f, Planet.centerHeight, 0f))

        // track the ship (for collective operations)
        shipMap[ship.id] = ship
    } // end spawnShip

    // we could add different spawn patterns (chosen by enum perhaps).
    //  without any arguments, spawns the default number of UFOs
    fun spawnWaveOfShips(numOfShips: Int = DEFAULT_NUM_OF_SHIPS_IN_WAVE, shipType: ShipType = ShipType.UFO) {
        for (item in 0..numOfShips) {

            spawnShip(shipType)
        }
    }

    fun damageShip(dmg: Int, shipId: String) {

        val ship = shipMap.get(shipId)!! // if it's been hit, it should always exist
        ship.hp -= dmg
        if (ship.hp <= 0) destroyShip(shipId)
    }

    private fun destroyShip(shipId: String) {

        val ship = shipMap[shipId]!!

        // I'm not sure if more is needed to remove the ship from
        // physical existence... setting the Node to null is impossible (at least directly)
        ship.node.renderable = null
        // TODO: play explosion animation
        shipMap.remove(shipId)
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
} // end class