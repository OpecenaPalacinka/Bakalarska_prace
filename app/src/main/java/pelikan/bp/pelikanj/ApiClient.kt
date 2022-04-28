package pelikan.bp.pelikanj

import okhttp3.ResponseBody
import pelikan.bp.pelikanj.viewModels.*
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface ApiClient {

    @GET("/institutions")
    fun getInstitutions(): Call<List<InstitutionsModelItem>>

    @GET("/institutions_images/{imageName}")
    fun getImage(@Path("imageName") imageName: String): Call<ResponseBody>

    @GET("/exhibits/all/{institutionId}")
    fun getAllExhibitsOfInstitution(@Path("institutionId") id: Int): Call<ExhibitModel>

    @POST("/exhibits/{institutionId}")
    fun uploadNewExhibitWithExhibitImage(@Path("institutionId") id: Int,
                         @Body exhibit: ExhibitItemWithExhibitImage): Call<ResponseBody>

    @POST("/exhibits/{institutionId}")
    fun uploadNewExhibit(@Path("institutionId") id: Int,
                         @Body exhibit: ExhibitItemWithoutExhibitImage): Call<ResponseBody>

    @POST("/users/register")
    fun registerUser(@Body user: User): Call<ResponseBody>

    @POST("/users/login")
    fun loginUser(@Body user: UserLogin): Call<TokenModel>

    @PUT("/users/updatePassword")
    fun updatePassword(@Header("Authorization") token: String, @Body password: PasswordModel): Call<ResponseBody>

    @GET("/users/token")
    fun updateToken(@Header("Authorization") token: String): Call<TokenModel>

    @GET("/translations/official/{exhibitId}/{languageCode}")
    fun getTranslation(@Path("exhibitId") exhibitId: Int,
                       @Path("languageCode") languageCode: String): Call<ResponseBody>

    @GET("/location/buildings/all/{institutionId}")
    fun getBuildings(@Path("institutionId") institutionId: Int): Call<List<Building>>

    @GET("/location/rooms/all/{buildingId}")
    fun getRooms(@Path("buildingId") buildingId: Int): Call<List<Room>>

    @GET("/location/showcases/all/{roomId}")
    fun getShowcases(@Path("roomId") roomId: Int): Call<List<Showcase>>

    companion object {

        private const val BASE_URL = "http://147.228.67.66/"
        private const val PHONE_URL_TEST = "http://192.168.0.15:8080/" // my PC IP address
        private const val LOCALHOST_URL = "http://10.0.2.2:8080/"

        fun create() : ApiClient {

            val retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BASE_URL)
                .build()
            return retrofit.create(ApiClient::class.java)

        }
    }
}