package com.example.computer.letsstudy;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by lucky_luke on 11/12/2015.
 */
public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.CategoriesViewHolder> {

    private List<CategoriesInfo> categoriesList;
    OnItemClickListener mItemClickListener;

    public CategoriesAdapter(List<CategoriesInfo> categoriesList)
    {
        this.categoriesList = categoriesList;
    }

    @Override
    public int getItemCount()
    {
        return categoriesList.size();
    }

    @Override
    public void onBindViewHolder(CategoriesViewHolder categoriesViewHolder, int i)
    {
        CategoriesInfo ci = categoriesList.get(i);
        categoriesViewHolder.title.setText(ci.title);
        categoriesViewHolder.info.setText(ci.info);
        //categoriesViewHolder.icon.setImageDrawable(ci.icon);
        categoriesViewHolder.icon.setImageResource(ci.icon);
    }

    @Override
    public CategoriesViewHolder onCreateViewHolder(ViewGroup viewGroup, int i)
    {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.categories_card_view, viewGroup, false);
        return new CategoriesViewHolder(itemView);
    }

    public class CategoriesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        public TextView title;
        public TextView info;
        public ImageView icon;

        public CategoriesViewHolder(View v)
        {
            super(v);
            title = (TextView) v.findViewById(R.id.title_categories);
            info = (TextView) v.findViewById(R.id.info_categories);
            icon = (ImageView) v.findViewById(R.id.profile_picture_categories);
        }

        @Override
        public void onClick(View v)
        {
            if(mItemClickListener != null)
            {
                mItemClickListener.onItemClick(itemView, getPosition());
            }
        }
    }

    public interface OnItemClickListener
    {
        void onItemClick(View view, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener)
    {
        this.mItemClickListener = mItemClickListener;
    }
}
