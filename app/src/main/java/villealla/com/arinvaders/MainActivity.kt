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
    private lateinit var sm: ShipManager
    private var noPlaneAttached = true

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

                            // NOTE: spawning the Earth should be a function in Planet.kt;
                            // I'll refactor this shortly
                            val anchor = hitResult!!.createAnchor()
                            earthNode = AnchorNode(anchor)
                            earthNode.setParent(arFragment.arSceneView.scene)
                            earthNode.renderable = earthRenderable
                            earthNode.name = "earthNode" // not used for anything atm

                            // in practice, the context here is MainActivity.
                            // not sure if this will cause problems, but it's the least
                            // cluttered solution to passing the proper context
                            sm = ShipManager(applicationContext, earthNode)

                            sm.spawnWaveOfShips(ShipManager.DEFAULT_NUM_OF_SHIPS_IN_WAVE, Ship())

                            // there must be a better way to do this... but it works
                            val frag = arFragment as CustomArFragment
                            frag.disablePlaneDetection()
                            noPlaneAttached = false // could just remove the listener I guess, but I'm not sure how
                        }
                    } // end onTapPlane
                }
        ) // end setOnTapArPlaneListener
    } // end onCreate

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