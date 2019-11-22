package com.example.epinavbar.ui.home;

import com.example.epinavbar.MainActivity;
import com.example.epinavbar.R;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public HomeViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue(".");
    }

    public LiveData<String> getText() {
        return mText;
    }
}