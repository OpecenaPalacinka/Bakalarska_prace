package pelikan.bp.pelikanj.ui.more.languageSetting

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import androidx.cardview.widget.CardView
import pelikan.bp.pelikanj.R

class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var textLanguage: TextView
    var textShort: TextView
    var cardView: CardView

    init {
        textLanguage = itemView.findViewById(R.id.language_name)
        textShort = itemView.findViewById(R.id.language_short)
        cardView = itemView.findViewById(R.id.card_view_container)
    }
}