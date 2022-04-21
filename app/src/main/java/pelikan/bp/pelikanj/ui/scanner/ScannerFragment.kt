package pelikan.bp.pelikanj.ui.scanner

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.google.gson.Gson
import kotlinx.android.synthetic.main.animation_failed.*
import okhttp3.ResponseBody
import pelikan.bp.pelikanj.ApiClient
import pelikan.bp.pelikanj.DBClient
import pelikan.bp.pelikanj.R
import pelikan.bp.pelikanj.viewModels.Translation
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ScannerFragment : Fragment() {

    private lateinit var codeScanner: CodeScanner

    private lateinit var fromsmall: Animation
    private lateinit var fromnothing: Animation
    private lateinit var foricon: Animation
    lateinit var overbox: LinearLayout
    lateinit var card: LinearLayout
    private lateinit var cardF: LinearLayout
    lateinit var textView: TextView

    private lateinit var animationF: LottieAnimationView

    lateinit var frameLayout: FrameLayout

    private lateinit var dbClient: DBClient

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view: View = inflater.inflate(R.layout.fragment_scanner, container, false)

        frameLayout = view.findViewById(R.id.scanner_fragment)

        frameLayout.addView(inflater.inflate(R.layout.translation_card,null))
        frameLayout.addView(inflater.inflate(R.layout.animation_failed,null))

        dbClient = DBClient(requireContext())

        initForm(view)

        setUpAnimation()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val scannerView = view.findViewById<CodeScannerView>(R.id.scanner_view)
        val activity = requireActivity()

        codeScanner = CodeScanner(activity, scannerView)

        codeScanner.decodeCallback = DecodeCallback {
            activity.runOnUiThread {
                // Vypíše text z qrkódu
                getTranslation(it.text.toInt())

            }
        }

        overbox.setOnClickListener {
            if (fromsmall.hasEnded()) {
                setUpAnimation()
                codeScanner.startPreview()
            }
        }

        scannerView.setOnClickListener {
            codeScanner.startPreview()
        }
    }

    private fun getTranslation(idOfExhibit: Int) {
        val lCode = dbClient.getAllUserData()?.language!!

        val client: Call<ResponseBody> = ApiClient.create().getTranslation(idOfExhibit,lCode)

        client.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                if (response.code() == 200){
                    // Show translation
                    val resp: String = response.body()!!.string()
                    val body = Gson().fromJson(resp, Translation::class.java)
                    val text = body.translatedText
                    textView.text = HtmlCompat.fromHtml(text,HtmlCompat.FROM_HTML_MODE_LEGACY)
                    doAnimation()
                } else {
                    // Try english
                    getEnglishTranslation(idOfExhibit)
                }

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.println(Log.ERROR,tag,t.toString())
            }
        })
    }

    private fun getEnglishTranslation(idOfExhibit: Int) {
        val lCode = "en"

        val client: Call<ResponseBody> = ApiClient.create().getTranslation(idOfExhibit,lCode)

        client.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                if (response.code() == 200){
                    // Show translation
                    val resp: String = response.body()!!.string()
                    val body = Gson().fromJson(resp, Translation::class.java)
                    val text = body.translatedText
                    textView.text = HtmlCompat.fromHtml(text, HtmlCompat.FROM_HTML_MODE_LEGACY)
                    doAnimation()
                } else if (response.code() == 400){
                    // Try english
                    animation_failed_text.text = resources.getString(R.string.no_translation)
                    doAnimationFailed()
                }

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.println(Log.ERROR,tag,t.toString())
            }
        })
    }

    private fun doAnimation() {
        overbox.alpha = 1F
        overbox.startAnimation(fromnothing)

        card.alpha = 1F
        card.startAnimation(fromsmall)
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

        card.alpha = 0F
        cardF.alpha = 0F
        overbox.alpha = 0F

        animationF.visibility = View.GONE
    }

    private fun initForm(view: View) {
        card = view.findViewById(R.id.popup_translation)
        overbox = view.findViewById(R.id.overbox)
        textView = view.findViewById(R.id.translated_text)
        textView.movementMethod = ScrollingMovementMethod()

        animationF = view.findViewById(R.id.animationFailed)
        cardF = view.findViewById(R.id.popup_failed)
    }

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }


}