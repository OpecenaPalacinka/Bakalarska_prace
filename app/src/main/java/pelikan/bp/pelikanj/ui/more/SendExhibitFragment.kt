package pelikan.bp.pelikanj.ui.more

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.fragment_send_exhibit.*
import pelikan.bp.pelikanj.R


class SendExhibitFragment : Fragment() {

    lateinit var exhibitName: TextInputLayout
    lateinit var exhibitInfoLabel: TextInputLayout
    lateinit var image: TextInputLayout
    lateinit var buildingNumber: TextInputLayout
    lateinit var roomNumber: TextInputLayout
    lateinit var showCaseNumber: TextInputLayout
    lateinit var sendExhibitButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, SavedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_send_exhibit, container, false)

        initForm(view)

        sendExhibitButton.setOnClickListener { checkRequiredValues() }


        return view
    }

    private fun initForm(view: View){
        exhibitName = view.findViewById(R.id.exhibit_name)
        exhibitInfoLabel = view.findViewById(R.id.exhibit_info_label)
        buildingNumber = view.findViewById(R.id.building_number)
        roomNumber = view.findViewById(R.id.room_number)
        showCaseNumber = view.findViewById(R.id.showCase_number)
        sendExhibitButton = view.findViewById(R.id.send_exhibit_button)
    }

    public fun sendExhibit(view: View) {


    }

    private fun checkRequiredValues(): Boolean {
        if (!validateExhibitName() or !validateExhibitInfoLabel()
            or !validateBuildingNumber() or !validateRoomNumber()
            or !validateShowCaseNumber()
        ) {
            return false
        }
        return true
    }

    private fun validateExhibitName(): Boolean {
        val exhibitNameString: String = exhibitName.editText?.text.toString()

        if (exhibitNameString.isEmpty()) {
            exhibitName.error = resources.getString(R.string.requiredNameOfExhibit)
            return false
        } else if (exhibitNameString.length > 100) {
            exhibitName.error = resources.getString(R.string.tooLong)
            return false
        } else {
            exhibitName.error = null
            exhibitName.isErrorEnabled = false
            return true
        }
    }

    private fun validateExhibitInfoLabel(): Boolean {
        val exhibitInfoLabelString: String = exhibitInfoLabel.editText?.text.toString()

        if (exhibitInfoLabelString.length > 25000) {
            exhibitInfoLabel.error = resources.getString(R.string.tooLong)
            return false
        } else {
            exhibitInfoLabel.error = null
            exhibitInfoLabel.isErrorEnabled = false
            return true
        }
    }

    private fun validateBuildingNumber(): Boolean {
        val exhibitBuildingNumberString: String = buildingNumber.editText?.text.toString()

        if (exhibitBuildingNumberString.isEmpty()) {
            buildingNumber.error = resources.getString(R.string.requiredNameOfBuilding)
            return false
        } else if (exhibitBuildingNumberString.length > 50) {
            buildingNumber.error = resources.getString(R.string.tooLong)
            return false
        } else {
            buildingNumber.error = null
            buildingNumber.isErrorEnabled = false
            return true
        }
    }

    private fun validateRoomNumber(): Boolean {
        val exhibitRoomNumberString: String = roomNumber.editText?.text.toString()

        if (exhibitRoomNumberString.isEmpty()) {
            roomNumber.error = resources.getString(R.string.requiredNameOfRoom)
            return false
        } else if (exhibitRoomNumberString.length > 50) {
            roomNumber.error = resources.getString(R.string.tooLong)
            return false
        } else {
            roomNumber.error = null
            roomNumber.isErrorEnabled = false
            return true
        }
    }

    private fun validateShowCaseNumber(): Boolean {
        val exhibitShowCaseNumberString: String = showCaseNumber.editText?.text.toString()

        if (exhibitShowCaseNumberString.isEmpty()) {
            showCaseNumber.error = resources.getString(R.string.requiredNameOfShowCase)
            return false
        } else if (exhibitShowCaseNumberString.length > 50) {
            showCaseNumber.error = resources.getString(R.string.tooLong)
            return false
        } else {
            showCaseNumber.error = null
            showCaseNumber.isErrorEnabled = false
            return true
        }
    }


    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SendExhibitFragment().apply { }
    }
}