package pelikan.bp.pelikanj

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import pelikan.bp.pelikanj.databinding.ActivityMainBinding
import pelikan.bp.pelikanj.viewModels.InstitutionsModelItem
import pelikan.bp.pelikanj.viewModels.TokenData
import pelikan.bp.pelikanj.viewModels.TokenModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    val institutionsList = ArrayList<InstitutionsModelItem>()
    private lateinit var dbClient: DBClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Hides top bar title
        //supportActionBar?.hide()

        dbClient = DBClient(applicationContext)

        val userData = dbClient.getAllUserData()

        if (userData == null){
            dbClient.insertDefaultData("cs",null,null)
        }

        checkPermission(Manifest.permission.CAMERA,42)

        getInstitutions()

        checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,43)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        if (userData?.token != null){

            // Get token without "BEARER "
            val onlyToken = userData.token!!.substring(7, userData.token!!.length)
            // Get only body part - first part is header, second body and third is signing
            val tokens = onlyToken.split(".")[1]
            // Get byte array of decoded
            val jwt = Base64.decode(tokens, Base64.DEFAULT)
            // Get byte array to string
            val info = String(jwt, Charsets.UTF_8)
            // Parse it to TokenData class
            val authors = Gson().fromJson(info, TokenData::class.java)

            // Not expired
            if (authors.exp >= (System.currentTimeMillis() / 1000).toInt()){
                // refresh token
                updateToken(onlyToken)
            }

        }

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_scanner, R.id.navigation_profile, R.id.navigation_more, R.id.navigation_institutions,
                R.id.navigation_pickExhibit
        ))
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    private fun updateToken(token: String) {

        val client: Call<TokenModel> = ApiClient.create().updateToken(token)

        client.enqueue(object : Callback<TokenModel> {
            override fun onResponse(
                call: Call<TokenModel>,
                response: Response<TokenModel>
            ) {
                if (response.code() == 200){
                    // OK
                    val newToken = response.body()!!.token
                    dbClient.updateToken(newToken)
                }
            }

            override fun onFailure(call: Call<TokenModel>, t: Throwable) {
                Log.println(Log.ERROR,"error",t.toString())
            }
        })

    }

    private fun getInstitutions(): ArrayList<InstitutionsModelItem> {
        val client: Call<List<InstitutionsModelItem>> = ApiClient.create().getInstitutions()

        client.enqueue(object : Callback<List<InstitutionsModelItem>> {
            override fun onResponse(
                call: Call<List<InstitutionsModelItem>?>,
                response: Response<List<InstitutionsModelItem>?>
            ) {
                val respBody = response.body()!!
                for (respo: InstitutionsModelItem in respBody) {
                    institutionsList.add(respo)
                }
                dbClient.insertAllInstitutions(institutionsList)
            }

            override fun onFailure(call: Call<List<InstitutionsModelItem>>, t: Throwable) {
                Log.println(Log.ERROR,"error","Error when getting institutions!")
            }
        })

        return institutionsList
    }

    private fun checkPermission(permission: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(this@MainActivity, permission) == PackageManager.PERMISSION_DENIED) {
            // Requesting the permission
            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(permission), requestCode)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 42) {
            checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,43)
        } else {
            checkPermission(Manifest.permission.CAMERA,42)
        }
    }
}