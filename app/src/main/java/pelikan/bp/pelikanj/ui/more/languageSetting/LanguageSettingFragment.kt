package pelikan.bp.pelikanj.ui.more.languageSetting

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pelikan.bp.pelikanj.R
import java.util.ArrayList
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader

class LanguageSettingFragment : Fragment(), OnSelectListener {

    lateinit var recyclerView: RecyclerView
    lateinit var myLanguages: ArrayList<MyLanguages>
    lateinit var customAdapter: CustomAdapter
    lateinit var searchLanguagesView: SearchView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = inflater.inflate(R.layout.fragment_language_setting, container, false)

        searchLanguagesView = view.findViewById(R.id.search_language_view)

        displayItems(view)

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

        // Zabarvit vybraný text barvou, ať je vidět výběr

        Toast.makeText(context,myLanguages.language,Toast.LENGTH_SHORT).show()

    }
}