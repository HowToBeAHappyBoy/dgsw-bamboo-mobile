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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static com.dgsw.bamboo.Tools.isInternetAvailable;

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

        if (isInternetAvailable()) {
            postAdapter.setOnline(true);
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
                postAdapter.setMaxContent(maxContent);
            }
        } else {
            postAdapter.setOnline(false);
            swipeRefreshLayout.setRefreshing(false);
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
        if (isInternetAvailable()) {
            postAdapter.setOnline(true);
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
        } else {
            postAdapter.setOnline(false);
            swipeRefreshLayout.setRefreshing(false);
            postDataArrayList.clear();
            postAdapter.notifyDataSetChanged();
        }
    }

    private static class GetCount extends AsyncTask<Void, Void, Integer> {

        private TaskListener taskListener;

        @Override
        protected Integer doInBackground(Void... voids) {
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(URLS.USER.GET.countURL);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestProperty("Accept", "application/json");
                InputStream inputStream = urlConnection.getInputStream();

                StringBuilder stringBuilder = new StringBuilder();
                if (inputStream == null) {
                    return 0;
                }
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                String inputLine;
                while ((inputLine = reader.readLine()) != null)
                    stringBuilder.append(inputLine).append("\n");
                if (stringBuilder.length() == 0) {
                    return 0;
                }

                if (urlConnection.getResponseCode() != HttpURLConnection.HTTP_INTERNAL_ERROR) {
                    if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_NO_CONTENT)
                        return 0;
                    JSONObject jsonObject = new JSONObject(stringBuilder.toString());
                    return jsonObject.getInt("count");
                }
                inputStream.close();
                reader.close();
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
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

            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(URLS.USER.GET.postedURL + index);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestProperty("Accept", "application/json");
                InputStream inputStream = urlConnection.getInputStream();

                StringBuilder stringBuilder = new StringBuilder();
                if (inputStream == null) {
                    return postDataArrayList;
                }
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                String inputLine;
                while ((inputLine = reader.readLine()) != null)
                    stringBuilder.append(inputLine).append("\n");
                if (stringBuilder.length() == 0) {
                    return postDataArrayList;
                }

                if (urlConnection.getResponseCode() != HttpURLConnection.HTTP_INTERNAL_ERROR) {
                    if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_NO_CONTENT)
                        return postDataArrayList;

                    JSONObject jsonObject = new JSONObject(stringBuilder.toString());
                    JSONArray jsonArray = jsonObject.getJSONArray("posted");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject dataObject = jsonArray.getJSONObject(i);

                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.KOREA);

                        Calendar calendarWrite = Calendar.getInstance();
                        calendarWrite.setTime(dateFormat.parse(dataObject.getString("writeDate")));
                        Calendar calendarAllow = Calendar.getInstance();
                        calendarAllow.setTime(dateFormat.parse(dataObject.getString("allowDate")));

                        postDataArrayList.add(new PostData(dataObject.getInt("idx"), dataObject.getString("desc"), convertDateTime(calendarWrite.getTime()), convertDateTime(calendarAllow.getTime())));
                    }
                }
                inputStream.close();
                reader.close();
            } catch (IOException | ParseException | JSONException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return postDataArrayList;
        }

        private String convertDateTime(Date date) {
            if (new SimpleDateFormat("m", Locale.KOREA).format(date.getTime()).equals("0"))
                return new SimpleDateFormat("M월 d일 h시", Locale.KOREA).format(date.getTime());
            else
                return new SimpleDateFormat("M월 d일 h시 m분", Locale.KOREA).format(date.getTime());
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
