package com.example.orderfoodgo.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.orderfoodgo.R;
import com.example.orderfoodgo.activity.PaymentActivity;
import com.example.orderfoodgo.adapter.CartItemAdapter;
import com.example.orderfoodgo.model.CartItem;
import com.example.orderfoodgo.util.SharedPreferencesUtil;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CartFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CartFragment extends Fragment {
    private RecyclerView recyclerView;
    private CartItemAdapter cartItemAdapter;
    private List<CartItem> cartItems;
    private FirebaseFirestore db;
    private Button btnCheckOut;
//    private TextView totalValue;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public CartFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CartFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CartFragment newInstance(String param1, String param2) {
        CartFragment fragment = new CartFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_cart, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        btnCheckOut = view.findViewById(R.id.btnCheckOut);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        cartItems = new ArrayList<>();
        cartItemAdapter = new CartItemAdapter(getContext(), cartItems);
        recyclerView.setAdapter(cartItemAdapter);

        db = FirebaseFirestore.getInstance();
        loadCartItems();

        btnCheckOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Kiểm tra nếu giỏ hàng trống
                if (cartItems.isEmpty()) {
                    // Nếu giỏ hàng trống, hiển thị thông báo
                    Toast.makeText(getContext(), "Giỏ hàng hiện tại không có đơn hàng nào!", Toast.LENGTH_SHORT).show();
                } else {
                    // Nếu có đơn hàng trong giỏ, chuyển đến trang thanh toán
                    Intent intent = new Intent(getContext(), PaymentActivity.class);
                    startActivity(intent);
                }
            }
        });

        return view;
    }

    private void loadCartItems() {
        SharedPreferencesUtil util = new SharedPreferencesUtil();
        String userId = util.getUserIdFromSharedPreferences(getContext());  // Lấy userId từ SharedPreferences

        if (userId != null && !userId.isEmpty()) {
            // Truy vấn Firestore theo idUser và lấy dữ liệu từ collection carts
            db.collection("users").document(userId)  // Lấy document của user từ Firestore
                    .collection("carts")  // Lấy collection carts của user
                    .get()  // Lấy tất cả các document trong collection carts
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Xử lý kết quả trả về từ Firestore
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Tạo CartItem từ dữ liệu trong Firestore
                                String id = document.getId();
                                String idUser = document.getString("idUser");
                                String idProduct = document.getString("idProduct");
                                int quantity = document.getLong("quantity").intValue();

                                // Tạo đối tượng CartItem và thêm vào danh sách
                                CartItem cartItem = new CartItem(id, idUser, idProduct, quantity);
                                cartItems.add(cartItem);  // Thêm CartItem vào danh sách
                            }
                            // Cập nhật RecyclerView sau khi lấy dữ liệu
                            cartItemAdapter.notifyDataSetChanged();
                        } else {
                            // Thông báo lỗi nếu không lấy được dữ liệu
                            Toast.makeText(getContext(), "Không thể tải giỏ hàng", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // Thông báo nếu không có userId trong SharedPreferences
            Toast.makeText(getContext(), "Không tìm thấy người dùng", Toast.LENGTH_SHORT).show();
        }
    }
}