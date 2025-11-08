package com.example.personalfoodlogapp

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.round

class ItemListActivity : AppCompatActivity(), ItemListAdapter.ClickListener {
    lateinit var globalApp: PersonalFoodApplication
    lateinit var adapter: ItemListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_item_list)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        globalApp = applicationContext as PersonalFoodApplication

        // Set up the RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.setLayoutManager(LinearLayoutManager(this))
        adapter = ItemListAdapter(this, globalApp.foodItems)
        adapter.setClickListener(this)
        recyclerView.setAdapter(adapter)

        updateTotalMacro()
    }

    override fun onDeleteClick(view: View, position: Int) {
        globalApp.foodItems.removeAt(position)
        adapter.notifyItemRemoved(position)
        updateTotalMacro()
    }

    override fun onEditClick(view: View?, position: Int) {
        // TO BE IMPLEMENTED
    }

    override fun onViewClick(view: View?, position: Int) {
        // TO BE IMPLEMENTED
    }

    fun updateTotalMacro() {
        val calorie = findViewById<TextView>(R.id.totalCal)
        val sodium = findViewById<TextView>(R.id.totalSodium)
        val fat = findViewById<TextView>(R.id.totalFat)
        val sugar = findViewById<TextView>(R.id.totalSugar)

        var calorieTotal = 0.0
        var sodiumTotal = 0.0
        var fatTotal = 0.0
        var sugarTotal = 0.0

        globalApp.foodItems.forEach({ item ->
            var macros = globalApp.foodMacroMap.get(item.first)
            if (macros != null) {
                calorieTotal += macros.get(0)*item.second
                sodiumTotal += macros.get(1)*item.second
                fatTotal += macros.get(2)*item.second
                sugarTotal += macros.get(3)*item.second
            }
        })

        calorie.setText("Total Calories: " + round(calorieTotal))
        sodium.setText("Total Sodium: " + round(sodiumTotal))
        fat.setText("Total Fat: " + round(fatTotal))
        sugar.setText("Total Sugar: " + round(sugarTotal))
    }
}