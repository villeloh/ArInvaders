package villealla.com.arinvaders.WorldEntities

import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import villealla.com.arinvaders.Sound.SoundEffectPlayer
import villealla.com.arinvaders.Sound.SoundEffects
import villealla.com.arinvaders.Static.StaticResources

class OwnShipWeapon(private val cameraNode: Node) {

    val gun = Gun(cameraNode)

    init {

    }

    // note: the passed callback is an object that contains a call to playerAttack in MainActivity
    fun fire(fireCallback: IFireCallback) {

        SoundEffectPlayer.playEffect(SoundEffects.LASER)
        gun.kickback()

        val laserBolt = LaserBolt().apply {
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

} // end class