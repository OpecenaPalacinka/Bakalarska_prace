package pelikan.bp.pelikanj.ui.institutions


import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.Toast
import androidx.core.text.bold
import androidx.fragment.app.Fragment
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


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view : View = inflater.inflate(R.layout.institutions_fragment, container, false)

        val aList: MutableList<HashMap<String, SpannableStringBuilder>> = ArrayList()
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

        val listView : ListView = view.findViewById(R.id.institutions)
        val adapter : SimpleAdapter = SimpleAdapter(activity?.baseContext,aList,R.layout.institutions_fragment,from,to)
        listView.adapter = adapter


        return view
    }

}