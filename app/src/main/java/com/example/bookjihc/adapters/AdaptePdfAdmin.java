package com.example.bookjihc.adapters;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookjihc.MyApplication;
import com.example.bookjihc.activities.PdfDetailActivity;
import com.example.bookjihc.activities.PdfEditActivity;
import com.example.bookjihc.databinding.RowPdfAdminBinding;
import com.example.bookjihc.filters.FilterPdfAdmin;
import com.example.bookjihc.models.ModelPdf;
import com.github.barteksc.pdfviewer.PDFView;


import java.util.ArrayList;

public class AdaptePdfAdmin extends RecyclerView.Adapter<AdaptePdfAdmin.HolderPdfAdmin> implements Filterable {


    private Context context;

    private FilterPdfAdmin filter;

    public ArrayList<ModelPdf> pdfArrayList, filterList;

    private RowPdfAdminBinding binding;
    private static final String Tag = "PDF_ADAPTER_TAG";

    private ProgressDialog progressDialog;


    public AdaptePdfAdmin(Context context, ArrayList<ModelPdf> pdfArrayList) {
        this.context = context;
        this.pdfArrayList = pdfArrayList;
        this.filterList = pdfArrayList;


        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Күте тұрыңыз");
        progressDialog.setCanceledOnTouchOutside(false);
    }

    @NonNull
    @Override
    public HolderPdfAdmin onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowPdfAdminBinding.inflate(LayoutInflater.from(context), parent, false);
        return new HolderPdfAdmin(binding.getRoot());

    }

    @Override
    public void onBindViewHolder(@NonNull HolderPdfAdmin holder, int position) {
        ModelPdf model = pdfArrayList.get(position);
        String pdfId = model.getId();
        String categoryId = model.getCategoryId();
        String title = model.getTitle();
        String descroption = model.getDescription();
        String pdfUrl = model.getUrl();
        long timestamp = model.getTimestamp();


        String formatteDate = MyApplication.formatTimestamp(timestamp);


        holder.titleTv.setText(title);
        holder.DesciptionTv.setText(descroption);
        holder.dateTv.setText(formatteDate);


        MyApplication.loadCategory("" + categoryId, holder.categoryTv);
        MyApplication.loadPdfFromUrlSinglePage(
                "" + pdfUrl,
                "" + title,
                holder.pdfView,
                holder.progressBar,
                null);
        MyApplication.loadPdfSize(
                "" + pdfUrl,
                "" + title,
                holder.sizeTv


        );


        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moreOptionsDialog(model, holder);
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PdfDetailActivity.class);
                intent.putExtra("bookId", pdfId);
                context.startActivity(intent);
            }
        });


    }

    private void moreOptionsDialog(ModelPdf model, HolderPdfAdmin holder) {
        String bookId = model.getId();
        String bookUrl = model.getUrl();
        String bookTitle = model.getTitle();


        String[] options = {"Edit", "Delete"};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Параметрлерді таңдаңыз").setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {

                    Intent intent = new Intent(context, PdfEditActivity.class);
                    intent.putExtra("bookId", bookId);
                    context.startActivity(intent);
                } else if (which == 1) {
                    MyApplication.deleteBook(context, "" + bookId, "" + bookUrl, "" + bookTitle);

                }

            }
        }).show();

    }


    @Override
    public int getItemCount() {
        return pdfArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new FilterPdfAdmin(filterList, this);
        }
        return filter;
    }

    class HolderPdfAdmin extends RecyclerView.ViewHolder {

        PDFView pdfView;
        ProgressBar progressBar;
        TextView titleTv, DesciptionTv, categoryTv, sizeTv, dateTv;
        ImageButton moreBtn;


        public HolderPdfAdmin(@NonNull View itemView) {
            super(itemView);
            pdfView = binding.pdfView;
            progressBar = binding.progressbar;
            titleTv = binding.titleTv;
            sizeTv = binding.sizeTv;
            categoryTv = binding.categoryTv;
            dateTv = binding.dateTv;
            DesciptionTv = binding.DesciptionTv;
            moreBtn = binding.moreBtn;


        }
    }


}
