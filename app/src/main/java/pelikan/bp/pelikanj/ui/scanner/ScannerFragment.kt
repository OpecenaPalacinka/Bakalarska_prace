package pelikan.bp.pelikanj.ui.scanner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.Camera
import androidx.fragment.app.Fragment
import pelikan.bp.pelikanj.R

class ScannerFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_scanner, container, false)
        val mCameraView: Camera
        return view
    }


}