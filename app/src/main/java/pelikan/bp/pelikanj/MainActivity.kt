package pelikan.bp.pelikanj

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import pelikan.bp.pelikanj.databinding.ActivityMainBinding
import pelikan.bp.pelikanj.viewModels.InstitutionsModelItem
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

        if (dbClient.getAllUserData() == null){
            dbClient.insertDefaultData("cs",null,null)
        }

        getInstitutions()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

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
}