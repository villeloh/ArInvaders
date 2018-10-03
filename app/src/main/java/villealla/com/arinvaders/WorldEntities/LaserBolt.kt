package villealla.com.arinvaders.WorldEntities

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.view.animation.AccelerateInterpolator
import com.google.ar.sceneform.math.Vector3
import villealla.com.arinvaders.Movement.AnimatableNode

/*
* An animatable class (node, whatever) for the bolts of laser
* that the ship fires.
* @author Ville Lohkovuori
* */

class LaserBolt : AnimatableNode() {

    lateinit var animation: ObjectAnimator

    fun fire(shootPosition: Vector3, fireCallback: IFireCallback) {

        val distanceFactor = calculateDistanceFactor(localPosition, shootPosition)
        val duration = (350 * distanceFactor).toLong() // ms

        animation = createVector3Animator(duration, "localPosition", AccelerateInterpolator(), localPosition, shootPosition)
        animation.addListener(object : AnimatorListenerAdapter() {

            override fun onAnimationEnd(animation: Animator?) {
                dispose()
                fireCallback.fireFinished()

            }
        })

        animation.start()

    } // end fire


    private fun dispose() {
        renderable = null
        setParent(null)
    }

} // end class

interface IFireCallback {
    fun fireFinished()
}