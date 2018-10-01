package villealla.com.arinvaders.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.ar.sceneform.ux.ArFragment

/*
* Custom ArFragment class (needed in order to be able to disable plane detection).
* @author Ville Lohkovuori
* */

class CustomArFragment : ArFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        return view
    }

    // disable the plane detection once a plane has been chosen
    fun disablePlaneDetection() {

        planeDiscoveryController.hide()
        planeDiscoveryController.setInstructionView(null)
        arSceneView.planeRenderer.isEnabled = false
    }

} // end class