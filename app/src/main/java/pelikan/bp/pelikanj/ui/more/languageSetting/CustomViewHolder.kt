package pelikan.bp.pelikanj.ui.more.languageSetting

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import pelikan.bp.pelikanj.R

class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var textLanguage: TextView
    var textShort: TextView

    init {
        textLanguage = itemView.findViewById(R.id.language_name)
        textShort = itemView.findViewById(R.id.language_short)
    }
}