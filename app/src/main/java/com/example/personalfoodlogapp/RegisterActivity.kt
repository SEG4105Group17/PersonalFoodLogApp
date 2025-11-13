package com.example.personalfoodlogapp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class RegisterActivity : AppCompatActivity() {
    lateinit var globalApp: PersonalFoodApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Load the global data
        globalApp = applicationContext as PersonalFoodApplication

        // Register button creates an account
        val emailBox = findViewById<EditText>(R.id.emailBox)
        val passwordBox = findViewById<EditText>(R.id.passwordBox)
        val registerButton = findViewById<Button>(R.id.registerButton)
        registerButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                globalApp.attemptRegistration(
                    emailBox.text.toString(),
                    passwordBox.text.toString(),
                    {globalApp.getDataFromServer(globalApp.currentDate) {
                        startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
                    }},
                    {showRegistrationFailDialog()}
                )
            }
        })

        // login button just moves you to the login page
        val loginButton = findViewById<Button>(R.id.loginButton)
        loginButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                finish()
            }
        })
    }

    fun showRegistrationFailDialog() {
        // Create the dialog
        val dialogBuilder = AlertDialog.Builder(this@RegisterActivity)
        dialogBuilder.setMessage("Please try again at a later date")
        dialogBuilder.setTitle("Registration Failed")

        val dialog = dialogBuilder.create()
        dialog.show()
    }


}