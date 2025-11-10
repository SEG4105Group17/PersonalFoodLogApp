package com.example.personalfoodlogapp

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class LoginActivity : AppCompatActivity() {
    lateinit var globalApp: PersonalFoodApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Load the global data
        globalApp = applicationContext as PersonalFoodApplication

        // Login button moves to the main activity on successful login
        val emailBox = findViewById<EditText>(R.id.emailBox)
        val passwordBox = findViewById<EditText>(R.id.passwordBox)
        val loginButton = findViewById<Button>(R.id.loginButton)
        loginButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                globalApp.attemptLogin(
                    emailBox.text.toString(),
                    passwordBox.text.toString(),
                    {globalApp.getDataFromServer(globalApp.currentDate) {
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    }},
                    {showLoginFailDialog()}
                )
            }
        })
    }

    fun showLoginFailDialog() {
        // Create the dialog
        val dialogBuilder = AlertDialog.Builder(this@LoginActivity)
        dialogBuilder.setMessage("Please try again with valid credentials")
        dialogBuilder.setTitle("Login Failed")

        val dialog = dialogBuilder.create()
        dialog.show()
    }

}