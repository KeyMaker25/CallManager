package bernat.caller.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import bernat.caller.MainActivity
import bernat.caller.receivers_interface.changeStateListener
import android.widget.Switch
import android.widget.TextView


class InBoundFragment : Fragment(), changeStateListener {

    private var switchAuto: Switch? = null
    private var picker : NumberPicker? = null
    private var myContext : Context? = null
    private var numberPickerShower: TextView? = null
    private val PHONE_AVAILBLE = 1
    private val PHONE_RINGING = 2

    companion object {
        var secBeforePickUp:Long = 1
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(bernat.caller.R.layout.fragment_inboundfragment, container, false)
        picker = view.findViewById(bernat.caller.R.id.numberPicker)
        switchAuto = view.findViewById(bernat.caller.R.id.fragment_switch)
        numberPickerShower = view.findViewById(bernat.caller.R.id.numberPickerShower)
        setPicker()

        return view
    }

    fun setPicker(){
        picker!!.maxValue = 10
        picker!!.minValue = 2
        picker!!.value = 3
        picker!!.wrapSelectorWheel = false

        picker!!.setOnValueChangedListener { _, _, newVal ->
            numberPickerShower!!.text = "Time $newVal"

            secBeforePickUp = (newVal * 1000).toLong()
        }
    }

    override fun onStart() {
        super.onStart()
        MainActivity.receiver?.setListener(this)
    }

    override fun stateChanged(state: Int) {
        if (state == PHONE_RINGING) {
            println("state: $state")
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        this.myContext = context
    }


    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden){
            MainActivity.receiver!!.setListener(this)

            println("listener InBound")
        }

    }

    override fun toString(): String {
        return "InBound Fragment"
    }



}