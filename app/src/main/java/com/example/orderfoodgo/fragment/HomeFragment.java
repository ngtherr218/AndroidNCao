package com.example.orderfoodgo.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.orderfoodgo.GridSpacingItemDecoration;
import com.example.orderfoodgo.R;
import com.example.orderfoodgo.adapter.ProductAdapter;
import com.example.orderfoodgo.model.Product;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {
    RecyclerView recyclerView;
    ImageView ivSearch;
    EditText etSearch;
    List<Product> productList = new ArrayList<>();
    ProductAdapter productAdapter;
    TextView tvAll, tvFastFood, tvMainDishes, tvVegetarianDishes, tvAppetizers, tvDesserts, tvBeverages;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
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
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        tvAll = view.findViewById(R.id.tvAll);
        tvFastFood = view.findViewById(R.id.tvFastFood);
        tvMainDishes = view.findViewById(R.id.tvMainDishes);
        tvVegetarianDishes = view.findViewById(R.id.tvVegetarianDishes);
        tvAppetizers = view.findViewById(R.id.tvAppetizers);
        tvDesserts = view.findViewById(R.id.tvDesserts);
        tvBeverages = view.findViewById(R.id.tvBeverages);
        ivSearch = view.findViewById(R.id.ivSearch);
        etSearch = view.findViewById(R.id.etSearch);

        loadProducts();

        ivSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadProductByName(etSearch.getText().toString());
            }
        });

        tvAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadProducts(); // Lấy tất cả sản phẩm
            }
        });

        tvVegetarianDishes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadProductsByCategory("Đồ chay");
            }
        });

        tvMainDishes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadProductsByCategory("Món chính");
            }
        });

        tvAppetizers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadProductsByCategory("Món khai vị");
            }
        });

        tvDesserts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadProductsByCategory("Đồ tráng miệng");
            }
        });

        tvBeverages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadProductsByCategory("Đồ uống");
            }
        });

        tvFastFood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadProductsByCategory("Thức ăn nhanh");
            }
        });
        return view;
    }

    // Hàm chung để lấy sản phẩm theo danh mục
    private void loadProductsByCategory(String category) {
        db.collection("products")
                .whereEqualTo("category", category)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Product> productList = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult()) {
                            Product product = document.toObject(Product.class);
                            product.setId(document.getId());  // Set the Firestore document ID
                            productList.add(product);
                        }
                        // Cập nhật adapter
                        productAdapter = new ProductAdapter(productList);
                        recyclerView.setAdapter(productAdapter);
                        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
                        int spacingInPixels = 6;
                        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, 0, true));
                    } else {
                        // Xử lý lỗi
                        Toast.makeText(getContext(), "Không thể tải sản phẩm", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadProducts() {

        db.collection("products")
                .get() // Lấy tất cả sản phẩm
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Product> productList = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult()) {
                            Product product = document.toObject(Product.class);
                            product.setId(document.getId());  // Set the Firestore document ID
                            productList.add(product);
                        }

                        // Cập nhật adapter
                        if (getContext() != null) {
                            productAdapter = new ProductAdapter(productList);
                            recyclerView.setAdapter(productAdapter);
                            recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
                            int spacingInPixels = 6;
                            recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, 0, true));
                        } else {
                            Log.e("loadProducts", "Context is null, cannot update RecyclerView");
                        }

                    } else {
                        // Xử lý lỗi
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Không thể tải sản phẩm", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e("loadProducts", "Context is null, cannot show Toast");
                        }
                    }
                });
    }

    private void loadProductByName(String name){
        db.collection("products")
                .whereGreaterThanOrEqualTo("name", name)
                .whereLessThan("name", name + '\uf8ff')  // '\uf8ff' is the last Unicode character, ensuring a partial match
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Product> productList = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult()) {
                            Product product = document.toObject(Product.class);
                            product.setId(document.getId());  // Set the Firestore document ID
                            productList.add(product);
                        }
                        // Update adapter
                        productAdapter = new ProductAdapter(productList);
                        recyclerView.setAdapter(productAdapter);
                        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
                        int spacingInPixels = 6;
                        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, spacingInPixels, true));
                    } else {
                        // Handle error
                        Toast.makeText(getContext(), "Không thể tải sản phẩm", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}