package villealla.com.arinvaders.WorldEntities

import android.util.Log
import android.view.MotionEvent
import com.google.ar.sceneform.HitTestResult
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import villealla.com.arinvaders.Movement.AnimatableNode
import villealla.com.arinvaders.ShipManager
import villealla.com.arinvaders.Static.Configuration
import java.util.*

// you only need a companion object if you want to refer to these
// from outside the class (I guess... not sure if this is good code or not)
const val DEFAULT_MOVE_SPEED = 10
const val DEFAULT_HP = 1
const val DEFAULT_UNSCALED_MIN_DMG = 100000000L
const val DEFAULT_UNSCALED_MAX_DMG = 200000000L

enum class ShipType(val modelName: String, val speed: Int, val hp: Int, val dmgScaleValue: Float) {
    UFO("CUPIC_FYINGSAUCER.sfb", DEFAULT_MOVE_SPEED, DEFAULT_HP, 1.0f),
    FIGHTER("SciFi_Fighter_AK5.sfb", DEFAULT_MOVE_SPEED, DEFAULT_HP, 1.3f)
    // TODO: add more ships as we get more models
}

private val rGen = Random()

private fun randomizedDmgValue(dmgScaleValue: Float): Long {

    val min = DEFAULT_UNSCALED_MIN_DMG
    val max = DEFAULT_UNSCALED_MAX_DMG

    // with the default scale value (1.0), dmg will be between 100 - 200 million ppl per ufo hit.
    // the formula is pretty clunky atm, but we don't really need something more elaborate
    return (rGen.nextFloat() * (max - min) + min * dmgScaleValue).toLong()
}

class Ship(
        // ships default to their type's hp and speed, but these can be varied manually if needed
        val type: ShipType = ShipType.UFO,
        val speed: Int = type.speed,
        var hp: Int = type.hp,
        val dmg: Long = randomizedDmgValue(ShipType.UFO.dmgScaleValue)) {

    companion object {
        val renderables = mutableMapOf<ShipType, ModelRenderable>()
    }

    // each ship has a unique identifier, to enable easy tracking
    val id = java.util.UUID.randomUUID().toString()
    val node: AnimatableNode = AnimatableNode(this)

    // called when the laser hits the ship from the middle of the screen
    fun onTouchNode(hitTestResult: HitTestResult, mEvent: MotionEvent) {

        if (hitTestResult.node == null) return

        Log.d(Configuration.DEBUG_TAG, "ship hit !")
    }

    fun attack(earthPosition: Vector3) {
        node.attack(earthPosition)
    }
} // end class