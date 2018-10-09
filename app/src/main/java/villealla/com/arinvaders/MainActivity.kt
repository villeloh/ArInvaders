package villealla.com.arinvaders

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.TransitionDrawable
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.*
import android.support.v7.app.AppCompatActivity
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.collision.Box
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.Color
import com.google.ar.sceneform.rendering.Light
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
import villealla.com.arinvaders.WorldEntities.*
import kotlin.math.roundToInt
import kotlin.math.sqrt

/*
* Manages UI and ties together most other parts of the app.
* @author Sinan Sakaoğlu, Ville Lohkovuori
* */

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var arFragment: CustomArFragment
    private lateinit var gameManager: GameManager
    private lateinit var earth: Planet

    private lateinit var laserLight: Light
    private lateinit var gun: Gun

    private lateinit var anchorNode: AnchorNode

    private lateinit var mSensorManager: SensorManager
    private lateinit var mSensor: Sensor
    private var shipSpeed: Float = 0f
    private var accX: Float = 0f
    private var accY: Float = 0f
    private var accZ: Float = 0f

    private lateinit var playerName: String
    private lateinit var difficulty: String
    private var score = 0

    // I just put this in MainActivity, because getSystemService can't easily be
    // used by the non-activity classes
    private lateinit var vibrator: Vibrator

    companion object {

        // referred to from the Ship class
        lateinit var explosionRenderable: ModelRenderable
        lateinit var explosionTexture: Texture
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // hide the top UI bar of the app
        this.requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        supportActionBar?.hide()
        setContentView(R.layout.fragment_custom_ar)

        arFragment = supportFragmentManager.findFragmentById(R.id.custom_ar_fragment) as CustomArFragment
        supportFragmentManager.beginTransaction().add(R.id.mainLayout, HudFragment()).commit()

        gameManager = GameManager.instance
        earth = Planet.instance

        earth.loadRenderable(this)
        loadShipRenderables()
        loadExplosionGraphics()
        loadLaserGraphics()
        loadFireModel()
        SoundEffectPlayer.loadAllEffects(this)

        setFragmentListeners()

        // monitor acceleration for the ship's speedometer
        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)

        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        // TODO: assign the actual received playerName and difficulty (from the arguments field)
        playerName = "huu"
        difficulty = "normal"

        quitTextView.setOnClickListener {

            startMenuActivity()
        }
    } // end onCreate

    private fun setFragmentListeners() {

        arFragment.setOnTapArPlaneListener { hitResult, _, _ ->

            // return if the earth renderable is not ready
            if (earth.earthRenderable == null) {
                return@setOnTapArPlaneListener
            }

            val anchor = hitResult.createAnchor()
            anchorNode = AnchorNode(anchor)
            anchorNode.setParent(arFragment.arSceneView.scene)

            earth.renderInArSpace(anchorNode)

            setupGun()

            gameManager.mainHandler = handler
            gameManager.earthNode = earth
            gameManager.anchorNode = anchorNode
            gameManager.startGameSession()

            // Play game music
            Maestro.playMusic(this, Music.BATTLE, true)

            // Starts attack/shooting listener
            setArViewTouchListener()

            arFragment.disablePlaneDetection()
            arFragment.setOnTapArPlaneListener(null)
        }
    } // end setFragmentListeners


    private fun setArViewTouchListener() {

        arFragment.arSceneView.scene.setOnTouchListener { _, motionEvent ->

            // this ensures that we only get one attack per finger tap
            if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                fireLaser()
            }
            true
        }
    }


    private fun fireLaser() {

        SoundEffectPlayer.playEffect(SoundEffects.LASER)
        gun.kickback()

        val laserBolt = LaserBolt()
        laserBolt.setParent(arFragment.arSceneView.scene.camera)
        laserBolt.renderable = LaserBolt.redRenderable
        laserBolt.localPosition = Vector3(0.0f, -0.07f, -0.2f) // simply what's needed for it to look right
        laserBolt.localRotation = Quaternion.axisAngle(Vector3(1f, 0f, 0f), 40f) // ditto
        laserBolt.name = "laser"

        val lightNode = Node()
        lightNode.setParent(laserBolt)
        lightNode.localPosition = Vector3(0f, 0.08f, 0f) // lift it up so the light can be seen
        lightNode.light = laserLight

        laserBolt.fire(Vector3(0.0f, 0.0f, -1.0f), fireCallback = object : IFireCallback {
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

                        //Flash background
                        val transition = peopleTextView.background as TransitionDrawable
                        transition.startTransition(500)
                        transition.reverseTransition(500)

                        vibrator.vibrate(1000L) // 1 second
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
                    Configuration.MESSAGE_RESET -> {
                        peopleTextView.text = Configuration.EARTH_POPULATION.toString()
                        killTextView.text = "0"
                    }

                } // end when
            } // end null check
        } // end handleMessage
    } // end handler

    override fun onPause() {
        super.onPause()
        if (gameManager.gameState == GameState.RUNNING) {
            gameManager.pauseGameSession()
        }
        vibrator.cancel()
        mSensorManager.unregisterListener(this)
    }

    override fun onResume() {
        super.onResume()
        if (gameManager.gameState == GameState.PAUSED) {
            gameManager.resumeGameSession()
        }

        // the only way to restart it seems to be to reassign it
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        // for some reason, this needs to be done again with every resume
        mSensor.also {
            mSensorManager.registerListener(this, it,
                    SensorManager.SENSOR_DELAY_NORMAL)
        }
    } // end onResume

    // monitor sensor events and update the speedometer value based on them
    override fun onSensorChanged(event: SensorEvent?) {

        if (event == null) return

        // to limit the update speed of the speedometer to a human-readable level
        val timeCondition = (event.timestamp / 100000000L) % 5 == 0L

        if (event.sensor == mSensor && timeCondition) {

            accX = event.values?.get(0) ?: 0f
            accY = event.values?.get(1) ?: 0f
            accZ = event.values?.get(2) ?: 0f
            val exp = 2.0
            val accXSquared = Math.pow(accX.toDouble(),  exp).toFloat()
            val accYSquared = Math.pow(accY.toDouble(),  exp).toFloat()
            val accZSquared = Math.pow(accZ.toDouble(),  exp).toFloat()
            shipSpeed = sqrt(accXSquared + accYSquared + accZSquared) * 1000 // gives believable 'space speeds'

            speedTextView.text = shipSpeed.roundToInt().toString()
        }
    } // end onSensorChanged

    // it needs to be implemented whether we need it or not
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { }

    // TODO: this should also be called when the game is over (we may need an interface to do that)
    private fun startMenuActivity() {

        val intent = Intent(this, MenuActivity::class.java).apply {

            // some funky bitwise operation; equivalent to '|' in Java.
            // this gets rid of the ar fragment back stack when returning to menu
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra("player_name", playerName)
            putExtra("difficulty", difficulty)
            putExtra("score", score)
        }
        this.finish() // finish the MainActivity
        startActivity(intent)
    } // end startMenuActivity

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

        ModelRenderable.builder()
                .setSource(this, Uri.parse("laser_2.sfb"))
                .build()
                .thenAccept { it ->
                    LaserBolt.redRenderable = it
                    LaserBolt.redRenderable.isShadowCaster = false
                    LaserBolt.redRenderable.isShadowReceiver = false
                }
        ModelRenderable.builder()
                .setSource(this, Uri.parse("laser_yellow.sfb"))
                .build()
                .thenAccept { it ->
                    LaserBolt.yellowRenderable = it
                    LaserBolt.yellowRenderable.isShadowCaster = false
                    LaserBolt.yellowRenderable.isShadowReceiver = false
                }

    } // end loadLaserGraphics

    private fun setupGun() {

        ModelRenderable.builder()
                .setSource(this, Uri.parse("Gun.sfb"))
                .build()
                .thenAccept { it ->

                    Gun.modelRenderable = it
                    Gun.modelRenderable.isShadowCaster = false
                    Gun.modelRenderable.isShadowReceiver = false
                    Gun.modelRenderable.collisionShape = Box(Vector3(0.001f, 0.001f, 0.001f)) // so that we don't hit the modelRenderable
                    gun = Gun()
                    gun.setParent(arFragment.arSceneView.scene.camera)
                    gun.renderable = Gun.modelRenderable
                    gun.localPosition = Vector3(0.015f, -0.065f, -0.2f) // simply what's needed for it to look right
                    gun.localRotation = Quaternion.axisAngle(Vector3(1f, 0.34f, 0f), 40f) // ditto
                    gun.name = "gun"
                    gun.setupAnimation() // must be called last due to needing the updated localposition!

                    // it's an awkward place for it, but meh, it's still static and gun-related
                    laserLight = Light.builder(Light.Type.POINT)
                            .setIntensity(4000f)
                            .setFalloffRadius(200f)
                            .setShadowCastingEnabled(false)
                            .setColorTemperature(10000f)
                            .setColor(Color(1f, 0f, 0f))
                            .build()
                }
    } // end setupGun

    private fun loadShipRenderables() {

        // Load all models that are present in ShipType enum
        ShipType.values().forEach { shipType ->
            ModelRenderable.builder()
                    .setSource(this, Uri.parse(shipType.modelName))
                    .build()
                    .thenAccept { it -> Ship.renderables[shipType] = it }
        }
    }

    private fun loadFireModel() {
        ModelRenderable.builder()
                .setSource(this, Uri.parse("fire.sfb"))
                .build()
                .thenAccept { it -> Fire.model = it }
    }

} // end class