package com.example.smartmodeswitcher.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.smartmodeswitcher.data.Profile;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SharedPrefUtils {

    private static final String PREF_NAME = "smart_mode_profiles";
    private static final String KEY_PROFILES = "profiles";

    public static void saveProfiles(Context context, List<Profile> profiles) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        String json = new Gson().toJson(profiles);
        editor.putString(KEY_PROFILES, json);
        editor.apply();
    }

    public static List<Profile> loadProfiles(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_PROFILES, null);

        if (json == null) return new ArrayList<>();
        return new Gson().fromJson(json, new TypeToken<List<Profile>>() {}.getType());
    }
}