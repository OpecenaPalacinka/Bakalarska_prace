package pelikan.bp.pelikanj.ui.more

import android.animation.Animator
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.Insets
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.util.Base64
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.textfield.TextInputLayout
import okhttp3.ResponseBody
import pelikan.bp.pelikanj.ApiClient
import pelikan.bp.pelikanj.DBClient
import pelikan.bp.pelikanj.R
import pelikan.bp.pelikanj.viewModels.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream


class SendExhibitFragment : Fragment() {

    private lateinit var exhibitName: TextInputLayout
    private lateinit var exhibitInfoLabel: TextInputLayout
    private lateinit var imageInfoLabel: ImageView
    private lateinit var infoLabelButton: Button
    private lateinit var imageExhibit: ImageView
    private lateinit var exhibitButton: Button
    private lateinit var institution: TextInputLayout
    private lateinit var buildingNumber: TextInputLayout
    private lateinit var roomNumber: TextInputLayout
    private lateinit var showCaseNumber: TextInputLayout
    private lateinit var sendExhibitButton: Button
    private lateinit var autoCompleteTextView: AutoCompleteTextView
    private lateinit var autoCompleteBuildings: AutoCompleteTextView
    private lateinit var autoCompleteRooms: AutoCompleteTextView
    private lateinit var autoCompleteShowCases: AutoCompleteTextView

    private lateinit var scrollView: ScrollView

    private var encodedImageInfo: String = ""
    private var encodedImageExhibit: String = ""

    lateinit var animation: LottieAnimationView
    lateinit var animationF: LottieAnimationView
    lateinit var frameLayout: FrameLayout

    private var code: Int = 1
    private var heightOverbox: Int = 0

    private lateinit var fromsmall: Animation
    private lateinit var fromnothing: Animation
    private lateinit var foricon: Animation
    lateinit var overbox: LinearLayout
    lateinit var card: LinearLayout
    private lateinit var cardF: LinearLayout

    private lateinit var dbClient: DBClient

    private var institutions: ArrayList<InstitutionsModelItem> = ArrayList()
    private var institutionsName: ArrayList<String> = ArrayList()
    private var buildings: ArrayList<Building> = ArrayList()
    private var rooms: ArrayList<Room> = ArrayList()
    private var showcases: ArrayList<Showcase> = ArrayList()

    /**
     * On create
     *
     * @param savedInstanceState savedInstanceState
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Hide back arrow on top bar
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    /**
     * On create view
     *
     * @param inflater inflater
     * @param container container
     * @param SavedInstanceState savedInstanceState
     * @return view
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, SavedInstanceState: Bundle?): View {
        val view: View = inflater.inflate(R.layout.fragment_send_exhibit, container, false)

        frameLayout = view.findViewById(R.id.send_exhibit_fragment)

        frameLayout.addView(inflater.inflate(R.layout.animation_success,null))
        frameLayout.addView(inflater.inflate(R.layout.animation_failed,null))

        dbClient = DBClient(requireContext())

        initForm(view)

        setUpAnimation(view)

        setUpListeners()

        fillAutocompleteInstitution(view)

        heightOverbox = countHeight()

        return view
    }

    /**
     * Set up listeners for textfields
     */
    private fun setUpListeners() {
        var idOfBuilding: Int
        var idOfRoom: Int

        infoLabelButton.setOnClickListener {
            code = 1
            chooseProfilePicture()
        }

        exhibitButton.setOnClickListener {
            code = 2
            chooseProfilePicture()
        }

        sendExhibitButton.setOnClickListener {
            if(checkRequiredValues()){
                sendExhibit()
            }
        }

        autoCompleteTextView.setOnItemClickListener { parent, _, position, _ ->
            //If institution changed, clear whole form
            autoCompleteBuildings.editableText.clear()
            buildings.clear()
            autoCompleteRooms.editableText.clear()
            rooms.clear()
            autoCompleteShowCases.editableText.clear()
            showcases.clear()

            // Get id of item, then institution and then get buildings
            val item = parent.getItemAtPosition(position)
            val idOfInstitution = getInstitutionIdByName(item.toString())
            if (idOfInstitution != -1){
                getAllBuildingsByInstitutionId(idOfInstitution)
            } else {
                Log.d(tag,"Cannot find institution")
            }

        }

        autoCompleteBuildings.setOnItemClickListener { parent, _, position, _ ->
            // If building changed, clear rooms and showcases
            autoCompleteRooms.editableText.clear()
            rooms.clear()
            autoCompleteShowCases.editableText.clear()
            showcases.clear()

            val item = parent.getItemAtPosition(position)
            idOfBuilding = getBuildingIdByName(item.toString())
            if (idOfBuilding != -1){
                getAllRoomsByBuildingId(idOfBuilding)
            } else {
                Log.d(tag,"Cannot find buildings")
            }

        }

        autoCompleteRooms.setOnItemClickListener { parent, _, position, _ ->
            // If room changed, clear showcases
            autoCompleteShowCases.editableText.clear()
            showcases.clear()

            val item = parent.getItemAtPosition(position)
            idOfRoom = getRoomIdByName(item.toString())
            if (idOfRoom != -1){
                getAllShowcasesByRoomId(idOfRoom)
            } else {
                Log.d(tag,"Cannot find rooms")
            }
        }

    }

    /**
     * Get all buildings by institution id
     *
     * @param idOfInstitution institution id
     */
    private fun getAllBuildingsByInstitutionId(idOfInstitution: Int) {
        val client: Call<List<Building>> = ApiClient.create().getBuildings(idOfInstitution)

        client.enqueue(object : Callback<List<Building>> {
            override fun onResponse(
                call: Call<List<Building>>,
                response: Response<List<Building>>
            ) {
                if (response.code() == 200){
                    for (resp: Building in response.body()!!){
                        buildings.add(resp)
                    }
                    fillAutocompleteBuildings()
                } else {
                    Log.d(tag,"Cannot get buildings")
                }
            }

            override fun onFailure(call: Call<List<Building>>, t: Throwable) {
                Log.println(Log.ERROR,tag,t.toString())
            }
        })
    }

    /**
     * Get all rooms by building id
     *
     * @param idOfBuilding building id
     */
    private fun getAllRoomsByBuildingId(idOfBuilding: Int) {
        val client: Call<List<Room>> = ApiClient.create().getRooms(idOfBuilding)

        client.enqueue(object : Callback<List<Room>> {
            override fun onResponse(
                call: Call<List<Room>>,
                response: Response<List<Room>>
            ) {
                if (response.code() == 200){
                    for (resp: Room in response.body()!!){
                        rooms.add(resp)
                    }
                    fillAutocompleteRooms()
                } else {
                    Log.d(tag,"Cannot get rooms")
                }
            }

            override fun onFailure(call: Call<List<Room>>, t: Throwable) {
                Log.println(Log.ERROR,tag,t.toString())
            }
        })
    }

    /**
     * Get all showcases by room id
     *
     * @param idOfRoom id of room
     */
    private fun getAllShowcasesByRoomId(idOfRoom: Int) {
        val client: Call<List<Showcase>> = ApiClient.create().getShowcases(idOfRoom)

        client.enqueue(object : Callback<List<Showcase>> {
            override fun onResponse(
                call: Call<List<Showcase>>,
                response: Response<List<Showcase>>
            ) {
                if (response.code() == 200){
                    for (resp: Showcase in response.body()!!){
                        showcases.add(resp)
                    }
                    fillAutocompleteShowcases()
                } else {
                    Log.d(tag,"Cannot get showcases")
                }
            }

            override fun onFailure(call: Call<List<Showcase>>, t: Throwable) {
                Log.println(Log.ERROR,tag,t.toString())
            }
        })
    }

    /**
     * Init all xml tags and text
     *
     * @param view view
     */
    private fun initForm(view: View){
        exhibitName = view.findViewById(R.id.exhibit_name)
        exhibitInfoLabel = view.findViewById(R.id.exhibit_info_label)
        imageInfoLabel = view.findViewById(R.id.imageView_info_label_image)
        infoLabelButton = view.findViewById(R.id.choose_info_label_image)
        imageExhibit = view.findViewById(R.id.imageView_exhibit_image)
        exhibitButton = view.findViewById(R.id.choose_exhibit_image)
        institution = view.findViewById(R.id.institution_name)
        buildingNumber = view.findViewById(R.id.building_number)
        roomNumber = view.findViewById(R.id.room_number)
        showCaseNumber = view.findViewById(R.id.showCase_number)
        sendExhibitButton = view.findViewById(R.id.send_exhibit_button)
        autoCompleteTextView = view.findViewById(R.id.autoComplete_institutions)
        autoCompleteBuildings = view.findViewById(R.id.autoComplete_buildings)
        autoCompleteRooms = view.findViewById(R.id.autoComplete_rooms)
        autoCompleteShowCases = view.findViewById(R.id.autoComplete_showCases)

        scrollView = view.findViewById(R.id.send_exhibit_scrollView)

        animation = view.findViewById(R.id.animationSuccess)
        card = view.findViewById(R.id.popup)

        animationF = view.findViewById(R.id.animationFailed)
        cardF = view.findViewById(R.id.popup_failed)

        overbox = view.findViewById(R.id.overbox)

        val textAnimation = view.findViewById<TextView>(R.id.animation_success_text)
        textAnimation.text = resources.getString(R.string.send_exhibit_success)

        val animationTextF: TextView = view.findViewById(R.id.animation_failed_text)
        animationTextF.text = resources.getString(R.string.registration_failed)
    }

    /**
     * Fill autocomplete institution with data
     *
     * @param view view
     */
    private fun fillAutocompleteInstitution(view: View){
        institutions = dbClient.getAllInstitutions()

        for (institution: InstitutionsModelItem in institutions){
            institutionsName.add(institution.name)
        }

        val adapter: ArrayAdapter<String> =
            ArrayAdapter(view.context,android.R.layout.simple_spinner_dropdown_item, institutionsName)

        autoCompleteTextView.setAdapter(adapter)
    }

    /**
     * Fill autocomplete of buildings with data
     */
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
     * Fill autocomplete of rooms with data
     */
    private fun fillAutocompleteRooms() {
        val roomsName = ArrayList<String>()

        for (room: Room in rooms){

                roomsName.add(room.name)

        }

        val adapter: ArrayAdapter<String> =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, roomsName)

        autoCompleteRooms.setAdapter(adapter)
    }

    /**
     * Fill autocomplete of showcases with data
     */
    private fun fillAutocompleteShowcases() {
        val showcasesName = ArrayList<String>()

        for (showcase: Showcase in showcases) {
                showcasesName.add(showcase.name)
        }

        val adapter: ArrayAdapter<String> =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, showcasesName)

        autoCompleteShowCases.setAdapter(adapter)
    }

    /**
     * Send exhibit to server
     */
    private fun sendExhibit() {
        // Get all data from form
        val exhibitNameS = exhibitName.editText?.text.toString()
        val infoLabel: String = exhibitInfoLabel.editText?.text.toString().ifEmpty {
            ""
        }
        val instiName = institution.editText?.text.toString()
        val instiId = getInstitutionIdByName(instiName)
        val buildingName = getBuildingIdByName(buildingNumber.editText?.text.toString()).toString()
        val roomName = getRoomIdByName(roomNumber.editText?.text.toString()).toString()
        var showCaseName: String? = getShowcaseIdByName(showCaseNumber.editText?.text.toString()).toString()
        if (getShowcaseIdByName(showCaseNumber.editText?.text.toString()) == -1){
            showCaseName = null
        }

        lateinit var exhibitItemWithExhibitImage: ExhibitItemWithExhibitImage
        lateinit var exhibitItemWithoutExhibitImage: ExhibitItemWithoutExhibitImage
        lateinit var client: Call<ResponseBody>

        // Added user a image of exhibit?
        if (encodedImageExhibit == ""){
            // Without image
            exhibitItemWithoutExhibitImage = ExhibitItemWithoutExhibitImage(exhibitNameS,infoLabel,encodedImageInfo,
                                                            buildingName,roomName,showCaseName)
            client = ApiClient.create().uploadNewExhibit(instiId,exhibitItemWithoutExhibitImage)
        } else {
            // With image
            exhibitItemWithExhibitImage = ExhibitItemWithExhibitImage(exhibitNameS,encodedImageExhibit,infoLabel,encodedImageInfo,
                                                            buildingName, roomName, showCaseName)
            client = ApiClient.create().uploadNewExhibitWithExhibitImage(instiId,exhibitItemWithExhibitImage)
        }

        client.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody?>,
                response: Response<ResponseBody>
            ) {
                hideKeyboard()
                //Check if OK
                if (response.code() == 201){
                    // Correct
                    doAnimation()
                    animation.addAnimatorListener(object : Animator.AnimatorListener {
                        override fun onAnimationStart(animation: Animator) {
                        }

                        override fun onAnimationEnd(animations: Animator) {
                            resetForm()
                            setUpAnimation(requireView())
                        }

                        override fun onAnimationCancel(animation: Animator) {
                        }

                        override fun onAnimationRepeat(animation: Animator) {
                        }
                    })
                } else {
                    // Something wrong happened
                    doAnimationFailed()
                    animationF.addAnimatorListener(object : Animator.AnimatorListener {
                        override fun onAnimationStart(animation: Animator) {
                        }
                        override fun onAnimationEnd(animations: Animator) {
                            setUpAnimation(requireView())
                        }
                        override fun onAnimationCancel(animation: Animator) {
                        }
                        override fun onAnimationRepeat(animation: Animator) {
                        }
                    })
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.d(Log.ERROR.toString(), t.toString())
            }


        })

    }

    /**
     * Get showcase id by name
     *
     * @param showcaseName showcase name
     * @return id of showcase OR -1 if not found
     */
    private fun getShowcaseIdByName(showcaseName: String): Int {
        for (showcase: Showcase in showcases){
            if (showcase.name == showcaseName){
                return showcase.showcaseId
            }
        }
        return -1
    }

    /**
     * Get room id by name
     *
     * @param roomName name of room
     * @return id of room OR -1 if not found
     */
    private fun getRoomIdByName(roomName: String): Int {
        for (room: Room in rooms){
            if (room.name == roomName){
                return room.roomId
            }
        }
        return -1
    }

    /**
     * Get building id by name
     *
     * @param buildingName building name
     * @return id of building OR -1 if not found
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
     * Get institution id by name
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
     * Reset the form after the submit
     */
    private fun resetForm() {
        exhibitName.editText?.text = Editable.Factory.getInstance().newEditable("")
        exhibitInfoLabel.editText?.text = Editable.Factory.getInstance().newEditable("")
        imageInfoLabel.setImageURI(null)
        imageInfoLabel.setImageResource(R.drawable.search_view_background)
        encodedImageInfo = ""
        imageExhibit.setImageURI(null)
        imageExhibit.setImageResource(R.drawable.search_view_background)
        encodedImageExhibit = ""
    }

    /**
     * Do animation when upload is successful
     */
    private fun doAnimation() {
        animation.visibility = View.VISIBLE
        animation.startAnimation(foricon)
        animation.playAnimation()
        animation.repeatCount = 1

        overbox.alpha = 1F
        overbox.startAnimation(fromnothing)

        card.alpha = 1F
        card.startAnimation(fromsmall)

        val params: ViewGroup.LayoutParams = overbox.layoutParams
        params.height = heightOverbox
        overbox.layoutParams = params

        scrollView.scrollY = 0
    }

    /**
     * Show animation when upload failed
     */
    private fun doAnimationFailed() {
        animationF.visibility = View.VISIBLE
        animationF.startAnimation(foricon)
        animationF.playAnimation()
        animationF.repeatCount = 1

        overbox.alpha = 1F
        overbox.startAnimation(fromnothing)

        cardF.alpha = 1F
        cardF.startAnimation(fromsmall)

        val params: ViewGroup.LayoutParams = overbox.layoutParams
        params.height = heightOverbox
        overbox.layoutParams = params

        scrollView.scrollY = 0
    }

    /**
     * Set up animations and texts
     *
     * @param view view
     */
    private fun setUpAnimation(view: View) {
        fromsmall = AnimationUtils.loadAnimation(context,R.anim.fromsmall)
        fromnothing = AnimationUtils.loadAnimation(context,R.anim.fromnothing)
        foricon = AnimationUtils.loadAnimation(context,R.anim.foricon)

        card.alpha = 0F
        cardF.alpha = 0F
        overbox.alpha = 0F
        animation.visibility = View.GONE
        animationF.visibility = View.GONE

        val params: ViewGroup.LayoutParams = overbox.layoutParams
        params.height = 0
        overbox.layoutParams = params


        val animationText: TextView = view.findViewById(R.id.animation_success_text)
        animationText.text = resources.getString(R.string.upload_successfull)

        val animationTextF: TextView = view.findViewById(R.id.animation_failed_text)
        animationTextF.text = resources.getString(R.string.upload_failed)
    }

    /**
     * Check required values
     *
     * @return true = ok, false = problem (one or more values are not valid)
     */
    private fun checkRequiredValues(): Boolean {
        // If institution doesn't have showcases, don't require them
        if (showcases.isEmpty()){
            if (!validateExhibitName() or !validateExhibitInfoLabel()
                or !validateInstitution() or !validateBuildingNumber()
                or !validateRoomNumber() or !validateImageInfoLabel()
            ) {
                return false
            }
        } else {
            if (!validateExhibitName() or !validateExhibitInfoLabel()
                or !validateInstitution() or !validateBuildingNumber()
                or !validateRoomNumber() or !validateShowCaseNumber()
                or !validateImageInfoLabel()
            ) {
                return false
            }
        }
        return true
    }

    /**
     * Validate exhibit name
     *
     * @return true = ok, false = problem (empty, too long)
     */
    private fun validateExhibitName(): Boolean {
        val exhibitNameString: String = exhibitName.editText?.text.toString()

        if (exhibitNameString.isEmpty()) {
            exhibitName.error = resources.getString(R.string.requiredNameOfExhibit)
            return false
        } else if (exhibitNameString.length > 100) {
            exhibitName.error = resources.getString(R.string.tooLong)
            return false
        } else {
            exhibitName.error = null
            exhibitName.isErrorEnabled = false
            return true
        }
    }

    /**
     * Validate exhibit info label (not required)
     *
     * @return true = ok, false = problem (too long)
     */
    private fun validateExhibitInfoLabel(): Boolean {
        val exhibitInfoLabelString: String = exhibitInfoLabel.editText?.text.toString()

        if (exhibitInfoLabelString.length > 25000) {
            exhibitInfoLabel.error = resources.getString(R.string.tooLong)
            return false
        } else {
            exhibitInfoLabel.error = null
            exhibitInfoLabel.isErrorEnabled = false
            return true
        }
    }

    /**
     * Validate image with exhibit info, it is required
     *
     * @return true = ok, false = problem (empty)
     */
    private fun validateImageInfoLabel(): Boolean {

        if (encodedImageInfo == "") {
            infoLabelButton.error = resources.getString(R.string.requiredInfoLabelImage)
            return false
        } else {
            infoLabelButton.error = null
            return true
        }
    }

    /**
     * Validate institution input field
     *
     * @return true = ok, false = problem (empty, not from list)
     */
    private fun validateInstitution(): Boolean {
        val exhibitInstitutionString: String = institution.editText?.text.toString()

        if (exhibitInstitutionString.isEmpty()) {
            institution.error = resources.getString(R.string.requiredNameOfInstitutionFromList)
            return false
        } else {
            for (suggestion: String in institutionsName) {
                if (institution.editText?.text.toString() == suggestion){
                    institution.error = null
                    institution.isErrorEnabled = false
                    return true
                }
            }
            institution.error = resources.getString(R.string.requiredNameOfInstitutionFromList)
            return false
        }
    }

    /**
     * Validate building input field
     *
     * @return true = ok, false = problem (empty, too long)
     */
    private fun validateBuildingNumber(): Boolean {
        val exhibitBuildingNumberString: String = buildingNumber.editText?.text.toString()

        if (exhibitBuildingNumberString.isEmpty()) {
            buildingNumber.error = resources.getString(R.string.requiredNameOfBuilding)
            return false
        } else if (exhibitBuildingNumberString.length > 50) {
            buildingNumber.error = resources.getString(R.string.tooLong)
            return false
        } else {
            for (suggestion: Building in buildings) {
                if (buildingNumber.editText?.text.toString() == suggestion.name){
                    buildingNumber.error = null
                    buildingNumber.isErrorEnabled = false
                    return true
                }
            }
            buildingNumber.error = resources.getString(R.string.requiredNameOfBuildingFromList)
            return false
        }
    }

    /**
     * Validate room input field
     *
     * @return true = ok, false = problem (empty, too long)
     */
    private fun validateRoomNumber(): Boolean {
        val exhibitRoomNumberString: String = roomNumber.editText?.text.toString()

        if (exhibitRoomNumberString.isEmpty()) {
            roomNumber.error = resources.getString(R.string.requiredNameOfRoom)
            return false
        } else if (exhibitRoomNumberString.length > 50) {
            roomNumber.error = resources.getString(R.string.tooLong)
            return false
        } else {
            for (suggestion: Room in rooms) {
                if (roomNumber.editText?.text.toString() == suggestion.name){
                    roomNumber.error = null
                    roomNumber.isErrorEnabled = false
                    return true
                }
            }
            roomNumber.error = resources.getString(R.string.requiredNameOfRoomFromList)
            return false
        }
    }

    /**
     * Validate showcase input field
     *
     * @return true = ok, false = problem (empty, too long, not from list)
     */
    private fun validateShowCaseNumber(): Boolean {
        val exhibitShowCaseNumberString: String = showCaseNumber.editText?.text.toString()

        if (exhibitShowCaseNumberString.isEmpty()) {
            showCaseNumber.error = resources.getString(R.string.requiredNameOfShowCase)
            return false
        } else if (exhibitShowCaseNumberString.length > 50) {
            showCaseNumber.error = resources.getString(R.string.tooLong)
            return false
        } else {
            for (suggestion: Showcase in showcases) {
                if (showCaseNumber.editText?.text.toString() == suggestion.name){
                    showCaseNumber.error = null
                    showCaseNumber.isErrorEnabled = false
                    return true
                }
            }
            showCaseNumber.error = resources.getString(R.string.requiredNameOfShowcaseFromList)
            return false
        }
    }

    /**
     * Creates alert dialog to let user choose
     */
    private fun chooseProfilePicture() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
        val dialogView: View = inflater.inflate(R.layout.aler_dialog, null)

        builder.setCancelable(false)
        builder.setView(dialogView)

        val imageViewADPPCamera: ImageView = dialogView.findViewById(R.id.imageViewADPPCamera)
        val imageViewADPPGallery: ImageView = dialogView.findViewById(R.id.imageViewADPPGallery)
        val alertDialogProfilePicture: AlertDialog = builder.create()

        alertDialogProfilePicture.show()

        // Camera
        imageViewADPPCamera.setOnClickListener {
             takePictureFromCamera()
             alertDialogProfilePicture.dismiss()

        }
        // Gallery
        imageViewADPPGallery.setOnClickListener {
            takePictureFromGallery()
            alertDialogProfilePicture.dismiss()
        }
    }

    /**
     * Start new intent to open a gallery
     */
    private fun takePictureFromGallery() {
        val pickPhoto = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(pickPhoto, 1)
    }

    /**
     * Start new intent to open a camera
     */
    private fun takePictureFromCamera() {
        val takePicture = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePicture.resolveActivity(requireActivity().packageManager) != null) {
            startActivityForResult(takePicture, 2)
        }
    }

    /**
     * Catch activity result, might be both images
     *
     * @param requestCode requestCode
     * @param resultCode resultCode
     * @param data data from intent
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            // Camera
            1 -> if (resultCode == RESULT_OK) {
                // Info label
                if (code == 1){
                    val selectedImageUri = data?.data!!
                    imageInfoLabel.setImageURI(selectedImageUri)
                    val bitmap = when {
                        Build.VERSION.SDK_INT < 28 -> MediaStore.Images.Media.getBitmap(
                            requireActivity().contentResolver,
                            selectedImageUri
                        )
                        else -> {
                            val source = ImageDecoder.createSource(requireActivity().contentResolver, selectedImageUri)
                            ImageDecoder.decodeBitmap(source)
                        }
                    }
                    val baos = ByteArrayOutputStream()
                    bitmap?.compress(Bitmap.CompressFormat.JPEG,100,baos)
                    encodedImageInfo = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)

                } else {
                    // Image of exhibit
                    val selectedImageUri = data?.data!!
                    imageExhibit.setImageURI(selectedImageUri)
                    val bitmap = when {
                        Build.VERSION.SDK_INT < 28 -> MediaStore.Images.Media.getBitmap(
                            requireActivity().contentResolver,
                            selectedImageUri
                        )
                        else -> {
                            val source = ImageDecoder.createSource(requireActivity().contentResolver, selectedImageUri)
                            ImageDecoder.decodeBitmap(source)
                        }
                    }
                    val baos = ByteArrayOutputStream()
                    bitmap?.compress(Bitmap.CompressFormat.JPEG,100,baos)
                    encodedImageExhibit = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
                }

            }
            // Gallery
            2 -> if (resultCode == RESULT_OK) {
                // Info label
                if (code == 1){
                    val bundle = data?.extras
                    val bitmapImage = bundle!!["data"] as Bitmap?
                    imageInfoLabel.setImageBitmap(bitmapImage)
                    val baos = ByteArrayOutputStream()
                    bitmapImage?.compress(Bitmap.CompressFormat.JPEG,100,baos)
                    encodedImageInfo = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)

                } else {
                    // Image of exhibit
                    val bundle = data?.extras
                    val bitmapImage = bundle!!["data"] as Bitmap?
                    imageExhibit.setImageBitmap(bitmapImage)
                    val baos = ByteArrayOutputStream()
                    bitmapImage?.compress(Bitmap.CompressFormat.JPEG,100,baos)
                    encodedImageExhibit = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
                }

            }
        }
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