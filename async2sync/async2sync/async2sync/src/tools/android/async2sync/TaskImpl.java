package android.compact.impl;

import android.content.Context;

public interface TaskImpl {
    void run(Context context, TaskPayload payload, TaskCallback callback);
}
