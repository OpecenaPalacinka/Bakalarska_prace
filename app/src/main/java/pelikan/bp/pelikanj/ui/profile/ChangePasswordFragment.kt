package pelikan.bp.pelikanj.ui.profile

import android.animation.Animator
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.textfield.TextInputLayout
import okhttp3.ResponseBody
import pelikan.bp.pelikanj.ApiClient
import pelikan.bp.pelikanj.DBClient
import pelikan.bp.pelikanj.R
import pelikan.bp.pelikanj.viewModels.PasswordModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChangePasswordFragment : Fragment() {

    lateinit var passwordInput: TextInputLayout
    lateinit var passwordAgainInput: TextInputLayout
    private lateinit var changePasswordButton: Button

    lateinit var animation: LottieAnimationView
    lateinit var animationF: LottieAnimationView
    lateinit var frameLayout: FrameLayout


    private lateinit var fromsmall: Animation
    private lateinit var fromnothing: Animation
    private lateinit var foricon: Animation
    lateinit var overbox: LinearLayout
    private lateinit var card: LinearLayout
    private lateinit var cardF: LinearLayout

    private lateinit var dbClient: DBClient
    private var navController: NavController?= null

    /**
     * On create
     *
     * @param savedInstanceState savedInstanceState
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Remove back arrow on top bar
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    /**
     * On create view
     *
     * @param inflater inflater
     * @param container container
     * @param savedInstanceState savedInstanceState
     * @return
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view: View = inflater.inflate(R.layout.fragment_change_password, container, false)

        frameLayout = view.findViewById(R.id.change_password_fragment)

        frameLayout.addView(inflater.inflate(R.layout.animation_success,null))
        frameLayout.addView(inflater.inflate(R.layout.animation_failed,null))

        initForm(view)

        setUpAnimation()

        dbClient = DBClient(requireContext())

        changePasswordButton.setOnClickListener {
            if (checkFormValues()){
                updatePassword()
            }
        }

        return view
    }

    /**
     * On view created (init nav controller)
     *
     * @param view view
     * @param savedInstanceState savedInstanceState
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
    }

    /**
     * Get user token
     *
     * @return
     */
    private fun getTokenForChange(): String {
        val userData = dbClient.getAllUserData()

        return userData?.token!!
    }

    /**
     * Send request to server
     */
    private fun updatePassword(){

        val password = PasswordModel(passwordInput.editText?.text.toString())

        val token = getTokenForChange()

        // This request need and authorization so token is needed
        val client: Call<ResponseBody> = ApiClient.create().updatePassword(token,password)

        client.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                hideKeyboard()

                if (response.code() == 200){
                    // OK
                    doAnimation()
                    animation.addAnimatorListener(object : Animator.AnimatorListener {
                        override fun onAnimationStart(animation: Animator) {
                        }
                        override fun onAnimationEnd(animation: Animator) {
                            navController?.navigate(R.id.action_change_password_to_navigation_profile)
                        }
                        override fun onAnimationCancel(animation: Animator) {
                        }
                        override fun onAnimationRepeat(animation: Animator) {
                        }
                    })

                } else {
                    // Wrong credential
                    doAnimationFailed()
                    animationF.addAnimatorListener(object : Animator.AnimatorListener {
                        override fun onAnimationStart(animation: Animator) {
                        }
                        override fun onAnimationEnd(animation: Animator) {
                            passwordInput.editText?.text = Editable.Factory.getInstance().newEditable("")
                            passwordAgainInput.editText?.text = Editable.Factory.getInstance().newEditable("")
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
                Log.println(Log.ERROR,tag,t.toString())
            }
        })
    }

    /**
     * Init all xml tags
     *
     * @param view view
     */
    private fun initForm(view: View) {
        passwordInput = view.findViewById(R.id.password)
        passwordAgainInput = view.findViewById(R.id.password_again)

        changePasswordButton = view.findViewById(R.id.change_password_button)

        animation = view.findViewById(R.id.animationSuccess)
        card = view.findViewById(R.id.popup)

        animationF = view.findViewById(R.id.animationFailed)
        cardF = view.findViewById(R.id.popup_failed)

        overbox = view.findViewById(R.id.overbox)

        val animationText: TextView = view.findViewById(R.id.animation_success_text)
        animationText.text = resources.getString(R.string.change_password_successfull)

        val animationTextF: TextView = view.findViewById(R.id.animation_failed_text)
        animationTextF.text = resources.getString(R.string.change_password_failed)
    }

    /**
     * When password is changed successfully
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
    }

    /**
     * When change of password is failed (should not happen)
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
    }

    /**
     * Set ups animations
     */
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

    /**
     * Check if form is correctly filled
     *
     * @return true = ok, false = one or more fields are not valid
     */
    private fun checkFormValues():Boolean {
        if (!validatePassword() or !validatePasswordAgain()){
            return false
        }
        return true
    }

    /**
     * Validate password
     *
     * @return true = ok, false = problem (empty, too long, too short, does not match regex)
     */
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

    /**
     * Validate password "again"
     *
     * @return true = ok, false = problem (empty, not equals)
     */
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