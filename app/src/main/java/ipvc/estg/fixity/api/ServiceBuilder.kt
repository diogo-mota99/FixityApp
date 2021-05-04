package ipvc.estg.fixity.api

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object ServiceBuilder {

    private val client = OkHttpClient.Builder().build();
    val gson = GsonBuilder().serializeNulls().create()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://fixity.pt/myslim/")
        .addConverterFactory(GsonConverterFactory.create(gson))
        .client(client)
        .build()

    fun <T> buildService(service: Class<T>): T {
        return retrofit.create(service)
    }
}