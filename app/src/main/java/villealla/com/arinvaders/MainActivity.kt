package villealla.com.arinvaders

import android.graphics.BitmapFactory
import android.graphics.drawable.TransitionDrawable
import android.net.Uri
import android.os.*
import android.support.v7.app.AppCompatActivity
import android.view.MotionEvent
import android.view.View
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
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
import villealla.com.arinvaders.WorldEntities.IFireCallback
import villealla.com.arinvaders.WorldEntities.LaserBolt
import villealla.com.arinvaders.WorldEntities.Planet
import villealla.com.arinvaders.WorldEntities.Ship

/*
* Manages UI and ties together most other parts of the app.
* @author Sinan Sakaoğlu, Ville Lohkovuori
* */

class MainActivity : AppCompatActivity() {

    private lateinit var arFragment: CustomArFragment
    private lateinit var gameManager: GameManager
    private lateinit var earth: Planet

    private lateinit var laserRenderable: ModelRenderable
    private lateinit var laserTexture: Texture

    private lateinit var anchorNode: AnchorNode

    companion object {

        // referred to from the Ship class
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
        loadLaserGraphics()
        SoundEffectPlayer.loadAllEffects(this)

        setFragmentListeners()
    } // end onCreate

    private fun setFragmentListeners() {

        arFragment.setOnTapArPlaneListener { hitResult, plane, motionEvent ->

            // return if the earth renderable is not ready
            if (earth.earthRenderable == null) {
                return@setOnTapArPlaneListener
            }

            val anchor = hitResult.createAnchor()
            anchorNode = AnchorNode(anchor)
            anchorNode.setParent(arFragment.arSceneView.scene)

            earth.renderInArSpace(anchorNode)

            gameManager.mainHandler = handler
            gameManager.earthNode = earth
            gameManager.startGameSession()

            // Play game music
            Maestro.playMusic(this, Music.BATTLE, true)

            // Starts attack/shooting listener
            setArViewTouchListener()

            arFragment.disablePlaneDetection()
            arFragment.setOnTapArPlaneListener(null)
        }
    } // end setFragmentListeners

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
                fireLaser()
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
    } // end loadExplosionGraphics

    private fun loadLaserGraphics() {

        val bitMap = BitmapFactory.decodeResource(resources, R.drawable.blaster_bolt)
        Texture.builder().setSource(bitMap).build().thenAccept { it -> laserTexture = it

            ModelRenderable.builder()
                    .setSource(this, Uri.parse("laser_2.sfb"))
                    .build()
                    .thenAccept { it2 -> laserRenderable = it2
                        laserRenderable.material.setTexture("", laserTexture)
                        laserRenderable.isShadowCaster = false
                    }
        }
    } // end loadLaserGraphics

    private fun fireLaser() {

        SoundEffectPlayer.playEffect(SoundEffects.LASER)

        val laserBolt = LaserBolt()
        laserBolt.setParent(arFragment.arSceneView.scene.camera)
        laserBolt.renderable = laserRenderable
        laserBolt.localPosition = Vector3(0.0f, -0.07f, -0.2f) // simply what's needed for it to look right
        laserBolt.localRotation = Quaternion.axisAngle(Vector3(1f, 0f, 0f), 40f) // ditto
        laserBolt.name = "laser"

        laserBolt.fire(Vector3(0.0f, 0.0f, -1.0f), object : IFireCallback {
            override fun fireFinished() {
                playerAttack()
            }
        }) // to the center of the screen
    } // end fireLaser

    private fun playerAttack() {
        val screenCenterMEvent = obtainScreenCenterMotionEvent()

        val hitTestResult = arFragment.arSceneView.scene.hitTest(screenCenterMEvent)
        val hitNode = hitTestResult.node

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

    // Handles receiving updates and applying them to the ui
    private val handler = object : Handler(Looper.getMainLooper()) {

        override fun handleMessage(message: Message?) {
            if (message != null) {
                val newValue = message.data.getString(message.what.toString())
                when (message.what) {

                    Configuration.MESSAGE_PEOPLE_ALIVE -> {
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
                } // end when
            } // end null check
        } // end handleMessage
    } // end handler

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