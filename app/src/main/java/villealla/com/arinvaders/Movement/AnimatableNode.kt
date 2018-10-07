package villealla.com.arinvaders.Movement

import android.animation.*
import android.view.animation.LinearInterpolator
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.QuaternionEvaluator
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.math.Vector3Evaluator
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
    }

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
    }

    protected fun createSpinAnimator(duration: Long, localRotation: Quaternion): Animator {

        val spinQuaternion1 = Quaternion.axisAngle(Vector3(0f, 1f, 0f), 180f)
        val spinQuaternion2 = Quaternion.axisAngle(Vector3(0f, 1f, 0f), -1f)


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

    }

    protected fun calculateDistanceFactor(start: Vector3, end: Vector3): Double {
        return Math.sqrt((end.x - start.x).pow(2).toDouble() + (end.y - start.y).pow(2).toDouble() + (end.z - start.z).pow(2).toDouble())
    }

    open fun dispose() {
        renderable = null
        setParent(null)
    }

} // end class