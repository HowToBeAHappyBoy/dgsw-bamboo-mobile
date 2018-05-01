package com.dgsw.bamboo.fragment;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatButton;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.dgsw.bamboo.R;
import com.dgsw.bamboo.activity.MainActivity;
import com.dgsw.bamboo.data.URLS;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;

public class PostFragment extends Fragment implements View.OnClickListener {

    TextInputLayout textInputLayout;
    TextInputEditText textInputEditText;

    public static PostFragment newInstance() {
        return new PostFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post, container, false);
        textInputLayout = view.findViewById(R.id.textInputLayout);
        textInputEditText = view.findViewById(R.id.editText);
        textInputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (textInputLayout.isErrorEnabled()) {
                    textInputLayout.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
        AppCompatButton sendButton = view.findViewById(R.id.sendButton);
        sendButton.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View view) {
        if (textInputEditText.getText() != null)
            if (textInputEditText.getText().toString().isEmpty()) {
                textInputLayout.setError(getString(R.string.empty_error));
                if (getActivity() != null) {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null)
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                Snackbar.make(view, getString(R.string.empty_error), Snackbar.LENGTH_SHORT).show();
                return;
            }
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("desc", textInputEditText.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        new PostTask().setTaskListener(responseCode -> {
            if (getActivity() != null)
                switch (responseCode) {
                    case HttpURLConnection.HTTP_CREATED:
                        textInputEditText.getText().clear();
                        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (imm != null)
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        new AlertDialog.Builder(getActivity())
                                .setMessage(R.string.post_ok)
                                .setCancelable(false)
                                .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
                                    getActivity().setTitle(R.string.app_name);
                                    ((MainActivity) getActivity()).toPostViewFragment();
                                })
                                .show();
                        break;
                    case HttpURLConnection.HTTP_INTERNAL_ERROR:
                        textInputLayout.setError(getActivity().getString(R.string.post_error));
                        Snackbar.make(view, getActivity().getString(R.string.unknown_error), Snackbar.LENGTH_SHORT).show();
                        break;
                }
        }).execute(jsonObject);
    }

    private static class PostTask extends AsyncTask<JSONObject, Integer, Integer> {

        private TaskListener taskListener;


        @Override
        protected Integer doInBackground(JSONObject... jsonObjects) {
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(URLS.USER.POST.postURL);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);

                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestProperty("Accept", "application/json");

                Writer writer = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream(), "UTF-8"));
                writer.write(jsonObjects[0].toString());

                writer.close();
                InputStream inputStream;
                try {
                    inputStream = urlConnection.getInputStream();
                } catch (IOException e) {
                    return urlConnection.getResponseCode();
                }

                StringBuilder stringBuilder = new StringBuilder();
                if (inputStream == null) {
                    return urlConnection.getResponseCode();
                }
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                String inputLine;
                while ((inputLine = reader.readLine()) != null)
                    stringBuilder.append(inputLine).append("\n");
                if (stringBuilder.length() == 0) {
                    return urlConnection.getResponseCode();
                }
                reader.close();
                return urlConnection.getResponseCode();
            } catch (IOException e) {
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
            if (taskListener != null) taskListener.onTaskFinished(responseCode);
        }

        PostTask setTaskListener(TaskListener taskListener) {
            this.taskListener = taskListener;
            return this;
        }

        public interface TaskListener {
            void onTaskFinished(Integer responseCode);
        }
    }
}
