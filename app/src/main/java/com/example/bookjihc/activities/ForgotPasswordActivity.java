package com.example.bookjihc.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.bookjihc.databinding.ActivityForgotPasswordBinding;
import com.google.android.gms.common.internal.AccountType;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {
    private ActivityForgotPasswordBinding binding;


    private FirebaseAuth firebaseAuth;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
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
                validateData();
            }
        });


    }
    private  String emaill  = "";
    private void validateData() {

        emaill = binding.emailEt.getText().toString().trim();


        if (emaill.isEmpty()){

            Toast.makeText(this, "Электрондық поштаны енгізіңіз..", Toast.LENGTH_SHORT).show();
            
            
        } else if (!Patterns.EMAIL_ADDRESS.matcher(emaill).matches()) {
            Toast.makeText(this, "Жарамсыз электрондық пошта форматы...", Toast.LENGTH_SHORT).show();
        }
        else {
            recoverPassword();

        }


        }

    private void recoverPassword() {

        progressDialog.setMessage("Құпия сөзді қалпына келтіру нұсқауларын келесіге жіберу"+emaill);
        progressDialog.show();


        firebaseAuth.sendPasswordResetEmail(emaill)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                        progressDialog.dismiss();
                        Toast.makeText(ForgotPasswordActivity.this, "Құпия сөзді қалпына келтіру бойынша нұсқаулар жіберілді"+emaill, Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        progressDialog.dismiss();
                        Toast.makeText(ForgotPasswordActivity.this, "Байланысты жіберу мүмкін болмады"+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });




    }
}
