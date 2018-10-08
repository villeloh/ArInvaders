package villealla.com.arinvaders.WorldEntities

import android.animation.Animator
import android.content.Context
import android.net.Uri
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.Color
import com.google.ar.sceneform.rendering.Light
import com.google.ar.sceneform.rendering.ModelRenderable
import villealla.com.arinvaders.Game.GameManager
import villealla.com.arinvaders.Movement.AnimatableNode
import java.text.NumberFormat
import java.util.*

/*
* Manages the planet Earth and the people on it.
* @author Ville Lohkovuori
* */

// needs to be a Singleton for easy reference...
class Planet private constructor(private var hitPoints: Long = 7000000000) : AnimatableNode() {

    init {
    }

    private object Holder {

        val INSTANCE = Planet()
    }

    companion object {
        val instance: Planet by lazy { Holder.INSTANCE }

        // determines the Y-coordinate that the ships will attack towards.
        // if the model is rescaled, this should be changed as well
        const val centerHeight = 0.07F
    }

    private lateinit var explosionLight: Light
    private val lightAnimators = mutableListOf<Animator>()
    private val lightNodes = mutableListOf<Node>()
    var earthRenderable: ModelRenderable? = null

    // needs to be its own function because of the delay in attaching
    // the renderable... feel free to refactor; I'm not very good at async stuffs
    fun loadRenderable(context: Context) {

        val renderable = ModelRenderable.builder()
                .setSource(context, Uri.parse("earth_ball.sfb"))
                .build()
        renderable.thenAccept { it -> earthRenderable = it }
    }

    // we could have a PlanetManager to do all this, to mimic the pattern with ships,
    // but since only one planet is needed, I think that's overkill
    fun renderInArSpace(anchorNode: AnchorNode) {

        this.renderable = earthRenderable
        this.name = "earthNode"
        this.setParent(anchorNode)
        addLights()
        startRotating()
    } // end renderInArSpace

    fun flashExplosionLights() {

        for (anim in lightAnimators) {
            anim.start()
        }
    }

    fun startRotating() {

        //Creates two rotation animations, one for half way of spin, then loops.
        createSpinAnimator(8000, this.localRotation).start()
    }

    // I've always wanted to write this :)
    fun killPeople(damage: Long) {

        hitPoints -= damage
        if (hitPoints < 0) {
            hitPoints = 0
            GameManager.instance.endGameSession()
        }
    }

    fun people(): String {

        return NumberFormat.getNumberInstance(Locale.US).format(hitPoints)
    }

    private fun addLights() {

        explosionLight = Light.builder(Light.Type.POINT)
                .setIntensity(0f)
                .setFalloffRadius(200000f)
                .setShadowCastingEnabled(false)
                .setColorTemperature(10000f)
                .setColor(Color(1f,1f,1f))
                .build()

        // the positions of the lights around the Earth
        val positions = arrayOf(
                Vector3(0.0f, 0.8f, 0.0f),
                Vector3(0.0f, -0.8f, 0.0f),
                Vector3(0.8f, centerHeight, 0.0f),
                Vector3(-0.8f, centerHeight, 0.0f),
                Vector3(0.0f, centerHeight, 0.8f),
                Vector3(0.0f, centerHeight, -0.8f)
        )

        for (vector in positions) {
            val node = createExplosionLightNode(vector)
            val anim = createLightIntensityAnimator(node.light,1000L, 0f, 120000f, 0f)
            lightAnimators.add(anim)
        }
    } // end addLights

    private fun createExplosionLightNode(locPos: Vector3): Node {

        return Node().apply {
            setParent(this@Planet)
            light = explosionLight
            localPosition = locPos
        }
    }

} // end class