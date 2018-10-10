package villealla.com.arinvaders.WorldEntities

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.animation.AccelerateInterpolator
import android.view.animation.BounceInterpolator
import android.view.animation.LinearInterpolator
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.Color
import com.google.ar.sceneform.rendering.Light
import com.google.ar.sceneform.rendering.ModelRenderable
import villealla.com.arinvaders.Movement.AnimatableNode
import villealla.com.arinvaders.Sound.SoundEffectPlayer
import villealla.com.arinvaders.Sound.SoundEffects
import villealla.com.arinvaders.Static.StaticResources

/*
* Manages the laser gun that the player fires.
* @author Ville Lohkovuori
* */

class Gun(private val cameraNode: Node) : AnimatableNode() {

    private lateinit var kickbackAnimation1: ObjectAnimator
    private var muzzleFlashFire: Fire
    private var scalingAnim: ObjectAnimator
    private var rotationAnim: ObjectAnimator
    private var muzzleFlashLightNode1 = AnimatableNode()
    private var muzzleFlashLightNode2 = AnimatableNode()
    private var muzzleFlashLightAnimator1: ValueAnimator
    private var muzzleFlashLightAnimator2: ValueAnimator

    private val flashDuration = 250L

    init {
        renderable = gunRenderable
        setParent(cameraNode)
        localPosition = Vector3(0.015f, -0.065f, -0.2f) // simply what's needed for it to look right
        localRotation = Quaternion.axisAngle(Vector3(1f, 0.34f, 0f), 40f) // ditto
        name = "gun"
        setupKickbackAnimation() // must be called last due to needing the updated localPosition!

        val muzzleFlashLight1 = Light.builder(Light.Type.POINT)
                .setIntensity(0f)
                .setFalloffRadius(0.05f)
                .setShadowCastingEnabled(false)
                .setColorTemperature(10000f)
                .setColor(Color(1f, 1f, 0f))
                .build()

        val muzzleFlashLight2 = Light.builder(Light.Type.POINT)
                .setIntensity(0f)
                .setFalloffRadius(0.05f)
                .setShadowCastingEnabled(false)
                .setColorTemperature(10000f)
                .setColor(Color(1f, 1f, 1f))
                .build()

        muzzleFlashFire = Fire(null).apply {

            renderable = fireRenderable
            setParent(this@Gun)
            localPosition = Vector3(-0.0159f, 0.05f, -0.0165f)
            localRotation = Quaternion.axisAngle(Vector3(-1f, 0f, 0f), 160f)
            localScale = this.localScale.scaled(0.001f)
        }

        muzzleFlashLightNode1.apply {

            light = muzzleFlashLight1
            setParent(muzzleFlashFire)
            localPosition = Vector3(0f, 0.02f, 0f)
        }

        muzzleFlashLightNode2.apply {
            light = muzzleFlashLight2
            setParent(muzzleFlashFire)
            localPosition = Vector3(0f, 0.02f, 0f)
        }

        muzzleFlashLightAnimator1 = ObjectAnimator.ofFloat(
                muzzleFlashLightNode1.light, "intensity",  50000f, 0f).apply {
            duration = flashDuration
        }
        muzzleFlashLightAnimator2 = ObjectAnimator.ofFloat(
                muzzleFlashLightNode2.light, "intensity", 50000f, 0f).apply {
            duration = flashDuration
        }

        val spinQuaternion = Quaternion.axisAngle(Vector3(0f, 0f, 1f), -360f)
        scalingAnim = muzzleFlashFire.createVector3Animator(
                        flashDuration,
                        "localScale",
                        BounceInterpolator(),
                        muzzleFlashFire.localScale.scaled(1000f),
                        muzzleFlashFire.localScale)
        rotationAnim = muzzleFlashFire.createQuaternionAnimator(
                flashDuration,
                "localRotation",
                LinearInterpolator(),
                muzzleFlashFire.localRotation,
                spinQuaternion)
    }

    companion object {
        lateinit var gunRenderable: ModelRenderable
        lateinit var fireRenderable: ModelRenderable
    }

    // note: the passed callback is an object that contains a call to playerAttack in MainActivity
    fun fire(fireCallback: IFireCallback) {

        SoundEffectPlayer.playEffect(SoundEffects.LASER)
        kickback()
        muzzleFlash()

        val laserBolt = LaserBolt().apply {

            // it would've been way easier to set its position relative to the gun, but
            // the gun didn't exist back then and we have no need to change it
            setParent(cameraNode)
            renderable = LaserBolt.redRenderable
            localPosition = Vector3(0.0f, -0.07f, -0.2f) // simply what's needed for it to look right
            localRotation = Quaternion.axisAngle(Vector3(1f, 0f, 0f), 40f) // ditto
            name = "laser"
        }

        val lightNode = Node()
        lightNode.setParent(laserBolt)
        lightNode.localPosition = Vector3(0f, 0.08f, 0f) // lift it up so the light can be seen
        lightNode.light = StaticResources.redLaserLight

        laserBolt.fire(Vector3(0.0f, 0.0f, -1.0f), fireCallback = fireCallback) // to the center of the screen
    } // end fire

    private fun setupKickbackAnimation() {

        val startPosition = localPosition
        val movePosition = Vector3(localPosition.x, localPosition.y - 0.008f, localPosition.z)
        val duration = 100L
        kickbackAnimation1 = createVector3Animator(duration, "localPosition", AccelerateInterpolator(), startPosition, movePosition)
        val kickbackAnimation2 = createVector3Animator(duration, "localPosition", AccelerateInterpolator(), movePosition, startPosition)

        kickbackAnimation1.addListener(object : AnimatorListenerAdapter() {

            override fun onAnimationEnd(animation: Animator?) {
                kickbackAnimation2.start()
            }
        })
    } // end setupKickAnimation

    private fun kickback() {

        kickbackAnimation1.start()
    }

    private fun muzzleFlash() {

        muzzleFlashFire.apply {
            animation = scalingAnim
            animation.start()
            animation = rotationAnim
            animation.start()
        }
        muzzleFlashLightAnimator1.start()
        muzzleFlashLightAnimator2.start()
    }

} // end class