package villealla.com.arinvaders.Fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.SeekBar
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_main_menu.*
import villealla.com.arinvaders.Interfaces.DataPassListener
import villealla.com.arinvaders.MainActivity
import villealla.com.arinvaders.R

class MenuFragment : Fragment() {

    private lateinit var allDiffButtons: MutableList<View>
    private lateinit var menuActivity: DataPassListener
    private lateinit var playerName: String
    private lateinit var difficulty: String

    private lateinit var audioManager: AudioManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main_menu, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        menuActivity = activity as DataPassListener
        val activityAsActivity = activity as Activity // seems a bit ridiculous, but we need to access the damned Activity -.-
        activityAsActivity.volumeControlStream = AudioManager.STREAM_MUSIC
        audioManager = activityAsActivity.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        // hook up the app volume bar
        initVolumeControls()

        val uri = Uri.parse("android.resource://" + activity!!.packageName + "/" + R.raw.bg_video)
        videoView.setVideoURI(uri)
        videoView.start()

        videoView.setOnCompletionListener { it.start() }

        allDiffButtons = getAllDirectChildViews(difficultyLayout)

        difficultyLayout.apply {

            // normal diff is chosen at start
            makeChosenDifficulty(normalButton)

            easyButton.setOnClickListener { makeChosenDifficulty(it) }
            normalButton.setOnClickListener { makeChosenDifficulty(it) }
            hardButton.setOnClickListener { makeChosenDifficulty(it) }
        }

        renameImageView.setOnClickListener {

            renameEditText.visibility = View.VISIBLE
            renameEditText.setText(playerName, TextView.BufferType.EDITABLE)
            renameEditText.requestFocus()
        }

        viewScoresBtn.setOnClickListener {

            val scoreFragment = HighScoreFragment()
            val args = Bundle()
            // TODO: put the actual scores in the args Bundle
            scoreFragment.arguments = args
            menuActivity.addFragment(scoreFragment)
        }

        renameEditText.setOnEditorActionListener {
            editText: View, actionId: Int, _ ->

            if (actionId == EditorInfo.IME_ACTION_DONE) {

                if (renameEditText.text.isNotEmpty()) {

                    playerName = renameEditText.text.toString()
                    nameTextView.text = playerName
                    editText.visibility = View.GONE
                    menuActivity.passData(playerName, false) // no need to recreate the menu
                }
            }
            true
        } // end setOnEditorActionListener

        newGameTextViewBtn.setOnClickListener {

            startMainActivity()
        }

        val args = arguments
        if (args != null) {
            playerName = args.getString("player_name") ?: "NO PLAYERNAME FOUND, UH-OH!"
            nameTextView.text = playerName
        }
    } // end onActivityCreated

    override fun onResume() {
        super.onResume()

        if (!videoView.isPlaying) videoView.start()
    }

    private fun makeChosenDifficulty(button: View) {

        val btn = button as TextView // technically, the 'buttons' are TextViews in this case
        difficulty = btn.text.toString().toLowerCase()
        button.setBackgroundResource(R.drawable.bg_darkblue_border_magenta)
        allDiffButtons.forEach { it ->

            // there's probably a way to avoid having to do this...
            if (it.id != button.id) {
                makeUnchosenDifficulty(it)
            }
        }
    } // end makeChosenDiffButton

    private fun makeUnchosenDifficulty(button: View) {

        button.setBackgroundResource(R.drawable.bg_darkblue_border_thin_lightblue)
    }

    private fun getAllDirectChildViews(view: View): MutableList<View> {

        val list = mutableListOf<View>()

        if (view is ViewGroup) {

            for (i in 0..view.childCount) {

                val child = view.getChildAt(i)

                // how tf it can be null is anyone's guess
                if (child != null) list.add(child)
            }
        } // end if
        return list
    } // end getAllDirectChildViews

    private fun startMainActivity() {

        val intent = Intent(activity as Context, MainActivity::class.java).apply {

            // some funky bitwise operation; equivalent to '|' in Java.
            // this gets rid of the menu view back stack when launching the main game
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra("player_name", playerName)
            putExtra("difficulty", difficulty)
        }
        (activity as Activity).finish()
        startActivity(intent)
    } // end startMainActivity

    private fun initVolumeControls() {

        volumeBar.apply {
            max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            progress = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

                override fun onStopTrackingTouch(arg0: SeekBar) {}

                override fun onStartTrackingTouch(arg0: SeekBar) {}

                override fun onProgressChanged(arg0: SeekBar, progress: Int, arg2: Boolean) {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                            progress, 0)
                }
            })
        }
    } // end initVolumeControls

} // end class