package com.example.personalfoodlogapp;

import android.app.Application;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import kotlin.Pair;

public class PersonalFoodApplication extends Application {

    // Note: Apparently this object is re-created anytime the app is closed/killed in the background
    // So whenever its killed, requiresReload is set to true and we need to grab data from the server
    public boolean requiresReload = true;

    // These 2 are the same irrespective of the selected date
    public int monthlyGoalsMet;
    public int monthlyGoalsPossible;

    // This data depends on the selected date
    public LocalDate currentDate;
    public int calorieGoal;
    public ArrayList<Pair<String, Integer>> foodItems;

    // Table of macros... Listed per gram (Calorie, Sodium, Fat, Sugar)
    public Map<String, ArrayList<Double>> foodMacroMap = new HashMap<>() {{
        put("Potato", new ArrayList<>(Arrays.asList(0.77, 0.00003, 0.0013, 0.005)));
        put("Tomato", new ArrayList<>(Arrays.asList(0.18, 0.00005, 0.002, 0.026)));
        put("Lettuce", new ArrayList<>(Arrays.asList(0.15, 0.0003, 0.001, 0.012)));
    }};

    public void onCreate() {
        super.onCreate();

        if (requiresReload) {
            currentDate = LocalDate.now();
            getDataFromServer(currentDate);
            requiresReload = false;
        }
    }

    public int getCalorieCurrent() {
        // Calculate total calorie
        double calorieTotal = 0.0;
        for (Pair<String, Integer> item: foodItems) {
            ArrayList<Double> macros = foodMacroMap.get(item.getFirst());
            if (macros != null) {
                calorieTotal += macros.get(0)*item.getSecond();
            }
        }
        return (int)calorieTotal;
    }

    public void getDataFromServer(LocalDate desiredDataDate) {
        currentDate = desiredDataDate;
        monthlyGoalsPossible = currentDate.getDayOfMonth();

        // TO BE IMPLEMENTED: READ DATABASE FOR DATA ON THIS DATE
        // Fake data
        foodItems = new ArrayList<>();
        foodItems.add(new Pair<>("Potato", 400));
        foodItems.add(new Pair<>("Tomato", 200));
        foodItems.add(new Pair<>("Lettuce", 500));

        monthlyGoalsMet = 5;
        calorieGoal = 1000;
    }

    public void sendDataToServer(LocalDate desiredDataDate) {
        // TO BE IMPLEMENTED
    }

}
