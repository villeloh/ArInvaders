package villealla.com.arinvaders

import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.BaseArFragment
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var arFragment: ArFragment
    private lateinit var earthRenderable: ModelRenderable
    private lateinit var earthNode: Node
    private var noPlaneAttached = true
    private val planetHp = Configuration.INITIAL_PLANET_HP

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_custom_ar)

        arFragment = supportFragmentManager.findFragmentById(R.id.custom_ar_fragment) as ArFragment

        val renderable = ModelRenderable.builder()
                .setSource(this, Uri.parse("earth_ball.sfb"))
                .build()
        renderable.thenAccept {  it -> earthRenderable = it }

        arFragment.arSceneView.scene.addOnUpdateListener{ frameTime ->
            onUpdate(frameTime)
        }

        arFragment.setOnTapArPlaneListener(

                object: BaseArFragment.OnTapArPlaneListener {

                    override fun onTapPlane(hitResult: HitResult?, plane: Plane?, motionEvent: MotionEvent?) {

                        // iirc, it can be null despite AS's reassurances
                        if(earthRenderable == null) {
                            return@onTapPlane
                        }

                        if (noPlaneAttached) {
                            val anchor = hitResult!!.createAnchor()
                            earthNode = AnchorNode(anchor)
                            earthNode.setParent(arFragment.arSceneView.scene)
                            earthNode.renderable = earthRenderable
                            earthNode.name = "earthNode" // not used for anything atm

                            // now that we have set earthNode, we can spawn the
                            // initial wave of ships
                            spawnWaveOfShips(Configuration.NUM_OF_SHIPS_IN_WAVE_DEFAULT, Ship())

                            // there must be a better way to do this... but it works
                            val frag = arFragment as CustomArFragment
                            frag.disablePlaneDetection()
                            noPlaneAttached = false // could just remove the listener I guess, but I'm not sure how
                        }
                    } // end onTapPlane
                }
        ) // end setOnTapArPlaneListener
    } // end onCreate

    // should be moved to the Ship class I guess...
    // moving it is problematic because of context and earthNode, though
    private fun spawnShip(ship: Ship) {

        val renderable = ModelRenderable.builder()
                .setSource(this, Uri.parse(ship.type + ".sfb"))
                .build()
        renderable.thenAccept {  it ->

            // we'll need to make our own Node subclass for moving the ships...
            // for now let's just try to create them
            val shipNode = Node()
            shipNode.renderable = it
            shipNode.setParent(earthNode)
            shipNode.renderable.isShadowCaster = false

            val spawnCoord = generateRandomCoord()
            shipNode.localPosition = spawnCoord
        }

        // TODO: we could make each ship move here...
        // or put a move method in the Ship class and call it
        // for each ship by putting all ships in an array. if we want to alter ship behavior
        // after spawning, we should ofc do the latter
    } // end spawnShip

    private fun spawnWaveOfShips(numOfShips: Int, ship: Ship) {
        for (item in 0..numOfShips) {

            spawnShip(ship)
        }
    }

    private fun generateRandomCoord(): Vector3 {

        // TODO: use the stored constants from Configuration.kt... I just made this work as quick as possible
        // NOTE: while this seems to work ok'ish, the UFOs appear all clustered up in one 'corner'
        // of the coordinate space -- not all around the Earth as you'd expect
        val randomGen = Random()
        val x = randomGen.nextFloat() * 0.35
        val y = randomGen.nextFloat() * 0.35
        val z = randomGen.nextFloat() * 0.35
        Log.d("XYZ", "$x $y $z")

        return Vector3(x.toFloat(), y.toFloat(), z.toFloat())
    }

    // it was used to deal with the image as anchor point in the labs...
    // could be used for something later on I guess
    private fun onUpdate(frameTime: FrameTime) {

        arFragment.onUpdate(frameTime)
        val arFrame= arFragment.arSceneView.arFrame
        if(arFrame == null || arFrame.camera.trackingState != TrackingState.TRACKING) {
            return
        }
    } // end onUpdate
} // end class