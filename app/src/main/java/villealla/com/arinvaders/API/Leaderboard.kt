package villealla.com.arinvaders.API

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

object Leaderboard {

    const val URL = "http://apolets.com"

    interface Service {
        @GET("/leaderboard.php")
        fun topTenLeaders(@Query("username") username: String = ""): Call<List<List<ScoreEntry>>>

        @FormUrlEncoded
        @POST("/leaderboard.php")
        fun postScore(@Field("username") username: String, @Field("difficulty") difficulty: String, @Field("score") score: Int): Call<List<List<ScoreEntry>>>
    }

    private val retrofit = Retrofit.Builder()
            .baseUrl(URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    val service = retrofit.create(Service::class.java)

}