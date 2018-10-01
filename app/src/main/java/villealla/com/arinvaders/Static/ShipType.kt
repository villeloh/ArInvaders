package villealla.com.arinvaders.Static

/*
* Simple class for storing Ship stats in 'package format'
* @author Ville Lohkovuori
* */

const val DEFAULT_MOVE_SPEED = 10
const val DEFAULT_HP = 1

enum class ShipType(val modelName: String, val speed: Int, val hp: Int, val dmgScaleValue: Float) {
    UFO("CUPIC_FYINGSAUCER.sfb", DEFAULT_MOVE_SPEED, DEFAULT_HP, 1.0f),
    FIGHTER("SciFi_Fighter_AK5.sfb", DEFAULT_MOVE_SPEED, DEFAULT_HP, 1.3f)
    // TODO: add more ships as we get more models
}