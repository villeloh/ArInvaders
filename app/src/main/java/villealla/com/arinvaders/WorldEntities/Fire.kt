package villealla.com.arinvaders.WorldEntities

import android.animation.ObjectAnimator
import android.view.animation.BounceInterpolator
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.collision.Box
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import villealla.com.arinvaders.Movement.AnimatableNode
import java.util.*

class Fire(ship: Node) : AnimatableNode() {

    init {
        renderable = model
        setParent(ship)
        localPosition = randomLocation(localPosition)

        // remove collision so the lasers hit the ship instead of this object
        this.collisionShape = Box(Vector3(0.00001f, 0.00001f, 0.00001f))

        startAnimation()
    }


    companion object {
        lateinit var model: ModelRenderable
        val MAX_SCALE = 3f
        val random = Random(System.currentTimeMillis())
    }

    lateinit var animation: ObjectAnimator

    private fun startAnimation() {

        // Burning animation
        animation = createVector3Animator(1000, "localScale", BounceInterpolator(), localScale, localScale.scaled(MAX_SCALE))
        animation.repeatCount = ObjectAnimator.INFINITE
        animation.start()

    }

    override fun dispose() {

        animation.cancel()

        super.dispose()
    }

    private fun randomLocation(center: Vector3): Vector3 {

        var sign = if (random.nextBoolean()) 1 else -1
        val x = (random.nextFloat() % 0.03f + 0.01f) * sign
        sign = if (random.nextBoolean()) 1 else -1
        val z = (random.nextFloat() % 0.03f + 0.01f) * sign

        return Vector3(center.x + x, center.y + 0.01f, center.z + z)
    }
}