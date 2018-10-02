package villealla.com.arinvaders.WorldEntities

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.util.Log
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import villealla.com.arinvaders.MainActivity
import villealla.com.arinvaders.Movement.AnimatableNode
import villealla.com.arinvaders.Sound.SoundEffectPlayer
import villealla.com.arinvaders.Sound.SoundEffects
import villealla.com.arinvaders.Static.Configuration
import villealla.com.arinvaders.Static.ShipType
import java.util.*
import kotlin.math.absoluteValue

/*
* Controls all Ship-related operations.
* @author Sinan Sakaoğlu, Ville Lohkovuori
* */

private const val DEFAULT_UNSCALED_MIN_DMG = 100000000L
private const val DEFAULT_UNSCALED_MAX_DMG = 200000000L

class Ship(
        val type: ShipType = ShipType.UFO,
        val speed: Int = type.speed,
        var hp: Int = type.hp,
        val dmg: Long = randomizedDmgValue(ShipType.UFO.dmgScaleValue),
        localPosition: Vector3,
        earthNode: Node,
        val observer: IonDeath
) : AnimatableNode() {

    init {
        // ships need a unique identifier
        this.name = java.util.UUID.randomUUID().toString()
        this.renderable = renderables[type]
        renderable.isShadowCaster = false
        this.localPosition = localPosition
        this.setParent(earthNode)
        this.attack(Vector3(0f, Planet.centerHeight, 0f))
    }

    companion object {

        val renderables = mutableMapOf<ShipType, ModelRenderable>()
        val rGen = Random(System.currentTimeMillis())

        private fun randomizedDmgValue(dmgScaleValue: Float): Long {

            val min = DEFAULT_UNSCALED_MIN_DMG
            val max = DEFAULT_UNSCALED_MAX_DMG

            // with the default scale value (1.0), dmg will be between 100 - 200 million ppl per ufo hit.
            // the formula is pretty clunky atm, but we don't really need anything more elaborate
            return (rGen.nextFloat() * (max - min) + min * dmgScaleValue).toLong()
        }
    } // end companion object

    lateinit var attackAnimation: Animator

    fun attack(earthPosition: Vector3) {

        val distanceFactor = calculateDistanceFactor(this.localPosition, earthPosition)

        val duration = (4000 * distanceFactor) + rGen.nextLong().absoluteValue % 1000 + (2000 * 1 / this.speed)
        //Log.d(Configuration.DEBUG_TAG, "factor: $distanceFactor, duration: $duration")

        attackAnimation = createAttackAnimator(duration.toLong(), this.localPosition, earthPosition)

        attackAnimation.addListener(object : AnimatorListenerAdapter() {

            override fun onAnimationEnd(animation: Animator?) {

                //Ship has reached the earth
                //Planet.instance.killPeople(dmg)
                SoundEffectPlayer.playEffect(SoundEffectPlayer.randomEarthEffect())

                //Kill this ship
                dispose()
                Log.d(Configuration.DEBUG_TAG, "Death by kamikaze $name")

                //broadcast ships death to any one that cares
                observer.onDeath(this@Ship, true)
            } // end onAnimationEnd
        })
        attackAnimation.start()
    } // end attack

    private fun die() {

        //cancel() and end() functions both call same callback function, so pause has to be called instead
        attackAnimation.pause()

        val deathAnimation = createDeathAnimator(this.localScale, this.localScale.scaled(2f))

        deathAnimation.addListener(object : AnimatorListenerAdapter() {

            override fun onAnimationEnd(animation: Animator?) {

                //Kill this ship
                dispose()
                // Log.d(Configuration.DEBUG_TAG, "Death by laser $name")
                observer.onDeath(this@Ship, false)
            }
        })

        this.renderable = MainActivity.explosionRenderable

        SoundEffectPlayer.playEffect(SoundEffects.EXPLOSION)
        deathAnimation.start()
    }

    fun pauseAttack() {
        if (attackAnimation.isRunning)
            attackAnimation.pause()
    }

    fun resumeAttack() {
        if (attackAnimation.isPaused)
            attackAnimation.resume()
    }

    fun damageShip(dmg: Int) {

        //the first if check is to prevent calling die() multiple times when death animation is already playing
        if (hp > 0) {
            this.hp -= dmg
            if (this.hp <= 0) {
                die()
            }
        }
    } // end damageShip

    fun dispose() {
        renderable = null
        setParent(null)
    }

    // enables callback in the registered Observer (in practice, an object in SpawnLoop)
    interface IonDeath {

        fun onDeath(ship: Ship, reachedEarth: Boolean)
    }

} // end class