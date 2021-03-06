package pelikan.bp.pelikanj.ui.profile

import android.animation.Animator
import android.app.Activity
import android.content.Context
import android.graphics.Insets
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.textfield.TextInputLayout
import okhttp3.ResponseBody
import pelikan.bp.pelikanj.ApiClient
import pelikan.bp.pelikanj.R
import pelikan.bp.pelikanj.viewModels.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class RegistrationFragment : Fragment() {

    lateinit var emailInput: TextInputLayout
    lateinit var usernameInput: TextInputLayout
    lateinit var passwordInput: TextInputLayout
    lateinit var passwordAgainInput: TextInputLayout
    private lateinit var registrationButton: Button
    lateinit var animation: LottieAnimationView
    lateinit var animationF: LottieAnimationView
    lateinit var frameLayout: FrameLayout


    private lateinit var fromsmall: Animation
    private lateinit var fromnothing: Animation
    private lateinit var foricon: Animation
    lateinit var overbox: LinearLayout
    lateinit var card: LinearLayout
    private lateinit var cardF: LinearLayout
    private var navController: NavController ?= null

    private var heightOverbox: Int = 0

    private lateinit var scrollView: ScrollView

    /**
     * On create
     *
     * @param savedInstanceState savedInstanceState
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Removes back arrow
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
        val view: View = inflater.inflate(R.layout.fragment_registration, container, false)

        frameLayout = view.findViewById(R.id.registration_fragment)

        frameLayout.addView(inflater.inflate(R.layout.animation_success,null))
        frameLayout.addView(inflater.inflate(R.layout.animation_failed,null))

        initForm(view)

        setUpAnimation()

        heightOverbox = countHeight()

        registrationButton.setOnClickListener {
            if(checkFormValues()){
                hideKeyboard()
                sendRegistration()
            }
        }

        return view
    }

    /**
     * On view created
     * Only initialize navigation controller
     *
     * @param view view
     * @param savedInstanceState savedInstanceState
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
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

    /**
     * Uses retrofit to send registration request to server
     * Does animation after getting response
     */
    private fun sendRegistration() {
        val user = User(usernameInput.editText?.text.toString(),
                             passwordInput.editText?.text.toString(),
                             emailInput.editText?.text.toString())

        val client: Call<ResponseBody> = ApiClient.create().registerUser(user)

        client.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody?>,
                response: Response<ResponseBody?>
            ) {
                if (response.code() == 201){
                    doAnimation()
                    animation.addAnimatorListener(object : Animator.AnimatorListener {
                        override fun onAnimationStart(animation: Animator) {
                        }

                        override fun onAnimationEnd(animation: Animator) {
                            navController?.navigate(R.id.action_navigation_registration_to_navigation_profile)
                        }

                        override fun onAnimationCancel(animation: Animator) {
                        }

                        override fun onAnimationRepeat(animation: Animator) {
                        }
                    })
                } else {
                    // Wrong code
                    doAnimationFailed()
                    animationF.addAnimatorListener(object : Animator.AnimatorListener {
                        override fun onAnimationStart(animation: Animator) {
                        }
                        override fun onAnimationEnd(animation: Animator) {
                            emailInput.editText?.text = Editable.Factory.getInstance().newEditable("")
                            usernameInput.editText?.text = Editable.Factory.getInstance().newEditable("")
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

    /** Do successful animation */
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

    /** Do animation failed */
    private fun doAnimationFailed() {
        animationF.visibility = View.VISIBLE
        animationF.startAnimation(foricon)
        animationF.playAnimation()
        animationF.repeatCount = 1

        overbox.alpha = 1F
        overbox.startAnimation(fromnothing)

        cardF.alpha = 1F
        cardF.startAnimation(fromsmall)

        // set height of black "background"
        val params: ViewGroup.LayoutParams = overbox.layoutParams
        params.height = heightOverbox
        overbox.layoutParams = params

        scrollView.scrollY = 0
    }

    /** Set up animation and hide all components*/
    private fun setUpAnimation() {
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

    }


    /**
     * Init all xml tags
     *
     * @param view view
     */
    private fun initForm(view: View) {
        usernameInput = view.findViewById(R.id.username)
        emailInput = view.findViewById(R.id.email)
        passwordInput = view.findViewById(R.id.password)
        passwordAgainInput = view.findViewById(R.id.password_again)

        registrationButton = view.findViewById(R.id.register_button)

        animation = view.findViewById(R.id.animationSuccess)
        card = view.findViewById(R.id.popup)

        animationF = view.findViewById(R.id.animationFailed)
        cardF = view.findViewById(R.id.popup_failed)

        overbox = view.findViewById(R.id.overbox)

        scrollView = view.findViewById(R.id.registration_fragment_scrollView)

        val animationText: TextView = view.findViewById(R.id.animation_success_text)
        animationText.text = resources.getString(R.string.registration_successfull)

        val animationTextF: TextView = view.findViewById(R.id.animation_failed_text)
        animationTextF.text = resources.getString(R.string.registration_failed)
    }

    /**
     * Check form values
     *
     * @return true = ok, false = one or more are not valid
     */
    private fun checkFormValues():Boolean {
        if (!validateUsername() or !validateEmail() or !validatePassword() or !validatePasswordAgain()){
            return false
        }
        return true
    }

    /**
     * Validates input in username field
     *
     * @return true = ok, false = problem (empty, too long, too short)
     */
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

    /**
     * Validate input in email field
     *
     * @return true = ok, false = problem (empty, not a valid email adress)
     */
    private fun validateEmail(): Boolean{
        val emailString: String = emailInput.editText?.text.toString()
        val regex = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"

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

    /**
     * Validates input in password field
     *
     * @return true = ok, false = problem (empty, too long, too short, not matches regex -> at least one small and big char and a digit)
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
     * Validates input in password again field
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
    private fun Fragment.hideKeyboard() {
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