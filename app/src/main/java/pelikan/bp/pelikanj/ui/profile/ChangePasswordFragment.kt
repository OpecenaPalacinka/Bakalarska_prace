package pelikan.bp.pelikanj.ui.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.google.android.material.textfield.TextInputLayout
import pelikan.bp.pelikanj.R

class ChangePasswordFragment : Fragment() {

    lateinit var passwordInput: TextInputLayout
    lateinit var passwordAgainInput: TextInputLayout
    lateinit var changePasswordButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_change_password, container, false)

        initForm(view)

        changePasswordButton.setOnClickListener {
            if (checkFormValues()){
                //do something
            }
        }

        return view
    }

    private fun initForm(view: View) {
        passwordInput = view.findViewById(R.id.password)
        passwordAgainInput = view.findViewById(R.id.password_again)

        changePasswordButton = view.findViewById(R.id.change_password_button)
    }

    private fun checkFormValues():Boolean {
        if (!validatePassword() or !validatePasswordAgain()){
            return false
        }
        return true
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