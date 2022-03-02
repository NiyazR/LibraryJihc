package com.example.bookjihc.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.example.bookjihc.MyApplication;
import com.example.bookjihc.R;
import com.example.bookjihc.adapters.AdapterComment;
import com.example.bookjihc.adapters.AdapterPdfFavourite;
import com.example.bookjihc.databinding.ActivityPdfDetailBinding;
import com.example.bookjihc.databinding.DialogCommentAddBinding;
import com.example.bookjihc.models.ModelComment;
import com.example.bookjihc.models.ModelPdf;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class PdfDetailActivity extends AppCompatActivity {

    private ActivityPdfDetailBinding binding;


    private static final String TAG_DOWNLOAD = "DOQNLOAD_TAG";

    private ProgressDialog progressDialog;
    private  ArrayList<ModelComment> commentArrayList;

    private AdapterComment adapterComment;

    String bookId, bookTitle, bookUrl;
    boolean isInMyFavorite = false;


    private FirebaseAuth firebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        bookId = intent.getStringExtra("bookId");


        binding.downloadBookBtn.setVisibility(View.GONE);


        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Күте тұрыңыз");
progressDialog.setCanceledOnTouchOutside(false);


        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null) {

            checkIsFavorite();

        }


        loadBookDetals();
        loadComment();

        MyApplication.incrementBookViewCount(bookId);

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        binding.reedBookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(PdfDetailActivity.this, PdfViewActivity.class);
                intent1.putExtra("bookId", bookId);
                startActivity(intent1);
            }
        });

        binding.downloadBookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG_DOWNLOAD, "onClick: Рұқсат тексерілуде");
                if (ContextCompat.checkSelfPermission(PdfDetailActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG_DOWNLOAD, "onClick: Рұқсат берілген, кітапты жүктеп алуға болады");
                    MyApplication.downloadBook(PdfDetailActivity.this, "" + bookId, "" + bookTitle, "" + bookUrl);
                } else {

                    Log.d(TAG_DOWNLOAD, "onClick: Рұқсат берілмеді, рұқсат сұраңыз...");
                    requestpermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }

            }
        });

        binding.favoriteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (firebaseAuth.getCurrentUser() == null) {
                    Toast.makeText(PdfDetailActivity.this, "Сіз жүйеге кірмегенсіз", Toast.LENGTH_SHORT).show();

                } else {
                    if (isInMyFavorite) {
                        MyApplication.removeFromFavorite(PdfDetailActivity.this, bookId);


                    } else {

                        MyApplication.addToFavorite(PdfDetailActivity.this, bookId);
                    }

                }
            }
        });

        binding.addCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (firebaseAuth.getCurrentUser() == null) {


                    Toast.makeText(PdfDetailActivity.this, "сіз жүйеге кірмегенсіз...", Toast.LENGTH_SHORT).show();
                } else {

                    addCommentDialog();
                }

            }
        });


    }

    private void loadComment() {

        commentArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId).child("Comments")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        commentArrayList.clear();
                        for (DataSnapshot ds: snapshot.getChildren()){
                            ModelComment model = ds.getValue(ModelComment.class);

                            commentArrayList.add(model);



                        }

                        adapterComment = new AdapterComment(PdfDetailActivity.this, commentArrayList);

                        binding.commentsRv.setAdapter(adapterComment);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private String comment = "";

    private void addCommentDialog() {

        DialogCommentAddBinding commentAddBinding = DialogCommentAddBinding.inflate(LayoutInflater.from(this));
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialog);
        builder.setView(commentAddBinding.getRoot());


        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        commentAddBinding.backBtn.setOnClickListener(new View.OnClickListener() {
                                                         @Override
                                                         public void onClick(View v) {
                                                             alertDialog.dismiss();
                                                         }
                                                     }
        );

        commentAddBinding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                comment = commentAddBinding.commentEt.getText().toString().trim();

                if (TextUtils.isEmpty(comment)) {
                    Toast.makeText(PdfDetailActivity.this, "Пікіріңізді енгізіңіз....", Toast.LENGTH_SHORT).show();


                } else {
                    alertDialog.dismiss();
                    addComment();

                }

            }
        });
    }

    private void addComment() {

        progressDialog.setMessage("Пікір қосылуда...");
        progressDialog.show();


        String timestamp = ""+System.currentTimeMillis();


        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("id",""+timestamp);
        hashMap.put("bookId",""+bookId);
        hashMap.put("timestamp",""+timestamp);
        hashMap.put("comment",""+comment);
        hashMap.put("uid",""+firebaseAuth.getUid());

        DatabaseReference ref =FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId).child("Comments").child(timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(@NonNull Void unused) {
                        Toast.makeText(PdfDetailActivity.this, "Пікір қосылды...", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                progressDialog.dismiss();
                Toast.makeText(PdfDetailActivity.this, "Пікір қосу мүмкін болмады"+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });



    }


    private ActivityResultLauncher<String> requestpermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {

                if (isGranted) {
                    Log.d(TAG_DOWNLOAD, "Рұқсат берілді");
                    MyApplication.downloadBook(this, "" + bookId, "" + bookTitle, "" + bookUrl);

                } else {
                    Log.d(TAG_DOWNLOAD, "Рұқсат берілмеді...");
                    Toast.makeText(this, "Рұқсат берілмеді......", Toast.LENGTH_SHORT).show();


                }
            });

    private void loadBookDetals() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                bookTitle = "" + snapshot.child("title").getValue();
                String description = "" + snapshot.child("description").getValue();
                String categoryId = "" + snapshot.child("categoryId").getValue();
                String viewsCount = "" + snapshot.child("viewsCount").getValue();
                String downloadsCount = "" + snapshot.child("downloadsCount").getValue();
                bookUrl = "" + snapshot.child("url").getValue();
                String timestamp = "" + snapshot.child("timestamp").getValue();

                binding.downloadBookBtn.setVisibility(View.VISIBLE);

                String date = MyApplication.formatTimestamp(Long.parseLong(timestamp));


                MyApplication.loadCategory(
                        "" + categoryId, binding.CategoryTv);

                MyApplication.loadPdfFromUrlSinglePage(
                        "" + bookUrl,
                        "" + bookTitle,
                        binding.pdfView,
                        binding.progressbar,
                        binding.pagesTv
                );
                MyApplication.loadPdfSize(
                        "" + bookUrl,
                        "" + bookTitle,
                        binding.sizeTv);


                binding.TitleTv.setText(bookTitle);
                binding.DesciptionTv.setText(description);
                binding.viewsTv.setText(viewsCount.replace("null", "N/A"));
                binding.DateTv.setText(date);
                binding.downloadsTv.setText(downloadsCount.replace("null", "N/A"));

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void checkIsFavorite() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Favorites").child(bookId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        isInMyFavorite = snapshot.exists();
                        if (isInMyFavorite) {
                            binding.favoriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_favorite_24, 0, 0);
                            binding.favoriteBtn.setText("Remove Favorite ");

                        } else {

                            binding.favoriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_favorite_border, 0, 0);
                            binding.favoriteBtn.setText("Add Favorite ");


                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


    }


}