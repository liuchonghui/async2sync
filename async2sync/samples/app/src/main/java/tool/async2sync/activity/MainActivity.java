package tool.async2sync.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import tool.async2sync.app.R;
import tools.android.async2sync.Connection;
import tools.android.async2sync.Packet;
import tools.android.async2sync.PacketCollector;
import tools.android.async2sync.PacketFilter;
import tools.android.async2sync.RandomIdGenerator;

public class MainActivity extends Activity {

    Connection mConnection = new Connection(5000L); // 设定5秒无结果超时

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button btn1 = (Button) findViewById(R.id.btn1);
        final TextView btn1ret = (TextView) findViewById(R.id.btn1_ret);
        final Button btn2 = (Button) findViewById(R.id.btn2);
        final TextView btn2ret = (TextView) findViewById(R.id.btn2_ret);

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                final long start = System.currentTimeMillis();
                Payload payload = new Payload();
                payload.title = "start@" + start;
                asyncTestFunction(payload, new PayloadResult() {
                    @Override
                    public void onResult(final Payload payload) {
                        // 异步处理后，获得结果，在UI线程刷新控件
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                String msg = payload.title;
                                Toast.makeText(view.getContext(), msg, Toast.LENGTH_LONG).show();
                                btn1ret.setText(msg);
                            }
                        });
                    }
                });
            }
        });

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                final long start = System.currentTimeMillis();
                String packetId = RandomIdGenerator.randomId(3);
                Payload payload = new Payload();
                payload.identify = packetId;
                payload.title = "start@" + start;
                // sendPacket启动异步任务，然后线程一直等，一直等到nextResult获取结果（或超时）
                PacketFilter filter = new PacketIDFilter(payload.identify);
                PacketCollector collector = mConnection.createPacketCollector(filter);
                sendPacket(payload);
                final Packet<Payload> packet = collector.nextResult(mConnection.getConnectionTimeOut());
                collector.cancel();
                // 由于一直运行在主线程，这里直接刷新处理
                // 界面上按钮会卡住，这只是为了说明同步线程执行，方便演示，实际使用不建议
                if (packet != null && packet.getContent() != null) {
                    Payload resulPayload = packet.getContent();
                    String msg = resulPayload.title;
                    Toast.makeText(view.getContext(), msg, Toast.LENGTH_LONG).show();
                    btn2ret.setText(msg);
                }
            }
        });
    }

    public interface PayloadResult {
        void onResult(Payload payload);
    }

    public void asyncTestFunction(final Payload payload, final PayloadResult callback) {
        // 模拟一个异步任务，通过输入，异步处理，返回处理后的结果
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000L);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                payload.title += "|end@" + System.currentTimeMillis();
                if (callback != null) {
                    callback.onResult(payload);
                }
            }
        }).start();
    }

    public class Payload {
        public String identify;
        public String title;
    }

    class PacketIDFilter implements PacketFilter {

        String packetId;

        public PacketIDFilter(String packetId) {
            this.packetId = packetId;
        }

        @Override
        public boolean accept(Packet packet) {
            if (packet == null) {
                return false;
            }
            if (!(packet.getContent() instanceof Payload)) {
                return false;
            }
            Payload payloadContent = (Payload) packet.getContent();
            if (TextUtils.isEmpty(payloadContent.identify)) {
                return false;
            }
            return payloadContent.identify.equals(this.packetId);
        }
    }

    public void sendPacket(Payload payload) {
        asyncTestFunction(payload, new PayloadResult() {
            @Override
            public void onResult(Payload payload) {
                Packet<Payload> newPacket = new Packet<Payload>();
                newPacket.setContent(payload);
                mConnection.processPacket(newPacket);
            }
        });
    }
}