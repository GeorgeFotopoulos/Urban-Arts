package com.aueb.urbanarts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchAdapter extends ArrayAdapter<ExampleItem> {
    private List<ExampleItem> countryListFull;
    public SearchAdapter(@NonNull Context context, @NonNull List<ExampleItem> countryList) {
        super(context, 0, countryList);
        countryListFull = new ArrayList<>(countryList);
    }
    @NonNull
    @Override
    public Filter getFilter() {
        return countryFilter;
    }
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(
                        R.layout.itemsearched, parent, false
                );
            }

        TextView textViewName = convertView.findViewById(R.id.text_view1);
        CircleImageView imageViewFlag = convertView.findViewById(R.id.image_view);
        ExampleItem countryItem = getItem(position);
        if (countryItem != null) {
            textViewName.setText(countryItem.getText1()+" ("+countryItem.getText2()+")");
            Glide.with(convertView).load(countryItem.getImageResource()).into(imageViewFlag);
        }
        return convertView;
    }
    private Filter countryFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            List<ExampleItem> suggestions = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                suggestions.addAll(countryListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (ExampleItem item : countryListFull) {
                    if (item.getText1().toLowerCase().contains(filterPattern)) {
                        suggestions.add(item);
                    }
                }
            }
            results.values = suggestions;
            results.count = suggestions.size();
            return results;
        }
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            clear();
            addAll((List) results.values);
            notifyDataSetChanged();
        }
        @Override
        public CharSequence convertResultToString(Object resultValue) {
            SearchArtists.ID=((ExampleItem) resultValue).getID();
            PostSomething.ID=((ExampleItem) resultValue).getID();
            return (((ExampleItem) resultValue).getText1()+" (UA User)");
        }
    };
}