package com.example.orderfoodgo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.orderfoodgo.R;
import com.example.orderfoodgo.model.DeliveryInfo;
import com.example.orderfoodgo.util.SharedPreferencesUtil;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddDeliveryInfoActivity extends AppCompatActivity {
    private EditText etNameReceiver, etPhoneNumber, etAddress;
    private Button btnAddDeliveryInfo;
    private String userId ;
    Spinner spinnerTinh, spinnerHuyen, spinnerXa;


    Map<String, List<String>> huyenMap = new HashMap<>();
    Map<String, List<String>> xaMap = new HashMap<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_delivery_info);

        etNameReceiver = findViewById(R.id.etNameReceiver);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        btnAddDeliveryInfo = findViewById(R.id.btnAddDeliveryInfo);
        spinnerTinh = findViewById(R.id.spinnerTinh);
        spinnerHuyen = findViewById(R.id.spinnerHuyen);
        spinnerXa = findViewById(R.id.spinnerXa);
        etAddress = findViewById(R.id.etAddress);

        btnAddDeliveryInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addDeliveryInfo();
            }
        });


        // 1. Danh sách Tỉnh
        List<String> tinhList = new ArrayList<>();
        tinhList.add("Hà Nội");
        tinhList.add("Hồ Chí Minh");
        tinhList.add("Đà Nẵng");
        tinhList.add("Cần Thơ");

        // 2. Huyện theo Tỉnh
        huyenMap.put("Hà Nội", List.of("Ba Đình", "Cầu Giấy"));
        huyenMap.put("Hồ Chí Minh", List.of("Quận 1", "Quận 7"));
        huyenMap.put("Đà Nẵng", List.of("Hải Châu", "Thanh Khê"));
        huyenMap.put("Cần Thơ", List.of("Ninh Kiều", "Bình Thủy"));

        // 3. Xã theo Huyện
        xaMap.put("Ba Đình", List.of("Phường Phúc Xá", "Phường Trúc Bạch"));
        xaMap.put("Cầu Giấy", List.of("Phường Dịch Vọng", "Phường Nghĩa Tân"));
        xaMap.put("Quận 1", List.of("Phường Bến Nghé", "Phường Bến Thành"));
        xaMap.put("Quận 7", List.of("Phường Tân Phong", "Phường Tân Hưng"));
        xaMap.put("Hải Châu", List.of("Phường Hải Châu 1", "Phường Hải Châu 2"));
        xaMap.put("Thanh Khê", List.of("Phường Xuân Hà", "Phường Tân Chính"));
        xaMap.put("Ninh Kiều", List.of("Phường An Bình", "Phường An Cư"));
        xaMap.put("Bình Thủy", List.of("Phường Bình Thủy", "Phường Trà An"));

        // 4. Adapter cho spinnerTinh
        ArrayAdapter<String> adapterTinh = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tinhList);
        adapterTinh.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTinh.setAdapter(adapterTinh);

        // 5. Khi chọn Tỉnh
        spinnerTinh.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                String selectedTinh = tinhList.get(position);
                List<String> huyenList = huyenMap.get(selectedTinh);
                if (huyenList != null) {
                    ArrayAdapter<String> adapterHuyen = new ArrayAdapter<>(AddDeliveryInfoActivity.this, android.R.layout.simple_spinner_item, huyenList);
                    adapterHuyen.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerHuyen.setAdapter(adapterHuyen);

                    // Khi đổi Tỉnh thì cũng reset Xã luôn
                    if (!huyenList.isEmpty()) {
                        String firstHuyen = huyenList.get(0);
                        List<String> xaList = xaMap.get(firstHuyen);
                        if (xaList != null) {
                            ArrayAdapter<String> adapterXa = new ArrayAdapter<>(AddDeliveryInfoActivity.this, android.R.layout.simple_spinner_item, xaList);
                            adapterXa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerXa.setAdapter(adapterXa);
                        }
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        // 6. Khi chọn Huyện
        spinnerHuyen.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                String selectedHuyen = (String) spinnerHuyen.getSelectedItem();
                List<String> xaList = xaMap.get(selectedHuyen);
                if (xaList != null) {
                    ArrayAdapter<String> adapterXa = new ArrayAdapter<>(AddDeliveryInfoActivity.this, android.R.layout.simple_spinner_item, xaList);
                    adapterXa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerXa.setAdapter(adapterXa);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
    }

    private void addDeliveryInfo() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        SharedPreferencesUtil util = new SharedPreferencesUtil();
        String userId = util.getUserIdFromSharedPreferences(this);
        DocumentReference userRef = db.collection("users").document(userId);

        String selectedTinh = spinnerTinh.getSelectedItem().toString();
        String selectedHuyen = spinnerHuyen.getSelectedItem().toString();
        String selectedXa = spinnerXa.getSelectedItem().toString();

        String nameReceiver = etNameReceiver.getText().toString();
        String address = etAddress.getText().toString() + ", " + selectedXa + ", " + selectedHuyen + ", " + selectedTinh;
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
