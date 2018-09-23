package villealla.com.arinvaders

import android.util.Log
import android.view.MotionEvent
import com.google.ar.sceneform.HitTestResult
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable

// you only need a companion object if you want to refer to these
// from outside the class (I guess... not sure if this is good code or not)
const val DEFAULT_MOVE_SPEED = 10
const val DEFAULT_HP = 1

enum class ShipType(val modelName: String, val speed: Int, val hp: Int) {
    UFO("CUPIC_FYINGSAUCER.sfb", DEFAULT_MOVE_SPEED, DEFAULT_HP),
    FIGHTER("SciFi_Fighter_AK5.sfb", DEFAULT_MOVE_SPEED, DEFAULT_HP)
    // TODO: add more ships as we get more models
}

class Ship(
        // ships default to their type's hp and speed, but these can be varied manually if needed
        val type: ShipType = ShipType.UFO,
        val speed: Int = type.speed,
        var hp: Int = type.hp,
        val node: AnimatableNode = AnimatableNode()) {

    companion object {
        val renderables = mutableMapOf<ShipType, ModelRenderable>()
    }

    // each ship has a unique identifier, to enable easy tracking
    val id = java.util.UUID.randomUUID().toString()

    // called when the laser hits the ship from the middle of the screen
    fun onTouchNode(hitTestResult: HitTestResult, mEvent: MotionEvent) {

        if (hitTestResult.node == null) return

        Log.d(Configuration.DEBUG_TAG, "ship hit !")
    }

    fun attack(earthPosition: Vector3) {
        node.attack(earthPosition)
    }
} // end class