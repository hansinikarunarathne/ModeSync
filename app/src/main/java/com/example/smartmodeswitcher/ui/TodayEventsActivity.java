package com.example.smartmodeswitcher.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.smartmodeswitcher.R;
import com.example.smartmodeswitcher.utils.CalendarHelper;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TodayEventsActivity extends AppCompatActivity {
    private RecyclerView rvMeetings;
    private TextView tvNoMeetings;
    private TextView tvDateHeader;
    private CalendarHelper calendarHelper;
    private MeetingAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_today_events);

        // Set up the toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Today's Events");

        // Initialize views
        rvMeetings = findViewById(R.id.rvMeetings);
        tvNoMeetings = findViewById(R.id.tvNoMeetings);
        tvDateHeader = findViewById(R.id.tvDateHeader);
        
        // Set up RecyclerView
        rvMeetings.setLayoutManager(new LinearLayoutManager(this));
        
        // Set today's date in the header
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM d", Locale.getDefault());
        tvDateHeader.setText(dateFormat.format(new Date()));
        
        calendarHelper = new CalendarHelper(this);
        updateEventsList();
    }

    private void updateEventsList() {
        List<CalendarHelper.CalendarEvent> todayEvents = calendarHelper.getTodayEvents();
        
        if (!todayEvents.isEmpty()) {
            adapter = new MeetingAdapter(todayEvents);
            rvMeetings.setAdapter(adapter);
            rvMeetings.setVisibility(View.VISIBLE);
            tvNoMeetings.setVisibility(View.GONE);
        } else {
            rvMeetings.setVisibility(View.GONE);
            tvNoMeetings.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
} 