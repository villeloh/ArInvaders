package villealla.com.arinvaders.WorldEntities

import android.content.Context
import android.net.Uri
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.ModelRenderable
import villealla.com.arinvaders.Game.GameManager
import villealla.com.arinvaders.Movement.AnimatableNode
import java.text.NumberFormat
import java.util.*
import villealla.com.arinvaders.Static.Configuration

/*
* Manages the planet Earth and the people on it.
* @author Ville Lohkovuori, Sinan Sakaoğlu
* */

// needs to be a Singleton for easy reference...
class Planet private constructor(private var hitPoints: Long = Configuration.EARTH_POPULATION) : AnimatableNode() {

    private object Holder {

        val INSTANCE = Planet()
    }

    companion object {
        val instance: Planet by lazy { Holder.INSTANCE }

        // determines the Y-coordinate that the ships will attack towards.
        // if the modelRenderable is rescaled, this should be changed as well
        const val centerHeight = 0.07F
    }

    // set when loading resources on game start
    var earthRenderable: ModelRenderable? = null


    fun resetHitPoints(){
        hitPoints = Configuration.EARTH_POPULATION
    }

    fun renderInArSpace(anchorNode: AnchorNode) {

        this.renderable = earthRenderable
        this.name = "earthNode"
        this.setParent(anchorNode)
        startRotating()
    } // end renderInArSpace

    fun startRotating() {

        //Creates two rotation animations, one for half way of spin, then loops.
        createSpinAnimator(duration = 8000, localRotation = this.localRotation).start()
    }

    // I've always wanted to write this :)
    fun killPeople(damage: Long) {

        hitPoints -= damage
        if (hitPoints < 0) {
            hitPoints = 0
            GameManager.instance.endGameSession()
        }
    }

    // add commas to the population number, to make it easier to read
    fun people(): String {

        return NumberFormat.getNumberInstance(Locale.US).format(hitPoints)
    }

} // end class