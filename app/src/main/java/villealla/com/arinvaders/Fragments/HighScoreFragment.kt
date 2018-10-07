package villealla.com.arinvaders.Fragments

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.fragment_highscore.*
import villealla.com.arinvaders.Game.HighScore
import villealla.com.arinvaders.R

/*
* Class for the high score 'modal' that pops up when clicking
* the 'View Score' button in the main menu.
* @author Ville Lohkovuori
* */

class HighScoreFragment : Fragment() {

    private lateinit var menuActivity: Activity
    private lateinit var adapter:  ArrayAdapter<HighScore>
    private var personalView = true

    private val scores = arrayOf<HighScore>(
            HighScore(1,"huu", 100),
            HighScore(1,"huu", 100),
            HighScore(1,"huu", 100),
            HighScore(1,"huu", 100),
            HighScore(1,"huu", 100),
            HighScore(1,"huu", 100),
            HighScore(1,"huu", 100),
            HighScore(1,"huu", 100)
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_highscore, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        menuActivity = activity as Activity
        adapter = ArrayAdapter(activity as Context, R.layout.scorelist_item, scores)
        setListViewAdapter(adapter)

        val personalBestText = menuActivity.resources.getString(R.string.personal_best)
        val globalBestText = menuActivity.resources.getString(R.string.global_best)

        switchBtn.setOnClickListener {

            if (personalView) {

                switchBtn.text = globalBestText
            } else {
                switchBtn.text = personalBestText
            }
            personalView = !personalView
        }
    } // end onActivityCreated

    private fun setListViewAdapter(adapter: ArrayAdapter<HighScore>) {

        easyListView.adapter = adapter
        normalListView.adapter = adapter
        hardListView.adapter = adapter
    }

} // end class