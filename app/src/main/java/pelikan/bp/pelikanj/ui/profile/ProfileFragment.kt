package pelikan.bp.pelikanj.ui.profile

import android.animation.Animator
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.util.Base64
import android.util.Log
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
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import pelikan.bp.pelikanj.ApiClient
import pelikan.bp.pelikanj.DBClient
import pelikan.bp.pelikanj.R
import pelikan.bp.pelikanj.viewModels.TokenData
import pelikan.bp.pelikanj.viewModels.TokenModel
import pelikan.bp.pelikanj.viewModels.UserLogin
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ProfileFragment : Fragment() {

    var navControler: NavController ?= null

    lateinit var usernameInput: TextInputLayout
    lateinit var passwordInput: TextInputLayout
    lateinit var buttonRegister: Button
    lateinit var buttonLogin: Button

    lateinit var animationF: LottieAnimationView
    lateinit var frameLayout: FrameLayout
    lateinit var fromsmall: Animation
    lateinit var fromnothing: Animation
    lateinit var foricon: Animation
    lateinit var overbox: LinearLayout
    lateinit var cardF: LinearLayout

    lateinit var dbClient: DBClient


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view: View = inflater.inflate(R.layout.fragment_profile, container, false)

        frameLayout = view.findViewById(R.id.profile_fragment)

        frameLayout.addView(inflater.inflate(R.layout.animation_failed,null))

        dbClient = DBClient(requireContext())

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navControler = Navigation.findNavController(view)

        val userData = dbClient.getAllUserData()

        if (userData?.token != null){

            val onlyToken = userData.token!!.substring(7, userData.token!!.length)
            val tokens = onlyToken.split(".")[1]
            val jwt = Base64.decode(tokens, Base64.DEFAULT)
            val info = String(jwt, Charsets.UTF_8)
            val authors = Gson().fromJson(info, TokenData::class.java)

            // Not expired
            if (authors.exp >= (System.currentTimeMillis() / 1000).toInt()){
                navControler?.navigate(R.id.action_navigation_profile_to_navigation_logged_user)
            }

        }

        initForm(view)

        setUpAnimation()

        buttonLogin.setOnClickListener{
            if (checkValues()){
                loginUser()
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

        animationF = view.findViewById(R.id.animationFailed)
        cardF = view.findViewById(R.id.popup_failed)

        overbox = view.findViewById(R.id.overbox)

        val animationTextF: TextView = view.findViewById(R.id.animation_failed_text)
        animationTextF.text = resources.getString(R.string.login_failed)
    }

    private fun loginUser(){
        val userN = usernameInput.editText?.text.toString()
        val userP = passwordInput.editText?.text.toString()

        val user = UserLogin(userN,userP)

        val client: Call<TokenModel> = ApiClient.create().loginUser(user)

        client.enqueue(object : Callback<TokenModel> {
            override fun onResponse(
                call: Call<TokenModel>,
                response: Response<TokenModel>
            ) {
                hideKeyboard()
                if (response.code() == 200){
                    // OK
                    val fullToken = response.body()?.token!!

                    dbClient.updateToken(fullToken)

                    navControler?.navigate(R.id.action_navigation_profile_to_navigation_logged_user)
                } else {
                    // Wrong credentials
                        doAnimationFailed()
                    animationF.addAnimatorListener(object : Animator.AnimatorListener {
                        override fun onAnimationStart(animation: Animator) {
                            usernameInput.isFocusableInTouchMode = false
                            usernameInput.isFocusable = false
                            passwordInput.isFocusableInTouchMode = false
                            passwordInput.isFocusable = false
                        }

                        override fun onAnimationEnd(animation: Animator) {
                            usernameInput.editText?.text = Editable.Factory.getInstance().newEditable("")
                            passwordInput.editText?.text = Editable.Factory.getInstance().newEditable("")

                            usernameInput.isFocusableInTouchMode = true
                            usernameInput.isFocusable = true
                            passwordInput.isFocusableInTouchMode = true
                            passwordInput.isFocusable = true
                            passwordInput.clearFocus()

                            setUpAnimation()
                        }

                        override fun onAnimationCancel(animation: Animator) {
                        }

                        override fun onAnimationRepeat(animation: Animator) {
                        }
                    })
                }

            }

            override fun onFailure(call: Call<TokenModel>, t: Throwable) {
                Log.println(Log.ERROR,tag,t.toString())
            }
        })
    }

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

    private fun setUpAnimation() {
        fromsmall = AnimationUtils.loadAnimation(context,R.anim.fromsmall)
        fromnothing = AnimationUtils.loadAnimation(context,R.anim.fromnothing)
        foricon = AnimationUtils.loadAnimation(context,R.anim.foricon)

        cardF.alpha = 0F
        overbox.alpha = 0F
        animationF.visibility = View.GONE

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
        } else {
            passwordInput.error = null
            passwordInput.isErrorEnabled = false
            return true
        }
    }

    fun Fragment.hideKeyboard() {
        view?.let { activity?.hideKeyboard(it) }
    }

    private fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

}