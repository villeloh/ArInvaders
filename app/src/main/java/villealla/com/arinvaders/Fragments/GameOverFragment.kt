package villealla.com.arinvaders.Fragments

import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import kotlinx.android.synthetic.main.fragment_game_over.*
import villealla.com.arinvaders.R

/*
* A modal-like fragment for showing the game over text and image
* over the main game screen when the Earth gets to zero hit points.
* @author Ville Lohkovuori
* */

class GameOverFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_game_over, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        gameOverImageView.setImageBitmap(BitmapFactory.decodeStream(resources.openRawResource(R.raw.game_over_image)))
        gameOverImageView.animate()
                .scaleY(250f)
                .scaleX(250f)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .duration = 1000

        val invaderImageView = ImageView(activity)
        invaderImageView.setImageBitmap(BitmapFactory.decodeStream(resources.openRawResource(R.raw.invader_image)))
        val layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.MATCH_PARENT)
        layoutParams.leftToLeft = R.id.gameoverLayout // centers that STUPID thing within the ConstraintLayout...
        layoutParams.rightToRight = R.id.gameoverLayout
        layoutParams.height = 100
        layoutParams.width = 200
        invaderImageView.layoutParams = layoutParams
        gameoverLayout.addView(invaderImageView)

        invaderImageView.animate()
                .scaleY(3.5f)
                .scaleX(3.5f)
                .translationY(280f)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .duration = 1000
    } // end onActivityCreated

} // end class