package villealla.com.arinvaders.WorldEntities

import android.animation.ObjectAnimator
import android.view.animation.BounceInterpolator
import com.google.ar.sceneform.math.Vector3
import villealla.com.arinvaders.Movement.AnimatableNode

/*
* Manages the laser gun that the player fires.
* @author Ville Lohkovuori
* */

class Gun : AnimatableNode() {

    private lateinit var shootingAnimation: ObjectAnimator

    fun setupAnimation() {

        val startPosition = localPosition
        val movePosition = Vector3(localPosition.x, localPosition.y - 0.008f, localPosition.z)
        val duration = 200L
        shootingAnimation = createVector3Animator(duration, "localPosition", BounceInterpolator(), startPosition, movePosition)
    }

    fun kickback() {

        shootingAnimation.start()
    }


}