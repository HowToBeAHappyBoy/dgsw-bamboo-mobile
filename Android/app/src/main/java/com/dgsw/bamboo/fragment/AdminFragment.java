package com.dgsw.bamboo.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import com.dgsw.bamboo.annotation.ViewVisibility;
import com.dgsw.bamboo.text.SimpleTextWatcher;
import com.dgsw.bamboo.tool.NetTool;
import com.dgsw.bamboo.recycler.AdminPostAdapter;
import com.dgsw.bamboo.data.Data;
import com.dgsw.bamboo.data.PostData;
import com.dgsw.bamboo.R;
import com.dgsw.bamboo.data.URLS;
import com.dgsw.bamboo.activity.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import static com.dgsw.bamboo.tool.NetTool.isInternetAvailable;

public class AdminFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private int count, maxContent;

    private ArrayList<PostData> postDataArrayList = new ArrayList<>();

    private SwipeRefreshLayout swipeRefreshLayout;

    private ConstraintLayout admin;
    private ConstraintLayout signIn;

    private TextInputEditText id;
    private TextInputEditText pw;

    AdminPostAdapter adminPostAdapter;

    public static AdminFragment newInstance() {
        return new AdminFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_admin_sets, container, false);

        admin = view.findViewById(R.id.admin);
        signIn = view.findViewById(R.id.signIn);
        Activity activity = getActivity();

        TextInputLayout idLayout = view.findViewById(R.id.inputIDLayout);
        TextInputLayout pwLayout = view.findViewById(R.id.inputPWLayout);
        id = view.findViewById(R.id.inputID);
        id.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (idLayout.isErrorEnabled())
                    idLayout.setErrorEnabled(false);
                if (pwLayout.isErrorEnabled())
                    pwLayout.setErrorEnabled(false);
            }
        });
        pw = view.findViewById(R.id.inputPW);
        pw.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (idLayout.isErrorEnabled())
                    idLayout.setErrorEnabled(false);
                if (pwLayout.isErrorEnabled())
                    pwLayout.setErrorEnabled(false);
            }
        });
        Button signInButton = view.findViewById(R.id.signInButton);
        signInButton.setOnClickListener(v -> {
            if (!NetTool.isInternetAvailable()) {
                Snackbar.make(view, getString(R.string.offline), Snackbar.LENGTH_SHORT).show();
                return;
            }
            JSONObject jsonObject = new JSONObject();
            if (id.getText() != null && pw.getText() != null)
                try {
                    jsonObject.put("id", id.getText().toString());
                    jsonObject.put("pw", pw.getText().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            new PostTask(v, (MainActivity) activity).setTaskListener(resCode -> {
                switch (resCode) {
                    case HttpURLConnection.HTTP_OK:
                        setViewVisibility(View.VISIBLE, View.GONE);
                        if (activity != null) {
                            activity.setTitle(R.string.admin);
                            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                            if (imm != null)
                                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        }
                        id.getText().clear();
                        pw.getText().clear();
                        adminViewInit(view);
                        break;
                    case HttpURLConnection.HTTP_UNAUTHORIZED:
                    case HttpURLConnection.HTTP_INTERNAL_ERROR:
                        idLayout.setError(" ");
                        pwLayout.setError(getString(R.string.sign_in_error));
                        Snackbar.make(v, getString(R.string.sign_in_error), Snackbar.LENGTH_SHORT).show();
                        break;
                }
            }).execute(new PostRequest(jsonObject, 0));
        });


        if (Data.isEmpty()) {
            setViewVisibility(View.GONE, View.VISIBLE);

            if (activity != null)
                activity.setTitle(R.string.admin_sign_in);

        } else {
            setViewVisibility(View.VISIBLE, View.GONE);
            adminViewInit(view);
        }
        return view;
    }

    public void adminViewInit(View view) {
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setRefreshing(true);
        swipeRefreshLayout.setOnRefreshListener(this);

        final RecyclerView recyclerView = view.findViewById(R.id.docs);

        adminPostAdapter = new AdminPostAdapter(postDataArrayList, getActivity(), this);

        if (isInternetAvailable()) {
            adminPostAdapter.setOnline(true);
            if (postDataArrayList.isEmpty()) {
                new GetCount().setTaskListener(i -> {
                    maxContent = i;
                    adminPostAdapter.setMaxContent(maxContent);
                    new GetPost(count).setTaskListener(postDataList -> {
                        postDataArrayList.addAll(postDataList);
                        if (maxContent >= count) if (maxContent != 0) count += 5;
                        adminPostAdapter.notifyDataSetChanged();
                        swipeRefreshLayout.setRefreshing(false);
                    }).execute(Data.getTokenToString());
                }).execute(Data.getTokenToString());
            } else {
                swipeRefreshLayout.setRefreshing(false);
                adminPostAdapter.setMaxContent(maxContent);
            }
        } else {
            adminPostAdapter.setOnline(false);
            swipeRefreshLayout.setRefreshing(false);
        }

        recyclerView.setAdapter(adminPostAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    public void setViewVisibility(@ViewVisibility int adminVisibility, @ViewVisibility int signInVisibility) {
        signIn.setVisibility(signInVisibility);
        admin.setVisibility(adminVisibility);
    }

    public void getPosts() {
        new GetPost(count).setTaskListener(postDataList -> {
            if (maxContent != 0) {
                if (maxContent >= count) count += 5;

                postDataArrayList.addAll(postDataList);
                adminPostAdapter.notifyItemInserted(postDataList.size() - 1);
                adminPostAdapter.notifyDataSetChanged();
            }
        }).execute(Data.getTokenToString());
    }

    public boolean postAllow(int idx) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", idx);
            jsonObject.put("admin", Data.adminName);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        int responseCode = 0;
        try {
            responseCode = new PostTask(getView(), (MainActivity) getActivity()).execute(new PostRequest(jsonObject, Data.getTokenToString(), 1)).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return (responseCode == HttpURLConnection.HTTP_CREATED || responseCode == 232);
    }

    public boolean postDeny(int idx) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", idx);
            jsonObject.put("admin", Data.adminName);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        int responseCode = 0;
        try {
            responseCode = new PostTask(getView(), (MainActivity) getActivity()).execute(new PostRequest(jsonObject, Data.getTokenToString(), 2)).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return (responseCode == 201 || responseCode == 232);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (id.getText() != null && pw.getText() != null) {
            id.getText().clear();
            pw.getText().clear();
        }

    }

    @Override
    public void onRefresh() {
        if (isInternetAvailable()) {
            adminPostAdapter.setOnline(true);
            count = 0;
            new GetCount().setTaskListener(i -> {
                maxContent = i;
                adminPostAdapter.setMaxContent(maxContent);
                postDataArrayList.clear();
                new GetPost(count).setTaskListener(postDataList -> {
                    if (maxContent >= count) count += 5;

                    postDataArrayList.addAll(postDataList);
                    adminPostAdapter.notifyItemRangeChanged(0, postDataArrayList.size() - 1);
                    adminPostAdapter.notifyDataSetChanged();
                    swipeRefreshLayout.setRefreshing(false);
                }).execute(Data.getTokenToString());
            }).execute(Data.getTokenToString());
        } else {
            adminPostAdapter.setOnline(false);
            swipeRefreshLayout.setRefreshing(false);
            postDataArrayList.clear();
            adminPostAdapter.notifyDataSetChanged();
        }
    }

    private class PostRequest {
        private JSONObject jsonObject;
        private String token;
        private int type;

        PostRequest(JSONObject jsonObject, int type) {
            this.jsonObject = jsonObject;
            this.type = type;
        }

        PostRequest(JSONObject jsonObject, String token, int type) {
            this.jsonObject = jsonObject;
            this.token = token;
            this.type = type;
        }

        public JSONObject getJsonObject() {
            return jsonObject;
        }

        public String getToken() {
            return token;
        }

        public int getType() {
            return type;
        }
    }

    private static class PostTask extends AsyncTask<PostRequest, Integer, Integer> {

        private TaskListener taskListener;

        private WeakReference<View> viewWeakReference;
        private WeakReference<MainActivity> activityWeakReference;

        PostTask(View view, MainActivity mainActivity) {
            this.viewWeakReference = new WeakReference<>(view);
            this.activityWeakReference = new WeakReference<>(mainActivity);
        }

        @Override
        protected Integer doInBackground(PostRequest... postRequests) {
            HttpURLConnection urlConnection = null;
            try {
                String URL = "";
                switch (postRequests[0].getType()) {
                    case 0:
                        URL = URLS.signInURL;
                        break;
                    case 1:
                        URL = URLS.ADMIN.POST.allowURL;
                        break;
                    case 2:
                        URL = URLS.ADMIN.POST.denyURL;
                        break;
                }
                URL url = new URL(URL);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);

                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestProperty("Accept", "application/json");
                if (postRequests[0].getType() != 0)
                    urlConnection.setRequestProperty("Authorization", postRequests[0].getToken());

                Writer writer = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream(), "UTF-8"));
                writer.write(postRequests[0].getJsonObject().toString());

                writer.close();
                InputStream inputStream;
                try {
                    inputStream = urlConnection.getInputStream();
                } catch (IOException e) {
                    return urlConnection.getResponseCode();
                }
                StringBuilder stringBuilder = new StringBuilder();
                if (inputStream == null) {
                    return 0;
                }
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                String inputLine;
                while ((inputLine = reader.readLine()) != null)
                    stringBuilder.append(inputLine).append("\n");
                if (stringBuilder.length() == 0) {
                    return urlConnection.getResponseCode();
                }
                if (urlConnection.getResponseCode() != HttpURLConnection.HTTP_UNAUTHORIZED
                        && urlConnection.getResponseCode() != HttpURLConnection.HTTP_INTERNAL_ERROR)
                    switch (postRequests[0].getType()) {
                        case 0:
                            JSONObject jsonObject = new JSONObject(stringBuilder.toString());
                            Data.adminName = jsonObject.getString("admin");
                            Data.setToken(jsonObject.getString("token"));
                            Snackbar.make(viewWeakReference.get(), "환영합니다, " + Data.adminName + "님", Snackbar.LENGTH_SHORT).show();
                            new Handler(Looper.getMainLooper()).post(() -> activityWeakReference.get().adminMenuVisible(true));
                            break;
                        case 1:
                            if (urlConnection.getResponseCode() == 232) {
                                Snackbar.make(viewWeakReference.get(), activityWeakReference.get().getString(R.string.already_do), Snackbar.LENGTH_SHORT).show();
                                break;
                            }
                            Snackbar.make(viewWeakReference.get(), activityWeakReference.get().getString(R.string.allow_ok), Snackbar.LENGTH_SHORT).show();
                            break;
                        case 2:
                            if (urlConnection.getResponseCode() == 232) {
                                Snackbar.make(viewWeakReference.get(), activityWeakReference.get().getString(R.string.already_do), Snackbar.LENGTH_SHORT).show();
                                break;
                            }
                            Snackbar.make(viewWeakReference.get(), activityWeakReference.get().getString(R.string.deny_ok), Snackbar.LENGTH_SHORT).show();
                            break;
                    }
                else
                    Snackbar.make(viewWeakReference.get(), activityWeakReference.get().getString(R.string.unknown_error), Snackbar.LENGTH_SHORT).show();
                writer.close();
                inputStream.close();
                reader.close();
                return urlConnection.getResponseCode();
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
        protected void onPostExecute(Integer responseCode) {
            super.onPostExecute(responseCode);
            if (taskListener != null)
                taskListener.onTaskFinished(responseCode);
        }

        PostTask setTaskListener(TaskListener taskListener) {
            this.taskListener = taskListener;
            return this;
        }

        public interface TaskListener {
            void onTaskFinished(Integer responseCode);
        }
    }

    private static class GetCount extends AsyncTask<String, Void, Integer> {

        private TaskListener taskListener;

        @Override
        protected Integer doInBackground(String... strings) {
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(URLS.ADMIN.GET.countURL);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestProperty("Accept", "application/json");
                urlConnection.setRequestProperty("Authorization", strings[0]);
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

    private static class GetPost extends AsyncTask<String, Integer, ArrayList<PostData>> {

        private int index;

        private TaskListener taskListener;

        GetPost(int index) {
            this.index = index;
        }

        @Override
        protected final ArrayList<PostData> doInBackground(String... strings) {

            ArrayList<PostData> postDataArrayList = new ArrayList<>();

            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(URLS.ADMIN.GET.postedURL + index);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestProperty("Accept", "application/json");
                urlConnection.setRequestProperty("Authorization", strings[0]);
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
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("M월 d일 h시 m분", Locale.KOREA);

                        Calendar calendarWrite = Calendar.getInstance();
                        calendarWrite.setTime(dateFormat.parse(dataObject.getString("writeDate")));

                        postDataArrayList.add(new PostData(dataObject.getInt("idx"), dataObject.getString("desc"), simpleDateFormat.format(calendarWrite.getTime()), ""));
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

        @Override
        protected void onPostExecute(ArrayList<PostData> postDataArrayList) {
            super.onPostExecute(postDataArrayList);
            if (taskListener != null) taskListener.onTaskFinished(postDataArrayList);
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
