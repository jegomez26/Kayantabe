package edu.sti.kayantabe.ui.sp_employees;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class EmployeesViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public EmployeesViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is slideshow fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}