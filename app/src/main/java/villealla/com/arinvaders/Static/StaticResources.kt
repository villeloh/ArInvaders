package villealla.com.arinvaders.Static

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.ar.sceneform.collision.Box
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.Color
import com.google.ar.sceneform.rendering.Light
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Texture
import villealla.com.arinvaders.R
import villealla.com.arinvaders.WorldEntities.Fire
import villealla.com.arinvaders.WorldEntities.Gun
import villealla.com.arinvaders.WorldEntities.LaserBolt
import villealla.com.arinvaders.WorldEntities.Ship

/*
* Static 'class' for loading all the needed resources at game start
* (to avoid lag during the actual game).
* @author Ville Lohkovuori
* */

object StaticResources {

    // referred to from OwnShipWeapon
    lateinit var redLaserLight: Light

    // referred to from the Ship class
    lateinit var explosionRenderable: ModelRenderable
    lateinit var explosionTexture: Texture

    // we call this in MainActivity to make available all the asynchronous
    // resources (mostly renderables) that are needed by the world entity classes
    fun loadAll(context: Context) {

        loadGunResources(context)
        loadLaserResources(context)
        loadExplosionGraphics(context)
        loadShipRenderables(context)
        loadFireModel(context)
    } // end loadResources

    private fun loadLaserResources(context: Context) {

        redLaserLight = Light.builder(Light.Type.POINT)
                .setIntensity(4000f)
                .setFalloffRadius(200f)
                .setShadowCastingEnabled(false)
                .setColorTemperature(10000f)
                .setColor(Color(1f, 0f, 0f))
                .build()

        ModelRenderable.builder()
                .setSource(context, Uri.parse("laser_2.sfb"))
                .build()
                .thenAccept { it ->
                    LaserBolt.redRenderable = it
                    LaserBolt.redRenderable.isShadowCaster = false
                    LaserBolt.redRenderable.isShadowReceiver = false
                }
        ModelRenderable.builder()
                .setSource(context, Uri.parse("laser_yellow.sfb"))
                .build()
                .thenAccept { it ->
                    LaserBolt.yellowRenderable = it
                    LaserBolt.yellowRenderable.isShadowCaster = false
                    LaserBolt.yellowRenderable.isShadowReceiver = false
                }
    } // end loadLaserResources

    private fun loadGunResources(context: Context) {

        ModelRenderable.builder()
                .setSource(context, Uri.parse("Gun.sfb"))
                .build().thenAccept {
                    Gun.gunRenderable = it
                    Gun.gunRenderable.isShadowReceiver = false
                    Gun.gunRenderable.isShadowCaster = false
                    Gun.gunRenderable.collisionShape = Box(Vector3(0.001f, 0.001f, 0.001f))
                }
    } // end loadGunResources

    private fun loadExplosionGraphics(context: Context) {

        val bitMap = BitmapFactory.decodeResource(context.resources, R.drawable.smoke_tx)
        Texture.builder().setSource(bitMap).build().thenAccept { it ->
            explosionTexture = it

            val renderable = ModelRenderable.builder()
                    .setSource(context, Uri.parse("model.sfb"))
                    .build()
            renderable.thenAccept { it2 ->

                it2.material.setTexture("", explosionTexture)
                explosionRenderable = it2
            }
        }
    } // end loadExplosionGraphics

    private fun loadShipRenderables(context: Context) {

        // Load all models that are present in ShipType enum
        ShipType.values().forEach { shipType ->
            ModelRenderable.builder()
                    .setSource(context, Uri.parse(shipType.modelName))
                    .build()
                    .thenAccept { it -> Ship.renderables[shipType] = it }
        }
    } // end loadShipRenderables

    private fun loadFireModel(context: Context) {
        ModelRenderable.builder()
                .setSource(context, Uri.parse("fire.sfb"))
                .build()
                .thenAccept { it -> Fire.model = it }
    }

} // end StaticResources