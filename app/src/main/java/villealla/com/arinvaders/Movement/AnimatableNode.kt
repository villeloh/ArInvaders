package villealla.com.arinvaders.Movement

import android.animation.*
import android.view.animation.BounceInterpolator
import android.view.animation.LinearInterpolator
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.QuaternionEvaluator
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.math.Vector3Evaluator
import com.google.ar.sceneform.rendering.Light
import kotlin.math.pow

/*
* Inheritable base class for entities that need to be physically
* present in the game space.
* @author Sinan Sakaoğlu
* */

open class AnimatableNode : Node() {

    protected fun createVector3Animator(duration: Long, propertyName: String, interpolator: TimeInterpolator, vararg values: Vector3?): ObjectAnimator {
        return ObjectAnimator().apply {
            this.target = this@AnimatableNode
            this.propertyName = propertyName
            this.duration = duration
            this.interpolator = interpolator

            setAutoCancel(false)
            // * = Spread operator, this will pass N `Any?` values instead of a single list `List<Any?>`
            setObjectValues(*values)
            // Always apply evaluator AFTER object values or it will be overwritten by a default one
            setEvaluator(Vector3Evaluator())
        }
    } // end createVector3Animator

    private fun createQuaternionAnimator(duration: Long, propertyName: String, interpolator: TimeInterpolator, vararg values: Quaternion?): ObjectAnimator {
        return ObjectAnimator().apply {
            this.target = this@AnimatableNode
            this.propertyName = propertyName
            this.duration = duration
            this.interpolator = interpolator

            setAutoCancel(true)
            // * = Spread operator, this will pass N `Any?` values instead of a single list `List<Any?>`
            setObjectValues(*values)
            // Always apply evaluator AFTER object values or it will be overwritten by a default one
            setEvaluator(QuaternionEvaluator())
        }
    } // end createQuaternionAnimator

    protected fun createSpinAnimator(directionIsClockwise: Boolean = true, duration: Long, localRotation: Quaternion): Animator {

        val sign = if (directionIsClockwise) 1 else -1

        val spinQuaternion1 = Quaternion.axisAngle(Vector3(0f, 1f, 0f), -180f * sign)
        val spinQuaternion2 = Quaternion.axisAngle(Vector3(0f, 1f, 0f), 1f * sign)

        val halfSpin1 = createQuaternionAnimator(duration, "localRotation", LinearInterpolator(), localRotation, spinQuaternion1)
        val halfSpin2 = createQuaternionAnimator(duration, "localRotation", LinearInterpolator(), spinQuaternion1, spinQuaternion2)

        val spinAnimator = AnimatorSet().apply {

            play(halfSpin1).before(halfSpin2)
        }
        spinAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                animation?.start()

            }
        })
        return spinAnimator
    } // end createSpinAnimator

    protected fun createLightIntensityAnimator(light: Light, dura: Long, startIntensity: Float, maxIntensity: Float, endIntensity: Float): Animator {

        val intensify = ObjectAnimator.ofFloat(light, "intensity", startIntensity, maxIntensity).apply {
            duration = dura / 2
        }

        val dim = ObjectAnimator.ofFloat(light, "intensity", maxIntensity, endIntensity).apply {
            duration = dura / 2
        }

        return AnimatorSet().apply {

            play(intensify).before(dim)
        }
    } // end createLightIntensityAnimator

    protected fun calculateDistanceFactor(start: Vector3, end: Vector3): Double {
        return Math.sqrt((end.x - start.x).pow(2).toDouble() + (end.y - start.y).pow(2).toDouble() + (end.z - start.z).pow(2).toDouble())
    }

    protected fun createFlashingAnimator(target: Light, duration: Long): ObjectAnimator {
        return ObjectAnimator().apply {
            this.target = target
            this.propertyName = "intensity"
            this.duration = duration / 2
            this.interpolator = BounceInterpolator()

            setAutoCancel(false)
            // * = Spread operator, this will pass N `Any?` values instead of a single list `List<Any?>`
            setObjectValues(target.intensity)
            setObjectValues(0)

            repeatCount = 2
            // Always apply evaluator AFTER object values or it will be overwritten by a default one
            setEvaluator(FloatEvaluator())
        }
    }

    open fun dispose() {
        renderable = null
        setParent(null)
    }

} // end class