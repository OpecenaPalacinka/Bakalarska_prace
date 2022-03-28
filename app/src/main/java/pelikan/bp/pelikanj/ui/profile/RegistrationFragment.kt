package pelikan.bp.pelikanj.ui.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import pelikan.bp.pelikanj.R


class RegistrationFragment : Fragment() {

    lateinit var emailInput: TextInputLayout
    lateinit var usernameInput: TextInputLayout
    lateinit var passwordInput: TextInputLayout
    lateinit var passwordAgainInput: TextInputLayout
    lateinit var registrationButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view: View = inflater.inflate(R.layout.fragment_registration, container, false)

        initForm(view)

        registrationButton.setOnClickListener { checkFormValues() }

        return view
    }


    private fun initForm(view: View) {
        usernameInput = view.findViewById(R.id.username)
        emailInput = view.findViewById(R.id.email)
        passwordInput = view.findViewById(R.id.password)
        passwordAgainInput = view.findViewById(R.id.password_again)

        registrationButton = view.findViewById(R.id.register_button)
    }

    private fun checkFormValues():Boolean {
        if (!validateUsername() or !validateEmail() or !validatePassword() or !validatePasswordAgain()){
            return false
        }
        return true
    }

    private fun validateUsername(): Boolean {
        val usernameString: String = usernameInput.editText?.text.toString()

        if (usernameString.isEmpty()) {
            usernameInput.error = resources.getString(R.string.requiredUsername)
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

    private fun validateEmail(): Boolean{
        val emailString: String = emailInput.editText?.text.toString()
        val regex: String = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"

        if (emailString.isEmpty()){
            emailInput.error = resources.getString(R.string.requiredEmail)
            return false
        } else if (!emailString.matches(regex.toRegex())) {
                emailInput.error = resources.getString(R.string.notMatchesEmail)
                return false
        } else {
            emailInput.error = null
            emailInput.isErrorEnabled = false
            return true
        }
    }

    private fun validatePassword(): Boolean {
        val passwordString: String = passwordInput.editText?.text.toString()

        if (passwordString.isEmpty()) {
            passwordInput.error = resources.getString(R.string.requiredPassword)
            return false
        } else if (passwordString.length > 50) {
            passwordInput.error = resources.getString(R.string.tooLong)
            return false
        } else if (passwordString.length < 8) {
            passwordInput.error = resources.getString(R.string.tooShort)
            return false
        } else if (!passwordString.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*\$".toRegex())){
            passwordInput.error = resources.getString(R.string.strongerPassword)
            return false
        } else {
            passwordInput.error = null
            passwordInput.isErrorEnabled = false
            return true
        }
    }

    private fun validatePasswordAgain(): Boolean {
        val passwordAgainString: String = passwordAgainInput.editText?.text.toString()

        if (passwordAgainString.isEmpty()){
            passwordAgainInput.error = resources.getString(R.string.requiredPassword)
            return false
        } else if (passwordAgainString != passwordInput.editText?.text.toString()){
            passwordAgainInput.error = resources.getString(R.string.passwordDoesntMatch)
            passwordAgainInput.errorIconDrawable = null
            return false
        } else {
            passwordAgainInput.error = null
            passwordAgainInput.isErrorEnabled = false
            return true
        }
    }

}