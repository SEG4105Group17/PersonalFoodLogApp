package com.example.personalfoodlogapp;

import android.app.Application;
import android.net.Uri;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kotlin.Pair;

public class PersonalFoodApplication extends Application {

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseFunctions func;
    private FirebaseStorage store;

    // Map of calorie goals for every date
    public Map<String, Pair<Integer, Integer>> dailyCalorieGoals;


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
        func = FirebaseFunctions.getInstance();
        store = FirebaseStorage.getInstance();

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
    }

    public interface MyCallback {
        void onSuccess();
    }

    public void attemptLogin(String email, String password, MyCallback callback, MyCallback failCallback) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> callback.onSuccess())
                .addOnFailureListener(e -> failCallback.onSuccess());
    }

    public void attemptRegistration(String email, String password, MyCallback callback, MyCallback failCallback) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(
                task -> callback.onSuccess()
                ).addOnFailureListener(
                e -> failCallback.onSuccess()
                );
    }

    public void databaseError(String e) {
        Log.e("", e);
    }

    public void sendDataToServer(LocalDate desiredDataDate) {
        // Load data into map
        Map<String, Object> dataHashmap = new HashMap<>();
        dataHashmap.put("calorieGoal", calorieGoal);
        dataHashmap.put("foodItems", foodItems);

        // Add the current calorie count, just so its easy to query later
        dataHashmap.put("calorieCurrent", getCalorieCurrent());


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

    public void parseDailyGoals(QuerySnapshot querySnap, MyCallback callback) {
        dailyCalorieGoals = new HashMap<>();
        for (QueryDocumentSnapshot doc : querySnap) {
            Double calorieCurrent = doc.getDouble("calorieCurrent");
            Double calorieGoal = doc.getDouble("calorieGoal");
            if (calorieCurrent == null) {
                calorieCurrent = 0.0;
            }
            if (calorieGoal == null) {
                calorieGoal = 0.0;
            }
            dailyCalorieGoals.put(doc.getId(), new Pair<>(calorieCurrent.intValue(), calorieGoal.intValue()));
        }

        callback.onSuccess();
    }

    public void getDailyGoalsData(MyCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            db
                    .collection("users").document(user.getUid())
                    .collection("date")
                    .get()
                    .addOnSuccessListener(documentSnapshot -> parseDailyGoals(documentSnapshot, callback));
        } else {
            databaseError("Failed to get data from server. Check if you are logged in");
        }

    }

    public void useAIFoodDetection(String fileName, MyCallback callback) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (fileName == null || currentUser == null) {
            return;
        }

        Map<String, Object> inputData = new HashMap<>();
        inputData.put("storagePath", "images/"+currentUser.getUid()+"/"+fileName);

        func.getHttpsCallable("process_image").call(inputData)
                .addOnSuccessListener(
                result -> {
                    Object outputData = result.getData();
                    if (outputData instanceof Map) {
                        Map<?,?> map = (Map<?, ?>) outputData;

                        // For the proof of concept; the AI model returns 3 nonsense values...
                        // We will assume that the data is good here, no type checking
                        List<Number> resultData = (List<Number>) map.get("result");
                        Log.i("","Data received from AI Model: "+resultData.get(0) +"|"+ resultData.get(1) +"|"+ resultData.get(2));

                        // Convert the 3 nonsense values into a food item
                        int foodItemIndex = resultData.get(0).intValue() % foodMacroMap.size();
                        String foodItemName = (new ArrayList<String>(foodMacroMap.keySet())).get(foodItemIndex);
                        foodItems.add(new Pair<>(foodItemName, resultData.get(1).intValue()));

                        // Update the server data with these new items
                        sendDataToServer(currentDate);

                        callback.onSuccess();
                    } else {
                        Log.e("", "Error reading result from AI function");
                    }
                }).addOnFailureListener(
                        result -> {
                            Log.e("", "Error running AI function", result);
                        }
        );
    }

    public void uploadImageToStorage(Uri localUri, String fileName, MyCallback callback) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (localUri == null || fileName == null || currentUser == null) {
            return;
        }

        StorageReference storageRef = store.getReference();
        StorageReference imageRef = storageRef.child("images/"+currentUser.getUid()+"/"+fileName);
        UploadTask uploadTask = imageRef.putFile(localUri);
        uploadTask.addOnSuccessListener(uri -> {
           String downloadUrl = uri.toString();
           Log.i("", "File uploaded to firebase storage. URL: "+downloadUrl);
            callback.onSuccess();
        }).addOnFailureListener(e -> {
            Log.e("", "Failed to upload image to firebase storage");
        });
    }

}
