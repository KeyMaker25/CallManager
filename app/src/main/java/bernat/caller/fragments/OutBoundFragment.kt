package bernat.caller.fragments

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import bernat.caller.MainActivity
import bernat.caller.models.Contact
import bernat.caller.R
import bernat.caller.receivers_interface.changeStateListener
import org.jetbrains.anko.makeCall

class OutBoundFragment : Fragment(), changeStateListener {


    private var havePermissionToCall = false
    private var havePermissionReadContacts = false
    private var contactListView: ListView? = null
    private var adapter: ArrayAdapter<Contact>? = null
    private var canRun: Boolean = true
    private var btnCall : Button? = null
    private var stopBtnCall : Button? = null
    private var listOfContacts = ArrayList<Contact>()
    private var myContext:Context? = null
    var index = 0
    private val PHONE_AVAILBLE = 1


    override fun stateChanged(state: Int) {
        if (canRun) {
            if (state == PHONE_AVAILBLE) {
                index++
                if (index < listOfContacts.size) {
                    print("state : $state")
                    Toast.makeText(myContext, "call next contact: ${listOfContacts[index].name}", Toast.LENGTH_LONG).show()
                    nextCall()
                } else {
                    Toast.makeText(myContext, "no more people to call", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        havePermissionToCall = checkPermissionToCall()
        havePermissionReadContacts = checkPermissionReadContacts()
        if (!havePermissionReadContacts){
            makeRequestReadContacts()
        }
        if (!havePermissionToCall){
            makeRequestToCall()
        }
        listOfContacts.add(Contact("123456789", "test"))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = inflater.inflate(R.layout.fragment_outnboundfragment, container, false)
        contactListView = view.findViewById(R.id.contacts_list)
        btnCall = view.findViewById(R.id.btnCalling)
        stopBtnCall = view.findViewById(R.id.stopCallBtn)

        if (havePermissionReadContacts) {
            getContactList()
            adapter = ArrayAdapter(myContext!!,R.layout.contact_list_item, listOfContacts)
            contactListView!!.adapter= adapter
            if (!havePermissionToCall) {
                println("no call permission")
            }
        }else{
            println("no read permission")
        }

        btnCall!!.setOnClickListener {
            if (havePermissionReadContacts && havePermissionToCall) {
                canRun = true
                nextCall()
                stopBtnCall!!.isEnabled = true
            }
        }
        stopBtnCall!!.setOnClickListener {
            print("stop!")
            canRun = false
            stopBtnCall!!.isEnabled = false
        }

        return view
    }



    private fun nextCall() {
        MainActivity.receiver!!.setListener(this)
        if (myContext == null) myContext = context
        myContext!!.makeCall(listOfContacts[index].number)
    }

    private fun getContactList() {
        val cr = context!!.contentResolver
        val cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null)
        if (cur?.count ?: 0 > 0) {
            while (cur != null && cur.moveToNext()) {
                val id = cur.getString(
                    cur.getColumnIndex(ContactsContract.Contacts._ID)
                )
                val name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                if (cur.getInt(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0)
                {
                    val pCur = cr.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        arrayOf(id), null)
                    while (pCur!!.moveToNext()) {
                        val phoneNo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                        listOfContacts.add(Contact(phoneNo, name))
                    }
                    pCur.close()
                }
            }
        }
        cur?.close()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        myContext = context
    }

    private fun checkPermissionToCall(): Boolean {
        if (ContextCompat.checkSelfPermission(myContext!!, Manifest.permission.CALL_PHONE)
            != PackageManager.PERMISSION_GRANTED) {
            return false
        }
        return true
    }

    private fun checkPermissionReadContacts() : Boolean{
        if (ContextCompat.checkSelfPermission(myContext!!, Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED) {
            return false
        }
        return true

    }

    private fun makeRequestToCall() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity!!,
                Manifest.permission.CALL_PHONE)) {
            Log.i("Info", "need to explain why we need CALL_PHONE permission")
        }
        // No explanation needed, we can request the permission.
        requestPermissions(arrayOf(Manifest.permission.CALL_PHONE), 0)

    }

    private fun makeRequestReadContacts(){
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity!!,
                Manifest.permission.READ_CONTACTS)) {
            Log.i("Info","need to explain why we need READ_CONTACTS permission")
        }
        // No explanation needed, we can request the permission.
        requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS), 0)

        if (checkPermissionReadContacts()){
            getContactList()
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden){
            MainActivity.receiver!!.setListener(this)
            println("listener OutBound")
        }
    }

    override fun toString(): String {
        return "OutBound Fragment"
    }


}