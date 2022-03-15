package pelikan.bp.pelikanj.ui.more

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ms.square.android.expandabletextview.ExpandableTextView
import pelikan.bp.pelikanj.R

class MoreFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view:View = inflater.inflate(R.layout.fragment_more, container, false)
        val expand: ExpandableTextView = view.findViewById<ExpandableTextView?>(R.id.expand_text_view)
        expand.setText("\n" +
                "O nás !O nás !O nás !O nás !O nás !O nás !O nás !" +
                "O nás !O nás !O nás !O nás !O nás !O nás !O nás !O nás !" +
                "O nás !O nás !O nás !O nás !O nás !O nás !O nás !O nás !" +
                "O nás !O nás !O nás !O nás !O nás !O nás !O nás !O nás !")
        return view
    }

}