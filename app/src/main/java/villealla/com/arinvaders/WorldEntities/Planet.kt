package villealla.com.arinvaders.WorldEntities

import android.content.Context
import android.net.Uri
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.ModelRenderable
import villealla.com.arinvaders.Game.GameManager
import villealla.com.arinvaders.Movement.AnimatableNode

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
        startRotating()
    } // end renderInArSpace

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

    fun people(): Long {
        return hitPoints
    }

} // end class1