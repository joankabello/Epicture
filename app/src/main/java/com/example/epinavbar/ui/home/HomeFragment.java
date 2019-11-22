package com.example.epinavbar.ui.home;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.epinavbar.Routes;
import com.example.epinavbar.ui.dashboard.DashboardFragment;
import com.example.epinavbar.ui.dashboard.model.Photo;

import com.example.epinavbar.MainActivity;
import com.example.epinavbar.R;
import com.example.epinavbar.ui.upload.UploadViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HomeFragment extends Fragment {
    private static final String TAG = HomeAdapter.class.getSimpleName();
    private OkHttpClient httpClient;

    private HomeViewModel homeViewModel;
    private TextView textView;
    private HomeAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        homeViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        Button logOut = (Button) root.findViewById(R.id.logOut);
        logOut.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                MainActivity.username = null;
                MainActivity.image = null;
                MainActivity.refreshToken = null;
                MainActivity.accessToken = null;
                MainActivity.contextOfApplication = null;
                getActivity().finish();
            }
        });
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fetchData();
        setUserInformation(view);
        setUpView(view);
        observeDataChange();
        setAdapter(view);
    }


    private void setUpView(View view) {
        textView = view.findViewById(R.id.text_home);
    }

    private void observeDataChange() {
        homeViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
    }

    private void setAdapter(View view) {
        adapter = new HomeAdapter(getContext(), new ArrayList<Photo>());
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

    private void fetchData() {
        httpClient = new OkHttpClient.Builder().build();

        System.out.println("---------------------FECTH DATA--------------------------");
        System.out.println(MainActivity.accessToken);

        Request request = new Request.Builder()
                .url("https://api.imgur.com/3/account/" + MainActivity.username + "/images/")
                .header("Authorization", "Bearer" + " " + MainActivity.accessToken)
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
                        if (item.getBoolean("is_ad")) {
                            photo.setId(item.getString("cover"));
                        } else {
                            photo.setId(item.getString("id"));
                        }
                        photos.add(photo);
                        photo.setViews(item.getString("views"));
                        photo.setUpvote(item.getString("ad_type"));
                        photo.setDownvote(item.getString("ad_type"));
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
    private void setUserInformation(View view)
    {
        TextView UserName = view.findViewById(R.id.UserName);
        UserName.setText(MainActivity.username);

        ImageView avatar = view.findViewById(R.id.UserAvatar);
        try {
            new GetImageFromUrl(avatar).execute(MainActivity.image);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}