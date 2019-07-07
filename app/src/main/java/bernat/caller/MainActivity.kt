package bernat.caller

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*
import android.content.IntentFilter
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import bernat.caller.fragments.InBoundFragment
import bernat.caller.fragments.OutBoundFragment
import bernat.caller.receivers_interface.PhoneStateBroadcastReceiver
import bernat.caller.receivers_interface.changeStateListener


class MainActivity : AppCompatActivity() {

    companion object {
        var receiver: PhoneStateBroadcastReceiver? = null
    }

    private val inCall = InBoundFragment()
    private val outCall = OutBoundFragment()
    private val fm = supportFragmentManager
    private lateinit var active : Fragment
    var currentItem = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)


        fm.beginTransaction().add(R.id.main_container, inCall).hide(inCall).commit()
        fm.beginTransaction().add(R.id.main_container, outCall).commit()
        active = outCall

        receiver = PhoneStateBroadcastReceiver()
        val action = IntentFilter()
        action.addAction("android.intent.action.PHONE_STATE")

        registerReceiver(receiver,action)

    }



    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        if (currentItem != item.itemId)
        {
            currentItem = item.itemId
            when (item.itemId) {
                //this is the place to load the Auto Dialer View and Fragment
                R.id.Outbound_calls -> {

                    println("click OutCalls")
                    fm.beginTransaction().hide(active).show(outCall).commit()
                    active = outCall

                    return@OnNavigationItemSelectedListener true
                }
                //this is where we load incoming calls handling
                R.id.InBound_calls -> {
                    //load fragment
                    println("click InCalls")
                    fm.beginTransaction().hide(active).show(inCall).commit()
                    active = inCall

                    return@OnNavigationItemSelectedListener true
                }
            }
        }
        false
    }


}

