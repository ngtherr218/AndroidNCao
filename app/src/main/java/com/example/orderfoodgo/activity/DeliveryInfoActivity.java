package com.example.orderfoodgo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.orderfoodgo.R;
import com.example.orderfoodgo.adapter.DeliveryInfoAdapter;
import com.example.orderfoodgo.model.DeliveryInfo;
import com.example.orderfoodgo.util.SharedPreferencesUtil;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class DeliveryInfoActivity extends AppCompatActivity {
    ImageView ivAdd;
    RecyclerView recyclerView;
    int check;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_info);
        ivAdd = findViewById(R.id.ivAdd);
        recyclerView = findViewById(R.id.recyclerView);

        ivAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DeliveryInfoActivity.this, AddDeliveryInfoActivity.class);
                startActivity(intent);
            }
        });
        Intent intent = getIntent();
        check = intent.getIntExtra("key",0);

        loadDeliveryInfo();

    }
    protected void onResume() {
        super.onResume();
        loadDeliveryInfo();
    }

    private void loadDeliveryInfo() {
        // Initialize the RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Fetch the data from Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        SharedPreferencesUtil util = new SharedPreferencesUtil();
        String userId = util.getUserIdFromSharedPreferences(this);
        DocumentReference userRef = db.collection("users").document(userId);

        // Fetch data from the subcollection "deliveryinfos"
        userRef.collection("deliveryinfos")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        List<DeliveryInfo> deliveryInfoList = new ArrayList<>();
                        List<String> deliveryIds = new ArrayList<>();
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            DeliveryInfo deliveryInfo = document.toObject(DeliveryInfo.class);
                            deliveryInfoList.add(deliveryInfo);
                            deliveryIds.add(document.getId());
                        }

                        // Set up the adapter with the data
                        DeliveryInfoAdapter adapter = new DeliveryInfoAdapter(deliveryInfoList, check,deliveryIds);
                        recyclerView.setAdapter(adapter);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load delivery info", Toast.LENGTH_SHORT).show();
                });
    }
}
