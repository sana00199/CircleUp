package com.sana.circleup;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel {
    private MutableLiveData<String> selectedData = new MutableLiveData<>();

    public LiveData<String> getSelectedData() {
        return selectedData;
    }

    public void setSelectedData(String data) {
        selectedData.setValue(data);
    }
}
