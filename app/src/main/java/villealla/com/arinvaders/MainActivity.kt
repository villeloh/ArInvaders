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
    private lateinit var sm: ShipManager
    private lateinit var earth: Planet
    private var noPlaneAttached = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_custom_ar)

        arFragment = supportFragmentManager.findFragmentById(R.id.custom_ar_fragment) as ArFragment

        earth = Planet()
        earth.obtainRenderable(this)

        arFragment.arSceneView.scene.addOnUpdateListener{ frameTime ->
            onUpdate(frameTime)
        }

        arFragment.setOnTapArPlaneListener(

                object: BaseArFragment.OnTapArPlaneListener {

                    override fun onTapPlane(hitResult: HitResult?, plane: Plane?, motionEvent: MotionEvent?) {

                        // disabling this for now, to see if it's actually needed or not
                    /*  if(earthRenderable == null) {
                            return@onTapPlane
                        }*/

                        if (noPlaneAttached) {

                            earth.renderInArSpace(arFragment, hitResult!!)

                            // in practice, the context here is MainActivity.
                            // not sure if this will cause problems, but it's the least
                            // cluttered solution to passing the proper context
                            sm = ShipManager(applicationContext, earth.earthNode)
                            sm.spawnWaveOfShips()

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