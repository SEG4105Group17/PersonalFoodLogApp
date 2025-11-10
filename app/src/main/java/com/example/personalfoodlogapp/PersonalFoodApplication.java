package com.example.personalfoodlogapp;

import android.app.Application;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kotlin.Pair;

public class PersonalFoodApplication extends Application {

    // These are the same irrespective of the selected date
    public int monthlyGoalsMet;
    public int monthlyGoalsPossible;

    private FirebaseFirestore db;
    private FirebaseAuth auth;


    // Table of macros... Listed per gram (Calorie, Sodium, Fat, Sugar)
    public Map<String, ArrayList<Double>> foodMacroMap = new HashMap<>() {{
        put("Potato", new ArrayList<>(Arrays.asList(0.77, 0.00003, 0.0013, 0.005)));
        put("Tomato", new ArrayList<>(Arrays.asList(0.18, 0.00005, 0.002, 0.026)));
        put("Lettuce", new ArrayList<>(Arrays.asList(0.15, 0.0003, 0.001, 0.012)));
    }};

    // This data depends on the selected date
    public LocalDate currentDate;
    public int calorieGoal;
    public ArrayList<Pair<String, Integer>> foodItems;

    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        currentDate = LocalDate.now();
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

    public void getDataFromServer(LocalDate desiredDataDate, MyCallback callback) {
        currentDate = desiredDataDate;
        monthlyGoalsPossible = currentDate.getDayOfMonth();
        foodItems = new ArrayList<>();

        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            db
                    .collection("users").document(user.getUid())
                    .collection("date").document(desiredDataDate.toString())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> dataRetrieveSuccess(documentSnapshot, callback));
        } else {
            databaseError("Failed to get data from server. Check if you are logged in");
        }

        // TO BE IMPLEMENTED: Calculate this value
        monthlyGoalsMet = 5;
    }

    public void dataRetrieveSuccess(DocumentSnapshot documentSnapshot, MyCallback callback) {
        Map<String, Object> retrievedData = null;
        if (documentSnapshot.exists()) {
            retrievedData = documentSnapshot.getData();
        }

        if (retrievedData == null) {
            retrievedData = new HashMap<>();
        }

        if (retrievedData.get("calorieGoal") == null) {
            calorieGoal = 0;
        } else {
            calorieGoal = ((Long) retrievedData.get("calorieGoal")).intValue();
        }

        foodItems = new ArrayList<>();
        if (retrievedData.get("foodItems") instanceof List<?>) {
            for (Object item : (List<?>) retrievedData.get("foodItems")) {
                if (item instanceof Map<?, ?>) {
                    Map<?,?> map = (Map<?,?>) item;
                    Object first = map.get("first");
                    Object second = map.get("second");

                    if (first instanceof String && second instanceof Long) {
                        foodItems.add(new Pair<>((String)first, ((Long)second).intValue()));
                    }
                }
            }
        }

        callback.onSuccess();

        // Do an advanced query to find the number of calorie goals met per month
    }

    public interface MyCallback {
        void onSuccess();
    }

    public void attemptLogin(String email, String password, MyCallback callback) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> callback.onSuccess())
                .addOnFailureListener(e -> databaseError("Failed to login"));
    }

    public void databaseError(String e) {
        // TO BE IMPLEMENTED: Display error dialog
        Log.e("", e);
    }

    public void sendDataToServer(LocalDate desiredDataDate) {
        // Load data into map
        Map<String, Object> dataHashmap = new HashMap<>();
        dataHashmap.put("calorieGoal", calorieGoal);
        dataHashmap.put("foodItems", foodItems);

        // Send data to server under the user's account
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            db
                    .collection("users").document(user.getUid())
                    .collection("date").document(desiredDataDate.toString())
                    .set(dataHashmap).addOnFailureListener(e -> databaseError("Failed to upload data"));
        } else {
            databaseError("Failed to send data to server. Check if you are logged in");
        }
    }

    public void useAIFoodDetection() {
        // TO BE IMPLEMENTED: Run code on server
    }

}
