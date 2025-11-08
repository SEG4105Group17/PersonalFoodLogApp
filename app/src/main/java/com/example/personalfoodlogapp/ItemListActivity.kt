package com.example.personalfoodlogapp

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
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
        val foodItem = globalApp.foodItems.get(position)

        val dialog = Dialog(this@ItemListActivity)
        dialog.setContentView(R.layout.set_item_grams)
        dialog.setTitle("Set Grams")

        val itemName = dialog.findViewById<TextView>(R.id.itemName)
        itemName.text = foodItem.first

        val confirmGramsButton = dialog.findViewById<Button>(R.id.confirmGramsButton)
        confirmGramsButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                val gramBox = dialog.findViewById<EditText>(R.id.gramBox)
                var gramBoxText = gramBox.text.toString()
                if (gramBoxText != "") {
                    setItemGrams(position,gramBoxText.toInt())
                }
                dialog.dismiss()
            }
        })

        dialog.show()
    }

    fun setItemGrams(position: Int, gramCount: Int) {
        var foodItem = globalApp.foodItems.get(position)
        val updatedFoodItem = foodItem.copy(first = foodItem.first, second = gramCount)

        globalApp.foodItems.removeAt(position)
        globalApp.foodItems.add(position, updatedFoodItem)

        adapter.notifyItemChanged(position)
        updateTotalMacro()
    }



    override fun onViewClick(view: View?, position: Int) {
        val foodItem = globalApp.foodItems.get(position)

        val dialog = Dialog(this@ItemListActivity)
        dialog.setContentView(R.layout.view_item_dialog)
        dialog.setTitle("View Item")

        val itemName = dialog.findViewById<TextView>(R.id.itemName)
        itemName.text = foodItem.first

        val macros = globalApp.foodMacroMap.get(foodItem.first)
        if (macros != null) {
            val calValue = dialog.findViewById<TextView>(R.id.calValue)
            calValue.setText("" + macros.get(0)*foodItem.second + "g")
            val sodValue = dialog.findViewById<TextView>(R.id.sodValue)
            sodValue.setText("" + macros.get(1)*foodItem.second + "g")
            val fatValue = dialog.findViewById<TextView>(R.id.fatValue)
            fatValue.setText("" + macros.get(2)*foodItem.second + "g")
            val sugValue = dialog.findViewById<TextView>(R.id.sugValue)
            sugValue.setText("" + macros.get(3)*foodItem.second + "g")
        }

        dialog.show()
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

        calorie.setText("Total Calories: " + round(calorieTotal) + "g")
        sodium.setText("Total Sodium: " + round(sodiumTotal) + "g")
        fat.setText("Total Fat: " + round(fatTotal) + "g")
        sugar.setText("Total Sugar: " + round(sugarTotal) + "g")
    }
}