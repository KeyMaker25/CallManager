package bernat.caller.receivers_interface

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log
import java.util.*
import android.view.KeyEvent
import java.io.IOException
import bernat.caller.fragments.InBoundFragment.Companion.secBeforePickUp


class PhoneStateBroadcastReceiver : BroadcastReceiver() {


    private var listener : changeStateListener? = null

    private var lastState = TelephonyManager.CALL_STATE_IDLE
    private var state = 0
    private var callStartTime: Date? = null
    private var isIncoming: Boolean = false
    private var savedNumber: String? = null
    private var number:String? = ""
    private val PHONE_AVAILBLE = 1
    private val PHONE_RINGING = 2


    fun setListener(listener: changeStateListener) {
        this.listener = listener
    }

    override fun onReceive(context: Context, intent: Intent) {
        println("intent action : ${intent.action}")
        if (intent.action == "android.intent.action.NEW_OUTGOING_CALL") {
            savedNumber = intent.extras!!.getString("android.intent.extra.PHONE_NUMBER")

        } else {
            val stateStr = intent.extras!!.getString(TelephonyManager.EXTRA_STATE)
            number = intent.extras!!.getString(TelephonyManager.EXTRA_INCOMING_NUMBER)
            when (stateStr) {
                TelephonyManager.EXTRA_STATE_IDLE -> state = TelephonyManager.CALL_STATE_IDLE
                TelephonyManager.EXTRA_STATE_OFFHOOK -> state = TelephonyManager.CALL_STATE_OFFHOOK
                TelephonyManager.EXTRA_STATE_RINGING -> state = TelephonyManager.CALL_STATE_RINGING
            }

        }
        onCallStateChanged(context, number)
    }

    private fun onCallStateChanged(context: Context, number: String?) {

        println("current state $state")
        println("last state $lastState")
        if (lastState == state) {
            //No change, debounce extras
            println("same same")
            return
        }
        when (state) {
            TelephonyManager.CALL_STATE_RINGING -> {
                isIncoming = true
                callStartTime = Date()
                savedNumber = number
                Log.i("info","call Ringing")

                if (listener != null){
                    listener!!.stateChanged(PHONE_RINGING)
                }else{
                    println("listener is null, working from BroadCast")
                    handleIncomingCall(context)
                }

            }
            TelephonyManager.CALL_STATE_OFFHOOK -> {
                //Transition of ringing->offhook are pickups of incoming calls.  Nothing done on them
                if (lastState != TelephonyManager.CALL_STATE_RINGING) {
                    isIncoming = false
                    callStartTime = Date()

                    Log.i("info","Outgoing Call")
                }else {
                    Log.i("info","phone incoming call")

                }
            }
            TelephonyManager.CALL_STATE_IDLE -> {
                Log.i("info","phone Available")
                //Went to idle-  this is the end of a call.  What type depends on previous state(s)
                if (listener != null){
                    listener!!.stateChanged(PHONE_AVAILBLE)

                }else{
                    println("listener is null")
                }
                when {
                    lastState == TelephonyManager.CALL_STATE_RINGING -> {
                        //Ring but no pickup -  a miss
                        Log.i("info","Ringing but no pickup $savedNumber Call time $callStartTime  Date " + Date())
                    }
                    isIncoming -> {
                        Log.i("info","Incoming $savedNumber Call time $callStartTime")
                    }else -> {
                    Log.i("info","outgoing $savedNumber Call time $callStartTime Date " + Date())
                    }
                }
            }
        }

        lastState = state
    }

    private fun handleIncomingCall(context: Context) {
        // answer call
        //Toast.makeText(context,"In coming call",Toast.LENGTH_SHORT).show()

        Thread(Runnable {

            Thread.sleep(secBeforePickUp)
            Log.i("time waiting", secBeforePickUp.toString())
            try {

                Runtime.getRuntime().exec("input keyevent " + Integer.toString(KeyEvent.KEYCODE_HEADSETHOOK))

            } catch (e: IOException) {

                print("Exception $e")

                val enforcedPerm = "android.permission.CALL_PRIVILEGED"
                val btnDown = Intent(Intent.ACTION_MEDIA_BUTTON).putExtra(
                    Intent.EXTRA_KEY_EVENT, KeyEvent(
                        KeyEvent.ACTION_DOWN,
                        KeyEvent.KEYCODE_HEADSETHOOK
                    )
                )
                val btnUp = Intent(Intent.ACTION_MEDIA_BUTTON).putExtra(
                    Intent.EXTRA_KEY_EVENT, KeyEvent(
                        KeyEvent.ACTION_UP,
                        KeyEvent.KEYCODE_HEADSETHOOK
                    )
                )

                context.sendOrderedBroadcast(btnDown, enforcedPerm)
                context.sendOrderedBroadcast(btnUp, enforcedPerm)
            }
        }).start()


    }

}