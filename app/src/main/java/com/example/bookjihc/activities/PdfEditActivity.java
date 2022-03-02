package com.example.bookjihc.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.bookjihc.databinding.ActivityPdfEditBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class PdfEditActivity extends AppCompatActivity {

    private ActivityPdfEditBinding binding;

    private String bookId;

    private ProgressDialog progressDialog;

    private ArrayList<String> categoryArrayTitleList, categoryArrayIdList;

    private static final String Tag = "BOOK_EDIT_TAG";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        bookId = getIntent().getStringExtra("bookId");

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Күте тұрыңыз");
        progressDialog.setCanceledOnTouchOutside(false);

        loadCategories();
        loadBookInfo();


        binding.categotyTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                categoryDialog();
            }
        });


        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                validdate();

            }
        });


    }

    private String title="", description="";

    private void validdate() {




        title = binding.titleEt.getText().toString().trim();
        description = binding.DesciptionEt.getText().toString().trim();


        if (TextUtils.isEmpty(title)){
            Toast.makeText(this, "тақырыпты енгізіңіз...", Toast.LENGTH_SHORT).show();


        }
       else if (TextUtils.isEmpty(description)){
            Toast.makeText(this, "сипаттаманы енгізіңіз...", Toast.LENGTH_SHORT).show();


        }
        else if (TextUtils.isEmpty(selectedcategoryId)){
            Toast.makeText(this, "Санат таңдаңыз", Toast.LENGTH_SHORT).show();


        } else {updatePdf();}

    }

    private void updatePdf() {
        Log.d(Tag, "updatePdf: ДБ-ге pdf жаңарту ақпаратын бастау...");
        progressDialog.setMessage("Кітап ақпараты жаңартылуда...");
        progressDialog.show();


        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("title", ""+title);
        hashMap.put("description", ""+description);
        hashMap.put("categoryId", ""+selectedcategoryId);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d(Tag, "onSuccess: Кітап жаңартылды...");
                progressDialog.dismiss();
                Toast.makeText(PdfEditActivity.this, "Кітап туралы ақпарат жаңартылды...", Toast.LENGTH_SHORT).show();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(Tag, "onFailure:  жаңарту сәтсіз аяқталды себебі"+e.getMessage());
                progressDialog.dismiss();
                Toast.makeText(PdfEditActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });



    }

    private void loadBookInfo() {
        Log.d(Tag, "loadBookInfo: Кітап туралы ақпарат жүктелуде...");

        DatabaseReference refBooks = FirebaseDatabase.getInstance().getReference("Books");
        refBooks.child(bookId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                selectedcategoryId = "" + snapshot.child("categoryId").getValue();
                String description = "" + snapshot.child("description").getValue();
                String title = "" + snapshot.child("title").getValue();


                binding.titleEt.setText(title);
                binding.DesciptionEt.setText(description);
                Log.d(Tag, "onDataChange:Кітап санаты туралы ақпарат жүктелуде");
                DatabaseReference refBookCategory = FirebaseDatabase.getInstance().getReference("Categories");
                refBookCategory.child(selectedcategoryId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String category = ""+snapshot.child("category").getValue();

                        binding.categotyTv.setText(category);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private String selectedcategoryId = "", selectedCategorytitle = "";

    private void categoryDialog() {
        String[] categoriesArray = new String[categoryArrayTitleList.size()];
        for (int i = 0; i < categoryArrayTitleList.size(); i++) {
            categoriesArray[i] = categoryArrayTitleList.get(i);

        }


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Санат таңдаңыз")
                .setItems(categoriesArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedcategoryId = categoryArrayIdList.get(which);
                        selectedCategorytitle = categoryArrayTitleList.get(which);

                        binding.categotyTv.setText(selectedCategorytitle);
                    }
                }).show();

    }

    private void loadCategories() {
        Log.d(Tag, "loadCategories: Санаттар жүктелуде");
        categoryArrayIdList = new ArrayList<>();
        categoryArrayTitleList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryArrayIdList.clear();
                categoryArrayTitleList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String id = "" + ds.child("id").getValue();
                    String category = "" + ds.child("category").getValue();
                    categoryArrayIdList.add(id);
                    categoryArrayTitleList.add(category);
                    Log.d(Tag, "onDataChange: ID" + id);
                    Log.d(Tag, "onDataChange: Category" + category);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}