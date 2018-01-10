package tool.imageloadercompact.activity;

import android.app.Activity;
import android.compact.impl.TaskCallback;
import android.compact.impl.TaskImpl;
import android.compact.impl.TaskPayload;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import tool.imageloadercompact.app2.R;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button btn1 = (Button) findViewById(R.id.btn1);
        final TextView btn1ret = (TextView) findViewById(R.id.btn1_ret);

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Task task = new Task();
                TaskPayload payload = new TaskPayload();
                payload.identify = "1";
                task.run(view.getContext(), payload, new TaskCallback() {
                    @Override
                    public void onResult(TaskPayload payload) {
                        btn1ret.setText(payload.identify + "|" + payload.state);
                        Log.d("PPP", "task|onResult|" + payload.identify + "|" + payload.state);
                    }
                });
            }
        });
    }

    class Task implements TaskImpl {
        @Override
        public void run(Context context, TaskPayload payload, TaskCallback callback) {
            if (context == null || payload == null || payload.identify == null) {
                return;
            }

            boolean success = false;
            // TODO
            payload.content = context.getPackageName();
            success = true;

            payload.state = success ? 1 : -1;

            if (callback != null) {
                callback.onResult(payload);
            }
        }
    }
}