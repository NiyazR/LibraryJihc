package com.example.bookjihc.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.bookjihc.MyApplication;
import com.example.bookjihc.R;
import com.example.bookjihc.adapters.AdapterPdfFavourite;
import com.example.bookjihc.databinding.ActivityProfileBinding;
import com.example.bookjihc.models.ModelPdf;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity {


    private ActivityProfileBinding binding;

    private FirebaseAuth firebaseAuth;
    private ArrayList<ModelPdf> pdfArrayList;

    private FirebaseUser firebaseUser;
    private ProgressDialog progressDialog;

    private AdapterPdfFavourite adapterPdfFavourite;


    private static final String Tag = "PROFILE_TAG";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.accountTypeTv.setText("N/A");
        binding.memberData.setText("N/A");
        binding.favouriteBookCountTv.setText("N/A");
        binding.accountStatusTv.setText("N/A");


        firebaseAuth = FirebaseAuth.getInstance();

        firebaseUser = firebaseAuth.getCurrentUser();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        loadUserInfo();
        loadFavoriteBooks();


        binding.profileEditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ProfileActivity.this, ProfileEditActivity.class));
            }
        });


        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        binding.accountStatusTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (firebaseUser.isEmailVerified()) {

                    Toast.makeText(ProfileActivity.this, "Қазірдің өзінде расталған...", Toast.LENGTH_SHORT).show();


                } else {

                    emailVerificationDialog();

                }
            }
        });


    }

    private void emailVerificationDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Электрондық поштаны растау")
                .setMessage("Электрондық поштаңызға электрондық поштаны растау нұсқауларын жібергіңіз келетініне сенімдісіз бе?" + firebaseUser.getEmail())
                .setPositiveButton("ЖІБЕРУ", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendEmailVerification();

                    }
                })
                .setNegativeButton("БАС ТАРТУ", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                    }
                })
                .show();


    }

    private void sendEmailVerification() {

        progressDialog.setMessage("Электрондық поштаңызға электрондық поштаны растау нұсқауларын жіберу" + firebaseUser.getEmail());
        progressDialog.show();


        firebaseUser.sendEmailVerification()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Toast.makeText(ProfileActivity.this, "Нұсқаулар жіберілді, электрондық поштаңызды тексеріңіз" + firebaseUser.getEmail(), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(ProfileActivity.this, "Себебі орындалмады" + e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

    }


    private void loadUserInfo() {
        Log.d("profile", "loadUserInfo: Пайдаланушы ақпараты жүктелуде" + firebaseAuth.getUid());


        if (firebaseUser.isEmailVerified()) {
            binding.accountStatusTv.setText("Тексерілді");
        } else {
            binding.accountStatusTv.setText("Тексерілмеген");

        }

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).
                addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String email = "" + snapshot.child("email").getValue();
                        String name = "" + snapshot.child("name").getValue();
                        String profileImage = "" + snapshot.child("profileImage").getValue();
                        String timestamp = "" + snapshot.child("timestamp").getValue();
//                        String uid = "" + snapshot.child("uid").getValue();
                        String userType = "" + snapshot.child("userType").getValue();

//                        Log.d(Tag, "image:" + profileImage);
//                        binding.accountStatusTv.setText(profileImage);

                        String formattedDate = MyApplication.formatTimestamp(Long.parseLong(timestamp));

                        binding.emailTv.setText(email);
                        binding.nameTv.setText(name);
                        binding.memberData.setText(formattedDate);
                        binding.accountTypeTv.setText(userType);

                        Glide.with(ProfileActivity.this)
                                .load(profileImage)
                                .placeholder(R.drawable.ic_person_gray)
                                .into(binding.profileTv);


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


    }

    private void loadFavoriteBooks() {

        pdfArrayList = new ArrayList<>();


        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Favorites")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        pdfArrayList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {

                            String bookId = "" + ds.child("bookId").getValue();


                            ModelPdf modelPdf = new ModelPdf();
                            modelPdf.setId(bookId);


                            pdfArrayList.add(modelPdf);
                        }

                        binding.favouriteBookCountTv.setText("" + pdfArrayList.size());


                        adapterPdfFavourite = new AdapterPdfFavourite(ProfileActivity.this, pdfArrayList);

                        binding.booksRv.setAdapter(adapterPdfFavourite);


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


    }
}