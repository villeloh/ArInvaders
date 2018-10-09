package villealla.com.arinvaders.WorldEntities

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.view.animation.AccelerateInterpolator
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import villealla.com.arinvaders.Movement.AnimatableNode

/*
* Manages the laser gun that the player fires.
* @author Ville Lohkovuori
* */

class Gun : AnimatableNode() {

    private lateinit var animation_1: ObjectAnimator

    companion object {
        lateinit var modelRenderable: ModelRenderable
    }

    fun setupAnimation() {

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
    }

    fun kickback() {

        animation_1.start()
    }


}