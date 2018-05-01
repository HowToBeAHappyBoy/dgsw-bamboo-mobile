package com.dgsw.bamboo.recycler;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.dgsw.bamboo.R;
import com.dgsw.bamboo.data.PostData;
import com.dgsw.bamboo.fragment.AdminFragment;

import java.util.ArrayList;
import java.util.Locale;

public class AdminPostAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Activity activity;
    private AdminFragment fragment;

    private int maxContent;

    private final static int TYPE_EMPTY = -1;
    private final static int TYPE_CONTENT = 0;
    private final static int TYPE_MORE = 1;

    private ArrayList<PostData> postDataArrayList;

    public AdminPostAdapter(ArrayList<PostData> postDataArrayList, Activity activity, AdminFragment fragment) {
        this.postDataArrayList = postDataArrayList;
        this.activity = activity;
        this.fragment = fragment;
    }

    @Override
    public int getItemViewType(int position) {
        if (maxContent == 0) return TYPE_EMPTY;
        if (postDataArrayList.size() < position + 1 && maxContent > 0) return TYPE_MORE;
        return TYPE_CONTENT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_EMPTY) return new RecyclerView.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.row_empty_admin, parent, false)) {
            @Override
            public String toString() {
                return super.toString();
            }
        };
        else if (viewType == TYPE_MORE)
            return new MoreHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.row_more, parent, false));
        return new AdminPostHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.row_admin_content, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof AdminPostHolder) {
            AdminPostHolder adminPostHolder = (AdminPostHolder) holder;
            PostData postData = postDataArrayList.get(position);

            adminPostHolder.doc.setText(postData.getContent());
            adminPostHolder.writeTime.setText(String.format(Locale.KOREA, activity.getResources().getString(R.string.write_time), postData.getWriteDay()));
            adminPostHolder.allowButton.setOnClickListener(v -> {
                if (fragment.postAllow(postData.getPostIndex())) {
                    postDataArrayList.remove(position);
                    --maxContent;
                    notifyItemRemoved(position);
                }
            });
            adminPostHolder.denyButton.setOnClickListener(v -> {
                if (fragment.postDeny(postData.getPostIndex())) {
                    postDataArrayList.remove(position);
                    --maxContent;
                    notifyItemRemoved(position);
                }
            });
        } else if (holder instanceof MoreHolder) {
            MoreHolder moreHolder = (MoreHolder) holder;
            moreHolder.moreButton.setOnClickListener(v -> fragment.getPosts());
        }
    }

    public void setMaxContent(int maxContent) {
        this.maxContent = maxContent;
    }

    @Override
    public int getItemCount() {
        if (maxContent == 0)
            return 1;
        if (maxContent > postDataArrayList.size())
            return postDataArrayList.size() + 1;
        return postDataArrayList.size();
    }
}