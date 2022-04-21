package pelikan.bp.pelikanj.ui.profile

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.gson.Gson
import com.mikhaellopez.circularimageview.CircularImageView
import pelikan.bp.pelikanj.DBClient
import pelikan.bp.pelikanj.R
import pelikan.bp.pelikanj.ui.more.ImagePicker
import pelikan.bp.pelikanj.viewModels.User
import java.io.ByteArrayOutputStream


class LoggedUserFragment : Fragment() {

    private var navControler: NavController ?= null

    private lateinit var username: TextView
    private lateinit var email: TextView
    private lateinit var profilePicture: CircularImageView

    private lateinit var changePassword: LinearLayout
    private lateinit var changeProfilePicture: LinearLayout
    private lateinit var favouriteInstitutions: LinearLayout
    private lateinit var changeLanguage: LinearLayout
    private lateinit var logout: LinearLayout

    private lateinit var dbClient: DBClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view: View = inflater.inflate(R.layout.fragment_logged_user, container, false)

        dbClient = DBClient(requireContext())

        initLayout(view)

        setUpListeners()

        setUserInfo()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navControler = Navigation.findNavController(view)

    }

    private fun setUpListeners() {

        changePassword.setOnClickListener {
            navControler?.navigate(R.id.action_navigation_logged_user_to_change_password)
        }

        changeProfilePicture.setOnClickListener {
            openImageChooser()
        }

        favouriteInstitutions.setOnClickListener {

        }

        changeLanguage.setOnClickListener {
            navControler?.navigate(R.id.action_navigation_logged_user_to_language_settings)
        }

        logout.setOnClickListener{
            dbClient.updateToken(null)
            navControler?.navigate(R.id.action_navigation_logged_user_to_navigation_profile)
        }
    }

    private fun setUserInfo(){
        val userData = dbClient.getAllUserData()
        val token = userData?.token
        val profilePic = userData?.profilePicture

        if (token!= null){
            val onlyToken = token.substring(7, token.length)
            val tokens = onlyToken.split(".")[1]
            val jwt = Base64.decode(tokens, Base64.DEFAULT)
            val info = String(jwt, Charsets.UTF_8)
            val authors = Gson().fromJson(info, User::class.java)

            username.text = authors.username
            email.text = authors.email
        }

        if (profilePic != null){
            val decodedString: ByteArray = Base64.decode(profilePic, Base64.DEFAULT)
            val decodedByte =
                BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
            profilePicture.setImageBitmap(decodedByte)
        }

    }

    private fun initLayout(view: View) {
        changePassword = view.findViewById(R.id.change_password)
        changeProfilePicture = view.findViewById(R.id.change_profil_picture)
        favouriteInstitutions = view.findViewById(R.id.favourite_institutions)
        changeLanguage = view.findViewById(R.id.language_settings)
        logout = view.findViewById(R.id.logout)

        username = view.findViewById(R.id.username)
        email = view.findViewById(R.id.email)
        profilePicture = view.findViewById(R.id.imageview_profile)
    }

    private fun openImageChooser() {
        val chooseImageIntent = ImagePicker.getPickImageIntent()
        startActivityForResult(chooseImageIntent, 42)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                42 -> {
                    val bm: Bitmap? = ImagePicker.getBitmapFromResult(requireView().context,resultCode,data)
                    val baos = ByteArrayOutputStream()
                    bm?.compress(Bitmap.CompressFormat.JPEG,100,baos)
                    val image = Base64.encodeToString(baos.toByteArray(),Base64.NO_WRAP)
                    dbClient.updateProfilePicture(image)
                    val decodedString: ByteArray = Base64.decode(image, Base64.DEFAULT)
                    val decodedByte =
                        BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                    profilePicture.setImageBitmap(decodedByte)
                }
            }
        }
    }

}