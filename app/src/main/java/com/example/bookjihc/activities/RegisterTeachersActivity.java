package com.example.bookjihc.activities;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bookjihc.databinding.ActivityRegisterTeacherBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterTeachersActivity extends AppCompatActivity {
    private ActivityRegisterTeacherBinding binding;


    private FirebaseAuth firebaseAuth;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityRegisterTeacherBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
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

        binding.registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });
    }

    private String name = "", email = "", password = "";

    private void validateData() {


        name = binding.nameEt.getText().toString().trim();
        email = binding.emaiEt.getText().toString().trim();
        password = binding.passwarddEt.getText().toString().trim();
        String cPassword = binding.cpasswarddEt.getText().toString().trim();


        if (TextUtils.isEmpty(name)) {

            Toast.makeText(this, "Атыңызды енгізіңіз бе? ...", Toast.LENGTH_SHORT).show();

        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {

            Toast.makeText(this, "Жарамсыз электрондық пошта үлгісі...!", Toast.LENGTH_SHORT).show();

        } else if (TextUtils.isEmpty(password)) {

            Toast.makeText(this, "Құпия сөзді енгізіңіз...?", Toast.LENGTH_SHORT).show();

        } else if (TextUtils.isEmpty(cPassword)) {

            Toast.makeText(this, "Құпия сөзді Растау...?", Toast.LENGTH_SHORT).show();

        } else if (!password.equals(cPassword)) {

            Toast.makeText(this, "Құпия сөз сәйкес келмейді...!", Toast.LENGTH_SHORT).show();

        } else {

            createUserAccount();

        }
    }

    private void createUserAccount() {


        progressDialog.setMessage("Аккаунт құрылуда...");
        progressDialog.show();


        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {


                        updateUserinfo();

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                progressDialog.dismiss();
                Toast.makeText(RegisterTeachersActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void updateUserinfo() {

        progressDialog.setMessage("Пайдаланушы ақпараты сақталуда...");


        long timestamp = System.currentTimeMillis();


        String uid = firebaseAuth.getUid();


        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", uid);
        hashMap.put("name", name);
        hashMap.put("email", email);
        hashMap.put("profileimage", "");
        hashMap.put("userType", "teacher");
        hashMap.put("timestamp", timestamp);


        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(uid)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Toast.makeText(RegisterTeachersActivity.this, "Аккаунт жасалды..", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(RegisterTeachersActivity.this, DashboardAdminActivity.class));
                        finish();
                    }
                })

                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(RegisterTeachersActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });


    }
}
