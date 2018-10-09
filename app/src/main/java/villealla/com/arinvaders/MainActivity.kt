package villealla.com.arinvaders

import android.content.Context
import android.content.Intent
import android.graphics.drawable.TransitionDrawable
import android.hardware.SensorManager
import android.os.*
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import com.google.ar.sceneform.AnchorNode
import kotlinx.android.synthetic.main.fragment_custom_ar.*
import retrofit2.Call
import retrofit2.Response
import villealla.com.arinvaders.API.Leaderboard
import villealla.com.arinvaders.API.ScoreEntry
import villealla.com.arinvaders.Fragments.CustomArFragment
import villealla.com.arinvaders.Fragments.GameOverFragment
import villealla.com.arinvaders.Fragments.HudFragment
import villealla.com.arinvaders.Game.GameManager
import villealla.com.arinvaders.Game.GameState
import villealla.com.arinvaders.Movement.Speedometer
import villealla.com.arinvaders.Sound.Maestro
import villealla.com.arinvaders.Sound.Music
import villealla.com.arinvaders.Sound.SoundEffectPlayer
import villealla.com.arinvaders.Static.Configuration
import villealla.com.arinvaders.Static.StaticResources
import villealla.com.arinvaders.WorldEntities.Gun
import villealla.com.arinvaders.WorldEntities.IFireCallback
import villealla.com.arinvaders.WorldEntities.Planet
import villealla.com.arinvaders.WorldEntities.Ship
import kotlin.math.roundToInt

/*
* Manages UI and ties together most other parts of the app.
* @author Sinan Sakaoğlu, Ville Lohkovuori
* */

class MainActivity : AppCompatActivity(), Speedometer.SpeedometerListener {

    private lateinit var arFragment: CustomArFragment
    private lateinit var gameManager: GameManager
    private lateinit var earth: Planet

    private lateinit var anchorNode: AnchorNode

    private lateinit var mSensorManager: SensorManager

    private lateinit var laserGun: Gun

    private lateinit var speedoMeter: Speedometer

    private lateinit var playerName: String
    private lateinit var difficulty: String
    private var score = 0

    private lateinit var vibrator: Vibrator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // hide the top UI bar of the app
        this.requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        supportActionBar?.hide()
        setContentView(R.layout.fragment_custom_ar)

        arFragment = supportFragmentManager.findFragmentById(R.id.custom_ar_fragment) as CustomArFragment
        supportFragmentManager.beginTransaction().add(R.id.mainLayout, HudFragment()).commit()

        StaticResources.loadAll(this)
        SoundEffectPlayer.loadAllEffects(this)

        gameManager = GameManager.instance
        earth = Planet.instance
        
        setFragmentListeners()

        // monitor acceleration for the ship's speedometer
        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        speedoMeter = Speedometer(mSensorManager, this)

        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        playerName = intent.extras.getString("player_name")
        difficulty = intent.extras.getString("difficulty")

        quitTextView.setOnClickListener {
            gameManager.endGameSession(exitEarly = true)
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

            laserGun = Gun(arFragment.arSceneView.scene.camera)

            gameManager.mainHandler = handler
            gameManager.earthNode = earth
            gameManager.anchorNode = anchorNode
            gameManager.startGameSession(difficulty)

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

                val fireCallback = object : IFireCallback {
                    override fun fireFinished() {
                        playerAttack()
                    }
                }
                laserGun.fire(fireCallback)
            }
            true
        }
    } // end setArViewTouchListener

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
                    Configuration.MESSAGE_VIBRATE -> {

                        vibrator.vibrate(1000)

                    }
                    Configuration.MESSAGE_GAME_OVER -> {

                        supportFragmentManager.beginTransaction().add(R.id.mainLayout, GameOverFragment()).commit()

                        score = killTextView.text.toString().toInt()

                        val totalScore = calculateTotalScore()

                        Leaderboard.service.postScore(playerName, difficulty, totalScore).enqueue(object : retrofit2.Callback<List<List<ScoreEntry>>> {
                            override fun onFailure(call: Call<List<List<ScoreEntry>>>, t: Throwable) {
                                Log.d(Configuration.DEBUG_TAG, "Score post failed.")
                                t.printStackTrace()
                                startMenuActivity()
                            }

                            override fun onResponse(call: Call<List<List<ScoreEntry>>>, response: Response<List<List<ScoreEntry>>>) {
                                Log.d(Configuration.DEBUG_TAG, "Score post successful.")
                                startMenuActivity()
                            }
                        })

                    }

                } // end when
            } // end null check
        } // end handleMessage
    } // end handler

    private fun calculateTotalScore(): Int {


        val multiplier: Float = when (difficulty) {
            "easy" -> 0.53f
            "hard" -> 1.52f
            else -> 1.06f
        }

        return (score * multiplier).toInt() * 17
    }

    override fun onPause() {
        super.onPause()
        if (gameManager.gameState == GameState.RUNNING) {
            gameManager.pauseGameSession()
        }
        vibrator.cancel()
        speedoMeter.onActivityPause()
    }

    override fun onResume() {
        super.onResume()
        if (gameManager.gameState == GameState.PAUSED) {
            gameManager.resumeGameSession()
        }

        // the only way to restart it seems to be to reassign it
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        speedoMeter.onActivityResume()
    } // end onResume

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

    // called from SpeedoMeter
    override fun onSpeedChange(ownShipSpeed: Float) {

        speedTextView.text = ownShipSpeed.roundToInt().toString()
    }

} // end class