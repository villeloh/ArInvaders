package villealla.com.arinvaders.Movement

import android.animation.Animator
import android.animation.ObjectAnimator
import android.util.Log
import android.view.animation.LinearInterpolator
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import villealla.com.arinvaders.ShipManager
import villealla.com.arinvaders.Sound.SoundEffectPlayer
import villealla.com.arinvaders.Sound.SoundEffects
import villealla.com.arinvaders.Static.Configuration
import villealla.com.arinvaders.WorldEntities.Planet
import villealla.com.arinvaders.WorldEntities.Ship
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.pow

class AnimatableNode(ship: Ship) : Node() {

    val random = ThreadLocalRandom.current()

    private fun localPositionAnimator(dur: Long, vararg values: Any?): ObjectAnimator {
        return ObjectAnimator().apply {
            target = this@AnimatableNode
            propertyName = "localPosition"
            // Change animation duration(ms). Gave some randomness to it.
            duration = dur
            interpolator = LinearInterpolator()

            setAutoCancel(true)
            // * = Spread operator, this will pass N `Any?` values instead of a single list `List<Any?>`
            setObjectValues(*values)
            // Always apply evaluator AFTER object values or it will be overwritten by a default one
            setEvaluator(VectorEvaluator())
        }
    }

    fun attack(earthPosition: Vector3) {

        val distanceFactor = calculateDistanceFactor(localPosition, earthPosition)

        // With min=2F and max=2.5F duration range is: 3.1s to 5s (1s of randomness)
        val duration = (4000 * distanceFactor).toLong() + random.nextLong() % 1000 + 1000
        //Log.d(Configuration.DEBUG_TAG, "factor: $distanceFactor, duration: $duration")

        val animation = localPositionAnimator(duration, localPosition, earthPosition)

        animation.addListener( animationListener )

        animation.start()
    }

    private fun calculateDistanceFactor(start: Vector3, end: Vector3): Double {
        return Math.sqrt((end.x - start.x).pow(2).toDouble() + (end.y - start.y).pow(2).toDouble() + (end.z - start.z).pow(2).toDouble())
    }

    private val animationListener = object : Animator.AnimatorListener {

        override fun onAnimationEnd(animation: Animator?) {

            Planet.instance.killPeople(ship.dmg)
            SoundEffectPlayer.playEffect(SoundEffectPlayer.randomEarthEffect())
            // Log.d(Configuration.DEBUG_TAG, "People left: " + Planet.instance.people())

            // it's not pretty, but since the animation doesn't stop until we 'reach' the Earth,
            // we need to check whether the ship has technically been destroyed earlier (by our laser)
            if (ShipManager.instance.getMap().containsKey(ship.id)) {
                ShipManager.instance.destroyShip(ship.id)
            }

            animation?.removeListener(this)
        }

        override fun onAnimationStart(animation: Animator?) {
        }

        override fun onAnimationCancel(animation: Animator?) {
        }

        override fun onAnimationRepeat(animation: Animator?) {
        }
    }
}