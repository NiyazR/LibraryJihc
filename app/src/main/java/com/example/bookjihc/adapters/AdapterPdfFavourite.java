package com.example.bookjihc.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.ColorSpace;
import android.provider.ContactsContract;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookjihc.MyApplication;
import com.example.bookjihc.activities.PdfDetailActivity;
import com.example.bookjihc.databinding.RowPdfFavouriteBinding;
import com.example.bookjihc.models.ModelPdf;
import com.github.barteksc.pdfviewer.PDFView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class AdapterPdfFavourite extends RecyclerView.Adapter<AdapterPdfFavourite.HolderPdfFavourite> {

    private Context context;
    private ArrayList<ModelPdf> pdfArrayList;

    private RowPdfFavouriteBinding binding;

    private static final String TAG = "FAV_BOOK_TAG";


    public AdapterPdfFavourite(Context context, ArrayList<ModelPdf> pdfArrayList) {
        this.context = context;
        this.pdfArrayList = pdfArrayList;
    }

    @NonNull
    @Override
    public HolderPdfFavourite onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowPdfFavouriteBinding.inflate(LayoutInflater.from(context), parent, false);
        return new HolderPdfFavourite(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderPdfFavourite holder, int position) {

        ModelPdf model = pdfArrayList.get(position);

        loadBookDetails(model, holder);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PdfDetailActivity.class);
                intent.putExtra("bookId",model.getId());


            }
        });


        holder.removeFavBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


            }
        });


    }

    private void loadBookDetails(ModelPdf model, HolderPdfFavourite holder) {


        String bookId = model.getId();
        Log.d(TAG, "loadBookDetails: Кітап идентификаторының кітап мәліметтері" + bookId);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId).
                addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String bookTitle = "" + snapshot.child("title").getValue();

                        String description = "" + snapshot.child("description").getValue();
                        String timestamp = "" + snapshot.child("timestamp").getValue();
                        String bookUrl = "" + snapshot.child("url").getValue();
                        String categoryId = "" + snapshot.child("categoryId").getValue();
                        String uid = "" + snapshot.child("uid").getValue();
                        String viewsCount = "" + snapshot.child("viewsCount").getValue();
                        String downloadsCount = "" + snapshot.child("downloadsCount").getValue();


                        model.setFavorite(true);
                        model.setTitle(bookTitle);
                        model.setDescription(description);
                        model.setTimestamp(Long.parseLong(timestamp));
                        model.setCategoryId(categoryId);
                        model.setUid(uid);
                        model.setUrl(bookUrl);

                        String date = MyApplication.formatTimestamp(Long.parseLong(timestamp));


                        MyApplication.loadCategory(categoryId , holder.categoryTv);
                        MyApplication.loadPdfFromUrlSinglePage(""+bookUrl,""+ bookTitle , holder.pdfView, holder.progressbar, null);
                        MyApplication.loadPdfSize(""+bookUrl, ""+bookTitle, holder.sizeTv);


                        holder.titleTv.setText(bookTitle);
                        holder.DesciptionTv.setText(description);
                        holder.dateTv.setText(date);






                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


    }

    @Override
    public int getItemCount() {
        return pdfArrayList.size();
    }

    class HolderPdfFavourite extends RecyclerView.ViewHolder {

        PDFView pdfView;
        ProgressBar progressbar;
        TextView titleTv, DesciptionTv, categoryTv, sizeTv, dateTv;
        ImageButton removeFavBtn;


        public HolderPdfFavourite(@NonNull View itemView) {
            super(itemView);

            pdfView = binding.pdfView;
            progressbar = binding.progressbar;
            titleTv = binding.titleTv;
            removeFavBtn = binding.removeFavBtn;
            DesciptionTv = binding.DesciptionTv;
            categoryTv = binding.categoryTv;
            sizeTv = binding.sizeTv;
            dateTv = binding.dateTv;


        }
    }
}
