package com.example.orderfoodgo.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.orderfoodgo.R;
import com.example.orderfoodgo.adapter.FavoriteAdapter;
import com.example.orderfoodgo.model.FavoriteProduct;
import com.example.orderfoodgo.util.SharedPreferencesUtil;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FavoriteFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FavoriteFragment extends Fragment {
    RecyclerView recyclerView;
    FavoriteAdapter adapter;
    SharedPreferencesUtil util = new SharedPreferencesUtil();
    List<FavoriteProduct> favoriteProductList = new ArrayList<>();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public FavoriteFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FavoriteFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FavoriteFragment newInstance(String param1, String param2) {
        FavoriteFragment fragment = new FavoriteFragment();
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
        View view = inflater.inflate(R.layout.fragment_favorite, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // Lấy danh sách các sản phẩm yêu thích từ Firestore
        loadFavoriteProducts();
        return view;
    }


    private void loadFavoriteProducts() {
        String userId = util.getUserIdFromSharedPreferences(getContext());

        if (userId != null && !userId.isEmpty()) {
            // Truy vấn vào collection "favorites" của người dùng
            DocumentReference userRef = db.collection("users").document(userId);
            CollectionReference favoritesRef = userRef.collection("favorites");

            // Lấy tất cả sản phẩm yêu thích của người dùng
            favoritesRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    QuerySnapshot querySnapshot = task.getResult();
                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        // Xử lý dữ liệu yêu thích từ Firestore
                        List<FavoriteProduct> favoriteProductList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : querySnapshot) {
                            // Chuyển đổi dữ liệu từ Firestore thành đối tượng FavoriteProduct
                            FavoriteProduct favoriteProduct = document.toObject(FavoriteProduct.class);
                            favoriteProductList.add(favoriteProduct);
                        }

                        // Khởi tạo adapter với dữ liệu và thiết lập RecyclerView
                        adapter = new FavoriteAdapter(getContext(), favoriteProductList);
                        recyclerView.setAdapter(adapter);
                    } else {
                        // Nếu không có sản phẩm yêu thích, hiển thị thông báo
                        Toast.makeText(getContext(), "Không có sản phẩm yêu thích", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Nếu có lỗi trong việc truy vấn dữ liệu từ Firestore
                    Toast.makeText(getContext(), "Lỗi khi tải dữ liệu yêu thích", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Nếu userId không hợp lệ, hiển thị thông báo
            Toast.makeText(getContext(), "Người dùng chưa đăng nhập", Toast.LENGTH_SHORT).show();
        }
    }
}

