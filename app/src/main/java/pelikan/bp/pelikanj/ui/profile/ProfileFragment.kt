package pelikan.bp.pelikanj.ui.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.fragment_profile.*
import pelikan.bp.pelikanj.R

class ProfileFragment : Fragment() {

    var navControler: NavController ?= null

    lateinit var usernameInput: TextInputLayout
    lateinit var passwordInput: TextInputLayout
    lateinit var buttonRegister: Button
    lateinit var buttonLogin: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view: View = inflater.inflate(R.layout.fragment_profile, container, false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navControler = Navigation.findNavController(view)

        initForm(view)

        buttonLogin.setOnClickListener{
            if (checkValues()){
                navControler?.navigate(R.id.action_navigation_profile_to_navigation_logged_user)
            }
        }

        buttonRegister.setOnClickListener{
            navControler?.navigate(R.id.action_navigation_profile_to_navigation_registration)
        }


    }

    private fun initForm(view: View) {
        usernameInput = view.findViewById(R.id.username)
        passwordInput = view.findViewById(R.id.password)
        buttonRegister = view.findViewById(R.id.register_button)
        buttonLogin = view.findViewById(R.id.signin_button)
    }

    private fun checkValues():Boolean {
        if (!validateUsername() or !validatePassword()) {
            return false
        }
        return true
    }


    private fun validateUsername(): Boolean {
        val usernameString: String = usernameInput.editText?.text.toString()

        if (usernameString.isEmpty()) {
            usernameInput.error = resources.getString(R.string.requiredNameOfExhibit)
            return false
        } else if (usernameString.length > 30) {
            usernameInput.error = resources.getString(R.string.tooLong)
            return false
        } else if (usernameString.length < 3) {
            usernameInput.error = resources.getString(R.string.tooShort)
            return false
        } else {
            usernameInput.error = null
            usernameInput.isErrorEnabled = false
            return true
        }
    }

    private fun validatePassword(): Boolean {
        val passwordString: String = passwordInput.editText?.text.toString()

        if (passwordString.isEmpty()) {
            passwordInput.error = resources.getString(R.string.requiredNameOfExhibit)
            return false
        } else if (passwordString.length > 50) {
            passwordInput.error = resources.getString(R.string.tooLong)
            return false
        } else if (passwordString.length < 8) {
            passwordInput.error = resources.getString(R.string.tooShort)
            return false
        } else {
            passwordInput.error = null
            passwordInput.isErrorEnabled = false
            return true
        }
    }


}