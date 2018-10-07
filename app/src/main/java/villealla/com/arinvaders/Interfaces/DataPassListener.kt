package villealla.com.arinvaders.Interfaces

import android.support.v4.app.Fragment

/*
* We use this in MainActivity to pass data between Fragments.
* there may be a better way, but this works.
* @author Ville Lohkovuori
*/

interface DataPassListener {

    fun passData(data: String, createMenu: Boolean)
    fun addFragment(fragment: Fragment)
}