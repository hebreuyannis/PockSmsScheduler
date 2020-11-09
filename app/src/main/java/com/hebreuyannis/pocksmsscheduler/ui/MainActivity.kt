package com.hebreuyannis.pocksmsscheduler.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.hebreuyannis.pocksmsscheduler.R
import com.hebreuyannis.pocksmsscheduler.model.Transaction
import com.hebreuyannis.pocksmsscheduler.model.TransactionDatabase
import com.hebreuyannis.pocksmsscheduler.model.TransactionPresentation
import com.hebreuyannis.pocksmsscheduler.model.toPresentation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


val transactionLive = MutableLiveData<ArrayList<Transaction>>()

class MainActivity : AppCompatActivity() {

    private val transactionList = MutableLiveData<List<TransactionPresentation>>()
    lateinit var adapter: TransactionAdapter
    val permissionCallSMS = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        adapter = TransactionAdapter { }
        val recyclerAdapter = findViewById<RecyclerView>(R.id.mainRecycler)
        recyclerAdapter.adapter = adapter

        requestPermission()

        transactionList.observe(this, {
            adapter.submitList(it)
        })

        val button = findViewById<Button>(R.id.button)
        button.setOnClickListener {
            startActivity(Intent(this, ContactActivity::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        getTransaction()
    }

    private fun getTransaction() {
        GlobalScope.launch(Dispatchers.IO) {
            val dataSource = TransactionDatabase.getInstance(baseContext).transactionDatabaseDao
            val transactions = dataSource.getAllTransactions()
            CoroutineScope(Dispatchers.Main).launch {
                transactionList.value = transactions.map { it.toPresentation() }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            permissionCallSMS -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    requestPermission()
                }
            }
        }
    }

    private fun requestPermission(){
        val callPermissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
        if(callPermissionCheck != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS),permissionCallSMS)
        }
    }

}
