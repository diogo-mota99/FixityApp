package ipvc.estg.fixity.api

import retrofit2.Call
import retrofit2.http.*

interface EndPoints {

    //GET USERS BY USERNAME
    @GET("fixity/users/byusername/{username}")
    fun getUsersByUsername(@Path("username") username: String): Call<User>

    //GET USERS BY EMAIL
    @GET("fixity/users/byemail/{email}")
    fun getUsersByEmail(@Path("email") email: String): Call<User>

    @FormUrlEncoded
    @POST("fixity/users")
    fun postUser(
        @Field("name") name: String?,
        @Field("username") username: String?,
        @Field("email") email: String?,
        @Field("password") password: String?
    ): Call<OutputPost>
}