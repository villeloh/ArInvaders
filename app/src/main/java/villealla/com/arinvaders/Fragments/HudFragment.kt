package villealla.com.arinvaders.Fragments


import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import kotlinx.android.synthetic.main.fragment_hud.*
import villealla.com.arinvaders.R

class HudFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_hud, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        hud.setImageBitmap(BitmapFactory.decodeStream(resources.openRawResource(R.raw.hud_with_aim)))
        hud.scaleType = ImageView.ScaleType.FIT_XY
        hud.adjustViewBounds = true
    }


}
