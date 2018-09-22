package villealla.com.arinvaders

import android.animation.ObjectAnimator
import android.view.animation.LinearInterpolator
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import java.util.concurrent.ThreadLocalRandom

class AnimatableNode : Node() {

    val random = ThreadLocalRandom.current()

    private fun localPositionAnimator(vararg values: Any?): ObjectAnimator {
        return ObjectAnimator().apply {
            target = this@AnimatableNode
            propertyName = "localPosition"
            // Change animation duration(ms). Gave some randomness to it.
            duration = 1000 + random.nextLong(1000)
            interpolator = LinearInterpolator()

            setAutoCancel(true)
            // * = Spread operator, this will pass N `Any?` values instead of a single list `List<Any?>`
            setObjectValues(*values)
            // Always apply evaluator AFTER object values or it will be overwritten by a default one
            setEvaluator(VectorEvaluator())
        }
    }

    fun attack(earthPosition: Vector3) {

        val animation = localPositionAnimator(localPosition, earthPosition)

        animation.start()
    }


}