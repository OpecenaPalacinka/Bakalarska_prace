package pelikan.bp.pelikanj.ui.findExhibit

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputLayout
import pelikan.bp.pelikanj.ApiClient
import pelikan.bp.pelikanj.R
import pelikan.bp.pelikanj.viewModels.InstitutionsModelItem
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class PickExhibitFragment : Fragment() {

    private lateinit var searchInstitutions: TextInputLayout
    private lateinit var autoCompleteInstitutions: AutoCompleteTextView
    private lateinit var searchBuildings: TextInputLayout
    private lateinit var autoCompleteBuildings: AutoCompleteTextView
    private lateinit var searchRooms: TextInputLayout
    private lateinit var autoCompleteRooms: AutoCompleteTextView
    private lateinit var searchShowCases: TextInputLayout
    private lateinit var autoCompleteShowCases: AutoCompleteTextView
    private lateinit var searchExhibits: TextInputLayout
    private lateinit var autoCompleteExhibits: AutoCompleteTextView

    private lateinit var searchTranslationButton: Button

    private var institutions: ArrayList<InstitutionsModelItem> = ArrayList()
    private lateinit var institutionsName: ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view: View = inflater.inflate(R.layout.fragment_pick_exhibit, container, false)

        initForm(view)

        fillAutocompleteInstitutions(view)

        autoCompleteInstitutions.setOnItemClickListener { parent, _, position, _ ->
            val item = parent.getItemAtPosition(position)
            Toast.makeText(requireContext(), item.toString(), Toast.LENGTH_LONG).show()
            val idOfInstitution = getInstitutionIdByName(item.toString())
            getAllExhibitsByInstitutionId(idOfInstitution)
        }

        return view
    }

    private fun getAllExhibitsByInstitutionId(idOfInstitution: Int) {
        val client: Call<String> = ApiClient.create().getAllExhibitsOfInstitution(idOfInstitution)

        /*
        client.enqueue(object : Callback<List<InstitutionsModelItem>> {
            override fun onResponse(
                call: Call<List<InstitutionsModelItem>?>,
                response: Response<List<InstitutionsModelItem>?>
            ) {
                val respBody = response.body()!!
                for (respo: InstitutionsModelItem in respBody) {
                    institutions.add(respo)
                    institutionsName.add(respo.name)
                }
            }

            override fun onFailure(call: Call<List<InstitutionsModelItem>>, t: Throwable?) {
                Log.println(Log.ERROR,tag,t.toString())
            }
        })

         */
    }

    private fun getInstitutionIdByName(nameOfInstitution: String): Int {
        for (institution: InstitutionsModelItem in institutions){
            if (institution.name == nameOfInstitution){
                return institution.institutionId
            }
        }
        return -1
    }


    private fun fillAutocompleteInstitutions(view: View) {
        institutionsName = ArrayList()

        val client: Call<List<InstitutionsModelItem>> = ApiClient.create().getInstitutions()

        client.enqueue(object : Callback<List<InstitutionsModelItem>> {
            override fun onResponse(
                call: Call<List<InstitutionsModelItem>?>,
                response: Response<List<InstitutionsModelItem>?>
            ) {
                val respBody = response.body()!!
                for (respo: InstitutionsModelItem in respBody) {
                    institutions.add(respo)
                    institutionsName.add(respo.name)
                }
            }

            override fun onFailure(call: Call<List<InstitutionsModelItem>>, t: Throwable) {
                Log.println(Log.ERROR,tag,t.toString())
            }
        })

        val adapter: ArrayAdapter<String> =
            ArrayAdapter(view.context, android.R.layout.simple_spinner_dropdown_item, institutionsName)

        autoCompleteInstitutions.setAdapter(adapter)
    }


    private fun validateInstitution(): Boolean {
        val exhibitInstitutionString: String = searchInstitutions.editText?.text.toString()

        if (exhibitInstitutionString.isEmpty()) {
            searchInstitutions.error =
                resources.getString(R.string.requiredNameOfInstitutionFromList)
            return false
        } else {
            for (suggestion in institutionsName) {
                if (searchInstitutions.editText?.text.toString() == suggestion) {
                    searchInstitutions.error = null
                    searchInstitutions.isErrorEnabled = false
                    return true
                }
            }
            searchInstitutions.error =
                resources.getString(R.string.requiredNameOfInstitutionFromList)
            return false
        }
    }


    private fun initForm(view: View) {
        searchInstitutions = view.findViewById(R.id.search_institutions)
        autoCompleteInstitutions = view.findViewById(R.id.autoComplete_institutions)
        searchBuildings = view.findViewById(R.id.search_buildings)
        autoCompleteBuildings = view.findViewById(R.id.autoComplete_buildings)
        searchRooms = view.findViewById(R.id.search_rooms)
        autoCompleteRooms = view.findViewById(R.id.autoComplete_rooms)
        searchShowCases = view.findViewById(R.id.search_showCases)
        autoCompleteShowCases = view.findViewById(R.id.autoComplete_showCases)
        searchExhibits = view.findViewById(R.id.search_exhibits)
        autoCompleteExhibits = view.findViewById(R.id.autoComplete_exhibits)
        searchTranslationButton = view.findViewById(R.id.search_translation)

    }

}