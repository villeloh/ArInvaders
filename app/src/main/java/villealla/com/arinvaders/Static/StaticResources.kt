package villealla.com.arinvaders.Static

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.google.ar.sceneform.collision.Box
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.Color
import com.google.ar.sceneform.rendering.Light
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Texture
import villealla.com.arinvaders.R
import villealla.com.arinvaders.WorldEntities.*

/*
* Static 'class' for loading all the needed resources at game start
* (to avoid lag during the actual game).
* @author Ville Lohkovuori
* */

object StaticResources {

    // referred to from Gun
    lateinit var redLaserLight: Light

    // referred to from the Ship class
    lateinit var explosionRenderable: ModelRenderable
    lateinit var explosionTexture: Texture

    const val TOTAL_NUMBER_OF_RENDERABLES = 10

    var loadedRenderablesCount = 0

    // we call this in MainActivity to make available all the asynchronous
    // resources (mostly renderables) that are needed by the world entity classes
    fun loadAll(context: Context) {

        if (loadedRenderablesCount != TOTAL_NUMBER_OF_RENDERABLES) {
            Log.d(Configuration.DEBUG_TAG, "loading models...")
            loadGunResources(context)
            loadLaserResources(context)
            loadExplosionGraphics(context)
            loadShipRenderables(context)
            loadFireModels(context)
            loadEarthRenderable(context)
        }
    } // end loadResources

    // needs to be its own function because of the delay in attaching the renderable
    fun loadEarthRenderable(context: Context) {

        ModelRenderable.builder()
                .setSource(context, Uri.parse("earth_ball.sfb"))
                .build()
                .thenAccept { it ->
                    Planet.instance.earthRenderable = it
                    loadedRenderablesCount++
                }
    } // end loadEarthRenderable

    private fun loadLaserResources(context: Context) {

        redLaserLight = Light.builder(Light.Type.POINT)
                .setIntensity(120000f)
                .setFalloffRadius(0.1f)
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
                    loadedRenderablesCount++
                }
        ModelRenderable.builder()
                .setSource(context, Uri.parse("laser_yellow.sfb"))
                .build()
                .thenAccept { it ->
                    LaserBolt.yellowRenderable = it
                    LaserBolt.yellowRenderable.isShadowCaster = false
                    LaserBolt.yellowRenderable.isShadowReceiver = false
                    loadedRenderablesCount++
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
                    loadedRenderablesCount++
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
                loadedRenderablesCount++
            }
        }
    } // end loadExplosionGraphics

    private fun loadShipRenderables(context: Context) {

        // Load all models that are present in ShipType enum
        ShipType.values().forEach { shipType ->
            ModelRenderable.builder()
                    .setSource(context, Uri.parse(shipType.modelName))
                    .build()
                    .thenAccept { it ->
                        Ship.renderables[shipType] = it
                        loadedRenderablesCount++
                    }
        }
    } // end loadShipRenderables

    private fun loadFireModels(context: Context) {
        ModelRenderable.builder()
                .setSource(context, Uri.parse("fire.sfb"))
                .build()
                .thenAccept { it ->
                    Fire.model = it
                    loadedRenderablesCount++
                }
        ModelRenderable.builder()
                .setSource(context, Uri.parse("fire2.sfb"))
                .build()
                .thenAccept { it ->
                    Gun.fireRenderable = it
                    loadedRenderablesCount++
                }
    }

} // end StaticResources