package com.example.smartmodeswitcher.mode;

import android.content.Context;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class WorkScheduler {

    private static final String UNIQUE_WORK_NAME = "ProfileEvaluation";

    public static void scheduleProfileEvaluation(Context context) {
        PeriodicWorkRequest workRequest =
                new PeriodicWorkRequest.Builder(ProfileEvaluationWorker.class, 15, TimeUnit.MINUTES)
                        .build();

        WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                        UNIQUE_WORK_NAME,
                        ExistingPeriodicWorkPolicy.REPLACE,
                        workRequest
                );
    }
}
