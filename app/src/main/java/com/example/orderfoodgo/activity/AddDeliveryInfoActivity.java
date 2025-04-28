package com.example.orderfoodgo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.orderfoodgo.R;
import com.example.orderfoodgo.model.DeliveryInfo;
import com.example.orderfoodgo.util.SharedPreferencesUtil;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddDeliveryInfoActivity extends AppCompatActivity {
    private EditText etNameReceiver, etAddress, etPhoneNumber;
    private Button btnAddDeliveryInfo;
    private String userId ;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_delivery_info);

        etNameReceiver = findViewById(R.id.etNameReceiver);
        etAddress = findViewById(R.id.etAddress);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        btnAddDeliveryInfo = findViewById(R.id.btnAddDeliveryInfo);

        btnAddDeliveryInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addDeliveryInfo();
            }
        });
    }

    private void addDeliveryInfo() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        SharedPreferencesUtil util = new SharedPreferencesUtil();
        String userId = util.getUserIdFromSharedPreferences(this);
        DocumentReference userRef = db.collection("users").document(userId);

        String nameReceiver = etNameReceiver.getText().toString();
        String address = etAddress.getText().toString();
        String phoneNumber = etPhoneNumber.getText().toString();

        DeliveryInfo newDeliveryInfo = new DeliveryInfo(nameReceiver, phoneNumber, address);

        // Add the new address as a document in the "deliveryinfos" subcollection
        userRef.collection("deliveryinfos")
                .add(newDeliveryInfo)  // Add the newDeliveryInfo as a document
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Thêm địa chỉ giao hàng thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Thêm thất bại", Toast.LENGTH_SHORT).show();
                });

    }
}
