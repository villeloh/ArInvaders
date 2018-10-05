package villealla.com.arinvaders.Static

/*
* Simple class for storing Ship stats in 'package format'
* @author Ville Lohkovuori
* */

const val DEFAULT_MOVE_SPEED = 10
const val DEFAULT_HP = 1

enum class ShipType(val modelName: String, val speed: Int, val hp: Int, val dmgScaleValue: Float) {
    UFO("ufo_small_red.sfb", DEFAULT_MOVE_SPEED, DEFAULT_HP, 1.0f),
    THRALL("ufo_medium_yellow.sfb", DEFAULT_MOVE_SPEED, 2, 2.0f),
    MOTHERSHIP("ufo_large_green.sfb", DEFAULT_MOVE_SPEED, 3, 3.0f)
}