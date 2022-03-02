package com.example.bookjihc.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.example.bookjihc.databinding.ActivityCategoryaddBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class CategoryaddActivity extends AppCompatActivity {


    private ActivityCategoryaddBinding binding;

    private FirebaseAuth firebaseAuth;

    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCategoryaddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Күте тұрыңыз");
        progressDialog.setCanceledOnTouchOutside(false);
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validDate();
            }


        });


    }

    private String category = "";

    private void validDate() {
        category = binding.categotyEt.getText().toString().trim();

        if (TextUtils.isEmpty(category)) {
            {

                Toast.makeText(this, "Санат енгізіңіз...!", Toast.LENGTH_SHORT).show();

            }


        } else {
            addCategoryFirebase();


        }

    }

    private void addCategoryFirebase() {

        progressDialog.setMessage("Санат қосуда...");
        progressDialog.show();

        long timestammp = System.currentTimeMillis();


        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("id", "" + timestammp);
        hashMap.put("category", "" + category);
        hashMap.put("timestamp", "" + timestammp);
        hashMap.put("uid", "" + firebaseAuth.getUid());

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.child("" + timestammp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                progressDialog.dismiss();
                Toast.makeText(CategoryaddActivity.this, "Санат сәтті қосылды", Toast.LENGTH_SHORT).show();

            }
        })

                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(CategoryaddActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });


    }
}