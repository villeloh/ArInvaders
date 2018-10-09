package villealla.com.arinvaders.WorldEntities

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.view.animation.AccelerateInterpolator
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import villealla.com.arinvaders.Movement.AnimatableNode

/*
* Manages the laser gun that the player fires.
* @author Ville Lohkovuori
* */

class Gun(cameraNode: Node) : AnimatableNode() {

    private lateinit var animation_1: ObjectAnimator

    init {
        renderable = gunRenderable
        setParent(cameraNode)
        localPosition = Vector3(0.015f, -0.065f, -0.2f) // simply what's needed for it to look right
        localRotation = Quaternion.axisAngle(Vector3(1f, 0.34f, 0f), 40f) // ditto
        name = "gun"
        setupKickbackAnimation() // must be called last due to needing the updated localPosition!
    }

    companion object {
        lateinit var gunRenderable: ModelRenderable
    }

    private fun setupKickbackAnimation() {

        val startPosition = localPosition
        val movePosition = Vector3(localPosition.x, localPosition.y - 0.008f, localPosition.z)
        val duration = 100L
        animation_1 = createVector3Animator(duration, "localPosition", AccelerateInterpolator(), startPosition, movePosition)
        val animation_2 = createVector3Animator(duration, "localPosition", AccelerateInterpolator(), movePosition, startPosition)

        animation_1.addListener(object : AnimatorListenerAdapter() {

            override fun onAnimationEnd(animation: Animator?) {
                animation_2.start()
            }
        })
    } // end setupKickAnimation

    fun kickback() {

        animation_1.start()
    }
}