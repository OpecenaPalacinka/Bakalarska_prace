package pelikan.bp.pelikanj.ui.more.languageSetting

import android.animation.Animator
import android.app.Activity
import android.content.Context
import android.graphics.Insets
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import pelikan.bp.pelikanj.DBClient
import pelikan.bp.pelikanj.R
import pelikan.bp.pelikanj.viewModels.MyLanguages

class LanguageSettingFragment : Fragment(), OnSelectListener {

    lateinit var recyclerView: RecyclerView
    private lateinit var myLanguages: ArrayList<MyLanguages>
    private lateinit var customAdapter: CustomAdapter
    private lateinit var searchLanguagesView: SearchView

    lateinit var frameLayout: FrameLayout
    private lateinit var fromsmall: Animation
    private lateinit var fromnothing: Animation
    private lateinit var foricon: Animation
    private lateinit var animation: LottieAnimationView
    lateinit var card: LinearLayout
    lateinit var overbox: LinearLayout

    private var heightOverbox: Int = 0

    lateinit var dbClient: DBClient
    private var navController: NavController?= null

    /**
     * On create
     *
     * @param savedInstanceState savedInstanceState
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // removes back arrow on top bar
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
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_language_setting, container, false)

        frameLayout = view.findViewById(R.id.language_settings_fragment)

        frameLayout.addView(inflater.inflate(R.layout.animation_success,null))

        searchLanguagesView = view.findViewById(R.id.search_language_view)

        dbClient = DBClient(requireContext())

        initForm(view)

        setUpAnimation()

        displayItems(view)

        heightOverbox = countHeight()

        searchLanguagesView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            // Submit is ignored
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            // On change call filter
            override fun onQueryTextChange(newText: String): Boolean {
                filter(newText)
                return true
            }
        })

        return view
    }

    /**
     * On view created (only init nav controller)
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
     * Init all xml tags and text
     *
     * @param view view
     */
    private fun initForm(view: View) {
        overbox = view.findViewById(R.id.overbox)
        animation = view.findViewById(R.id.animationSuccess)
        card = view.findViewById(R.id.popup)

        val animationText: TextView = view.findViewById(R.id.animation_success_text)
        animationText.text = resources.getString(R.string.change_successfull)
    }

    /**
     * Set up the animation
     */
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

    /**
     * Do animation when change was successful
     */
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

    /**
     * Filter right options only
     * Called everytime user input a char or deletes a char
     *
     * @param newText new text from user
     */
    private fun filter(newText: String) {
        val filteredList = ArrayList<MyLanguages>()

        for (item in myLanguages){
            if (item.language.lowercase().contains(newText.lowercase())){
                filteredList.add(item)
            }
        }

        customAdapter.filterList(filteredList)
    }

    /**
     * Display items in recycler view
     *
     * @param view view
     */
    private fun displayItems(view: View) {
        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = GridLayoutManager(context,1)

        myLanguages = ArrayList()

        // Get items from .csv file (same languages as on server)
        csvReader().open(resources.openRawResource(R.raw.language_codes)) {
            readAllAsSequence().forEach { row ->
                myLanguages.add(MyLanguages(row[1], row[0]))
            }
        }

        customAdapter = CustomAdapter(view.context,myLanguages,this)
        recyclerView.adapter = customAdapter
    }

    /**
     * Update user language and do the animation after users clicks on item
     *
     * @param myLanguages item clicked
     */
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