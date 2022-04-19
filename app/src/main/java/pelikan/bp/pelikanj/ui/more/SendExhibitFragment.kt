package pelikan.bp.pelikanj.ui.more

import android.animation.Animator
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
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

    private var infoLabelImageUri: Uri? = null
    private var exhibitImageUri: Uri? = null
    private var encodedImageInfo: String = ""
    private var encodedImageExhibit: String = ""

    lateinit var animation: LottieAnimationView
    lateinit var animationF: LottieAnimationView
    lateinit var frameLayout: FrameLayout

    private lateinit var fromsmall: Animation
    private lateinit var fromnothing: Animation
    private lateinit var foricon: Animation
    lateinit var overbox: LinearLayout
    lateinit var card: LinearLayout
    lateinit var cardF: LinearLayout

    private lateinit var dbClient: DBClient

    private var institutions: ArrayList<InstitutionsModelItem> = ArrayList()
    private var institutionsName: ArrayList<String> = ArrayList()
    private var buildings: ArrayList<Building> = ArrayList()
    private var rooms: ArrayList<Room> = ArrayList()
    private var showcases: ArrayList<Showcase> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, SavedInstanceState: Bundle?): View {
        val view: View = inflater.inflate(R.layout.fragment_send_exhibit, container, false)

        frameLayout = view.findViewById(R.id.send_exhibit_fragment)

        frameLayout.addView(inflater.inflate(R.layout.animation_success,null))
        frameLayout.addView(inflater.inflate(R.layout.animation_failed,null))

        dbClient = DBClient(requireContext())

        initForm(view)

        setUpAnimation()

        setUpListeners()

        fillAutocomplete(view)

        infoLabelButton.setOnClickListener {
            openImageChooser(42)
        }

        exhibitButton.setOnClickListener {
            openImageChooser(7)
        }

        sendExhibitButton.setOnClickListener {
            if(checkRequiredValues()){
                sendExhibit()
            }
        }


        return view
    }

    private fun setUpListeners() {
        var idOfBuilding: Int
        var idOfRoom: Int

        autoCompleteTextView.setOnItemClickListener { parent, _, position, _ ->
            autoCompleteBuildings.editableText.clear()
            buildings.clear()
            autoCompleteRooms.editableText.clear()
            rooms.clear()
            autoCompleteShowCases.editableText.clear()
            showcases.clear()
            val item = parent.getItemAtPosition(position)
            val idOfInstitution = getInstitutionIdByName(item.toString())
            if (idOfInstitution != -1){
                getAllBuildingsByInstitutionId(idOfInstitution)
            } else {
                Log.d(tag,"Cannot find institution")
            }

        }

        autoCompleteBuildings.setOnItemClickListener { parent, _, position, _ ->
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

    private fun getAllBuildingsByInstitutionId(idOfInstitution: Int) {
        val client: Call<List<Building>> = ApiClient.create().getBuildings(idOfInstitution)

        client.enqueue(object : Callback<List<Building>> {
            override fun onResponse(
                call: Call<List<Building>>,
                response: Response<List<Building>>
            ) {
                if (response.code() == 200){
                    // Show translation
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

    private fun getAllRoomsByBuildingId(idOfBuilding: Int) {
        val client: Call<List<Room>> = ApiClient.create().getRooms(idOfBuilding)

        client.enqueue(object : Callback<List<Room>> {
            override fun onResponse(
                call: Call<List<Room>>,
                response: Response<List<Room>>
            ) {
                if (response.code() == 200){
                    // Show translation
                    for (resp: Room in response.body()!!){
                        rooms.add(resp)
                    }
                    fillAutocompleteRooms()
                } else {
                    Log.d(tag,"Cannot get buildings")
                }

            }

            override fun onFailure(call: Call<List<Room>>, t: Throwable) {
                Log.println(Log.ERROR,tag,t.toString())
            }
        })
    }

    private fun getAllShowcasesByRoomId(idOfRoom: Int) {
        val client: Call<List<Showcase>> = ApiClient.create().getShowcases(idOfRoom)

        client.enqueue(object : Callback<List<Showcase>> {
            override fun onResponse(
                call: Call<List<Showcase>>,
                response: Response<List<Showcase>>
            ) {
                if (response.code() == 200){
                    // Show translation
                    for (resp: Showcase in response.body()!!){
                        showcases.add(resp)
                    }
                    fillAutocompleteShowcases()
                } else {
                    Log.d(tag,"Cannot get buildings")
                }

            }

            override fun onFailure(call: Call<List<Showcase>>, t: Throwable) {
                Log.println(Log.ERROR,tag,t.toString())
            }
        })
    }

    private fun openImageChooser(flag: Int) {
        val chooseImageIntent = ImagePicker.getPickImageIntent(requireView().context)
        startActivityForResult(chooseImageIntent, flag)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                42 -> {
                    infoLabelImageUri = ImagePicker.getImageFromResult(requireView().context,resultCode,data)
                    imageInfoLabel.setImageURI(infoLabelImageUri)
                    val bm: Bitmap? = ImagePicker.getBitmapFromResult(requireView().context,resultCode,data)
                    val baos = ByteArrayOutputStream()
                    bm?.compress(Bitmap.CompressFormat.JPEG,100,baos)
                    encodedImageInfo = Base64.encodeToString(baos.toByteArray(),Base64.NO_WRAP)
                }
                7 -> {
                    exhibitImageUri = ImagePicker.getImageFromResult(requireView().context,resultCode,data)
                    imageExhibit.setImageURI(exhibitImageUri)
                    val bm: Bitmap? = ImagePicker.getBitmapFromResult(requireView().context,resultCode,data)
                    val baos = ByteArrayOutputStream()
                    bm?.compress(Bitmap.CompressFormat.JPEG,100,baos)
                    encodedImageExhibit = Base64.encodeToString(baos.toByteArray(),Base64.NO_WRAP)
                }
            }
        }
    }

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

    private fun fillAutocomplete(view: View){

        institutions = dbClient.getAllInstitutions()

        for (institution: InstitutionsModelItem in institutions){
            institutionsName.add(institution.name)
        }

        val adapter: ArrayAdapter<String> = ArrayAdapter(view.context,android.R.layout.simple_spinner_dropdown_item,institutionsName)

        autoCompleteTextView.setAdapter(adapter)
    }

    private fun fillAutocompleteBuildings() {
        val buildingsName = ArrayList<String>()

        for (building: Building in buildings){
            buildingsName.add(building.name)
        }

        val adapter: ArrayAdapter<String> =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, buildingsName)

        autoCompleteBuildings.setAdapter(adapter)
    }

    private fun fillAutocompleteRooms() {
        val roomsName = ArrayList<String>()

        for (room: Room in rooms){

                roomsName.add(room.name)

        }

        val adapter: ArrayAdapter<String> =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, roomsName)

        autoCompleteRooms.setAdapter(adapter)
    }

    private fun fillAutocompleteShowcases() {
        val showcasesName = ArrayList<String>()

        for (showcase: Showcase in showcases) {

                showcasesName.add(showcase.name)
        }

        val adapter: ArrayAdapter<String> =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, showcasesName)

        autoCompleteShowCases.setAdapter(adapter)
    }

    private fun sendExhibit() {

        val exhibitNameS = exhibitName.editText?.text.toString()
        val infoLabel: String = exhibitInfoLabel.editText?.text.toString().ifEmpty {
            ""
        }

        val instiName = institution.editText?.text.toString()
        val instiId = getInstitutionIdByName(instiName)
        val buildingName = getBuildingIdByName(buildingNumber.editText?.text.toString()).toString()
        val roomName = getRoomIdByName(roomNumber.editText?.text.toString()).toString()
        val showCaseName = getShowcaseIdByName(showCaseNumber.editText?.text.toString()).toString()
        lateinit var exhibitItemWithExhibitImage: ExhibitItemWithExhibitImage
        lateinit var exhibitItemWithoutExhibitImage: ExhibitItemWithoutExhibitImage
        lateinit var client: Call<ResponseBody>

        if (encodedImageExhibit == ""){
            // Bez obrázku
            exhibitItemWithoutExhibitImage = ExhibitItemWithoutExhibitImage(exhibitNameS,infoLabel,encodedImageInfo,
                                                            buildingName,roomName,showCaseName)
            client = ApiClient.create().uploadNewExhibit(instiId,exhibitItemWithoutExhibitImage)
        } else {
            // S obrázkem
            exhibitItemWithExhibitImage = ExhibitItemWithExhibitImage(exhibitNameS,encodedImageExhibit,infoLabel,encodedImageInfo,
                                                            buildingName, roomName, showCaseName)
            client = ApiClient.create().uploadNewExhibitWithExhibitImage(instiId,exhibitItemWithExhibitImage)
        }

        client.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody?>,
                response: Response<ResponseBody>
            ) {
                //Check if correct
                if (response.code() == 201){
                    // Correct
                    doAnimation()
                    animation.addAnimatorListener(object : Animator.AnimatorListener {
                        override fun onAnimationStart(animation: Animator) {
                        }

                        override fun onAnimationEnd(animations: Animator) {
                            resetForm()
                            // reset animation possition
                            card.y -= scrollView.scrollY
                            animation.y -= scrollView.scrollY
                            setUpAnimation()
                        }

                        override fun onAnimationCancel(animation: Animator) {
                        }

                        override fun onAnimationRepeat(animation: Animator) {
                        }
                    })
                } else {
                    // Chyba
                    doAnimationFailed()
                    animationF.addAnimatorListener(object : Animator.AnimatorListener {
                        override fun onAnimationStart(animation: Animator) {
                        }

                        override fun onAnimationEnd(animations: Animator) {
                            // reset animation possition
                            cardF.y -= scrollView.scrollY
                            animationF.y -= scrollView.scrollY
                            setUpAnimation()
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

    private fun getShowcaseIdByName(showcaseName: String): Int {
        for (showcase: Showcase in showcases){
            if (showcase.name == showcaseName){
                return showcase.showcaseId
            }
        }
        return -1
    }

    private fun getRoomIdByName(roomName: String): Int {
        for (room: Room in rooms){
            if (room.name == roomName){
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

    private fun doAnimation() {
        animation.visibility = View.VISIBLE
        animation.y += scrollView.scrollY.toFloat()
        animation.startAnimation(foricon)
        animation.playAnimation()
        animation.repeatCount = 1

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
        animation.visibility = View.GONE
        animationF.visibility = View.GONE

    }

    private fun checkRequiredValues(): Boolean {
        if (!validateExhibitName() or !validateExhibitInfoLabel()
            or !validateInstitution() or !validateBuildingNumber()
            or !validateRoomNumber() or !validateShowCaseNumber()
            or !validateImageInfoLabel()
        ) {
            return false
        }
        return true
    }

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

    private fun validateImageInfoLabel(): Boolean {

        if (infoLabelImageUri == null) {
            infoLabelButton.error = resources.getString(R.string.requiredInfoLabelImage)
            return false
        } else {
            infoLabelButton.error = null
            return true
        }
    }

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

    private fun validateBuildingNumber(): Boolean {
        val exhibitBuildingNumberString: String = buildingNumber.editText?.text.toString()

        if (exhibitBuildingNumberString.isEmpty()) {
            buildingNumber.error = resources.getString(R.string.requiredNameOfBuilding)
            return false
        } else if (exhibitBuildingNumberString.length > 50) {
            buildingNumber.error = resources.getString(R.string.tooLong)
            return false
        } else {
            buildingNumber.error = null
            buildingNumber.isErrorEnabled = false
            return true
        }
    }

    private fun validateRoomNumber(): Boolean {
        val exhibitRoomNumberString: String = roomNumber.editText?.text.toString()

        if (exhibitRoomNumberString.isEmpty()) {
            roomNumber.error = resources.getString(R.string.requiredNameOfRoom)
            return false
        } else if (exhibitRoomNumberString.length > 50) {
            roomNumber.error = resources.getString(R.string.tooLong)
            return false
        } else {
            roomNumber.error = null
            roomNumber.isErrorEnabled = false
            return true
        }
    }

    private fun validateShowCaseNumber(): Boolean {
        val exhibitShowCaseNumberString: String = showCaseNumber.editText?.text.toString()

        if (exhibitShowCaseNumberString.isEmpty()) {
            showCaseNumber.error = resources.getString(R.string.requiredNameOfShowCase)
            return false
        } else if (exhibitShowCaseNumberString.length > 50) {
            showCaseNumber.error = resources.getString(R.string.tooLong)
            return false
        } else {
            showCaseNumber.error = null
            showCaseNumber.isErrorEnabled = false
            return true
        }
    }

    companion object {
    }

}