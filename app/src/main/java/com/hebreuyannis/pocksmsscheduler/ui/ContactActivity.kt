package com.hebreuyannis.pocksmsscheduler.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.github.tamir7.contacts.Contact
import com.github.tamir7.contacts.Contacts
import com.github.tamir7.contacts.Query
import com.hebreuyannis.pocksmsscheduler.R
import com.hebreuyannis.pocksmsscheduler.model.Transaction
import com.hebreuyannis.pocksmsscheduler.model.TransactionDatabase
import com.hebreuyannis.pocksmsscheduler.model.toEntity
import com.hebreuyannis.pocksmsscheduler.worker.LogRegistryWorker
import com.hebreuyannis.pocksmsscheduler.worker.SmsDispatchWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


class ContactActivity : AppCompatActivity() {

    lateinit var mapper: List<Pair<String, String>>
    private val liveData = MutableLiveData<List<String>>()
    private val receiverContact = ArrayList<Transaction>()
    private val permissionReadContact = 2
    var att = 20L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact)
        val recycler = findViewById<ListView>(R.id.recyclerview1)
        liveData.observe(this, {
            val itemsAdapter: ArrayAdapter<String> =
                ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, it)
            recycler.adapter = itemsAdapter
        })

        recycler.setOnItemClickListener { _, _, position, _ ->
            mapper[position].let {
                val transaction =
                    Transaction(
                        it.first,
                        it.second,
                        "PockSmsScheduler : ${Date()} : ${it.first} delivery msg",
                        System.currentTimeMillis() + 20_000L
                    )
                att += 60
                Toast.makeText(this, "scheduled ${it.first} to 20 sec", Toast.LENGTH_SHORT).show()
                registerRequest(transaction)
                transactionLive.value = receiverContact
            }
        }

        requestPermission()
    }

    private fun retriveContact() {
        GlobalScope.launch(Dispatchers.IO) {
            val q: Query = Contacts.getQuery()
            val contacts: List<Contact> = q.hasPhoneNumber().find()
            val number = contacts.map { it.displayName }
            mapper = contacts.map { it.displayName to it.phoneNumbers.first().normalizedNumber }
            liveData.postValue(number)
        }
    }

    private fun registerRequest(transaction: Transaction) {
        GlobalScope.launch(Dispatchers.IO) {
            val dataSource = TransactionDatabase.getInstance(baseContext).transactionDatabaseDao
            dataSource.insert(transaction.toEntity())
            val workManager = WorkManager.getInstance(application)
            set(workManager, transaction)
        }
    }

    fun set(workManager: WorkManager, transactions: Transaction) {
        val blurRequest = OneTimeWorkRequestBuilder<SmsDispatchWorker>()
            .setInitialDelay(transactions.geTimeMillisSinceNow(), TimeUnit.MILLISECONDS)
            .addTag(transactions.id)
            .setInputData(transactions.convertToWorkData())
            .build()

        val test = OneTimeWorkRequestBuilder<LogRegistryWorker>()
            .addTag(transactions.id)
            .build()

        workManager.beginWith(blurRequest).then(test).enqueue()

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            permissionReadContact -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    retriveContact()
                } else {
                    requestPermission()
                }
            }
        }
    }

    private fun requestPermission(){
        val callPermissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
        if(callPermissionCheck != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CONTACTS),permissionReadContact)
        }else{
            retriveContact()
        }
    }
}