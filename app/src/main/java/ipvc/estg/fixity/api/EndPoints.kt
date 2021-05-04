package ipvc.estg.fixity.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface EndPoints {

    //GET USERS BY USERNAME
    @GET("fixity/users/byusername/{username}")
    fun getUsersByUsername(@Path("username") username: String): Call<User>

    //GET USERS BY EMAIL
    @GET("fixity/users/byemail/{email}")
    fun getUsersByEmail(@Path("email") email: String): Call<User>

    //REGISTER USER
    @FormUrlEncoded
    @POST("fixity/users")
    fun postUser(
        @Field("name") name: String?,
        @Field("username") username: String?,
        @Field("email") email: String?,
        @Field("password") password: String?
    ): Call<OutputPost>

    //LOGIN
    @FormUrlEncoded
    @POST("fixity/users/login")
    fun postUserLogin(
        @Field("username") username: String?,
        @Field("password") password: String?
    ): Call<OutputPost>

    //GET COORDINATES (ALL REPORTS INFORMATION)
    @GET("fixity/reports")
    fun getCoordinates(): Call<List<Report>>

    //REPORT PROBLEM
    @Multipart
    @POST("fixity/report")
    fun postReportProblem(
        @Part("problem") problem: RequestBody,
        @Part("latitude") latitude: RequestBody,
        @Part("longitude") longitude: RequestBody,
        @Part image: MultipartBody.Part?,
        @Part("problemType") problemType: RequestBody,
        @Part("userID") userID: RequestBody

    ): Call<OutputPost>

    //GET PROBLEM BY ID
    @GET("fixity/reports/{id}")
    fun getProblemById(
        @Path("id") id: String?
    ): Call<Report>

    //DELETE PROBLEM BY ID
    @DELETE("fixity/report/delete/{id}")
    fun deleteProblemById(@Path("id") id: String?): Call<OutputPost>

    //UPDATE PROBLEM
    @Multipart
    @POST("fixity/report/update/{id}")
    fun postEditProblem(
        @Path("id") id: String?,
        @Part("problem") problem: RequestBody,
        @Part image: MultipartBody.Part?,
        @Part("problemType") problemType: RequestBody,
    ): Call<OutputPost>
}