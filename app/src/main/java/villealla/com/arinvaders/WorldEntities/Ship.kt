package villealla.com.arinvaders.WorldEntities

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.TimeInterpolator
import android.os.Handler
import android.util.Log
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.Color
import com.google.ar.sceneform.rendering.Light
import com.google.ar.sceneform.rendering.ModelRenderable
import villealla.com.arinvaders.Movement.AnimatableNode
import villealla.com.arinvaders.Sound.SoundEffectPlayer
import villealla.com.arinvaders.Sound.SoundEffects
import villealla.com.arinvaders.Static.Configuration
import villealla.com.arinvaders.Static.StaticResources
import villealla.com.arinvaders.Static.ShipType
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.absoluteValue

/*
* Controls all Ship-related operations.
* @author Sinan Sakaoğlu, Ville Lohkovuori
* */

private const val DEFAULT_UNSCALED_MIN_DMG = 100000000L
private const val DEFAULT_UNSCALED_MAX_DMG = 200000000L

open class Ship(
        val type: ShipType = ShipType.UFO,
        val speed: Int = type.speed,
        var hp: Int = type.hp,
        val dmg: Long = randomizedDmgValue(ShipType.UFO.dmgScaleValue),
        localPosition: Vector3,
        val earthNode: Node,
        val observer: IonDeath,
        val mainHandler: Handler
) : AnimatableNode() {

    var attackInterpolator: TimeInterpolator
    var directionalUnitVector3: Vector3

    init {
        // ships need a unique identifier
        this.name = java.util.UUID.randomUUID().toString()
        this.renderable = renderables[type]
        renderable.isShadowCaster = false
        this.localPosition = localPosition
        this.setParent(earthNode)

        //Change attack animation behaviour depending on the ship type
        attackInterpolator = when (type) {
            ShipType.UFO -> DecelerateInterpolator()
            ShipType.THRALL -> AccelerateInterpolator()
            ShipType.MOTHERSHIP -> LinearInterpolator()
        }

        //calculate the directional unit vector
        directionalUnitVector3 = vector3Difference(localPosition, earthNode.localPosition).normalized()

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

    private lateinit var attackAnimation: Animator
    private lateinit var firingThread: Thread
    private var isFiring = true
    private val fireList = ArrayList<Fire>(2)


    private fun attack(earthPosition: Vector3) {

        val distanceFactor = calculateDistanceFactor(this.localPosition, earthPosition)

        val duration = (4000 * distanceFactor) + rGen.nextLong().absoluteValue % 2000 + (4000 * 1 / this.speed)
        //Log.d(Configuration.DEBUG_TAG, "factor: $distanceFactor, duration: $duration")

        attackAnimation = createVector3Animator(duration.toLong(), "localPosition", attackInterpolator, this.localPosition, earthPosition)

        attackAnimation.addListener(object : AnimatorListenerAdapter() {

            // This function is called when the ship reaches the earth without dying
            override fun onAnimationEnd(animation: Animator?) {
                //Ship has reached the earth
                SoundEffectPlayer.playEffect(SoundEffectPlayer.randomEarthEffect())

                killFiresStopLasers()

                //Kill this ship
                dispose()
                Log.d(Configuration.DEBUG_TAG, "Death by kamikaze $name")

                //signal ship's death to observer
                observer.onDeath(this@Ship, true)

            } // end onAnimationEnd
        })

        //create spin/hover animation
        val spinAnimation = createSpinAnimator(rGen.nextBoolean(), 1000, this.localRotation)

        spinAnimation.start()
        attackAnimation.start()
        startShootingLasers()

    } // end attack

    private fun die() {

        //cancel() and end() functions both call same callback function, so pause has to be called instead
        pauseAttack()

        //destroy fire objects on the ships if any and stop firing lasers
        killFiresStopLasers()

        // Audible and visual death effect for the ship
        this.renderable = StaticResources.explosionRenderable
        SoundEffectPlayer.playEffect(SoundEffects.EXPLOSION)

        playDeathAnimation()

    }

    private fun playDeathAnimation() {
        val deathAnimation = createVector3Animator(1000, "localScale", AccelerateInterpolator(), this.localScale, this.localScale.scaled(2f))

        deathAnimation.addListener(object : AnimatorListenerAdapter() {

            override fun onAnimationEnd(animation: Animator?) {

                //Kill this ship
                dispose()
                // Log.d(Configuration.DEBUG_TAG, "Death by laser $name")
                observer.onDeath(this@Ship, false)
            }
        })

        deathAnimation.start()
        playShipExplosionLightsAnim()
    }

    private fun killFiresStopLasers() {
        firingThread.interrupt()

        //stop all fire animations
        fireList.forEach {
            it.dispose()
        }
        fireList.clear()
    }


    private fun playShipExplosionLightsAnim() {

        //create one red on yellow light place them next to ship and flash
        for (i in 0..1) {
            val light = Light.builder(Light.Type.POINT)
                    .setColor(Color(1f, (i % 2).toFloat(), 0f))
                    .setIntensity(5000f)
                    .setColorTemperature(1500f)
                    .build()

            val lightNode = AnimatableNode()
            lightNode.light = light
            lightNode.setParent(earthNode)
            lightNode.localPosition = vector3Sum(this.localPosition, Vector3(0.1f, 0f, 0f + i * 0.1f))

            val animator = createFlashingAnimator(lightNode.light, 1000)
            animator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    lightNode.dispose()
                }
            })
            animator.start()
        }


    }

    fun pauseAttack() {
        if (attackAnimation.isRunning) {
            attackAnimation.pause()
            isFiring = false
        }
    }

    fun resumeAttack() {
        if (attackAnimation.isPaused) {
            attackAnimation.resume()
            isFiring = true
        }
    }

    fun damageShip(dmg: Int) {

        //the first if check is to prevent calling die() multiple times when death animation is already playing
        if (hp > 0) {
            this.hp -= dmg

            if (this.hp <= 0) {
                die()
                return
            }

            //spawn a fire on this ship if it is not dead yet
            fireList.add(Fire(this))

            SoundEffectPlayer.playEffect(SoundEffects.SHIP_HIT)
        }
    } // end damageShip

    private fun startShootingLasers() {

        firingThread = Thread(Runnable {

            while (isFiring && true) {
                try {
                    Thread.sleep(rGen.nextLong().absoluteValue % 5000 + 2000)
                } catch (e: InterruptedException) {
                    break
                }

                SoundEffectPlayer.playEffect(SoundEffects.SHIP_LASER)

                // create and fire a laser towards the earth
                mainHandler.post {
                    val laserBolt = LaserBolt()
                    laserBolt.setParent(earthNode.parent)
                    laserBolt.localPosition = localPosition
                    laserBolt.renderable = LaserBolt.yellowRenderable
                    laserBolt.setLookDirection(directionalUnitVector3)

                    /* // Gets close to the earth, but doesn't reach it

                     var target = vector3Difference(localPosition,earthNode.localPosition)

                     target = vector3Sum(target.normalized().scaled(0.5f),localPosition)*/


                    laserBolt.fire(earthNode.localPosition, dur = 700, fireCallback = object : IFireCallback {
                        override fun fireFinished() {

                            //laser kills a little bit of people
                            Planet.instance.killPeople(dmg / 1000)

                            val message = mainHandler.obtainMessage()
                            message.what = Configuration.MESSAGE_PEOPLE_ALIVE
                            message.data.putString(Configuration.MESSAGE_PEOPLE_ALIVE.toString(), Planet.instance.people())
                            mainHandler.sendMessage(message)
                        }
                    })
                }

            }

        })

        firingThread.start()
    }

    private fun vector3Difference(vec1: Vector3, vec2: Vector3): Vector3 {
        return Vector3(vec2.x - vec1.x, vec2.y - vec1.y, vec2.z - vec1.z)
    }

    private fun vector3Sum(vec1: Vector3, vec2: Vector3): Vector3 {
        return Vector3(vec2.x + vec1.x, vec2.y + vec1.y, vec2.z + vec1.z)
    }

    // enables callback in the registered observer (in practice, an object in SpawnLoop)
    interface IonDeath {

        fun onDeath(ship: Ship, reachedEarth: Boolean)
    }

} // end class