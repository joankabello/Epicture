package com.example.epinavbar.ui.upload;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;


import com.example.epinavbar.MainActivity;
import com.example.epinavbar.R;
import com.example.epinavbar.ui.dashboard.DashboardFragment;
import com.example.epinavbar.ui.dashboard.DashboardViewModel;
import com.example.epinavbar.ui.home.HomeAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class UploadFragment extends Fragment {

    private static final String TAG = DashboardFragment.class.getSimpleName();
    private String encodedImage;
    private OkHttpClient httpClient;
    private UploadViewModel uploadViewModel;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uploadViewModel = ViewModelProviders.of(this).get(UploadViewModel.class);
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_upload, container, false);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getImageFromGallery();
    }

    private void getImageFromGallery() {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto , 1);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_CANCELED) {
            switch (requestCode) {
                case 0:
                    if (resultCode == RESULT_OK) {
                        Bitmap selectedImage = (Bitmap) data.getExtras().get("data");
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        selectedImage.compress(Bitmap.CompressFormat.PNG, 100, baos);
                        byte[] b = baos.toByteArray();
                        encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
                        sendRequest();
                    }
                    break;
                case 1:
                    if (resultCode == RESULT_OK) {
                        Uri selectedImage = data.getData();
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        if (selectedImage != null) {
                            Cursor cursor = MainActivity.getContextOfApplication().getContentResolver().query(selectedImage, filePathColumn, null, null,null);
                            if (cursor != null) {
                                cursor.moveToFirst();
                                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                                String picturePath = cursor.getString(columnIndex);
                                Bitmap bm = BitmapFactory.decodeFile(picturePath);
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                bm.compress(Bitmap.CompressFormat.JPEG, 100, baos); //bm is the bitmap object
                                byte[] b = baos.toByteArray();
                                encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
                                sendRequest();
                                cursor.close();
                                Toast toast2 = Toast.makeText(getContext(),"Upload Completed", Toast.LENGTH_LONG);
                                toast2.setGravity(Gravity.CENTER, 0, 0);
                                toast2.show();
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    break;
            }
        }
    }

    private void sendRequest()
    {
        httpClient = new OkHttpClient.Builder().build();
        MediaType MEDIA_TYPE = MediaType.parse("application/json");

        JSONObject postdata = new JSONObject();
        try {
            postdata.put("image", encodedImage);
        } catch(JSONException e){
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(MEDIA_TYPE, postdata.toString());

        Request request = new Request.Builder()
                .url("https://api.imgur.com/3/upload")
                .header("Authorization", "Bearer" + " " + MainActivity.accessToken)
                .post(body)
                .build();
        httpClient.newCall(request).enqueue(new Callback()
        {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "An error occured " + e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                System.out.println(response.code());
            }
        });
    }
}