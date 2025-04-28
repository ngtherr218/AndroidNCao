package com.example.orderfoodgo.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.orderfoodgo.R;
import com.example.orderfoodgo.activity.DeliveryInfoActivity;
import com.example.orderfoodgo.activity.OrderHistoryActivity;
import com.example.orderfoodgo.activity.ProfileActivity;
import com.example.orderfoodgo.activity.SignInActivity;
import com.example.orderfoodgo.util.SharedPreferencesUtil;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {
    View vProfile;
    TextView tvName, tvEmail;
    LinearLayout lnAddress, lnHistory, lnSignOut;
    SharedPreferencesUtil util = new SharedPreferencesUtil();
    FirebaseFirestore db = FirebaseFirestore.getInstance();



    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
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
        View view =  inflater.inflate(R.layout.fragment_profile, container, false);
        vProfile = view.findViewById(R.id.vProfile);
        tvName = view.findViewById(R.id.tvName);
        tvEmail = view.findViewById(R.id.tvEmail);
        lnAddress = view.findViewById(R.id.lnAddress);
        lnHistory = view.findViewById(R.id.lnHistory);
        lnSignOut = view.findViewById(R.id.lnSignOut);

        loadProfileData();


        vProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), ProfileActivity.class);
                getContext().startActivity(intent);
            }
        });

        lnAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), DeliveryInfoActivity.class);
                getContext().startActivity(intent);
            }
        });

        lnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), SignInActivity.class);
                getContext().startActivity(intent);
                util.clearUserIdFromSharedPreferences(getContext());
                if (getActivity() != null) {
                    getActivity().finishAffinity();  // Kết thúc tất cả các activity trước đó
                }
            }
        });

        lnHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), OrderHistoryActivity.class);
                getContext().startActivity(intent);
            }
        });
        return  view;
    }
    @Override
    public void onResume() {
        super.onResume();
        // Reload the data from Firestore or refresh the UI
        loadProfileData(); // This is a custom method where you fetch the profile data
    }

    private void loadProfileData(){
        String userId = util.getUserIdFromSharedPreferences(getContext());
        if (userId != null && !userId.isEmpty()) {
            db.collection("users")
                    .document(userId)  // Lấy document của người dùng với userId
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                // Lấy thông tin từ Firestore
                                String name = document.getString("name");
                                String email = document.getString("email");
                                String password = document.getString("password");

                                // Hiển thị thông tin lên các EditText
                                tvName.setText(name);
                                tvEmail.setText(email);

                            } else {
                                Toast.makeText(getContext(), "Không tìm thấy người dùng", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getContext(), "Lỗi khi tải thông tin người dùng", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(getContext(), "Không có ID người dùng", Toast.LENGTH_SHORT).show();
        }
    }
}