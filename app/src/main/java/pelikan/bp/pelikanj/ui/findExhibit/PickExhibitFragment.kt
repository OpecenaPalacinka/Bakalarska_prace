package pelikan.bp.pelikanj.ui.findExhibit

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.method.ScrollingMovementMethod
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import kotlinx.android.synthetic.main.animation_failed.*
import okhttp3.ResponseBody
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
    private lateinit var scrollView: ScrollView

    private var exhibits: ArrayList<ExhibitModelItem> = ArrayList()
    private var institutions: ArrayList<InstitutionsModelItem> = ArrayList()
    private var buildings: ArrayList<Building> = ArrayList()
    private var rooms: ArrayList<Room> = ArrayList()
    private var showcases: ArrayList<Showcase> = ArrayList()
    private lateinit var institutionsName: ArrayList<String>

    private lateinit var fromsmall: Animation
    private lateinit var fromnothing: Animation
    private lateinit var foricon: Animation
    lateinit var overbox: LinearLayout
    lateinit var card: LinearLayout
    lateinit var cardF: LinearLayout
    lateinit var textView: TextView

    lateinit var parent: ConstraintLayout

    lateinit var animationF: LottieAnimationView

    lateinit var frameLayout: FrameLayout

    var heightOverbox: Int = 0
    var heightCard: Int = 0
    var heightScroll: Int = 0

    private lateinit var dbClient: DBClient


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view: View = inflater.inflate(R.layout.fragment_pick_exhibit, container, false)

        frameLayout = view.findViewById(R.id.pick_exhibit_fragment)

        frameLayout.addView(inflater.inflate(R.layout.translation_card,null))
        frameLayout.addView(inflater.inflate(R.layout.animation_failed,null))

        dbClient = DBClient(requireContext())

        initForm(view)

        fillAutocompleteInstitutions(view)

        heightOverbox = countHeight()
        heightCard = heightOverbox / 2

        setUpListeners(view)

        setUpAnimation()

        return view
    }

    private fun countHeight(): Int{
        val tv = TypedValue()
        var actionBarHeight = 0
        if (requireActivity().theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, resources.displayMetrics)
        }

        val resource = requireContext().resources.getIdentifier("status_bar_height", "dimen", "android")
        var statusBarHeight = 0
        if (resource > 0) {
            statusBarHeight = requireContext().resources.getDimensionPixelSize(resource)
        }

        val wm = requireContext().getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        wm.defaultDisplay.getMetrics(metrics)

        return metrics.heightPixels - (2 * actionBarHeight) - statusBarHeight
    }

    private fun setUpListeners(view: View){
        var idOfBuilding = 0
        var idOfRoom = 0
        var idOfShowcase = 0

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

            if (rooms.isEmpty()){
                fillAutocompleteExhibits(view,-1)
            }

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

            if (showcases.isEmpty()){
                fillAutocompleteExhibits(view,-1)
            }

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
                //validateShowcases()
            }
        }

        searchTranslationButton.setOnClickListener {
            if (checkValues()){
                val idOfExhibit = getExhibitIdByName(autoCompleteExhibits.editableText.toString(),idOfBuilding)
                // funkce pro vyhledání exponátu
                getTranslation(idOfExhibit)
            }
        }

        overbox.setOnClickListener {
            if (fromsmall.hasEnded()) {
                setUpAnimation()
            }
        }

        scrollView.setOnTouchListener { _, _ ->
            textView.getParent().requestDisallowInterceptTouchEvent(false)
            false
        }

        textView.setOnTouchListener { _, _ ->
            textView.getParent().requestDisallowInterceptTouchEvent(true)
            false
        }


    }

    private fun checkValues():Boolean {
        if (showcases.isEmpty()){
            if (!validateInstitution() or !validateBuildings()
                or !validateRooms() or !validateExhibits()) {
                return false
            }
        } else {
            if (!validateInstitution() or !validateBuildings()
                or !validateRooms() or !validateShowcases()
                or !validateExhibits()) {
                return false
            }
        }
        return true
    }

    private fun getTranslation(idOfExhibit: Int) {
        val lCode = dbClient.getAllUserData()?.language!!

        val client: Call<ResponseBody> = ApiClient.create().getTranslation(idOfExhibit,lCode)

        client.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                hideKeyboard()
                if (response.code() == 200){
                    // Show translation
                    val resp: String = response.body()!!.string()
                    val body = Gson().fromJson(resp, Translation::class.java)
                    val text = body.translatedText
                    textView.text = HtmlCompat.fromHtml(text,HtmlCompat.FROM_HTML_MODE_LEGACY)
                    heightScroll = scrollView.layoutParams.height
                    doAnimation()
                    resetForm()
                } else if (response.code() == 400){
                    // Try english
                    getEnglishTranslation(idOfExhibit)
                }

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.println(Log.ERROR,tag,t.toString())
            }
        })
    }

    private fun getEnglishTranslation(idOfExhibit: Int) {
        val lCode = "en"

        val client: Call<ResponseBody> = ApiClient.create().getTranslation(idOfExhibit,lCode)

        client.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                if (response.code() == 200){
                    // Show translation
                    val resp: String = response.body()!!.string()
                    val body = Gson().fromJson(resp, Translation::class.java)
                    val text = body.translatedText
                    heightScroll = scrollView.layoutParams.height
                    textView.text = HtmlCompat.fromHtml(text,HtmlCompat.FROM_HTML_MODE_LEGACY)
                    doAnimation()

                    resetForm()
                } else if (response.code() == 400){
                    // Try english
                    animation_failed_text.text = resources.getString(R.string.no_translation)
                    doAnimationFailed()
                    heightScroll = scrollView.layoutParams.height
                    resetForm()
                    cardF.y -= scrollView.scrollY
                    animationF.y -= scrollView.scrollY
                }

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.println(Log.ERROR,tag,t.toString())
            }
        })
    }

    private fun resetForm() {
        searchExhibits.editText?.text = Editable.Factory.getInstance().newEditable("")
    }

    private fun getExhibitIdByName(exhibitName: String, buildingId: Int): Int {
        for (exhibit: ExhibitModelItem in exhibits){
            if (exhibit.name == exhibitName && buildingId == exhibit.building.buildingId){
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

                if (!buildings.isNullOrEmpty()){
                    dbClient.insertAllBuildings(buildings)
                }
                if (!rooms.isNullOrEmpty()){
                    dbClient.insertAllRooms(rooms)
                }
                if (!showcases.isNullOrEmpty()){
                    dbClient.insertAllShowcases(showcases)
                }

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
        if (!bool && building != null){
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
        if (!bool && room != null){
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
        if (!bool && showcase != null){
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
            if (idOfShowcase == -1){
                exhibitName.add(exhibit.name)
            } else if (idOfShowcase == exhibit.showcase.showcaseId)
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
            searchBuildings.error =
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

    private fun doAnimation() {

        val params: ViewGroup.LayoutParams = overbox.layoutParams
        params.height = heightOverbox
        overbox.layoutParams = params

        val paramsCard: ViewGroup.LayoutParams = card.layoutParams
        paramsCard.height = heightCard
        card.layoutParams = paramsCard
        card.visibility = VISIBLE


        scrollView.scrollY = 0

        overbox.alpha = 1F
        overbox.startAnimation(fromnothing)

        card.alpha = 1F
        card.y += scrollView.scrollY.toFloat()
        card.startAnimation(fromsmall)
    }

    private fun doAnimationFailed() {
        animationF.visibility = View.VISIBLE
        animationF.y += scrollView.scrollY.toFloat()
        animationF.startAnimation(foricon)
        animationF.playAnimation()
        animationF.repeatCount = 1

        overbox.alpha = 1F
        overbox.startAnimation(fromnothing)

        val params: ViewGroup.LayoutParams = overbox.layoutParams
        params.height = heightOverbox
        overbox.layoutParams = params

        scrollView.scrollY = 0

        cardF.alpha = 1F
        cardF.y += scrollView.scrollY.toFloat()
        cardF.startAnimation(fromsmall)
    }

    private fun setUpAnimation() {
        fromsmall = AnimationUtils.loadAnimation(context,R.anim.fromsmall)
        fromnothing = AnimationUtils.loadAnimation(context,R.anim.fromnothing)
        foricon = AnimationUtils.loadAnimation(context,R.anim.foricon)

        card.alpha = 0F
        cardF.alpha = 0F
        overbox.alpha = 0F

        animationF.visibility = GONE

        val paramsCard: ViewGroup.LayoutParams = card.layoutParams
        paramsCard.height = 0
        card.layoutParams = paramsCard
        card.visibility = GONE

        val params: ViewGroup.LayoutParams = overbox.layoutParams
        params.height = 0
        overbox.layoutParams = params
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

        scrollView = view.findViewById(R.id.pick_exhibit_fragment_scrollView)

        card = view.findViewById(R.id.popup_translation)
        overbox = view.findViewById(R.id.overbox)
        textView = view.findViewById(R.id.translated_text)
        textView.movementMethod = ScrollingMovementMethod()

        animationF = view.findViewById(R.id.animationFailed)
        cardF = view.findViewById(R.id.popup_failed)

        parent = view.findViewById(R.id.main_parent)

    }

    fun Fragment.hideKeyboard() {
        view?.let { activity?.hideKeyboard(it) }
    }

    private fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

}