package pelikan.bp.pelikanj.ui.more

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.ms.square.android.expandabletextview.ExpandableTextView
import kotlinx.android.synthetic.main.fragment_more.*
import pelikan.bp.pelikanj.ApiClient
import pelikan.bp.pelikanj.R
import pelikan.bp.pelikanj.viewModels.InstitutionsModelItem
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MoreFragment : Fragment() {

    private var navControler: NavController?= null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view:View = inflater.inflate(R.layout.fragment_more, container, false)

        val expand: ExpandableTextView = view.findViewById(R.id.expand_text_view_about_us)
        expand.text = "\n" +
                "O nás !O nás !O nás !O nás !O nás !O nás !O nás !" +
                "O nás !O nás !O nás !O nás !O nás !O nás !O nás !O nás !" +
                "O nás !O nás !O nás !O nás !O nás !O nás !O nás !O nás !" +
                "O nás !O nás !O nás !O nás !O nás !O nás !O nás !" +
                "O nás !O nás !O nás !O nás !O nás !O nás !O nás !O nás !" +
                "O nás !O nás !O nás !O nás !O nás !O nás !O nás !O nás !" +
                "O nás !O nás !O nás !O nás !O nás !O nás !O nás !O nás !"

        val expands: ExpandableTextView = view.findViewById(R.id.expand_text_view_become_translator)
        expands.text = "\n" +
                "Aby jste mohli překládat texty pro ostatní uživatele, musíte se nejdříve zaregistrovat. " +
                "K registraci můžete využít formulář na stránce profil, nebo jděte na stránky xyz.cz. " +
                "Pokud již účet máte, na stránkách xyz.cz se přihlašte, v navigaci najdete možnost Překládat."




        return view
    }


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