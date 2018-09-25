package villealla.com.arinvaders.WorldEntities

import android.content.Context
import android.net.Uri
import com.google.ar.core.HitResult
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import villealla.com.arinvaders.Game.GameManager

/*
* @author Ville Lohkovuori
* */

// needs to be a Singleton for easy reference...
class Planet private constructor(private var hitPoints: Long = 7000000000) {

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

    lateinit var earthNode: Node
    var earthRenderable: ModelRenderable? = null

    // needs to be its own function because of the delay in attaching
    // the renderable... feel free to refactor; I'm not very good at async stuffs
    fun obtainRenderable(context: Context) {

        val renderable = ModelRenderable.builder()
                .setSource(context, Uri.parse("earth_ball.sfb"))
                .build()
        renderable.thenAccept { it -> earthRenderable = it }
    }

    // we could have a PlanetManager to do all this, to mimic the pattern with ships,
    // but since only one planet is needed, I think that's overkill
    fun renderInArSpace(arFragment: ArFragment, hitResult: HitResult) {

        val anchor = hitResult.createAnchor()
        earthNode = AnchorNode(anchor)
        earthNode.setParent(arFragment.arSceneView.scene)
        earthNode.renderable = earthRenderable
        earthNode.name = "earthNode" // not used for anything atm
    } // end renderInArSpace

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

} // end class