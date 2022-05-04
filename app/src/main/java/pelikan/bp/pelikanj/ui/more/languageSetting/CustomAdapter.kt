package pelikan.bp.pelikanj.ui.more.languageSetting

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import pelikan.bp.pelikanj.R
import pelikan.bp.pelikanj.viewModels.MyLanguages

class CustomAdapter(private val context: Context, private var languagesList: ArrayList<MyLanguages>
                    , private val listener: OnSelectListener) :


    RecyclerView.Adapter<CustomViewHolder>() {

    /**
     * On create view holder
     *
     * @param parent parent
     * @param viewType viewType
     * @return custom view holder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        return CustomViewHolder(
            LayoutInflater.from(context).inflate(R.layout.languages, parent, false)
        )
    }

    /**
     * On bind view holder
     *
     * @param holder custom holder
     * @param position position of item (clicked)
     */
    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        holder.textLanguage.text = languagesList[position].language
        holder.textShort.text = languagesList[position].languageShort

        holder.cardView.setOnClickListener {
            listener.onItemClicked(languagesList[position])
        }
    }

    /**
     * Get item count
     *
     * @return size of list
     */
    override fun getItemCount(): Int {
        return languagesList.size
    }

    /**
     * Change data in language list with new actual one
     *
     * @param filteredList filtered list with new data
     */
    fun filterList(filteredList: ArrayList<MyLanguages>){
        languagesList = filteredList
        notifyDataSetChanged()
    }
}