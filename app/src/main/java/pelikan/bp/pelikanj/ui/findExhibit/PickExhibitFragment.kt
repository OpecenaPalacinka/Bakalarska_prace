package pelikan.bp.pelikanj.ui.findExhibit

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputLayout
import pelikan.bp.pelikanj.ApiClient
import pelikan.bp.pelikanj.DBClient
import pelikan.bp.pelikanj.R
import pelikan.bp.pelikanj.viewModels.*
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

    private var exhibits: ArrayList<ExhibitModelItem> = ArrayList()
    private var institutions: ArrayList<InstitutionsModelItem> = ArrayList()
    private var buildings: ArrayList<Building> = ArrayList()
    private var rooms: ArrayList<Room> = ArrayList()
    private var showcases: ArrayList<Showcase> = ArrayList()
    private lateinit var institutionsName: ArrayList<String>

    private lateinit var dbClient: DBClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view: View = inflater.inflate(R.layout.fragment_pick_exhibit, container, false)

        dbClient = DBClient(requireContext())

        initForm(view)

        fillAutocompleteInstitutions(view)

        setUpListeners(view)

        return view
    }

    private fun setUpListeners(view: View){
        var idOfBuilding: Int = 0
        var idOfRoom = 0
        var idOfShowcase: Int = 0

        autoCompleteInstitutions.setOnItemClickListener { parent, _, position, _ ->
            autoCompleteBuildings.editableText.clear()
            autoCompleteRooms.editableText.clear()
            autoCompleteShowCases.editableText.clear()
            autoCompleteExhibits.editableText.clear()
            val item = parent.getItemAtPosition(position)
            val idOfInstitution = getInstitutionIdByName(item.toString())
            if (idOfInstitution != -1){
                getAllExhibitsByInstitutionId(idOfInstitution, view)
            } else {
                Log.d(tag,"Cannot find institution")
            }
        }

        autoCompleteInstitutions.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                validateInstitution()
            }
        }

        autoCompleteBuildings.setOnItemClickListener { parent, _, position, _ ->
            autoCompleteRooms.editableText.clear()
            autoCompleteShowCases.editableText.clear()
            autoCompleteExhibits.editableText.clear()
            val item = parent.getItemAtPosition(position)
            idOfBuilding = getBuildingIdByName(item.toString())
            if (idOfBuilding != -1){
                fillAutocompleteRooms(view,idOfBuilding)
            } else {
                Log.d(tag,"Cannot find buildings")
            }
        }

        autoCompleteBuildings.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                validateBuildings()
            }
        }

        autoCompleteRooms.setOnItemClickListener { parent, _, position, _ ->
            autoCompleteShowCases.editableText.clear()
            autoCompleteExhibits.editableText.clear()
            val item = parent.getItemAtPosition(position)
            idOfRoom = getRoomIdByName(item.toString(),idOfBuilding)
            if (idOfRoom != -1){
                fillAutocompleteShowcases(view,idOfRoom)
            } else {
                Log.d(tag,"Cannot find rooms")
            }
        }

        autoCompleteRooms.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                validateRooms()
            }
        }

        autoCompleteShowCases.setOnItemClickListener { parent, _, position, _ ->
            autoCompleteExhibits.editableText.clear()
            val item = parent.getItemAtPosition(position)
            idOfShowcase = getShowcaseIdByName(item.toString(),idOfRoom)
            if (idOfShowcase != -1){
                fillAutocompleteExhibits(view, idOfShowcase)
            } else {
                Log.d(tag,"Cannot find showcase")
            }
        }

        autoCompleteShowCases.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                validateShowcases()
            }
        }

        searchTranslationButton.setOnClickListener {
            if (checkValues()){
                val idOfExhibit = getExhibitIdByName(autoCompleteExhibits.editableText.toString(),idOfShowcase,idOfRoom,idOfBuilding)
                // funkce pro vyhledání exponátu
                getTranslation(idOfExhibit)
            }
        }
    }

    private fun checkValues():Boolean {
        if (!validateInstitution() or !validateBuildings()
            or !validateRooms() or !validateShowcases()
            or !validateExhibits()) {
            return false
        }
        return true
    }

    private fun getTranslation(idOfExhibit: Int) {
        val lCode = dbClient.getAllUserData()?.language!!

        val client: Call<Translation> = ApiClient.create().getTranslation(idOfExhibit,lCode)

        client.enqueue(object : Callback<Translation> {
            override fun onResponse(
                call: Call<Translation>,
                response: Response<Translation>
            ) {
                if (response.code() == 200){
                    // Show translation
                    Toast.makeText(requireContext(),response.body()?.translatedText,Toast.LENGTH_LONG).show()
                } else {
                    // Try english
                }

            }

            override fun onFailure(call: Call<Translation>, t: Throwable) {
                Log.println(Log.ERROR,tag,t.toString())
            }
        })
    }

    private fun getExhibitIdByName(exhibitName: String, showcaseId: Int, roomId: Int, buildingId: Int): Int {
        for (exhibit: ExhibitModelItem in exhibits){
            if (exhibit.name == exhibitName && showcaseId == exhibit.showcase.showcaseId
                && roomId == exhibit.room.roomId && buildingId == exhibit.building.buildingId){
                return exhibit.exhibitId
            }
        }
        return -1
    }

    private fun getShowcaseIdByName(showcaseName: String, roomId: Int): Int {
        for (showcase: Showcase in showcases){
            if (showcase.name == showcaseName && roomId == showcase.roomId){
                return showcase.showcaseId
            }
        }
        return -1
    }

    private fun getRoomIdByName(roomName: String, buildingId: Int): Int {
        for (room: Room in rooms){
            if (room.name == roomName && room.buildingId == buildingId){
                return room.roomId
            }
        }
        return -1
    }

    private fun getBuildingIdByName(buildingName: String): Int {
        for (building: Building in buildings){
            if (building.name == buildingName){
                return building.buildingId
            }
        }
        return -1
    }

    private fun getInstitutionIdByName(nameOfInstitution: String): Int {
        for (institution: InstitutionsModelItem in institutions){
            if (institution.name == nameOfInstitution){
                return institution.institutionId
            }
        }
        return -1
    }

    private fun getAllExhibitsByInstitutionId(idOfInstitution: Int, view: View) {
        val client: Call<ExhibitModel> = ApiClient.create().getAllExhibitsOfInstitution(idOfInstitution)

        client.enqueue(object : Callback<ExhibitModel> {
            override fun onResponse(
                call: Call<ExhibitModel>,
                response: Response<ExhibitModel>
            ) {
                val respBody = response.body()!!
                for (respo: ExhibitModelItem in respBody) {
                    // Save to local db

                    exhibits.add(respo)
                    addBuilding(respo.building)
                    addRoom(respo.room)
                    addShowcase(respo.showcase)
                }

                dbClient.insertAllBuildings(buildings)
                dbClient.insertAllRooms(rooms)
                dbClient.insertAllShowcases(showcases)

                fillAutocompleteBuildings(view)
            }

            override fun onFailure(call: Call<ExhibitModel>, t: Throwable) {
                Log.println(Log.ERROR,tag,t.toString())
            }
        })

    }

    private fun addBuilding(building: Building) {
        var bool = false

        for (build: Building in buildings){
            if (build.buildingId == building.buildingId){
                bool = true
            }
        }
        if (!bool){
            buildings.add(building)
        }
    }

    private fun addRoom(room: Room) {
        var bool = false

        for (r: Room in rooms){
            if (r.roomId == room.roomId){
                bool = true
            }
        }
        if (!bool){
            rooms.add(room)
        }
    }

    private fun addShowcase(showcase: Showcase) {
        var bool = false

        for (show: Showcase in showcases){
            if (show.showcaseId == showcase.showcaseId){
                bool = true
            }
        }
        if (!bool){
            showcases.add(showcase)
        }

    }

    private fun fillAutocompleteInstitutions(view: View) {
        institutionsName = ArrayList()

        institutions = dbClient.getAllInstitutions()

        for (institution: InstitutionsModelItem in institutions){
            institutionsName.add(institution.name)
        }

        val adapter: ArrayAdapter<String> =
            ArrayAdapter(view.context, android.R.layout.simple_spinner_dropdown_item, institutionsName)

        autoCompleteInstitutions.setAdapter(adapter)
    }

    private fun fillAutocompleteBuildings(view: View) {
        val buildingsName = ArrayList<String>()

        for (building: Building in buildings){
            buildingsName.add(building.name)
        }

        val adapter: ArrayAdapter<String> =
            ArrayAdapter(view.context, android.R.layout.simple_spinner_dropdown_item, buildingsName)

        autoCompleteBuildings.setAdapter(adapter)
    }

    private fun fillAutocompleteRooms(view: View, idOfBuilding: Int) {
        val roomsName = ArrayList<String>()

        for (room: Room in rooms){
            if (room.buildingId == idOfBuilding){
                roomsName.add(room.name)
            }
        }

        val adapter: ArrayAdapter<String> =
            ArrayAdapter(view.context, android.R.layout.simple_spinner_dropdown_item, roomsName)

        autoCompleteRooms.setAdapter(adapter)
    }

    private fun fillAutocompleteShowcases(view: View, idOfRoom: Int) {
         val showcasesName = ArrayList<String>()

        for (showcase: Showcase in showcases) {
            if (idOfRoom == showcase.roomId)
                showcasesName.add(showcase.name)
        }

        val adapter: ArrayAdapter<String> =
            ArrayAdapter(view.context, android.R.layout.simple_spinner_dropdown_item, showcasesName)

        autoCompleteShowCases.setAdapter(adapter)
    }

    private fun fillAutocompleteExhibits(view: View, idOfShowcase: Int) {
        val exhibitName = ArrayList<String>()

        for (exhibit: ExhibitModelItem in exhibits) {
            if (idOfShowcase == exhibit.showcase.showcaseId)
                exhibitName.add(exhibit.name)
        }

        val adapter: ArrayAdapter<String> =
            ArrayAdapter(view.context, android.R.layout.simple_spinner_dropdown_item, exhibitName)

        autoCompleteExhibits.setAdapter(adapter)
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

    private fun validateBuildings(): Boolean {
        val exhibitBuildingString: String = searchBuildings.editText?.text.toString()

        if (exhibitBuildingString.isEmpty()) {
            searchInstitutions.error =
                resources.getString(R.string.requiredNameOfBuildingFromList)
            return false
        } else {
            for (suggestion in buildings) {
                if (searchBuildings.editText?.text.toString() == suggestion.name) {
                    searchBuildings.error = null
                    searchBuildings.isErrorEnabled = false
                    return true
                }
            }
            searchBuildings.error =
                resources.getString(R.string.requiredNameOfBuildingFromList)
            return false
        }
    }

    private fun validateRooms(): Boolean {
        val exhibitRoomString: String = searchRooms.editText?.text.toString()

        if (exhibitRoomString.isEmpty()) {
            searchRooms.error =
                resources.getString(R.string.requiredNameOfRoomFromList)
            return false
        } else {
            for (suggestion in rooms) {
                if (searchRooms.editText?.text.toString() == suggestion.name) {
                    searchRooms.error = null
                    searchRooms.isErrorEnabled = false
                    return true
                }
            }
            searchRooms.error =
                resources.getString(R.string.requiredNameOfRoomFromList)
            return false
        }
    }

    private fun validateShowcases(): Boolean {
        val exhibitShowcaseString: String = searchShowCases.editText?.text.toString()

        if (exhibitShowcaseString.isEmpty()) {
            searchShowCases.error =
                resources.getString(R.string.requiredNameOfShowcaseFromList)
            return false
        } else {
            for (suggestion in showcases) {
                if (searchShowCases.editText?.text.toString() == suggestion.name) {
                    searchShowCases.error = null
                    searchShowCases.isErrorEnabled = false
                    return true
                }
            }
            searchShowCases.error =
                resources.getString(R.string.requiredNameOfShowcaseFromList)
            return false
        }
    }

    private fun validateExhibits(): Boolean {
        val exhibitExhibitString: String = searchExhibits.editText?.text.toString()

        if (exhibitExhibitString.isEmpty()) {
            searchExhibits.error =
                resources.getString(R.string.requiredNameOfExhibitFromList)
            return false
        } else {
            for (suggestion in exhibits) {
                if (searchExhibits.editText?.text.toString() == suggestion.name) {
                    searchExhibits.error = null
                    searchExhibits.isErrorEnabled = false
                    return true
                }
            }
            searchExhibits.error =
                resources.getString(R.string.requiredNameOfExhibitFromList)
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