package villealla.com.arinvaders.Fragments

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListAdapter
import android.widget.ListView
import kotlinx.android.synthetic.main.fragment_highscore.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import villealla.com.arinvaders.API.Leaderboard
import villealla.com.arinvaders.API.ScoreEntry
import villealla.com.arinvaders.Game.HighScore
import villealla.com.arinvaders.R
import villealla.com.arinvaders.Static.Configuration


/*
* Class for the high score 'modal' that pops up when clicking
* the 'View Score' button in the main menu.
* @author Ville Lohkovuori
* */

class HighScoreFragment : Fragment() {

    private lateinit var menuActivity: Activity
    private var personalView = true
    private lateinit var listArray: Array<ListView>
    private var personalBestAdapters = mutableListOf<ListAdapter>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_highscore, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        menuActivity = activity as Activity

        //create an array of ListViews to be used in requests
        listArray = arrayOf(easyListView, normalListView, hardListView)

        getScoreboardData(false, menuActivity)

        val personalBestText = menuActivity.resources.getString(R.string.personal_best)
        val globalBestText = menuActivity.resources.getString(R.string.global_best)

        switchBtn.setOnClickListener {
            personalView = !personalView
            if (personalView) {
                switchBtn.text = personalBestText

                // Check if the personal scores are loaded, if yes use the loaded adapter
                if (personalBestAdapters.isEmpty()) {
                    getScoreboardData(false, menuActivity)
                } else {
                    for (i in 0..2) {
                        listArray[i].adapter = personalBestAdapters[i]
                    }
                }


            } else {
                switchBtn.text = globalBestText
                getScoreboardData(true, menuActivity)
            }

        }
    } // end onActivityCreated


    private fun getScoreboardData(globalScores: Boolean, activity: Activity) {
        var queryString = ""

        if (!globalScores) {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            queryString = preferences.getString("player_name", "")!!
        }

        Leaderboard.service.topTenLeaders(queryString).enqueue(object : Callback<List<List<ScoreEntry>>> {
            override fun onFailure(call: Call<List<List<ScoreEntry>>>, t: Throwable) {
                Log.d(Configuration.DEBUG_TAG, "Leaderboard request failed.")
                t.printStackTrace()
            }

            override fun onResponse(call: Call<List<List<ScoreEntry>>>, response: Response<List<List<ScoreEntry>>>) {
                Log.d(Configuration.DEBUG_TAG, "Successful response")
                var listIndex = 0
                response.body()?.forEach {
                    val indexedDataArray = ArrayList<HighScore>()
                    for (i in 0..(it.size - 1)) {
                        indexedDataArray.add(HighScore(i + 1, it[i].username, it[i].score))
                    }
                    val adapter = ArrayAdapter(activity as Context, R.layout.scorelist_item, indexedDataArray)
                    listArray[listIndex].adapter = adapter

                    // Do not save global scores since they can change between each refresh
                    if (!globalScores)
                        personalBestAdapters.add(adapter)
                    listIndex++
                }
            }
        })
    }

} // end class