package com.aueb.urbanarts.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;

import com.aueb.urbanarts.R;
import com.bumptech.glide.Glide;

import java.util.List;

public class GridViewAdapter extends BaseAdapter {

    Context context;
    private final List<String> images;
    View view;
    LayoutInflater layoutInFlater;

    public GridViewAdapter(Context context, List<String> images) {
        this.context = context;
        this.images = images;
    }

    public static void setDynamicHeight(GridView gridView) {
        ListAdapter gridViewAdapter = gridView.getAdapter();
        if (gridViewAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = 0;
        int items = gridViewAdapter.getCount();
        int rows = 0;

        View listItem = gridViewAdapter.getView(0, null, gridView);
        listItem.measure(0, 0);
        totalHeight = listItem.getMeasuredHeight();


        float x;
        x = items / 3;
        if (items % 3 == 0) {
            rows = (int) x;
        } else {
            rows = (int) (x + 1);
        }

        totalHeight *= rows;


        ViewGroup.LayoutParams params = gridView.getLayoutParams();
        params.height = totalHeight;
        gridView.setLayoutParams(params);
    }

    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public Object getItem(int position) {
        return images.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        layoutInFlater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            view = new View(context);
            view = layoutInFlater.inflate(R.layout.grid_item, null);
            ImageView imageDisplay = view.findViewById(R.id.image);
            Glide.with(context)
                    .load(images.get(position))
                    .override(200, 200)
                    .into(imageDisplay);
        }
        return view;
    }

    public void clear(GridView gridView) {
        ListAdapter gridViewAdapter = gridView.getAdapter();
        if (gridViewAdapter == null) {
            // pre-condition
            return;
        }

        ViewGroup.LayoutParams params = gridView.getLayoutParams();
        params.height = 0;
        gridView.setLayoutParams(params);
    }
}
