package com.example.bookjihc.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import com.example.bookjihc.adapters.AdaptePdfAdmin;
import com.example.bookjihc.databinding.ActivityPdfListAdminBinding;
import com.example.bookjihc.models.ModelPdf;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class PdfListAdminActivity extends AppCompatActivity {
    private ActivityPdfListAdminBinding binding;


    private ArrayList<ModelPdf> pdfArrayList;
    private AdaptePdfAdmin adaptePdfAdmin;

    private String categoryId, categoryTitle;

    private static  final String Tag = "PDF_LIST_TAG";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfListAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        Intent intent = getIntent();
        categoryId = intent.getStringExtra("categoryId");
        categoryTitle = intent.getStringExtra("categoryTitle");

        binding.subTitleTv.setText(categoryTitle);

        loadpdflist();

        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                try {
                    adaptePdfAdmin.getFilter().filter(s);

                }
                catch (Exception e ){
                    Log.d(Tag, "onTextChanged: "+e.getMessage());


                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


    }

    private void loadpdflist() {

        pdfArrayList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.orderByChild("categoryId").equalTo(categoryId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                pdfArrayList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelPdf model = ds.getValue(ModelPdf.class);
                    pdfArrayList.add(model);

                    Log.d(Tag, "onDataChange: "+model.getId()+""+model.getTitle());

                }

                adaptePdfAdmin = new AdaptePdfAdmin(PdfListAdminActivity.this,pdfArrayList);
                binding.bookRv.setAdapter(adaptePdfAdmin);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }
}