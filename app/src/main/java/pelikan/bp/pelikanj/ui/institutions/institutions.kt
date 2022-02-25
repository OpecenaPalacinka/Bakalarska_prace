package pelikan.bp.pelikanj.ui.institutions

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import pelikan.bp.pelikanj.R

class institutions : Fragment() {

    companion object {
        fun newInstance() = institutions()
    }

    private lateinit var viewModel: InstitutionsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.institutions_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(InstitutionsViewModel::class.java)
        // TODO: Use the ViewModel
    }

}