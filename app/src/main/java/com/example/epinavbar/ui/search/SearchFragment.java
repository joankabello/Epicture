package com.example.epinavbar.ui.search;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.graphics.Rect;
import android.util.Log;
import com.example.epinavbar.ui.dashboard.model.Photo;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


import com.example.epinavbar.R;

public class SearchFragment extends Fragment {
    private static String userInput;
    private static final String TAG = SearchFragment.class.getSimpleName();
    private OkHttpClient httpClient;

    private SearchViewModel searchViewModel;
    private TextView textView;
    private SearchAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        searchViewModel = ViewModelProviders.of(this).get(SearchViewModel.class);
        setHasOptionsMenu(true);//Make sure you have this line of code.

    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_search, container, false);
        return root;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getText(view);
        System.out.println(userInput);
        setUpView(view);
        observeDataChange();
    }


    private void setUpView(View view) {
        textView = view.findViewById(R.id.text_search);
    }

    private void observeDataChange() {
        searchViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
    }

    private void setAdapter(View view) {
        adapter = new SearchAdapter(getContext(), new ArrayList<Photo>());
        RecyclerView recyclerView = view.findViewById(R.id.rv_of_photos);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.bottom = 30;
            }
        });
    }

    public void getText(final View view)
    {

        final SearchView sv = view.findViewById(R.id.searchView);
        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(query.length() >= 1){
                    userInput = query;
                    fetchData();

                    setAdapter(view);
                    sv.clearFocus();

                    view.requestFocus();
//                    view.clearAnimation();
                    return true;
                }
                setUpView(view);
                observeDataChange();
                setAdapter(view);

                sv.clearFocus();
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                if(newText.length() >= 1){
                    userInput = newText;
                    fetchData();

                    setAdapter(view);
                    //sv.clearFocus();

                    view.requestFocus();
//                    view.clearAnimation();
                    return true;
                }
                return false;
            }

        });

    }
    private void fetchData() {
        httpClient = new OkHttpClient.Builder().build();

        Request request = new Request.Builder()
                .url("https://api.imgur.com/3/gallery/search/?q=" + userInput)
                .header("Authorization", "Client-ID 4ec810250ea92fb")
                .header("User-Agent", "Epicture")
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "An error has occurred " + e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    JSONObject data = new JSONObject(response.body().string());
                    JSONArray items = data.getJSONArray("data");
                    final List<Photo> photos = new ArrayList<>();

                    for (int i = 0; i < items.length(); i++) {
                        JSONObject item = items.getJSONObject(i);
                        Photo photo = new Photo();
                        if (item.getBoolean("is_album")) {
                            photo.setId(item.getString("cover"));
                        } else {
                            photo.setId(item.getString("id"));
                        }

                        photo.setViews(item.getString("views"));
                        photo.setUpvote(item.getString("ups"));
                        photo.setDownvote(item.getString("downs"));
                        photo.setTitle(item.getString("title"));
                        photos.add(photo);
                    }
                    new Handler(Looper.getMainLooper()).post(
                            new Runnable() {
                                @Override
                                public void run() {
                                    updateDataItems(photos);
                                }
                            }
                    );

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        });

    }
    private void updateDataItems(List<Photo> list) {
        if (adapter != null) {
            adapter.updatePhotosList(list);
        }
    }

}