package pelikan.bp.pelikanj.ui.institutions


import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.Image
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import okhttp3.ResponseBody
import okhttp3.internal.wait
import pelikan.bp.pelikanj.ApiClient
import pelikan.bp.pelikanj.R
import pelikan.bp.pelikanj.viewModels.InstitutionsModelItem
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.awaitResponse


class InstitutionsFragment : Fragment() {

    private val institutionsName: ArrayList<String> = ArrayList()
    private val institutionsAddress: ArrayList<String> = ArrayList()
    private val institutionsImagesStrings: ArrayList<String> = ArrayList()
    private val institutionsImages: ArrayList<Bitmap> = ArrayList()
    private lateinit var institutionImageFromServer: Image

    val aList: MutableList<HashMap<String, SpannableStringBuilder>> = ArrayList()

    lateinit var overbox: LinearLayout
    lateinit var card: LinearLayout
    lateinit var imageIcon: ImageView
    lateinit var fromsmall: Animation
    lateinit var fromnothing: Animation
    lateinit var foricon: Animation
    lateinit var institutionNameCard: TextView
    lateinit var institutionAddressCard: TextView
    lateinit var institutionDescriptionCard: TextView

    lateinit var constraintLayout: ConstraintLayout

    lateinit var listView: ListView


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view : View = inflater.inflate(R.layout.institutions_fragment, container, false)

        constraintLayout = view.findViewById(R.id.institutions_fragment)

        constraintLayout.addView(inflater.inflate(R.layout.institution_card,null))

        initInstitutions(view)

        if (institutionsName.size == 0){
            fillInstitutions()
        }

        setUpAnimation()

        return view
    }

    private fun getInstitutionImages() {
        for (imageName: String in institutionsImagesStrings){
            val client: Call<ResponseBody> = ApiClient.create().getImage(imageName)

            client.enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody?>,
                    response: Response<ResponseBody?>
                ) {
                    val respBody = response.body()!!
                    institutionsImages.add(BitmapFactory.decodeStream(respBody.byteStream()))
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable?) {
                    Log.println(Log.ERROR,tag,"Error when getting institutions image!")
                }
            })
        }
    }

    private fun setUpAnimation() {
        fromsmall = AnimationUtils.loadAnimation(context,R.anim.fromsmall)
        fromnothing = AnimationUtils.loadAnimation(context,R.anim.fromnothing)
        foricon = AnimationUtils.loadAnimation(context,R.anim.foricon)

        card.alpha = 0F
        overbox.alpha = 0F
        imageIcon.visibility = View.GONE

        listView.setOnItemClickListener { _, _, position, _ ->

            imageIcon.visibility = View.VISIBLE
            imageIcon.startAnimation(foricon)

            overbox.alpha = 1F
            overbox.startAnimation(fromnothing)

            card.alpha = 1F
            card.startAnimation(fromsmall)

            imageIcon.setImageBitmap(institutionsImages[position])
            institutionNameCard.text = institutionsName[position]
            institutionAddressCard.text = institutionsAddress[position]

        }
    }

    private fun initInstitutions(view: View) {
        listView = view.findViewById(R.id.institutions)
        card = view.findViewById(R.id.popup)
        overbox = view.findViewById(R.id.overbox)
        imageIcon = view.findViewById(R.id.institution_image)
        institutionNameCard = view.findViewById(R.id.institution_name_card)
        institutionAddressCard = view.findViewById(R.id.institution_address_card)
        institutionDescriptionCard = view.findViewById(R.id.institution_description_card)
    }

    private fun fillInstitutions() {

        val client: Call<List<InstitutionsModelItem>> = ApiClient.create().getInstitutions()

        client.enqueue(object : Callback<List<InstitutionsModelItem>> {
            override fun onResponse(
                call: Call<List<InstitutionsModelItem>?>,
                response: Response<List<InstitutionsModelItem>?>
            ) {
                val respBody = response.body()!!
                for (respo: InstitutionsModelItem in respBody) {
                    institutionsName.add(respo.name)
                    institutionsAddress.add(respo.address)
                    institutionsImagesStrings.add(respo.image)
                }
                setupInstitutions()
                getInstitutionImages()
            }

            override fun onFailure(call: Call<List<InstitutionsModelItem>>, t: Throwable?) {
                Log.println(Log.ERROR,tag,"Error when getting institutions!")
            }
        })

    }

    private fun setupInstitutions() {
        val boldText = StyleSpan(android.graphics.Typeface.BOLD)
        for (i in 0 until institutionsName.size) {
            val hm = HashMap<String, SpannableStringBuilder>()
            val ssb = SpannableStringBuilder(resources.getString(R.string.address) + ": "+institutionsAddress[i])
            ssb.setSpan(boldText,0,6,Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            hm["institution_name"] = SpannableStringBuilder().append("       : " + institutionsName[i])
            hm["address"] = ssb
            hm["image"] = SpannableStringBuilder().append(R.drawable.ic_baseline_location_city_24_gray.toString())
            aList.add(hm)
        }

        val from = arrayOf("image", "institution_name", "address")

        val to = intArrayOf(R.id.image, R.id.institution_name, R.id.address)

        val adapter: SimpleAdapter =
            SimpleAdapter(activity?.baseContext, aList, R.layout.institutions_fragment, from, to)
        listView.adapter = adapter
    }

}