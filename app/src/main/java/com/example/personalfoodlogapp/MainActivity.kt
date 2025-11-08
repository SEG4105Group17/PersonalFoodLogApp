package com.example.personalfoodlogapp

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date


class MainActivity : AppCompatActivity() {
    // For global variables
    lateinit var globalApp: PersonalFoodApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Load the global data
        globalApp = applicationContext as PersonalFoodApplication
        setDate()
        updateCalorieCounts()


        // Calendar button moves to the calendar activity
        val calendarButton = findViewById<ImageButton>(R.id.calendarButton)
        calendarButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                startActivity(Intent(this@MainActivity, CalendarActivity::class.java))
                finish()
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

        // Target button opens the popup
        val targetButton = findViewById<Button>(R.id.editTargetButton)
        targetButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                val dialog = Dialog(this@MainActivity)
                dialog.setContentView(R.layout.set_target_dialog)
                dialog.setTitle("Set Target")
                dialog.show()

                // Confirm button runs the update target function
                val confirmTarget = dialog.findViewById<Button>(R.id.confirmTargetButton)
                confirmTarget.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View?) {
                        val calorieCount = dialog.findViewById<EditText>(R.id.calorieBox)
                        var calorieCountText = calorieCount.text.toString()
                        if (calorieCountText == "") {
                            calorieCountText = "0"
                        }
                        setCalorieGoal(calorieCountText.toInt())
                        dialog.dismiss()
                    }
                })
            }
        })

        // Button to add food items manually
        val enterFoodButton = findViewById<Button>(R.id.viewItemsButton)
        enterFoodButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                startActivity(Intent(this@MainActivity, ItemListActivity::class.java))
                finish()
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

    private fun setCalorieGoal(calorieGoal: Int) {
        globalApp.calorieGoal = calorieGoal
        globalApp.sendDataToServer(globalApp.currentDate)
        updateCalorieCounts()
    }

    private fun updateCalorieCounts(){
        val dateTextView = findViewById<TextView>(R.id.calorieCount)
        dateTextView.setText(String.format("%d", globalApp.getCalorieCurrent()) + "/" + String.format("%d", globalApp.calorieGoal))


        val progressBar = findViewById<ProgressBar>(R.id.calorieProgressBar)
        progressBar.setMax(globalApp.calorieGoal)
        progressBar.setProgress(globalApp.getCalorieCurrent())
    }

    private fun setDate() {
        val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
        val currentDate = globalApp.currentDate.format(formatter)

        val dateTextView = findViewById<TextView>(R.id.dateText)
        dateTextView.setText(currentDate)
    }

}