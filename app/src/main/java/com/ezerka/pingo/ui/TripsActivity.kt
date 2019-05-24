package com.ezerka.pingo.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toolbar
import com.ezerka.pingo.R

class TripsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trips)

        assignTheViews()

    }

    private fun assignTheViews() {
        val toolbar: Toolbar = findViewById(R.id.id_Toolbar_TripsActivity)
        toolbar.navigationIcon = getDrawable(R.drawable.ic_icon_back)

    }
}