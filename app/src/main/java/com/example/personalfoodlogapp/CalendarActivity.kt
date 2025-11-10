package com.example.personalfoodlogapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CalendarView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class CalendarActivity : AppCompatActivity() {
    lateinit var globalApp: PersonalFoodApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_calendar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        globalApp = applicationContext as PersonalFoodApplication
        // The calendar screen will always have data from the current date
        globalApp.getDataFromServer(LocalDate.now()) {
            updateGoalsBar()
        }

        // Make the calendar actually update when the user clicks
        val calendarView = findViewById<CalendarView>(R.id.calendar)
        calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            val selectedTimeInMillis = java.util.Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }.timeInMillis
            calendarView.date = selectedTimeInMillis
        }


        // Date confirm button - returns to main activity after loading the new data
        val dateButton = findViewById<Button>(R.id.changeDateButton)
        dateButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                val calendarView = findViewById<CalendarView>(R.id.calendar)
                globalApp.getDataFromServer(Instant.ofEpochMilli(calendarView.date).atZone(ZoneId.systemDefault()).toLocalDate()) {
                    startActivity(Intent(this@CalendarActivity, MainActivity::class.java))
                    finish()
                }
            }
        })


    }

    fun updateGoalsBar() {
        val goalTextView = findViewById<TextView>(R.id.goalsCompleteText)
        goalTextView.setText(String.format("%d", globalApp.monthlyGoalsMet) + "/" + String.format("%d", globalApp.monthlyGoalsPossible))


        val progressBar = findViewById<ProgressBar>(R.id.goalsProgressBar)
        progressBar.setMax(globalApp.monthlyGoalsPossible)
        progressBar.setProgress(globalApp.monthlyGoalsMet)

    }
}