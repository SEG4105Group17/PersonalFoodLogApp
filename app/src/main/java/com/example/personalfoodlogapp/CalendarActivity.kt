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
        globalApp.getDailyGoalsData {
            updateGoalsBar(globalApp.currentDate.year,globalApp.currentDate.month.value,globalApp.currentDate.dayOfMonth)
        }

        // Make the calendar actually update when the user clicks
        val calendarView = findViewById<CalendarView>(R.id.calendar)
        calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            val selectedTimeInMillis = java.util.Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }.timeInMillis
            calendarView.date = selectedTimeInMillis

            // Update the goals bar for the selected date
            updateGoalsBar(year, month, dayOfMonth)
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

    fun updateGoalsBar(year: Int, month: Int, dayOfMonth: Int) {
        var dayGoals = globalApp.dailyCalorieGoals.get(String.format("%04d-%02d-%02d", year, month+1, dayOfMonth))

        if (dayGoals == null) {
            dayGoals = Pair(0,0)
        }

        val goalTextView = findViewById<TextView>(R.id.progressBarText)
        goalTextView.setText(String.format("%d", dayGoals.first) + "/" + String.format("%d", dayGoals.second))

        val progressBar = findViewById<ProgressBar>(R.id.goalsProgressBar)
        progressBar.setMax(dayGoals.second)
        progressBar.setProgress(dayGoals.first)

    }
}