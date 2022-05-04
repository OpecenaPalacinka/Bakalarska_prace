package pelikan.bp.pelikanj

import okhttp3.ResponseBody
import pelikan.bp.pelikanj.viewModels.*
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

/**
 * Interface for all API calls and functions
 *
 * @constructor Create Api client
 */
interface ApiClient {

    /**
     * Get institutions from server
     *
     * @return List of institutions
     */
    @GET("/institutions")
    fun getInstitutions(): Call<List<InstitutionsModelItem>>

    /**
     * Get image of institution
     *
     * @param imageName string name of image
     * @return ResponseBody.body contains bytestream of image
     */
    @GET("/institutions_images/{imageName}")
    fun getImage(@Path("imageName") imageName: String): Call<ResponseBody>

    /**
     * Get all exhibits of institution
     *
     * @param id id of institution
     * @return Data class model (list of exhibits)
     */
    @GET("/exhibits/all/{institutionId}")
    fun getAllExhibitsOfInstitution(@Path("institutionId") id: Int): Call<ExhibitModel>

    /**
     * Upload new exhibit with exhibit image
     *
     * @param id id of institution
     * @param exhibit Data class that is represented as JSON in body
     * @return ResponseBody, important is response code
     */
    @POST("/exhibits/{institutionId}")
    fun uploadNewExhibitWithExhibitImage(@Path("institutionId") id: Int,
                         @Body exhibit: ExhibitItemWithExhibitImage): Call<ResponseBody>

    /**
     * Upload new exhibit without exhibit image
     *
     * @param id id of institution
     * @param exhibit Data class that is represented as JSON in body
     * @return ResponseBody, important is response code
     */
    @POST("/exhibits/{institutionId}")
    fun uploadNewExhibit(@Path("institutionId") id: Int,
                         @Body exhibit: ExhibitItemWithoutExhibitImage): Call<ResponseBody>

    /**
     * Register user
     *
     * @param user user credentials
     * @return ResponseBody, important is response code
     */
    @POST("/users/register")
    fun registerUser(@Body user: User): Call<ResponseBody>

    /**
     * Login user
     *
     * @param user username + password
     * @return Token with information about user
     */
    @POST("/users/login")
    fun loginUser(@Body user: UserLogin): Call<TokenModel>

    /**
     * Update user password
     * @PUT!!
     *
     * @param token User token, needed for authorization
     * @param password New password
     * @return ResponseBody, important is response code
     */
    @PUT("/users/updatePassword")
    fun updatePassword(@Header("Authorization") token: String,
                       @Body password: PasswordModel): Call<ResponseBody>

    /**
     * Update user token, prolong the expiration date
     *
     * @param token User token, needed for authorization
     * @return New token
     */
    @GET("/users/token")
    fun updateToken(@Header("Authorization") token: String): Call<TokenModel>

    /**
     * Get translation
     *
     * @param exhibitId Exhibit id
     * @param languageCode two char language code
     * @return Translation model with translated text inside
     */
    @GET("/translations/official/{exhibitId}/{languageCode}")
    fun getTranslation(@Path("exhibitId") exhibitId: Int,
                       @Path("languageCode") languageCode: String): Call<Translation>

    /**
     * Get buildings of institution
     *
     * @param institutionId institution id
     * @return List of buildings
     */
    @GET("/location/buildings/all/{institutionId}")
    fun getBuildings(@Path("institutionId") institutionId: Int): Call<List<Building>>

    /**
     * Get rooms of building
     *
     * @param buildingId building id
     * @return List of rooms
     */
    @GET("/location/rooms/all/{buildingId}")
    fun getRooms(@Path("buildingId") buildingId: Int): Call<List<Room>>

    /**
     * Get showcases of room
     *
     * @param roomId room id
     * @return List of showcases
     */
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