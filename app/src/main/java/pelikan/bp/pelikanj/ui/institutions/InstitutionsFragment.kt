package pelikan.bp.pelikanj.ui.institutions


import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import okhttp3.ResponseBody
import pelikan.bp.pelikanj.ApiClient
import pelikan.bp.pelikanj.DBClient
import pelikan.bp.pelikanj.R
import pelikan.bp.pelikanj.viewModels.InstitutionsModelItem
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class InstitutionsFragment : Fragment() {

    private val institutionsName: ArrayList<String> = ArrayList()
    private val institutionsAddress: ArrayList<String> = ArrayList()
    private val institutionsImagesStrings: MutableMap<String,String> = mutableMapOf()
    private val institutionsDescriptions: ArrayList<String> = ArrayList()
    private val institutionsImages: MutableMap<String,Bitmap> = mutableMapOf()
    private var institutionsList: ArrayList<InstitutionsModelItem> = ArrayList()

    private val aList: MutableList<HashMap<String, SpannableStringBuilder>> = ArrayList()

    lateinit var overbox: LinearLayout
    lateinit var card: LinearLayout
    private lateinit var imageIcon: ImageView
    private lateinit var fromsmall: Animation
    private lateinit var fromnothing: Animation
    private lateinit var foricon: Animation
    private lateinit var institutionNameCard: TextView
    private lateinit var institutionAddressCard: TextView
    private lateinit var institutionDescriptionCard: TextView

    var height: Int = 0

    private lateinit var constraintLayout: ConstraintLayout

    private lateinit var listView: ListView

    /**
     * On create view
     *
     * @param inflater inflater
     * @param container container
     * @param savedInstanceState savedInstanceState
     * @return view
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view : View = inflater.inflate(R.layout.institutions_fragment, container, false)

        constraintLayout = view.findViewById(R.id.institutions_fragment)

        constraintLayout.addView(inflater.inflate(R.layout.institution_card,null))

        initInstitutions(view)

        val dbClient = DBClient(requireContext())

        institutionsList = dbClient.getAllInstitutions()

        fillInstitutions()

        height = WindowManager.LayoutParams().height

        setUpAnimation()

        setUpListeners()

        return view
    }

    /** Set up listeners */
    private fun setUpListeners(){
        listView.setOnItemClickListener { _, _, position, _ ->

            doAnimation()

            imageIcon.setImageBitmap(institutionsImages[institutionsName[position]])
            institutionNameCard.text = institutionsName[position]
            institutionAddressCard.text = institutionsAddress[position]
            institutionDescriptionCard.text = institutionsDescriptions[position]
        }

        overbox.setOnClickListener {
            if (fromsmall.hasEnded()) {
                setUpAnimation()
            }
        }

    }

    /** Get images of all institutions */
    private fun getInstitutionImages() {
        for (imageName in institutionsImagesStrings){
            val client: Call<ResponseBody> = ApiClient.create().getImage(imageName.value)

            client.enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody?>,
                    response: Response<ResponseBody?>
                ) {
                    val respBody = response.body()!!
                    institutionsImages[imageName.key] = BitmapFactory.decodeStream(respBody.byteStream())
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.println(Log.ERROR,tag,"Error when getting institutions image!")
                }
            })
        }
    }

    /** Do animation with institution card */
    private fun doAnimation() {
        imageIcon.visibility = View.VISIBLE
        imageIcon.startAnimation(foricon)

        overbox.alpha = 1F
        overbox.startAnimation(fromnothing)

        val params: ViewGroup.LayoutParams = overbox.layoutParams
        params.height = height
        overbox.layoutParams = params

        card.alpha = 1F
        card.startAnimation(fromsmall)


    }

    /** Set up animation */
    private fun setUpAnimation() {
        fromsmall = AnimationUtils.loadAnimation(context,R.anim.fromsmall)
        fromnothing = AnimationUtils.loadAnimation(context,R.anim.fromnothing)
        foricon = AnimationUtils.loadAnimation(context,R.anim.foricon)

        card.alpha = 0F
        overbox.alpha = 0F
        imageIcon.visibility = View.GONE

        val params: ViewGroup.LayoutParams = overbox.layoutParams
        params.height = 0
        overbox.layoutParams = params
    }

    /**
     * Init all xml tags
     *
     * @param view view
     */
    private fun initInstitutions(view: View) {
        listView = view.findViewById(R.id.institutions)
        card = view.findViewById(R.id.popup)
        overbox = view.findViewById(R.id.overbox)
        imageIcon = view.findViewById(R.id.institution_image)
        institutionNameCard = view.findViewById(R.id.institution_name_card)
        institutionAddressCard = view.findViewById(R.id.institution_address_card)
        institutionDescriptionCard = view.findViewById(R.id.institution_description_card)
    }

    /** Fill lists with important data then get images*/
    private fun fillInstitutions() {
        for (respo: InstitutionsModelItem in institutionsList) {
            institutionsName.add(respo.name)
            institutionsAddress.add(respo.address)
            institutionsImagesStrings[respo.name] = (respo.image)
            institutionsDescriptions.add(respo.description)
        }
        setupInstitutions()
        getInstitutionImages()
    }

    /** Setup institutions and add them to list view */
    private fun setupInstitutions() {
        val boldText = StyleSpan(android.graphics.Typeface.BOLD)

        // Iterate through all institutions
        for (i in 0 until institutionsName.size) {
            val hm = HashMap<String, SpannableStringBuilder>()
            val ssb = SpannableStringBuilder(resources.getString(R.string.address) + ": "+institutionsAddress[i])
            ssb.setSpan(boldText,0,6,Spannable.SPAN_INCLUSIVE_INCLUSIVE)

            // Institution name
            hm["institution_name"] = SpannableStringBuilder().append("       : " + institutionsName[i])
            // Institution address
            hm["address"] = ssb
            // Icon of building
            hm["image"] = SpannableStringBuilder().append(R.drawable.ic_baseline_location_city_24_gray.toString())

            aList.add(hm)
        }

        val from = arrayOf("image", "institution_name", "address")

        val to = intArrayOf(R.id.image, R.id.institution_name, R.id.address)

        val adapter =
            SimpleAdapter(activity?.baseContext, aList, R.layout.institutions_fragment, from, to)
        listView.adapter = adapter
    }

}