package villealla.com.arinvaders

import android.graphics.BitmapFactory
import android.graphics.drawable.TransitionDrawable
import android.net.Uri
import android.os.*
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Texture
import kotlinx.android.synthetic.main.fragment_custom_ar.*
import villealla.com.arinvaders.Fragments.CustomArFragment
import villealla.com.arinvaders.Fragments.HudFragment
import villealla.com.arinvaders.Game.GameManager
import villealla.com.arinvaders.Game.GameState
import villealla.com.arinvaders.Sound.Maestro
import villealla.com.arinvaders.Sound.Music
import villealla.com.arinvaders.Sound.SoundEffectPlayer
import villealla.com.arinvaders.Sound.SoundEffects
import villealla.com.arinvaders.Static.Configuration
import villealla.com.arinvaders.Static.ShipType
import villealla.com.arinvaders.WorldEntities.Planet
import villealla.com.arinvaders.WorldEntities.Ship


class MainActivity : AppCompatActivity() {

    private lateinit var arFragment: CustomArFragment
    private lateinit var gameManager: GameManager
    private lateinit var earth: Planet

    companion object {

        lateinit var explosionRenderable: ModelRenderable
        lateinit var explosionTexture: Texture
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_custom_ar)

        arFragment = supportFragmentManager.findFragmentById(R.id.custom_ar_fragment) as CustomArFragment
        supportFragmentManager.beginTransaction().add(R.id.mainLayout, HudFragment()).commit()

        gameManager = GameManager.instance
        earth = Planet.instance

        earth.loadRenderable(this)
        loadShipRenderables()
        loadExplosionGraphics()
        SoundEffectPlayer.loadAllEffects(this)


        setFragmentListeners()
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

            gameManager.mainHandler = handler
            gameManager.earthNode = earth.earthNode
            gameManager.startGameSession()


            //Play game music
            Maestro.playMusic(this, Music.BATTLE, true)

            //Starts attack/shooting listener
            setArViewTouchListener()

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

        arFragment.arSceneView.scene.setOnTouchListener { _, motionEvent ->

            // this ensures that we only get one attack per finger tap
            if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                playerAttack()
            }
            true
        }
    }

    private fun loadExplosionGraphics() {

        val bitMap = BitmapFactory.decodeResource(resources, R.drawable.smoke_tx)
        Texture.builder().setSource(bitMap).build().thenAccept { it ->
            explosionTexture = it

            val renderable = ModelRenderable.builder()
                    .setSource(this, Uri.parse("model.sfb"))
                    .build()
            renderable.thenAccept { it2 ->

                it2.material.setTexture("", explosionTexture)
                explosionRenderable = it2
            }
        }
    }

    private fun playerAttack() {

        val screenCenterMotionEvent = obtainScreenCenterMotionEvent()

        val hitTestResult = arFragment.arSceneView.scene.hitTest(screenCenterMotionEvent)
        val hitNode = hitTestResult.node

        SoundEffectPlayer.playEffect(SoundEffects.LASER)


        if (hitNode is Ship) {
            hitNode.damageShip(1)
        }


    } // end playerAttack

    // creates a 'fake' MotionEvent that 'touches' the center of the screen
    private fun obtainScreenCenterMotionEvent(): MotionEvent {

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


    // Handles recieving updates and applying them to the ui
    private val handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(message: Message?) {
            //Log.d(Configuration.DEBUG_TAG,"Handle message ${message?.what}")
            if (message != null) {
                val newValue = message.data.getString(message.what.toString())
                when (message.what) {

                    Configuration.MESSAGE_PEOPLE_ALIVE -> {
                        Log.d(Configuration.DEBUG_TAG, "alive: $newValue")
                        peopleTextView.text = newValue
                        val transition = peopleTextView.background as TransitionDrawable
                        transition.startTransition(500)
                        transition.reverseTransition(500)
                    }
                    Configuration.MESSAGE_KILL_COUNT -> {
                        killTextView.text = newValue
                    }
                    Configuration.MESSAGE_SHIPS_LEFT_IN_WAVE -> {
                        waveKillTextView.text = newValue
                    }
                    Configuration.MESSAGE_WAVE_NUMBER -> {
                        waveNumberTextView.text = "WAVE " + newValue
                    }


                }
            }
        }
    }


    override fun onPause() {
        super.onPause()
        if (gameManager.gameState == GameState.RUNNING)
            gameManager.pauseGameSession()
    }

    override fun onResume() {
        super.onResume()
        if (gameManager.gameState == GameState.PAUSED)
            gameManager.resumeGameSession()
    }

} // end class