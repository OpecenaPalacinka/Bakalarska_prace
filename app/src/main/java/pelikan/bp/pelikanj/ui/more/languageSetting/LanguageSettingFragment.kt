package pelikan.bp.pelikanj.ui.more.languageSetting

import android.animation.Animator
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import pelikan.bp.pelikanj.R
import java.util.ArrayList
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import pelikan.bp.pelikanj.DBClient
import pelikan.bp.pelikanj.ui.findExhibit.PickExhibitFragment

class LanguageSettingFragment : Fragment(), OnSelectListener {

    lateinit var recyclerView: RecyclerView
    lateinit var myLanguages: ArrayList<MyLanguages>
    lateinit var customAdapter: CustomAdapter
    lateinit var searchLanguagesView: SearchView

    lateinit var frameLayout: FrameLayout
    lateinit var fromsmall: Animation
    lateinit var fromnothing: Animation
    lateinit var foricon: Animation
    lateinit var animation: LottieAnimationView
    lateinit var card: LinearLayout
    lateinit var overbox: LinearLayout

    var heightOverbox: Int = 0

    lateinit var dbClient: DBClient
    private var navController: NavController?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = inflater.inflate(R.layout.fragment_language_setting, container, false)

        frameLayout = view.findViewById(R.id.language_settings_fragment)

        frameLayout.addView(inflater.inflate(R.layout.animation_success,null))

        searchLanguagesView = view.findViewById(R.id.search_language_view)

        dbClient = DBClient(requireContext())

        initForm(view)

        setUpAnimation()

        displayItems(view)

        heightOverbox = countHeight()

        searchLanguagesView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {

                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                filter(newText)
                return true
            }
        })

        return view
    }

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

        val wm = requireContext().getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        wm.defaultDisplay.getMetrics(metrics)

        return metrics.heightPixels - (2 * actionBarHeight) - statusBarHeight
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
    }

    private fun initForm(view: View) {
        overbox = view.findViewById(R.id.overbox)
        animation = view.findViewById(R.id.animationSuccess)
        card = view.findViewById(R.id.popup)

        val animationText: TextView = view.findViewById(R.id.animation_success_text)
        animationText.text = resources.getString(R.string.change_successfull)
    }

    private fun setUpAnimation() {
        fromsmall = AnimationUtils.loadAnimation(context,R.anim.fromsmall)
        fromnothing = AnimationUtils.loadAnimation(context,R.anim.fromnothing)
        foricon = AnimationUtils.loadAnimation(context,R.anim.foricon)

        card.alpha = 0F
        overbox.alpha = 0F
        animation.visibility = View.GONE

        val params: ViewGroup.LayoutParams = overbox.layoutParams
        params.height = 0
        overbox.layoutParams = params
    }

    private fun doAnimation() {
        animation.visibility = View.VISIBLE
        animation.startAnimation(foricon)
        animation.playAnimation()
        animation.repeatCount = 1

        overbox.alpha = 1F
        overbox.startAnimation(fromnothing)

        val params: ViewGroup.LayoutParams = overbox.layoutParams
        params.height = heightOverbox
        overbox.layoutParams = params

        card.alpha = 1F
        card.startAnimation(fromsmall)
    }

    private fun filter(newText: String) {
        val filteredList = ArrayList<MyLanguages>()

        for (item in myLanguages){
            if (item.language.lowercase().contains(newText.lowercase())){
                filteredList.add(item)
            }
        }
        customAdapter.filterList(filteredList)
    }

    private fun displayItems(view: View) {
        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = GridLayoutManager(context,1)

        myLanguages = ArrayList()

        csvReader().open(resources.openRawResource(R.raw.language_codes)) {
            readAllAsSequence().forEach { row ->
                myLanguages.add(MyLanguages(row[1], row[0]))
            }
        }

        customAdapter = CustomAdapter(view.context,myLanguages,this)
        recyclerView.adapter = customAdapter
    }

    override fun onItemClicked(myLanguages: MyLanguages) {

        dbClient.updateLanguage(myLanguages.languageShort)

        hideKeyboard()

        doAnimation()

        animation.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                recyclerView.isClickable = false
            }

            override fun onAnimationEnd(animation: Animator) {
                navController?.navigate(R.id.action_language_settings_to_navigation_more)
            }

            override fun onAnimationCancel(animation: Animator) {
            }

            override fun onAnimationRepeat(animation: Animator) {
            }
        })
    }

    fun Fragment.hideKeyboard() {
        view?.let { activity?.hideKeyboard(it) }
    }

    private fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}