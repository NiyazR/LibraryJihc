package com.example.bookjihc.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.bookjihc.databinding.ActivityPdfAddBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;

public class PdfAddActivity extends AppCompatActivity {
    private ActivityPdfAddBinding binding;
    private static final String Tag = "ADD_PDF_TAG";
    private FirebaseAuth firebaseAuth;
    private static final int PDF_PICK_CODE = 1000;
    private Uri pdfUri;
    private ProgressDialog progressDialog;
    private ArrayList<String> categoryTitleArrayList, categoryIdArrayList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfAddBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());


        firebaseAuth = FirebaseAuth.getInstance();
        loadPdfCategories();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Күте тұрыңыз");
        progressDialog.setCanceledOnTouchOutside(false);


        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        binding.attachBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pdfPickIntent();
            }
        });


        binding.categotyTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                categotyPickDialog();
            }
        });

        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                validdate();
            }
        });

    }

    private String title = "", description = "";

    private void validdate() {
        Log.d(Tag, "validdate: validating data... ");

        title = binding.titleEt.getText().toString().trim();
        description = binding.DesciptionEt.getText().toString().trim();


        if (TextUtils.isEmpty(title)) {
            Toast.makeText(this, "Тақырыпты енгізіңіз...", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(description)) {
            Toast.makeText(this, "Сипаттаманы енгізіңіз...", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(selectedcategoryTitle)) {
            Toast.makeText(this, "Санатты таңдағыз...", Toast.LENGTH_SHORT).show();
        } else if (pdfUri == null) {
            Toast.makeText(this, "Pdf таңдаңыз...", Toast.LENGTH_SHORT).show();

        } else {
            uploadPdftoStorage();

        }


    }

    private void uploadPdftoStorage() {
        Log.d(Tag, "uploadPdftoStorage: uploading to storage");
        progressDialog.setMessage("Pdf жүктеп салынуда...");
        progressDialog.show();


        long timestamp = System.currentTimeMillis();
        String filePathAndName = "Books/" + timestamp;

        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
        storageReference.putFile(pdfUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d(Tag, "onSuccess:Pdf upload to storage..");
                Log.d(Tag, "onSuccess:getting pdf url");


                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();

                while (!uriTask.isSuccessful()) ;
                String uploadPdfUrl = "" + uriTask.getResult();


                uploadPdfInfotoDb(uploadPdfUrl, timestamp);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Log.d(Tag, "onFailure: Pdf upload failed due to" + e.getMessage());
                Toast.makeText(PdfAddActivity.this, " Pdf жүктеп салу себебіне байланысты орындалмады " + e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });


    }

    private void uploadPdfInfotoDb(String uploadPDfUrl, long timestamp) {
        Log.d(Tag, "uploadPdfInfotoDb: uploading PDf info to db...");
        progressDialog.setMessage("pdf ақпарат жүктеп салынуда...");

        String uid = firebaseAuth.getUid();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", "" + uid);
        hashMap.put("id", "" + timestamp);
        hashMap.put("title", "" + title);
        hashMap.put("description", "" + description);
        hashMap.put("categoryId", "" + selectedCategoryId);
        hashMap.put("url", "" + uploadPDfUrl);
        hashMap.put("timestamp", timestamp);
        hashMap.put("viewsCount" , 0);
        hashMap.put("downloadsCount", 0);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child("" + timestamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                progressDialog.dismiss();
                Log.d(Tag, "onSuccess: Successfully uploaded...");
                Toast.makeText(PdfAddActivity.this, "Сәтті жүктеп салынды", Toast.LENGTH_SHORT).show();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Log.d(Tag, "onFailure: Faile to upload to db due to" + e.getMessage());
                Toast.makeText(PdfAddActivity.this, "себебіне байланысты db-ге жүктеп салу мүмкін болмады " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void loadPdfCategories() {
        Log.d(Tag, "loadPdfCategories: Loading pdf categpries...");
        categoryTitleArrayList = new ArrayList<>();
        categoryIdArrayList = new ArrayList<>();


        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryTitleArrayList.clear();
                categoryIdArrayList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {

                    String categoryId = ""+ds.child("id").getValue();
                    String categoryTitle = ""+ds.child("category").getValue();


                    categoryTitleArrayList.add(categoryTitle);
                    categoryIdArrayList.add(categoryId);



                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private  String selectedCategoryId, selectedcategoryTitle;
    private void categotyPickDialog() {
        Log.d(Tag, "categotyPickDialog: showing category pick dialog");

        String[] categoriesArray = new String[categoryTitleArrayList.size()];
        for (int i = 0; i < categoryTitleArrayList.size(); i++) {
            categoriesArray[i] = categoryTitleArrayList.get(i);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick category").setItems(categoriesArray, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
               selectedcategoryTitle = categoryTitleArrayList.get(which);
               selectedCategoryId = categoryIdArrayList.get(which);





                binding.categotyTv.setText(selectedcategoryTitle);
                Log.d(Tag, "onClick: Selected category" + selectedCategoryId+""+selectedcategoryTitle);

            }
        }).show();

    }


    private void pdfPickIntent() {

        Log.d(Tag, "pdfpickIntent: Strating pick intent");

        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Pdf"), PDF_PICK_CODE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == PDF_PICK_CODE) {

                Log.d(Tag, "onActivityResult: Pdf Picked");
                pdfUri = data.getData();

                Log.d(Tag, "onActivityResult: Uri" + pdfUri);


            }
        } else {
            Log.d(Tag, "onActivityResult: canceleld pickig");
            Toast.makeText(this, "pdf таңдауы тоқтатылды ", Toast.LENGTH_SHORT).show();


        }
    }
}