package com.example.bookjihc.filters;

import android.widget.Filter;

import com.example.bookjihc.adapters.AdaptePdfAdmin;
import com.example.bookjihc.adapters.AdapterCategory;
import com.example.bookjihc.models.ModelCategory;
import com.example.bookjihc.models.ModelPdf;

import java.util.ArrayList;


public class FilterPdfAdmin extends Filter {
    ArrayList<ModelPdf> filterList;
    AdaptePdfAdmin adaptePdfAdmin;

    public FilterPdfAdmin(ArrayList<ModelPdf> filterList, AdaptePdfAdmin adaptePdfAdmin) {
        this.adaptePdfAdmin = adaptePdfAdmin;
        this.filterList = filterList;


    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();

        ArrayList<ModelPdf> filterModels = null;
        if (constraint != null && constraint.length() > 0) {
            constraint = constraint.toString().toUpperCase();
            filterModels = new ArrayList<>();
            for (int i = 0; i < filterList.size(); i++) {

                if (filterList.get(i).getTitle().toUpperCase().contains(constraint)) {

                    filterModels.add(filterList.get(i));
                }
            }


            results.count = filterModels.size();
            results.values = filterModels;

        } else {

            results.count = filterList.size();
            results.values = filterList;
        }

        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {


        adaptePdfAdmin.pdfArrayList = (ArrayList<ModelPdf>) results.values;

        adaptePdfAdmin.notifyDataSetChanged();


    }
}
