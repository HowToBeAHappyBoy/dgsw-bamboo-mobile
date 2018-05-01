package com.dgsw.bamboo.recycler;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.dgsw.bamboo.R;
import com.dgsw.bamboo.data.PostData;
import com.dgsw.bamboo.fragment.PostViewFragment;

import java.util.ArrayList;
import java.util.Locale;

public class PostAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Activity activity;
    private PostViewFragment fragment;

    private int maxContent;

    private final static int TYPE_CONTENT = 0;
    private final static int TYPE_MORE = 1;

    private ArrayList<PostData> postDataArrayList;

    public PostAdapter(ArrayList<PostData> postDataArrayList, Activity activity, PostViewFragment fragment) {
        this.postDataArrayList = postDataArrayList;
        this.activity = activity;
        this.fragment = fragment;
    }

    @Override
    public int getItemViewType(int position) {
        if (postDataArrayList.size() < position + 1 && maxContent > 0) return TYPE_MORE;
        return TYPE_CONTENT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_MORE)
            return new MoreHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.row_more, parent, false));
        return new PostHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.row_content, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof PostHolder) {
            PostHolder postHolder = (PostHolder) holder;
            //holder.setIsRecyclable(false);

            PostData postData = postDataArrayList.get(position);
            postHolder.title.setText(String.format(Locale.KOREA, activity.getResources().getString(R.string.title), postData.getPostIndex()));
            postHolder.doc.setText(postData.getContent());
            postHolder.writeTime.setText(String.format(Locale.KOREA, activity.getResources().getString(R.string.write_time), postData.getWriteDay()));
            postHolder.allowTime.setText(String.format(Locale.KOREA, activity.getResources().getString(R.string.allow_time), postData.getAllowDay()));
            postHolder.fbButton.setOnClickListener(v -> {
                try {
                    activity.getPackageManager().getPackageInfo("com.facebook.katana", 0);
                    activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("fb://facewebmodal/f?href=https://www.facebook.com/hashtag/%EB%8C%80%EC%86%8C%EA%B3%A0_" + postData.getPostIndex() + "%EB%B2%88%EC%A7%B8_%EC%9D%B4%EC%95%BC%EA%B8%B0")));
                } catch (PackageManager.NameNotFoundException e) {
                    activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/hashtag/%EB%8C%80%EC%86%8C%EA%B3%A0_" + postData.getPostIndex() + "%EB%B2%88%EC%A7%B8_%EC%9D%B4%EC%95%BC%EA%B8%B0")));
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
        if (maxContent > postDataArrayList.size())
            return postDataArrayList.size() + 1;
        return postDataArrayList.size();
    }
}