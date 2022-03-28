package pelikan.bp.pelikanj.ui.more.languageSetting

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pelikan.bp.pelikanj.R
import java.util.ArrayList

class LanguageSettingFragment : Fragment() {

    lateinit var recyclerView: RecyclerView
    lateinit var myLanguages: ArrayList<MyLanguages>
    lateinit var customAdapter: CustomAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = inflater.inflate(R.layout.fragment_language_setting, container, false)

        displayItems(view)

        return view
    }

    private fun displayItems(view: View) {
        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = GridLayoutManager(context,1)

        myLanguages = ArrayList()

        myLanguages.add(MyLanguages("Ahoj","jde to"))
        myLanguages.add(MyLanguages("Ahoj","jde to"))
        myLanguages.add(MyLanguages("Ahoj","jde to"))
        myLanguages.add(MyLanguages("Ahoj","jde to"))

        customAdapter = CustomAdapter(view.context,myLanguages)
        recyclerView.adapter = customAdapter
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            LanguageSettingFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}