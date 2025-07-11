package com.example.smartmodeswitcher.models;

public class ContextResult {

    private final String motionContext;
    private final String stableContext;

    public ContextResult(String stableContext,String motionContext ) {
        this.motionContext = motionContext;
        this.stableContext = stableContext;
    }

    public String getMotionContext() {
        return motionContext;
    }

    public String getStableContext() {
        return stableContext;
    }
}
