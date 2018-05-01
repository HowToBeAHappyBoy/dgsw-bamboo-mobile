package com.dgsw.bamboo.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dgsw.bamboo.recycler.PostAdapter;
import com.dgsw.bamboo.data.PostData;
import com.dgsw.bamboo.R;
import com.dgsw.bamboo.data.URLS;

import net.htmlparser.jericho.Source;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class PostViewFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private int count, maxContent;

    private ArrayList<PostData> postDataArrayList = new ArrayList<>();

    private PostAdapter postAdapter;

    private SwipeRefreshLayout swipeRefreshLayout;

    public static PostViewFragment newInstance() {
        return new PostViewFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post_view, container, false);

        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setRefreshing(true);
        swipeRefreshLayout.setOnRefreshListener(this);
        final RecyclerView recyclerView = view.findViewById(R.id.docs);

        postAdapter = new PostAdapter(postDataArrayList, getActivity(), this);

        if (postDataArrayList.isEmpty()) {
            new GetCount().setTaskListener(i -> {
                maxContent = i;
                postAdapter.setMaxContent(maxContent);
                new GetPost(count).setTaskListener(postDataList -> {
                    postDataArrayList.addAll(postDataList);
                    if (maxContent >= count) count += 5;
                    postAdapter.notifyDataSetChanged();
                    swipeRefreshLayout.setRefreshing(false);
                }).execute();
            }).execute();
        } else {
            swipeRefreshLayout.setRefreshing(false);
            swipeRefreshLayout.setRefreshing(false);
            postAdapter.setMaxContent(maxContent);
        }

        recyclerView.setAdapter(postAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        return view;
    }

    public void getPosts() {
        new GetPost(count).setTaskListener(postDataList -> {
            if (maxContent >= count) count += 5;

            postDataArrayList.addAll(postDataList);
            postAdapter.notifyItemInserted(postDataList.size() - 1);
            postAdapter.notifyDataSetChanged();
        }).execute();
    }

    @Override
    public void onRefresh() {
        count = 0;
        new GetCount().setTaskListener(i -> {
            maxContent = i;
            postAdapter.setMaxContent(maxContent);
            postDataArrayList.clear();
            new GetPost(count).setTaskListener(postDataList -> {
                if (maxContent >= count) count += 5;

                postDataArrayList.addAll(postDataList);
                postAdapter.notifyItemRangeChanged(0, postDataArrayList.size() - 1);
                postAdapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
            }).execute();
        }).execute();
    }

    private static class GetCount extends AsyncTask<Void, Void, Integer> {

        private TaskListener taskListener;

        @Override
        protected Integer doInBackground(Void... voids) {
            try {
                Source cntSource = new Source(new URL(URLS.USER.GET.countURL));
                cntSource.fullSequentialParse();
                JSONObject jsonObject = new JSONObject(cntSource.getSource().toString());

                return jsonObject.getInt("count");
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return 0;
        }

        @Override
        protected void onPostExecute(final Integer i) {
            super.onPostExecute(i);
            if (taskListener != null) taskListener.onTaskFinished(i);
        }

        GetCount setTaskListener(TaskListener taskListener) {
            this.taskListener = taskListener;
            return this;
        }

        public interface TaskListener {
            void onTaskFinished(Integer i);
        }
    }

    private static class GetPost extends AsyncTask<Void, Integer, ArrayList<PostData>> {

        private int index;

        private TaskListener taskListener;

        GetPost(int index) {
            this.index = index;
        }

        @Override
        protected final ArrayList<PostData> doInBackground(Void... v) {

            ArrayList<PostData> postDataArrayList = new ArrayList<>();
            try {
                Source cntSource = new Source(new URL(URLS.USER.GET.postedURL + index));
                cntSource.fullSequentialParse();
                JSONObject jsonObject = new JSONObject(cntSource.getSource().toString());
                JSONArray jsonArray = jsonObject.getJSONArray("posted");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject dataObject = jsonArray.getJSONObject(i);

                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.KOREA);
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("M월 d일 h시 m분", Locale.KOREA);

                    Calendar calendarWrite = Calendar.getInstance();
                    calendarWrite.setTime(dateFormat.parse(dataObject.getString("writeDate")));
                    Calendar calendarAllow = Calendar.getInstance();
                    calendarAllow.setTime(dateFormat.parse(dataObject.getString("allowDate")));


                    postDataArrayList.add(new PostData(dataObject.getInt("idx"), dataObject.getString("desc"), simpleDateFormat.format(calendarWrite.getTime()), simpleDateFormat.format(calendarAllow.getTime())));
                }

            } catch (IOException | JSONException | ParseException e) {
                e.printStackTrace();
            }
            return postDataArrayList;
        }

        @Override
        protected void onPostExecute(ArrayList<PostData> postDatas) {
            super.onPostExecute(postDatas);
            if (taskListener != null) taskListener.onTaskFinished(postDatas);
        }

        GetPost setTaskListener(TaskListener taskListener) {
            this.taskListener = taskListener;
            return this;
        }

        public interface TaskListener {
            void onTaskFinished(ArrayList<PostData> postDataArrayList);
        }
    }
}
