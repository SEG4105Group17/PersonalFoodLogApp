package com.example.personalfoodlogapp;

import android.app.Application;

import java.time.LocalDate;

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
    public int calorieCurrent;

    public void onCreate() {
        super.onCreate();

        if (requiresReload) {
            currentDate = LocalDate.now();
            getDataFromServer(currentDate);
            requiresReload = false;
        }
    }


    public void getDataFromServer(LocalDate desiredDataDate) {
        currentDate = desiredDataDate;
        monthlyGoalsPossible = currentDate.getDayOfMonth();

        // TO BE IMPLEMENTED: READ DATABASE FOR DATA ON THIS DATE
        // Fake data
        monthlyGoalsMet = 5;
        calorieGoal = 1;
        calorieCurrent = 0;
    }

}
