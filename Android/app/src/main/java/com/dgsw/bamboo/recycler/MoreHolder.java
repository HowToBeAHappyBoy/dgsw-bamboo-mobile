package com.dgsw.bamboo.recycler;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import com.dgsw.bamboo.R;

public class MoreHolder extends RecyclerView.ViewHolder {
    Button moreButton;

    public MoreHolder(View itemView) {
        super(itemView);
        moreButton = itemView.findViewById(R.id.more_button);
    }
}
