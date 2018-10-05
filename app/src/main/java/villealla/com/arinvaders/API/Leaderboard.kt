package villealla.com.arinvaders.API

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

object Leaderboard {

    const val URL = "http://apolets.com"

    interface Service {
        @GET("/leaderboard.php")
        fun topTenLeaders(): Call<List<ScoreEntry>>

        @POST("/leaderboard.php")
        fun postScore(@Body scoreEntry: ScoreEntry): Call<List<ScoreEntry>>
    }

    private val retrofit = Retrofit.Builder()
            .baseUrl(URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    val service = retrofit.create(Service::class.java)

}