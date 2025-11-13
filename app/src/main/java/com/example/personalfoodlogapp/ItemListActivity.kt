package com.example.personalfoodlogapp

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
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
        adapter = ItemListAdapter(this, globalApp.foodItems, globalApp.foodMacroMap.keys.toCollection(ArrayList()))
        adapter.setClickListener(this)
        recyclerView.setAdapter(adapter)

        // Add item button
        val addItemButton = findViewById<Button>(R.id.addItemButton)
        addItemButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                sendItemDialog(null)
            }
        })

        // Back to main page button
        val backButton = findViewById<Button>(R.id.backButton)
        backButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                startActivity(Intent(this@ItemListActivity, MainActivity::class.java))
                finish()
            }
        })

        updateTotalMacro()
    }

    override fun onDeleteClick(view: View, position: Int) {
        globalApp.foodItems.removeAt(position)
        globalApp.sendDataToServer(globalApp.currentDate)
        adapter.notifyItemRemoved(position)
        updateTotalMacro()
    }

    override fun onEditClick(view: View?, position: Int) {
        sendItemDialog(position)
    }

    fun setItem(position: Int?, foodItem: String, gramCount: Int) {
        val updatedFoodItem = Pair<String, Int>(first = foodItem, second = gramCount)

        if (position != null) {
            globalApp.foodItems.removeAt(position)
            globalApp.foodItems.add(position, updatedFoodItem)
            adapter.notifyItemChanged(position)
        } else {
            globalApp.foodItems.add(updatedFoodItem)
            adapter.notifyItemInserted(globalApp.foodItems.size - 1)
        }

        globalApp.sendDataToServer(globalApp.currentDate)
        updateTotalMacro()
    }

    fun sendItemDialog(position: Int?) {

        // Create the dialog
        val dialog = Dialog(this@ItemListActivity)
        dialog.setContentView(R.layout.set_item_details)
        val instructionText = dialog.findViewById<TextView>(R.id.instructionText)
        if (position == null) {
            instructionText.text = "Add Item"
            dialog.setTitle("Add Item")
        } else {
            instructionText.text = "Edit Item"
            dialog.setTitle("Edit Item")
        }

        // Set up the autocomplete text adapter
        val possibleFoodItems = globalApp.foodMacroMap.keys.toList()
        val autocompleteAdapter: ArrayAdapter<String?> = ArrayAdapter<String?>(this, android.R.layout.simple_dropdown_item_1line, possibleFoodItems)
        val autocompleteTextView = dialog.findViewById<AutoCompleteTextView>(R.id.itemNameAutocomplete)
        autocompleteTextView.setAdapter<ArrayAdapter<String?>?>(autocompleteAdapter)

        // Grab the gram box text
        val gramBox = dialog.findViewById<EditText>(R.id.gramBox)

        // Set the text if editing an item
        if (position != null) {
            autocompleteTextView.setText(globalApp.foodItems.get(position).first)
            gramBox.setText("" + globalApp.foodItems.get(position).second)
        }

        // Confirm button locks in the change
        val confirmItemButton = dialog.findViewById<Button>(R.id.confirmItemButton)
        confirmItemButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {

                var gramBoxText = gramBox.text.toString()
                var itemNameText = autocompleteTextView.text.toString()

                if (gramBoxText != "") {
                    if (position == null) {
                        setItem(null, itemNameText,gramBoxText.toInt())
                    } else {
                        setItem(position,itemNameText,gramBoxText.toInt())
                    }
                }
                dialog.dismiss()
            }
        })

        dialog.show()
    }

    override fun onViewClick(view: View?, position: Int) {
        val foodItem = globalApp.foodItems.get(position)

        val dialog = Dialog(this@ItemListActivity)
        dialog.setContentView(R.layout.view_item_dialog)
        dialog.setTitle("View Item")

        val itemName = dialog.findViewById<TextView>(R.id.itemName)
        itemName.text = foodItem.first

        var macros = globalApp.foodMacroMap.get(foodItem.first)
        if (macros == null) {
            macros = arrayListOf(0.0,0.0,0.0,0.0)
        }
        val calValue = dialog.findViewById<TextView>(R.id.calValue)
        calValue.setText("" + macros.get(0)*foodItem.second)
        val sodValue = dialog.findViewById<TextView>(R.id.sodValue)
        sodValue.setText("" + macros.get(1)*foodItem.second + "g")
        val fatValue = dialog.findViewById<TextView>(R.id.fatValue)
        fatValue.setText("" + macros.get(2)*foodItem.second + "g")
        val sugValue = dialog.findViewById<TextView>(R.id.sugValue)
        sugValue.setText("" + macros.get(3)*foodItem.second + "g")

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
            if (macros == null) {
                macros = arrayListOf(0.0,0.0,0.0,0.0)
            }
            calorieTotal += macros.get(0)*item.second
            sodiumTotal += macros.get(1)*item.second
            fatTotal += macros.get(2)*item.second
            sugarTotal += macros.get(3)*item.second
        })

        calorie.setText("Total Calories: " + round(calorieTotal))
        sodium.setText("Total Sodium: " + round(sodiumTotal) + "g")
        fat.setText("Total Fat: " + round(fatTotal) + "g")
        sugar.setText("Total Sugar: " + round(sugarTotal) + "g")
    }
}