package villealla.com.arinvaders.WorldEntities

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.view.animation.DecelerateInterpolator
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import villealla.com.arinvaders.Movement.AnimatableNode

/*
* An animatable class (node, whatever) for the bolts of laser
* that the ship fires.
* @author Ville Lohkovuori, Sinan Sakaoğlu
* */

class LaserBolt(cameraNode: Node?) : AnimatableNode() {

    lateinit var animation: ObjectAnimator

    init {
        if (cameraNode != null) {
            setParent(cameraNode)
            renderable = redRenderable
            localPosition = Vector3(0.0f, -0.07f, -0.2f) // simply what's needed for it to look right
            localRotation = Quaternion.axisAngle(Vector3(1f, 0f, 0f), 40f) // ditto
            name = "laser"
        }

    }

    companion object {
        lateinit var redRenderable: ModelRenderable
        lateinit var yellowRenderable: ModelRenderable
    }

    fun fire(shootPosition: Vector3, dur: Int = 350, fireCallback: IFireCallback = defaultCallback) {

        val distanceFactor = calculateDistanceFactor(localPosition, shootPosition)
        val duration = (dur * distanceFactor).toLong() // ms

        animation = createVector3Animator(duration, "localPosition", DecelerateInterpolator(), localPosition, shootPosition)
        animation.addListener(object : AnimatorListenerAdapter() {

            override fun onAnimationEnd(animation: Animator?) {
                dispose()
                fireCallback.fireFinished()
            }
        })
        animation.start()
    } // end fire


    private val defaultCallback = object : IFireCallback {
        override fun fireFinished() {

        }
    }
} // end class

interface IFireCallback {
    fun fireFinished()
}