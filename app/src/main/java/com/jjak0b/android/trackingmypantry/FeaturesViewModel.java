package com.jjak0b.android.trackingmypantry;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.jjak0b.android.trackingmypantry.data.api.Resource;
import com.jjak0b.android.trackingmypantry.data.auth.LoggedAccount;
import com.jjak0b.android.trackingmypantry.data.repositories.ExpirationEventsRepository;

public class FeaturesViewModel extends AndroidViewModel {
    private ExpirationEventsRepository eventsRepository;

    public FeaturesViewModel(@NonNull Application application) {
        super(application);
        eventsRepository = ExpirationEventsRepository.getInstance(application);
    }

    public LiveData<Resource<LoggedAccount>> enableFeatureExpirationReminders(boolean enable) {
        return eventsRepository.enableFeatureExpirationReminders(enable);
    }
}
