package co.esclub.searchnshop.net

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

/**
 * Created by tae.kim on 15/06/2017.
 */

interface NaverSearchService {
    @Headers("X-Naver-Client-Id: x5I5QXUNmtF3UEM7Bhek", "X-Naver-Client-Secret: Xb5boXN0s8")
    @GET("search/shop.json")
    fun getShopItems(@Query("query") query: String,
                     @Query("start") start: Int,
                     @Query("display") display: Int): Call<NaverSearchResult>

    @Headers("X-Naver-Client-Id: x5I5QXUNmtF3UEM7Bhek", "X-Naver-Client-Secret: Xb5boXN0s8")
    @GET("search/shop.json")
    fun getShopItemsForFireBase(@Query("query") query: String,
                                @Query("start") start: Int,
                                @Query("display") display: Int): Call<NaverSearchResult2>


    companion object {
        val retrofit = Retrofit.Builder()
                .baseUrl("https://openapi.naver.com/v1/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
    }


}
