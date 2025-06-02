package com.example.smartmodeswitcher.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.smartmodeswitcher.R;
import com.example.smartmodeswitcher.utils.CalendarHelper;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MeetingAdapter extends RecyclerView.Adapter<MeetingAdapter.MeetingViewHolder> {
    private List<CalendarHelper.CalendarEvent> meetings;
    private SimpleDateFormat timeFormat;

    public MeetingAdapter(List<CalendarHelper.CalendarEvent> meetings) {
        this.meetings = meetings;
        this.timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public MeetingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_meeting, parent, false);
        return new MeetingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MeetingViewHolder holder, int position) {
        CalendarHelper.CalendarEvent meeting = meetings.get(position);
        
        holder.tvMeetingTitle.setText(meeting.title);
        holder.tvMeetingTime.setText(String.format("%s - %s",
                timeFormat.format(new Date(meeting.startTime)),
                timeFormat.format(new Date(meeting.endTime))));
        holder.tvCalendarName.setText(meeting.calendarName);

        // Set meeting type icon based on meeting properties
        if (isVirtualMeeting(meeting)) {
            holder.ivMeetingType.setImageResource(R.drawable.ic_video_call);
        } else if (isImportantMeeting(meeting)) {
            holder.ivMeetingType.setImageResource(R.drawable.ic_priority_high);
        } else {
            holder.ivMeetingType.setImageResource(R.drawable.ic_event);
        }
    }

    @Override
    public int getItemCount() {
        return meetings.size();
    }

    private boolean isVirtualMeeting(CalendarHelper.CalendarEvent event) {
        return event.description != null && (
            event.description.contains("meet.google.com") ||
            event.description.contains("zoom.us") ||
            event.description.contains("teams.microsoft.com") ||
            event.description.contains("webex.com")
        );
    }

    private boolean isImportantMeeting(CalendarHelper.CalendarEvent event) {
        return event.description != null && (
            event.description.contains("important") ||
            event.description.contains("urgent") ||
            event.description.contains("priority")
        );
    }

    static class MeetingViewHolder extends RecyclerView.ViewHolder {
        TextView tvMeetingTitle;
        TextView tvMeetingTime;
        TextView tvCalendarName;
        ImageView ivMeetingType;

        MeetingViewHolder(View itemView) {
            super(itemView);
            tvMeetingTitle = itemView.findViewById(R.id.tvMeetingTitle);
            tvMeetingTime = itemView.findViewById(R.id.tvMeetingTime);
            tvCalendarName = itemView.findViewById(R.id.tvCalendarName);
            ivMeetingType = itemView.findViewById(R.id.ivMeetingType);
        }
    }
} 