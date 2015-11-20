package com.example.computer.letsstudy;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by lucky_luke on 11/18/2015.
 */
public class WordAdapter extends RecyclerView.Adapter<WordAdapter.ViewHolder> {

    private List<Word> listWord;

    public WordAdapter(List<Word> list)
    {
        listWord = list;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        public TextView word;
        public TextView mean;

        public ViewHolder(View view)
        {
            super(view);
            word = (TextView) view.findViewById(R.id.word);
            mean = (TextView) view.findViewById(R.id.mean);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //create a new view
        View rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.wordslistitem, parent, false);
        ViewHolder vh = new ViewHolder(rootView);

        return vh;
    }

    //replace the content of the view
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.word.setText(listWord.get(position).word);
        holder.mean.setText(listWord.get(position).mean);
    }

    //return the size of the dataset
    @Override
    public int getItemCount() {
        return listWord.size();
    }
}
