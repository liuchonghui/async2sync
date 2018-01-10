# async2sync
compile 'tools.android:async2sync:1.0.0'
```
src
└── tools
    └── android
        └── async2sync
            ├── Connection.java
            ├── Packet.java
            ├── PacketCollector.java
            ├── PacketFilter.java
            └── RandomIdGenerator.java
```

假如有一个异步任务实现如下：
```
 void asyncTestFunction(final Payload payload, final PayloadResult callback) {
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
```
那么这个异步任务使用起来是这个样子：
```
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
```
那么async2sync就是用下面同步的写法实现一样的效果：
```
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
 // 同步拿到结果后，运行到UI线程刷新控件
 new Handler(Looper.getMainLooper()).post(new Runnable() {
    @Override
    public void run() {
       if (packet != null && packet.getContent() != null) {
           Payload resulPayload = packet.getContent();
           String msg = resulPayload.title;
           Toast.makeText(view.getContext(), msg, Toast.LENGTH_LONG).show();
           btn2ret.setText(msg);
       }
    }
 });
```
sendPacket里面干了这样的事：
```
 public void sendPacket(Payload payload) {
    asyncTestFunction(payload, new PayloadResult() {
        @Override
        public void onResult(Payload payload) {
            // 异步处理结果填充Packet中，通知Connection继续执行
            Packet<Payload> newPacket = new Packet<Payload>();
            newPacket.setContent(payload);
            mConnection.processPacket(newPacket);
        }
    });
 }
```
异步回调同步化在特殊情况下会解燃眉之急，也可以让旧方法变得更加灵活，最少，方法还是要会的
