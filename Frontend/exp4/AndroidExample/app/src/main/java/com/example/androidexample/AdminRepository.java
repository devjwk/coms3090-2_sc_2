package com.example.androidexample;

import android.content.Context;

public class AdminRepository {

    public interface ActionCallback {
        void onSuccess();
        void onError(String error);
    }

    public void getPendingModerators(Context context) {
        // TODO: Connect to backend when pending moderator endpoint is ready.
    }

    public void approveModerator(Context context, int moderatorId, ActionCallback callback) {
        // TODO: Connect to backend approve endpoint.
    }

    public void rejectModerator(Context context, int moderatorId, ActionCallback callback) {
        // TODO: Connect to backend reject endpoint.
    }
}