package pelikan.bp.pelikanj.ui.more.languageSetting

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import pelikan.bp.pelikanj.R

class CustomAdapter(private val context: Context, private val languagesList: List<MyLanguages>) :
    RecyclerView.Adapter<CustomViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        return CustomViewHolder(
            LayoutInflater.from(context).inflate(R.layout.languages, parent, false)
        )
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        holder.textLanguage.text = languagesList[position].language
        holder.textShort.text = languagesList[position].languageShort
    }

    override fun getItemCount(): Int {
        return languagesList.size
    }
}