package villealla.com.arinvaders.WorldEntities

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.util.Log
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import villealla.com.arinvaders.Movement.AnimatableNode
import villealla.com.arinvaders.ShipManager
import villealla.com.arinvaders.Sound.SoundEffectPlayer
import villealla.com.arinvaders.Sound.SoundEffects
import villealla.com.arinvaders.Static.Configuration
import villealla.com.arinvaders.Static.Configuration.Companion.DEFAULT_UNSCALED_MAX_DMG
import villealla.com.arinvaders.Static.Configuration.Companion.DEFAULT_UNSCALED_MIN_DMG
import villealla.com.arinvaders.Static.ShipType
import java.util.*

class Ship(
        // ships default to their type's hp and speed, but these can be varied manually if needed
        val type: ShipType = ShipType.UFO,
        val speed: Int = type.speed,
        var hp: Int = type.hp,
        val dmg: Long = randomizedDmgValue(ShipType.UFO.dmgScaleValue)) : AnimatableNode() {

    companion object {
        val renderables = mutableMapOf<ShipType, ModelRenderable>()
        val rGen = Random(System.currentTimeMillis())

        private fun randomizedDmgValue(dmgScaleValue: Float): Long {

            val min = DEFAULT_UNSCALED_MIN_DMG
            val max = DEFAULT_UNSCALED_MAX_DMG

            // with the default scale value (1.0), dmg will be between 100 - 200 million ppl per ufo hit.
            // the formula is pretty clunky atm, but we don't really need something more elaborate
            return (rGen.nextFloat() * (max - min) + min * dmgScaleValue).toLong()
        }
    }

    // each ship has a unique identifier, to enable easy tracking
    val id = java.util.UUID.randomUUID().toString()
    lateinit var attackAnimation: Animator

    fun attack(earthPosition: Vector3) {

        val distanceFactor = calculateDistanceFactor(this.localPosition, earthPosition)

        val duration = (4000 * distanceFactor).toLong() + rGen.nextLong() % 1000 + (500 + 500 * this.speed)
        //Log.d(Configuration.DEBUG_TAG, "factor: $distanceFactor, duration: $duration")

        attackAnimation = createAttackAnimator(duration, this.localPosition, earthPosition)

        attackAnimation.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {

                //Ship has reached the earth
                Planet.instance.killPeople(dmg)
                SoundEffectPlayer.playEffect(SoundEffectPlayer.randomEarthEffect())

                //Kill this ship
                renderable = null
                setParent(null)
                Log.d(Configuration.DEBUG_TAG, "Death by kamikaze $id")
            }

            override fun onAnimationCancel(animation: Animator?) {

            }
        })


        attackAnimation.start()
    }

    private fun die() {
        Log.d(Configuration.DEBUG_TAG, "Dying started")

        //cancel() and end() functions both call same callback function, so pause has to be called instead
        attackAnimation.pause()

        val deathAnimation = createDeathAnimator(this.localScale, this.localScale.scaled(2f))

        deathAnimation.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {


                //Kill this ship
                renderable = null
                setParent(null)
                Log.d(Configuration.DEBUG_TAG, "Death by laser $id")
            }
        })

        this.renderable = ShipManager.explosionRenderable

        SoundEffectPlayer.playEffect(SoundEffects.EXPLOSION)
        deathAnimation.start()
    }

    fun damageShip(dmg: Int) {

        this.hp -= dmg
        if (this.hp == 0) {
            die()
        }
    }

}