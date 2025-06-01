package com.example.smartmodeswitcher.mode;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.smartmodeswitcher.data.Profile;
import com.example.smartmodeswitcher.utils.ProfileEvaluator;
import com.example.smartmodeswitcher.modeswitcher.ModeSwitcher;

public class ProfileEvaluationWorker extends Worker {

    public ProfileEvaluationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Profile activeProfile = ProfileEvaluator.getActiveProfile(getApplicationContext());

        if (activeProfile != null) {
            ModeSwitcher.switchMode(getApplicationContext(), activeProfile.getMode());
        }

        return Result.success();
    }
}
