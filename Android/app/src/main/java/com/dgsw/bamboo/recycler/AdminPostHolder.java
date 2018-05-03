package com.dgsw.bamboo.recycler;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.dgsw.bamboo.R;

public class AdminPostHolder extends RecyclerView.ViewHolder {
    TextView doc, writeTime;
    Button allowButton, denyButton;

    public AdminPostHolder(View itemView) {
        super(itemView);

        doc = itemView.findViewById(R.id.doc);
        writeTime = itemView.findViewById(R.id.writeTime);
        allowButton = itemView.findViewById(R.id.allowButton);
        denyButton = itemView.findViewById(R.id.denyButton);
    }
}
