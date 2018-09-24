package villealla.com.arinvaders

import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MotionEvent
import android.view.View
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.rendering.ModelRenderable
import android.os.SystemClock
import android.util.Log

class MainActivity : AppCompatActivity() {

    private lateinit var arFragment: CustomArFragment
    private lateinit var shipManager: ShipManager
    private lateinit var gameManager: GameManager
    private lateinit var earth: Planet

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_custom_ar)

        arFragment = supportFragmentManager.findFragmentById(R.id.custom_ar_fragment) as CustomArFragment

        earth = Planet()
        earth.obtainRenderable(this)

        loadShipRenderables()
        setFragmentListeners()
        setArViewTouchListener()

        gameManager = GameManager.instance
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

            earth.renderInArSpace(arFragment, hitResult!!)

            shipManager = ShipManager.instance
            shipManager.setEarthNode(earth.earthNode)

            gameManager.startGameSession()

            arFragment.disablePlaneDetection()
            arFragment.setOnTapArPlaneListener(null)
        }
    }

    private fun loadShipRenderables() {

        // Load all models that are present in ShipType enum
        ShipType.values().forEach { shipType ->
            ModelRenderable.builder()
                    .setSource(this, Uri.parse(shipType.modelName))
                    .build()
                    .thenAccept { it -> Ship.renderables[shipType] = it }
        }
    }

    private fun setArViewTouchListener() {

        arFragment.arSceneView.scene.setOnTouchListener {
            _, _ ->
            // the 'real' HitTestResult and MotionEvent are not needed here

            playerAttack()
            true
        }
    }

    private fun playerAttack() {

        val screenCenterMotionEvent = obtainScreenCenterMotionEvent()

        val hitTestResult = arFragment.arSceneView.scene.hitTest(screenCenterMotionEvent)
        val hitNode = hitTestResult.node

        if (hitNode != null && hitNode.name != "earthNode") {

            Log.d(Configuration.DEBUG_TAG, "node name: " + hitNode.name)
            shipManager.damageShip(1, hitNode.name)
        }
    } // end playerAttack

    // creates a 'fake' MotionEvent that 'touches' the center of the screen
    private  fun obtainScreenCenterMotionEvent(): MotionEvent {

        val screenCenter = getScreenCenter()

        val downTime = SystemClock.uptimeMillis()
        val eventTime = SystemClock.uptimeMillis() + 100
        val x = screenCenter.x.toFloat()
        val y = screenCenter.y.toFloat()

        val metaState = 0
        return MotionEvent.obtain(
                downTime,
                eventTime,
                MotionEvent.ACTION_UP,
                x,
                y,
                metaState)
    } // end obtainScreenCenterMotionEvent

    private fun getScreenCenter(): android.graphics.Point {

        val mainView = findViewById<View>(android.R.id.content)
        return android.graphics.Point(mainView.width / 2, mainView.height / 2)
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