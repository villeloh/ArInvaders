package villealla.com.arinvaders

import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.rendering.ModelRenderable

class MainActivity : AppCompatActivity() {

    private lateinit var arFragment: CustomArFragment
    private lateinit var shipManager: ShipManager
    private lateinit var earth: Planet
    private var noPlaneAttached = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_custom_ar)

        arFragment = supportFragmentManager.findFragmentById(R.id.custom_ar_fragment) as CustomArFragment

        earth = Planet()
        earth.obtainRenderable(this)

        loadShipRenderables()

        setFragmentListeners()

        // end setOnTapArPlaneListener
    } // end onCreate

    private fun setFragmentListeners() {

        arFragment.arSceneView.scene.addOnUpdateListener { frameTime ->
            onUpdate(frameTime)
        }

        arFragment.setOnTapArPlaneListener { hitResult, plane, motionEvent ->

            // return if the earth renderable is not ready
            if (earth.earthRenderable == null) {
                return@setOnTapArPlaneListener
            }

            if (noPlaneAttached) {

                earth.renderInArSpace(arFragment, hitResult!!)

                shipManager = ShipManager(this, earth.earthNode)
                shipManager.spawnWaveOfShips(shipType = ShipType.UFO)

                arFragment.disablePlaneDetection()

                arFragment.setOnTapArPlaneListener(null)
            }
        }
    }

    private fun loadShipRenderables() {

        //Load all models that are present in ShipType enum
        ShipType.values().forEach { shipType ->
            ModelRenderable.builder()
                    .setSource(this, Uri.parse(shipType.modelName))
                    .build()
                    .thenAccept { it -> Ship.renderables[shipType] = it }
        }


    }


    // it was used to deal with the image as anchor point in the labs...
    // could be used for something later on I guess
    private fun onUpdate(frameTime: FrameTime) {

        arFragment.onUpdate(frameTime)
        val arFrame = arFragment.arSceneView.arFrame
        if (arFrame == null || arFrame.camera.trackingState != TrackingState.TRACKING) {
            return
        }
    } // end onUpdate
} // end class