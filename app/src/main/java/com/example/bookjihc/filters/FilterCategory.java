package com.example.bookjihc.filters;

import java.util.ArrayList;


import android.widget.Filter;

import com.example.bookjihc.adapters.AdapterCategory;
import com.example.bookjihc.models.ModelCategory;


public class FilterCategory extends Filter {
    ArrayList<ModelCategory> filterList;
    AdapterCategory adapterCategory;

    public FilterCategory(ArrayList<ModelCategory> filterList, AdapterCategory adapterCategory) {
        this.adapterCategory = adapterCategory;
        this.filterList = filterList;


    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();

        ArrayList<ModelCategory> filterModels = null;
        if (constraint != null && constraint.length() > 0) {
            constraint = constraint.toString().toUpperCase();
            filterModels = new ArrayList<>();
            for (int i = 0; i < filterList.size(); i++) {

                if (filterList.get(i).getCategory().toUpperCase().contains(constraint)) {

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


        adapterCategory.categoryArrayList = (ArrayList<ModelCategory>) results.values;

        adapterCategory.notifyDataSetChanged();


    }
}
