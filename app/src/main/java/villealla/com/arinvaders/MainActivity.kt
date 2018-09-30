package villealla.com.arinvaders

import android.graphics.drawable.TransitionDrawable
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.support.v7.app.AppCompatActivity
import android.view.MotionEvent
import android.view.View
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.rendering.ModelRenderable
import kotlinx.android.synthetic.main.fragment_custom_ar.*
import villealla.com.arinvaders.Fragments.CustomArFragment
import villealla.com.arinvaders.Fragments.HudFragment
import villealla.com.arinvaders.Game.GameManager
import villealla.com.arinvaders.Sound.Maestro
import villealla.com.arinvaders.Sound.Music
import villealla.com.arinvaders.Sound.SoundEffectPlayer
import villealla.com.arinvaders.Sound.SoundEffects
import villealla.com.arinvaders.Static.ShipType
import villealla.com.arinvaders.WorldEntities.Planet
import villealla.com.arinvaders.WorldEntities.Ship


class MainActivity : AppCompatActivity(), GameManager.BridgeActivity {

    private lateinit var arFragment: CustomArFragment
    private lateinit var shipManager: ShipManager
    private lateinit var gameManager: GameManager
    private lateinit var earth: Planet

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_custom_ar)

        arFragment = supportFragmentManager.findFragmentById(R.id.custom_ar_fragment) as CustomArFragment
        supportFragmentManager.beginTransaction().add(R.id.mainLayout, HudFragment()).commit()

        gameManager = GameManager.instance
        earth = Planet.instance
        shipManager = ShipManager.instance
        earth.loadRenderable(this)

        loadShipRenderables()
        shipManager.loadExplosionGraphics(this)
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


            shipManager.setEarthNode(earth.earthNode)

            gameManager.setMainActivity(this)
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

    // these could be combined into one method (or two at most);
    // I'll refactor later
    override fun updateKillCount(newValue: Int) {
        killTextView.text = newValue.toString()
    }

    override fun updateWaveNumber(newValue: Int) {
        waveNumberTextView.text = newValue.toString()
    }

    override fun updateNumberLeftInWave(newValue: Int) {
        waveKillTextView.text = newValue.toString()
    }

    override fun updatePeopleLeftOnPlanet(newValue: Long) {
        peopleTextView.text = newValue.toString()
        val transition = peopleTextView.background as TransitionDrawable
        transition.startTransition(500)
        transition.reverseTransition(500)
    }

    override fun onPause() {
        super.onPause()
        Maestro.pauseMusic()
    }

    override fun onResume() {
        super.onResume()
        Maestro.resumeMusic()
    }

} // end class