package pelikan.bp.pelikanj.ui.scanner

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import pelikan.bp.pelikanj.ApiClient
import pelikan.bp.pelikanj.DBClient
import pelikan.bp.pelikanj.R
import pelikan.bp.pelikanj.viewModels.Translation
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ScannerFragment : Fragment() {
    private lateinit var codeScanner: CodeScanner
    private lateinit var dbClient: DBClient

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view: View = inflater.inflate(R.layout.fragment_scanner, container, false)

        dbClient = DBClient(requireContext())

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val scannerView = view.findViewById<CodeScannerView>(R.id.scanner_view)
        val activity = requireActivity()
        codeScanner = CodeScanner(activity, scannerView)
        codeScanner.decodeCallback = DecodeCallback {
            activity.runOnUiThread {
                // Vypíše text z qrkódu
                getTranslation(it.text.toInt())
            }
        }
        scannerView.setOnClickListener {
            codeScanner.startPreview()
        }
    }

    private fun getTranslation(idOfExhibit: Int) {
        val lCode = dbClient.getAllUserData()?.language!!

        val client: Call<Translation> = ApiClient.create().getTranslation(idOfExhibit,lCode)

        client.enqueue(object : Callback<Translation> {
            override fun onResponse(
                call: Call<Translation>,
                response: Response<Translation>
            ) {
                if (response.code() == 200){
                    // Show translation
                    Toast.makeText(requireContext(),response.body()?.translatedText,Toast.LENGTH_LONG).show()
                } else {
                    // Try english
                }

            }

            override fun onFailure(call: Call<Translation>, t: Throwable) {
                Log.println(Log.ERROR,tag,t.toString())
            }
        })
    }

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }


}