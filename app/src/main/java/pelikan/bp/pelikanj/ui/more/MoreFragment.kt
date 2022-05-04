package pelikan.bp.pelikanj.ui.more

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.ms.square.android.expandabletextview.ExpandableTextView
import kotlinx.android.synthetic.main.fragment_more.*
import pelikan.bp.pelikanj.R

class MoreFragment : Fragment() {

    private var navControler: NavController?= null

    /**
     * On create view
     *
     * @param inflater inflater
     * @param container container
     * @param savedInstanceState savedInstanceState
     * @return
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view:View = inflater.inflate(R.layout.fragment_more, container, false)

        val expand: ExpandableTextView = view.findViewById(R.id.expand_text_view_about_us)
        expand.text = resources.getString(R.string.about_us_textView)

        val expands: ExpandableTextView = view.findViewById(R.id.expand_text_view_become_translator)
        expands.text = resources.getString(R.string.about_us_becomeTranslator)

        return view
    }

    /**
     * On view created
     *
     * @param view view
     * @param savedInstanceState savedInstanceState
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navControler = Navigation.findNavController(view)

        sendExhibit.setOnClickListener{
            navControler?.navigate(R.id.action_navigation_more_to_send_exhibit)
        }

        language_settings.setOnClickListener {
            navControler?.navigate(R.id.action_navigation_more_to_language_settings)
        }
    }
}