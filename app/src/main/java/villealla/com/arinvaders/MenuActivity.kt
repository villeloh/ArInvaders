package villealla.com.arinvaders

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AppCompatActivity
import android.view.Window
import android.view.WindowManager
import android.widget.SeekBar
import kotlinx.android.synthetic.main.fragment_main_menu.*
import villealla.com.arinvaders.Fragments.MenuFragment
import villealla.com.arinvaders.Fragments.NamePromptFragment
import villealla.com.arinvaders.Interfaces.DataPassListener

/*
* Acts as the main menu of the app, where you can choose/change the player name,
* set the desired difficulty and app volume and start the game.
* @author Ville Lohkovuori
* */

class MenuActivity : AppCompatActivity(), DataPassListener {

    private lateinit var playerPrefs: SharedPreferences
    private lateinit var playerName: String

    private val DEFAULT_PLAYER_NAME = "NO_NAME_1234"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // hide the top UI bar of the app
        this.requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        supportActionBar?.hide()
        setContentView(R.layout.activity_menu)

        playerPrefs = this.getSharedPreferences("player_prefs", Context.MODE_PRIVATE)
        playerName = playerPrefs.getString("player_name", DEFAULT_PLAYER_NAME) ?: DEFAULT_PLAYER_NAME

        if (playerName == DEFAULT_PLAYER_NAME) {

            supportFragmentManager.beginTransaction().replace(R.id.mainMenuLayout, NamePromptFragment()).addToBackStack("namePrompt").commit()
        } else {
            createMenuFragment(playerName)
        }
    } // end onCreate

    override fun onBackPressed() {
        super.onBackPressed()

        // God alone knows why this works to close the app
        // with no lingering empty Activity, but it does
        if (supportFragmentManager.fragments.size == 0) {
            this.finish()
        }
    }

    // pass the entered player name from the NamePromptFragment
    override fun passData(data: String, createMenu: Boolean) {

        playerName = data
        playerPrefs.edit().putString("player_name", data).apply()

        if (createMenu) createMenuFragment(data)
    }

    // used for adding the high score 'modal' over the main menu layout
    override fun addFragment(fragment: Fragment) {

        supportFragmentManager.beginTransaction().add(R.id.mainMenuLayout, fragment).addToBackStack(null).commit()
    }

    private fun createMenuFragment(playerName: String) {

        // the only way to remove the NamePromptFragment from the back stack... for some reason
        // it's added there even when not calling .addToBackStack on it
        supportFragmentManager.popBackStack("namePrompt", FragmentManager.POP_BACK_STACK_INCLUSIVE)
        val menuFragment = MenuFragment()
        val args = Bundle()
        args.putString("player_name", playerName)
        menuFragment.arguments = args
        supportFragmentManager.beginTransaction().replace(R.id.mainMenuLayout, menuFragment).addToBackStack(null).commit()
    } // end createMenuFragment

} // end class