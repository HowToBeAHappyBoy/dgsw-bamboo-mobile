package com.dgsw.bamboo.recycler;

import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.dgsw.bamboo.R;

public class PostHolder extends RecyclerView.ViewHolder {
    TextView title, doc, writeTime, allowTime;
    FloatingActionButton fbButton;

    public PostHolder(View itemView) {
        super(itemView);

        title = itemView.findViewById(R.id.title);
        doc = itemView.findViewById(R.id.doc);
        writeTime = itemView.findViewById(R.id.writeTime);
        allowTime = itemView.findViewById(R.id.allowTime);
        fbButton = itemView.findViewById(R.id.fb_button);
    }
}
