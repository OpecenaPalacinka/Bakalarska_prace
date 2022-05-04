package pelikan.bp.pelikanj.ui.profile

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.gson.Gson
import com.mikhaellopez.circularimageview.CircularImageView
import pelikan.bp.pelikanj.DBClient
import pelikan.bp.pelikanj.R
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

    /**
     * On create
     *
     * @param savedInstanceState savedInstanceState
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Hides the back arrow on top bar
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    /**
     * On create view
     *
     * @param inflater inflater
     * @param container container
     * @param savedInstanceState savedInstanceState
     * @return view
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view: View = inflater.inflate(R.layout.fragment_logged_user, container, false)

        dbClient = DBClient(requireContext())

        // Hides the back arrow on top bar, in some cases works this line of code
        // in some works the line in onCreate() function
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)

        initLayout(view)

        setUpListeners()

        setUserInfo()

        return view
    }

    /**
     * On view created
     * Only to init navigation controller
     *
     * @param view view
     * @param savedInstanceState savedInstanceState
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navControler = Navigation.findNavController(view)
    }

    /**
     * Prepare listeners for all clickable texts
     */
    private fun setUpListeners() {

        changePassword.setOnClickListener {
            navControler?.navigate(R.id.action_navigation_logged_user_to_change_password)
        }

        changeProfilePicture.setOnClickListener {
            chooseProfilePicture()
        }

        favouriteInstitutions.setOnClickListener {
            //not yet implemented
            Toast.makeText(context,"Není zatím implementováno",Toast.LENGTH_LONG).show()
        }

        changeLanguage.setOnClickListener {
            navControler?.navigate(R.id.action_navigation_logged_user_to_language_settings)
        }

        logout.setOnClickListener{
            dbClient.updateToken(null)
            navControler?.navigate(R.id.action_navigation_logged_user_to_navigation_profile)
        }
    }

    /**
     * Set up the user info (profile pic, username and email)
     */
    private fun setUserInfo() {
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
            // Decode encoded image
            val decodedString: ByteArray = Base64.decode(profilePic, Base64.DEFAULT)
            val decodedByte =
                BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
            profilePicture.setImageBitmap(decodedByte)
        }

    }

    /**
     * Init all xml tags
     *
     * @param view view
     */
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

    /**
     * Create an alert dialog for user to choose
     */
    private fun chooseProfilePicture() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
        val dialogView: View = inflater.inflate(R.layout.aler_dialog, null)

        builder.setCancelable(true)
        builder.setView(dialogView)

        val imageViewADPPCamera: ImageView = dialogView.findViewById(R.id.imageViewADPPCamera)
        val imageViewADPPGallery: ImageView = dialogView.findViewById(R.id.imageViewADPPGallery)
        val alertDialogProfilePicture: AlertDialog = builder.create()

        alertDialogProfilePicture.show()

        // Camera chose
        imageViewADPPCamera.setOnClickListener {
            takePictureFromCamera()
            alertDialogProfilePicture.dismiss()

        }

        // Gallery chose
        imageViewADPPGallery.setOnClickListener {
            takePictureFromGallery()
            alertDialogProfilePicture.dismiss()
        }
    }

    /**
     * Create an intent to open a gallery
     */
    private fun takePictureFromGallery() {
        val pickPhoto = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(pickPhoto, 1)
    }

    /**
     * Create an intent to open a camera
     */
    private fun takePictureFromCamera() {
        val takePicture = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePicture.resolveActivity(requireActivity().packageManager) != null) {
            startActivityForResult(takePicture, 2)
        }
    }

    /**
     * Catch activity result and update user profile picture
     * Way of updating depends on resultCode
     *
     * @param requestCode requestCode
     * @param resultCode resultCode
     * @param data data from intent
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            // CAMERA
            1 -> if (resultCode == Activity.RESULT_OK) {
                val selectedImageUri = data?.data!!
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
                val image = Base64.encodeToString(baos.toByteArray(),Base64.NO_WRAP)
                dbClient.updateProfilePicture(image)
                val decodedString: ByteArray = Base64.decode(image, Base64.DEFAULT)
                val decodedByte =
                    BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                profilePicture.setImageBitmap(decodedByte)

            }
            // GALLERY
            2 -> if (resultCode == Activity.RESULT_OK) {

                val bundle = data?.extras
                val bitmapImage = bundle!!["data"] as Bitmap?
                val baos = ByteArrayOutputStream()
                bitmapImage?.compress(Bitmap.CompressFormat.JPEG,100,baos)
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