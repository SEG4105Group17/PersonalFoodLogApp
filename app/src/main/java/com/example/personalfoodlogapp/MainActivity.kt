package com.example.personalfoodlogapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.SimpleDateFormat
import java.util.Date


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // TO BE IMPLEMENTED: Make the login/register screen the first thing to load
        
        setDate()
        // TO BE IMPLEMENTED: READ DATABASE AND SEND THE DATA HERE
        setCalorieCounts(1,2)

        // Calendar button moves to the calendar activity... Is there a better way to do this?
        val calendarButton = findViewById<ImageButton>(R.id.calendarButton)
        calendarButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                startActivity(Intent(this@MainActivity, CalendarActivity::class.java))
            }
        })
    }

    private fun setCalorieCounts(currentCalories: Int, goalCalories: Int){
        val dateTextView = findViewById<TextView>(R.id.calorieCount)
        dateTextView.setText(String.format("%d", currentCalories) + "/" + String.format("%d", goalCalories))


        val progressBar = findViewById<ProgressBar>(R.id.calorieProgressBar)
        progressBar.setMax(goalCalories)
        progressBar.setProgress(currentCalories)
    }

    private fun setDate() {
        val sdf = SimpleDateFormat("MMM dd, yyyy")
        val currentDate = sdf.format(Date())

        val dateTextView = findViewById<TextView>(R.id.dateText)
        dateTextView.setText(currentDate)
    }

}