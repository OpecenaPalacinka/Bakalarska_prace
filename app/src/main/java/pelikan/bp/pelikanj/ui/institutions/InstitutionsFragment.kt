package pelikan.bp.pelikanj.ui.institutions


import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.institution_card.*
import kotlinx.android.synthetic.main.institutions_fragment.*
import pelikan.bp.pelikanj.R


class InstitutionsFragment : Fragment() {

    private val mNames = arrayOf(
        "Fabian", "Carlos", "Alex", "Andrea", "Karla",
        "Freddy", "Lazaro", "Hector", "Carolina", "Edwin", "Jhon",
        "Edelmira", "Andres"
    )

    private val mAnimals = arrayOf(
        "Pilsen", "Prague", "Oveja", "Elefante", "Pez",
        "Nicuro", "Bocachico", "Chucha", "Curie", "Raton", "Aguila",
        "Leon", "Jirafa"
    )

    val aList: MutableList<HashMap<String, SpannableStringBuilder>> = ArrayList()

    lateinit var overbox: LinearLayout
    lateinit var card: LinearLayout
    lateinit var imageIcon: ImageView
    lateinit var fromsmall: Animation
    lateinit var fromnothing: Animation
    lateinit var foricon: Animation

    lateinit var constraintLayout: ConstraintLayout

    lateinit var listView: ListView


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view : View = inflater.inflate(R.layout.institutions_fragment, container, false)

        listView = view.findViewById(R.id.institutions)

        constraintLayout = view.findViewById(R.id.institutions_fragment)

        constraintLayout.addView(inflater.inflate(R.layout.institution_card,null))

        fillInstitutions(view)

        return view
    }

    private fun fillInstitutions(view: View){
        val boldText = StyleSpan(android.graphics.Typeface.BOLD)
        for (i in 0 until mNames.size) {
            val hm = HashMap<String, SpannableStringBuilder>()
            val ssb = SpannableStringBuilder("Address: "+mAnimals[i])
            ssb.setSpan(boldText,0,6,Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            hm["institution_name"] = SpannableStringBuilder().append("       : " + mNames[i])
            hm["address"] = ssb
            hm["image"] = SpannableStringBuilder().append(R.drawable.ic_baseline_location_city_24_gray.toString())
            aList.add(hm)
        }

        val from = arrayOf("image", "institution_name", "address")

        val to = intArrayOf(R.id.image, R.id.institution_name, R.id.address)

        fromsmall = AnimationUtils.loadAnimation(context,R.anim.fromsmall)
        fromnothing = AnimationUtils.loadAnimation(context,R.anim.fromnothing)
        foricon = AnimationUtils.loadAnimation(context,R.anim.foricon)

        card = view.findViewById(R.id.popup)
        overbox = view.findViewById(R.id.overbox)
        imageIcon = view.findViewById(R.id.institution_image)

        val adapter: SimpleAdapter =
            SimpleAdapter(activity?.baseContext, aList, R.layout.institutions_fragment, from, to)
        listView.adapter = adapter

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

            institution_name_card.text = mNames[position]
            institution_address_card.text = mAnimals[position]

        }


    }

}