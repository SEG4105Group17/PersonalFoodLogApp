package com.example.personalfoodlogapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.File
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


        // Calendar button moves to the calendar activity
        val calendarButton = findViewById<ImageButton>(R.id.calendarButton)
        calendarButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                startActivity(Intent(this@MainActivity, CalendarActivity::class.java))
            }
        })

        // Photo button asks phone for a photo
        // TO BE IMPLEMENTED: SEND THE PHOTO TO A DATABASE INSTEAD OF STORING IT LOCALLY
        val photoButton = findViewById<Button>(R.id.captureFoodButton)
        photoButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                invokeCamera()
            }
        })

    }


    // Camera Stuff:    https://www.youtube.com/watch?v=T8T1HAUdz1Y

    // Uniform Resource Identifier
    private var uri: Uri? = null
    // Variable that makes the phone take an image
    private val getCameraImage = registerForActivityResult(ActivityResultContracts.TakePicture()) {
        success ->
        if (success) {
            Log.i(null,"Image saved: $uri")
        } else {
            Log.e(null,"ERROR: Image not saved")
        }
    }
    // Create image file, invoke taking picture, save picture to image file
    private fun invokeCamera() {
        val file = createImageFile()
        try {
            uri = FileProvider.getUriForFile(this, "com.example.personalfoodlogapp.fileprovider", file)
        } catch (e: Exception) {
            Log.e(null,"ERROR: ${e.message}")
        }
        getCameraImage.launch(uri)
    }
    // Create an image file
    private fun createImageFile() : File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "PersonalFoodApp_${timestamp}", ".jpg", imageDirectory
        )
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