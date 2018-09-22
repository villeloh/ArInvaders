package villealla.com.arinvaders

// you only need a companion object if you want to refer to these
// from outside the class (I guess... not sure if this is good code or not)
const val DEFAULT_MOVE_SPEED = 10
const val DEFAULT_HP = 1

enum class ShipType(val modelName: String, val speed: Int, val hp: Int) {
    UFO("CUPIC_FYINGSAUCER.sfb", DEFAULT_MOVE_SPEED, DEFAULT_HP)
    // add more ships as we get more models
}

class Ship(
        // ships default to their type's hp and speed, but they can be varied manually if needed
        val type: ShipType = ShipType.UFO,
        val speed: Int = type.speed,
        val hp: Int = type.hp) {

    // each ship has a unique identifier, to enable easy tracking
    val id = java.util.UUID.randomUUID().toString()
} // end class