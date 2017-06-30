package co.esclub.searchnshop.net

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.simplexml.SimpleXmlConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Created by tae.kim on 30/06/2017.
 */
interface PlayStoreService {
    @GET("details")
    fun getShopItems(@Query("id") id: String): Call<PlayStore>


    companion object {
        val retrofit = Retrofit.Builder()
                .baseUrl("https://play.google.com/store/apps/")
                .addConverterFactory(SimpleXmlConverterFactory.create())
                .build()
    }
}