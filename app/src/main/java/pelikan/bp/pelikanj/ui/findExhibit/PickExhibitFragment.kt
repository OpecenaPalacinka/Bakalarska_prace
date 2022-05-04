package pelikan.bp.pelikanj.ui.findExhibit

import android.app.Activity
import android.content.Context
import android.graphics.Insets
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.method.ScrollingMovementMethod
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.animation_failed.*
import pelikan.bp.pelikanj.ApiClient
import pelikan.bp.pelikanj.DBClient
import pelikan.bp.pelikanj.R
import pelikan.bp.pelikanj.viewModels.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Pick exhibit fragment
 * Contains all functionality and data for form and getting translation
 *
 * @constructor Create empty Pick exhibit fragment
 */
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
    private lateinit var cardF: LinearLayout
    lateinit var textView: TextView

    lateinit var parent: ConstraintLayout

    private lateinit var animationF: LottieAnimationView

    lateinit var frameLayout: FrameLayout

    private var heightOverbox: Int = 0
    private var heightCard: Int = 0
    var heightScroll: Int = 0

    private lateinit var dbClient: DBClient

    /**
     * On create view
     *
     * @param inflater inflater
     * @param container container
     * @param savedInstanceState savedInstanceState
     * @return view
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view: View = inflater.inflate(R.layout.fragment_pick_exhibit, container, false)

        frameLayout = view.findViewById(R.id.pick_exhibit_fragment)

        frameLayout.addView(inflater.inflate(R.layout.translation_card,null))
        frameLayout.addView(inflater.inflate(R.layout.animation_failed,null))

        dbClient = DBClient(requireContext())

        initForm(view)

        fillAutocompleteInstitutions(view)

        // Count sizes for translation card and background
        heightOverbox = countHeight()
        heightCard = heightOverbox / 2

        setUpListeners()

        setUpAnimation()

        return view
    }

    /**
     * Function counts the height of screen, no top bar, no navigation bar
     *
     * @return height of screen
     */
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

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = requireActivity().windowManager.currentWindowMetrics
            val insets: Insets = windowMetrics.windowInsets
                .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            // return API level 30+
            windowMetrics.bounds.height() - insets.bottom - insets.top - (2 * actionBarHeight)
        } else {
            val displayMetrics = DisplayMetrics()
            requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
            // return API level 29-
            displayMetrics.heightPixels - (2 * actionBarHeight) - statusBarHeight
        }
    }

    /** Set up listeners */
    private fun setUpListeners(){
        var idOfBuilding = 0
        var idOfRoom = 0
        var idOfShowcase: Int

        autoCompleteInstitutions.setOnItemClickListener { parent, _, position, _ ->

            autoCompleteBuildings.editableText.clear()
            autoCompleteRooms.editableText.clear()
            autoCompleteShowCases.editableText.clear()
            autoCompleteExhibits.editableText.clear()

            resetLists()

            val item = parent.getItemAtPosition(position)
            val idOfInstitution = getInstitutionIdByName(item.toString())
            if (idOfInstitution != -1){
                getAllExhibitsByInstitutionId(idOfInstitution)
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
                fillAutocompleteExhibits(-1)
            }

            if (idOfBuilding != -1){
                fillAutocompleteRooms(idOfBuilding)
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
                fillAutocompleteExhibits(-1)
            }

            if (idOfRoom != -1){
                fillAutocompleteShowcases(idOfRoom)
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
                fillAutocompleteExhibits(idOfShowcase)
            } else {
                Log.d(tag,"Cannot find showcase")
            }
        }

        // When institution does not have showcases doesn't work
        autoCompleteShowCases.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                //validateShowcases()
            }
        }

        searchTranslationButton.setOnClickListener {
            if (checkValues()){

                val idOfExhibit = getExhibitIdByName(autoCompleteExhibits.editableText.toString(),idOfBuilding)

                // Find translation
                getTranslation(idOfExhibit)
            }
        }

        overbox.setOnClickListener {
            if (fromsmall.hasEnded()) {
                setUpAnimation()
            }
        }

        // IMPORTANT!!!
        // Disable scrolling of scroll view and lets scroll text inside of translation card
        scrollView.setOnTouchListener { _, _ ->
            textView.getParent().requestDisallowInterceptTouchEvent(false)
            false
        }

        // IMPORTANT!!!
        textView.setOnTouchListener { _, _ ->
            textView.getParent().requestDisallowInterceptTouchEvent(true)
            false
        }


    }

    /**
     * Check values in form
     * If institution does not have showcases ignore them
     *
     * @return true = ok, false = problem (one or more values are not valid)
     */
    private fun checkValues():Boolean {
        if (showcases.isEmpty()){
            if (!validateInstitution() or !validateBuildings()
                or !validateRooms() or !validateExhibits()) {
                searchShowCases.error = null
                searchShowCases.isErrorEnabled = false
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

    /**
     * Get translation with language of users choice
     *
     * @param idOfExhibit id of exhibit
     */
    private fun getTranslation(idOfExhibit: Int) {
        val lCode = dbClient.getAllUserData()?.language!!

        val client: Call<Translation> = ApiClient.create().getTranslation(idOfExhibit,lCode)

        client.enqueue(object : Callback<Translation> {
            override fun onResponse(
                call: Call<Translation>,
                response: Response<Translation>
            ) {
                hideKeyboard()
                if (response.code() == 200){
                    // Show translation
                    textView.text = HtmlCompat.fromHtml(response.body()!!.translatedText,HtmlCompat.FROM_HTML_MODE_LEGACY)
                    heightScroll = scrollView.layoutParams.height
                    doAnimation()
                    resetForm()
                } else if (response.code() == 400){
                    // Try english
                    getEnglishTranslation(idOfExhibit)
                }

            }

            override fun onFailure(call: Call<Translation>, t: Throwable) {
                Log.println(Log.ERROR,tag,t.toString())
            }
        })
    }

    /**
     * Get english translation of exhibit
     *
     * @param idOfExhibit id of exhibit
     */
    private fun getEnglishTranslation(idOfExhibit: Int) {
        val lCode = "en"

        val client: Call<Translation> = ApiClient.create().getTranslation(idOfExhibit,lCode)

        client.enqueue(object : Callback<Translation> {
            override fun onResponse(
                call: Call<Translation>,
                response: Response<Translation>
            ) {
                if (response.code() == 200){
                    // Show translation
                    heightScroll = scrollView.layoutParams.height
                    textView.text = HtmlCompat.fromHtml(response.body()!!.translatedText,HtmlCompat.FROM_HTML_MODE_LEGACY)
                    doAnimation()

                    resetForm()
                } else if (response.code() == 400){
                    // No translation
                    animation_failed_text.text = resources.getString(R.string.no_translation)
                    doAnimationFailed()
                    heightScroll = scrollView.layoutParams.height
                    resetForm()
                }
            }

            override fun onFailure(call: Call<Translation>, t: Throwable) {
                Log.println(Log.ERROR,tag,t.toString())
            }
        })
    }

    /** Clear input fields in form for next usage */
    private fun resetForm() {
        searchExhibits.editText?.text = Editable.Factory.getInstance().newEditable("")
    }

    /**
     * Get exhibit id by building id and exhibit name
     *
     * @param exhibitName exhibit name
     * @param buildingId building id
     * @return id of exhibit OR -1 if not found
     */
    private fun getExhibitIdByName(exhibitName: String, buildingId: Int): Int {
        for (exhibit: ExhibitModelItem in exhibits){
            if (exhibit.name == exhibitName && buildingId == exhibit.building.buildingId){
                return exhibit.exhibitId
            }
        }
        return -1
    }

    /**
     * Get showcase id by room id and showcase name
     *
     * @param showcaseName showcase name
     * @param roomId room id
     * @return showcase id OR -1 if not found
     */
    private fun getShowcaseIdByName(showcaseName: String, roomId: Int): Int {
        for (showcase: Showcase in showcases){
            if (showcase.name == showcaseName && roomId == showcase.roomId){
                return showcase.showcaseId
            }
        }
        return -1
    }

    /**
     * Get room id by building id and room name
     *
     * @param roomName room name
     * @param buildingId building id
     * @return room id OR -1 if not found
     */
    private fun getRoomIdByName(roomName: String, buildingId: Int): Int {
        for (room: Room in rooms){
            if (room.name == roomName && room.buildingId == buildingId){
                return room.roomId
            }
        }
        return -1
    }

    /**
     * Get building id by building name
     *
     * @param buildingName building name
     * @return
     */
    private fun getBuildingIdByName(buildingName: String): Int {
        for (building: Building in buildings){
            if (building.name == buildingName){
                return building.buildingId
            }
        }
        return -1
    }

    /**
     * Get institution id by institution name
     *
     * @param nameOfInstitution name of institution
     * @return id of institution OR -1 if not found
     */
    private fun getInstitutionIdByName(nameOfInstitution: String): Int {
        for (institution: InstitutionsModelItem in institutions){
            if (institution.name == nameOfInstitution){
                return institution.institutionId
            }
        }
        return -1
    }

    /**
     * Get all exhibits by institution id
     * Use exhibits information to create a list of buildings, rooms and showcases
     * The lists might not be full, but there is no reason to add e.g. showcases that don´t have exhibits
     *
     * @param idOfInstitution id of institution
     */
    private fun getAllExhibitsByInstitutionId(idOfInstitution: Int) {
        val client: Call<ExhibitModel> = ApiClient.create().getAllExhibitsOfInstitution(idOfInstitution)

        client.enqueue(object : Callback<ExhibitModel> {
            override fun onResponse(
                call: Call<ExhibitModel>,
                response: Response<ExhibitModel>
            ) {
                val respBody = response.body()!!

                for (respo: ExhibitModelItem in respBody) {
                    exhibits.add(respo)
                    addBuilding(respo.building)
                    addRoom(respo.room)
                    addShowcase(respo.showcase)
                }

                fillAutocompleteBuildings()

                /* Save to local db, not used yet
                if (!buildings.isNullOrEmpty()){
                    dbClient.insertAllBuildings(buildings)
                }
                if (!rooms.isNullOrEmpty()){
                    dbClient.insertAllRooms(rooms)
                }
                if (!showcases.isNullOrEmpty()){
                    dbClient.insertAllShowcases(showcases)
                }
                */
            }

            override fun onFailure(call: Call<ExhibitModel>, t: Throwable) {
                Log.println(Log.ERROR,tag,t.toString())
            }
        })

    }

    /** Reset lists when new institution is selected and clear adapters */
    private fun resetLists() {
        exhibits.clear()
        buildings.clear()
        rooms.clear()
        showcases.clear()

        autoCompleteExhibits.setAdapter(null)
        autoCompleteShowCases.setAdapter(null)
        autoCompleteRooms.setAdapter(null)
        autoCompleteBuildings.setAdapter(null)

    }

    /**
     * Try to add building to list, if not already in
     *
     * @param building building to add
     */
    private fun addBuilding(building: Building) {
        var bool = false

        if (building != null){
            for (build: Building in buildings){
                if (build.buildingId == building.buildingId){
                    bool = true
                }
            }
            if (!bool){
                buildings.add(building)
            }
        }
    }

    /**
     * Try to add room to list, if not already in
     *
     * @param room room to add
     */
    private fun addRoom(room: Room) {
        var bool = false

        if (room != null){
            for (r: Room in rooms){
                if (r.roomId == room.roomId){
                    bool = true
                }
            }
            if (!bool){
                rooms.add(room)
            }
        }
    }

    /**
     * Try to add showcase to list if not already in
     *
     * @param showcase showcase to add
     */
    private fun addShowcase(showcase: Showcase) {
        var bool = false

        if (showcase != null){
            for (show: Showcase in showcases){
                if (show.showcaseId == showcase.showcaseId){
                    bool = true
                }
            }
            if (!bool){
                showcases.add(showcase)
            }
        }
    }

    /**
     * Fill autocomplete with institutions
     * View is need because this is used in onCreate fun (no global view yet)
     *
     * @param view view
     */
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

    /** Fill autocomplete with buildings */
    private fun fillAutocompleteBuildings() {
        val buildingsName = ArrayList<String>()

        for (building: Building in buildings){
            buildingsName.add(building.name)
        }

        val adapter: ArrayAdapter<String> =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, buildingsName)

        autoCompleteBuildings.setAdapter(adapter)
    }

    /**
     * Fill autocomplete with rooms
     *
     * @param idOfBuilding id of building
     */
    private fun fillAutocompleteRooms(idOfBuilding: Int) {
        val roomsName = ArrayList<String>()

        for (room: Room in rooms){
            if (room.buildingId == idOfBuilding){
                roomsName.add(room.name)
            }
        }

        val adapter: ArrayAdapter<String> =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, roomsName)

        autoCompleteRooms.setAdapter(adapter)
    }

    /**
     * Fill autocomplete with showcases
     *
     * @param idOfRoom id of room
     */
    private fun fillAutocompleteShowcases(idOfRoom: Int) {
         val showcasesName = ArrayList<String>()

        for (showcase: Showcase in showcases) {
            if (idOfRoom == showcase.roomId)
                showcasesName.add(showcase.name)
        }

        val adapter: ArrayAdapter<String> =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, showcasesName)

        autoCompleteShowCases.setAdapter(adapter)
    }

    /**
     * Fill autocomplete with exhibits
     *
     * @param idOfShowcase id of showcase
     */
    private fun fillAutocompleteExhibits(idOfShowcase: Int) {
        val exhibitName = ArrayList<String>()

        for (exhibit: ExhibitModelItem in exhibits) {
            if (idOfShowcase == -1){
                exhibitName.add(exhibit.name)
            } else if (idOfShowcase == exhibit.showcase.showcaseId)
                exhibitName.add(exhibit.name)
        }

        val adapter: ArrayAdapter<String> =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, exhibitName)

        autoCompleteExhibits.setAdapter(adapter)
    }

    /**
     * Validate institution input field
     *
     * @return true = ok, false = problem (empty, not from list)
     */
    private fun validateInstitution(): Boolean {
        val exhibitInstitutionString: String = searchInstitutions.editText?.text.toString()

        if (exhibitInstitutionString.isEmpty()) {
            searchInstitutions.error =
                resources.getString(R.string.choose_institution)
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

    /**
     * Validate buildings input field
     *
     * @return true = ok, false = problem (empty, not from list)
     */
    private fun validateBuildings(): Boolean {
        val exhibitBuildingString: String = searchBuildings.editText?.text.toString()

        if (exhibitBuildingString.isEmpty()) {
            searchBuildings.error =
                resources.getString(R.string.requiredNameOfBuilding)
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

    /**
     * Validate rooms input field
     *
     * @return true = ok, false = problem (empty, not from list)
     */
    private fun validateRooms(): Boolean {
        val exhibitRoomString: String = searchRooms.editText?.text.toString()

        if (exhibitRoomString.isEmpty()) {
            searchRooms.error =
                resources.getString(R.string.requiredNameOfRoom)
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

    /**
     * Validate showcases input field
     *
     * @return true = ok, false = problem (empty, not from list)
     */
    private fun validateShowcases(): Boolean {
        val exhibitShowcaseString: String = searchShowCases.editText?.text.toString()

        if (exhibitShowcaseString.isEmpty()) {
            searchShowCases.error =
                resources.getString(R.string.requiredNameOfShowCase)
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

    /**
     * Validate exhibits input field
     *
     * @return true = ok, false = problem (empty, not from list)
     */
    private fun validateExhibits(): Boolean {
        val exhibitExhibitString: String = searchExhibits.editText?.text.toString()

        if (exhibitExhibitString.isEmpty()) {
            searchExhibits.error =
                resources.getString(R.string.requiredNameOfExhibit)
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

    /** Show translation */
    private fun doAnimation() {
        // Set height of black "background"
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

    /** Do animation failed (no translation for exhibit) */
    private fun doAnimationFailed() {
        animationF.visibility = VISIBLE
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

    /** Set up animation and hide all components */
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

    /**
     * Init all xml tags
     *
     * @param view view
     */
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

    /** Hide keyboard */
    fun Fragment.hideKeyboard() {
        view?.let { activity?.hideKeyboard(it) }
    }

    /**
     * Hide keyboard
     *
     * @param view view
     */
    private fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

}