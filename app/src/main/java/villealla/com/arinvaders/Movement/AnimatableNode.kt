package villealla.com.arinvaders.Movement

import android.animation.ObjectAnimator
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import kotlin.math.pow

open class AnimatableNode : Node() {


    protected fun createAttackAnimator(dur: Long, vararg values: Any?): ObjectAnimator {
        return ObjectAnimator().apply {
            target = this@AnimatableNode
            propertyName = "localPosition"
            // Change animation duration(ms). Gave some randomness to it.
            duration = dur
            interpolator = DecelerateInterpolator()

            setAutoCancel(true)
            // * = Spread operator, this will pass N `Any?` values instead of a single list `List<Any?>`
            setObjectValues(*values)
            // Always apply evaluator AFTER object values or it will be overwritten by a default one
            setEvaluator(VectorEvaluator())
        }
    }

    protected fun calculateDistanceFactor(start: Vector3, end: Vector3): Double {
        return Math.sqrt((end.x - start.x).pow(2).toDouble() + (end.y - start.y).pow(2).toDouble() + (end.z - start.z).pow(2).toDouble())
    }


    protected fun createDeathAnimator(vararg values: Any?): ObjectAnimator {
        return ObjectAnimator().apply {
            target = this@AnimatableNode
            propertyName = "localScale"
            duration = 1000
            interpolator = AccelerateInterpolator()

            setAutoCancel(false)

            setObjectValues(*values)

            setEvaluator(VectorEvaluator())
        }
    }

}