package pelikan.bp.pelikanj.ui.profile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_logged_user.*
import kotlinx.android.synthetic.main.fragment_more.*
import pelikan.bp.pelikanj.R
import pelikan.bp.pelikanj.ui.more.ImagePicker

class LoggedUserFragment : Fragment() {

    var navControler: NavController ?= null

    lateinit var changePassword: LinearLayout
    lateinit var changeProfilePicture: LinearLayout
    lateinit var favouriteInstitutions: LinearLayout
    lateinit var changeLanguage: LinearLayout
    lateinit var logout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_logged_user, container, false)

        initLayout(view)


        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navControler = Navigation.findNavController(view)

        setUpListeners()


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
            navControler?.navigate(R.id.action_navigation_logged_user_to_navigation_profile)
        }
    }

    private fun initLayout(view: View) {
        changePassword = view.findViewById(R.id.change_password)
        changeProfilePicture = view.findViewById(R.id.change_profil_picture)
        favouriteInstitutions = view.findViewById(R.id.favourite_institutions)
        changeLanguage = view.findViewById(R.id.language_settings)
        logout = view.findViewById(R.id.logout)
    }

    private fun openImageChooser() {
        val chooseImageIntent = ImagePicker.getPickImageIntent(requireView().context)
        startActivityForResult(chooseImageIntent, 42)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                42 -> {
                    Toast.makeText(requireView().context,ImagePicker.getImageFromResult(requireView().context,resultCode,data).toString()+"",Toast.LENGTH_LONG).show()
                }
            }
        }
    }

}