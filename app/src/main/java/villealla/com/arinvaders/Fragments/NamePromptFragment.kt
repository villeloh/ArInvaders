package villealla.com.arinvaders.Fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import kotlinx.android.synthetic.main.fragment_name_prompt.*
import villealla.com.arinvaders.Interfaces.DataPassListener
import villealla.com.arinvaders.R

/*
* If the app has just been installed, we'll show this fragment
* to ask for the plaeyr's name.
* @author Ville Lohkovuori
* */

class NamePromptFragment : Fragment() {

    lateinit var menuActivity: DataPassListener

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_name_prompt, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        namePromptEditText.requestFocus()
        namePromptEditText.setOnEditorActionListener {
            editText: View, actionId: Int, _ ->

            if (actionId == EditorInfo.IME_ACTION_DONE) {

                menuActivity = activity as DataPassListener
                val playerName = namePromptEditText.text.toString()
                menuActivity.passData(playerName, true)
            }
            true
        }
    } // end onActivityCreated

} // end class