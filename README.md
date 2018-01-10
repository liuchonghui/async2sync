# async2sync

假如有一个异步任务实现如下：
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
