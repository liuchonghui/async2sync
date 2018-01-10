package tools.android.async2sync;

import java.util.concurrent.ConcurrentLinkedQueue;

public class Connection {
    protected final ConcurrentLinkedQueue<PacketCollector> collectors = new ConcurrentLinkedQueue<PacketCollector>();

    private long connectionTimeout = 5000L;

    public Connection(long timeout) {
        if (timeout < 5000L) {
            timeout = 5000L;
        }
        this.connectionTimeout = timeout;
    }

    public PacketCollector createPacketCollector(PacketFilter packetFilter) {
        PacketCollector collector = new PacketCollector(this, packetFilter);
        // Add the collector to the list of active collectors.
        collectors.add(collector);
        return collector;
    }

    public void removePacketCollector(PacketCollector collector) {
        try {
            collectors.remove(collector);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public void processPacket(Packet packet) {
        if (packet == null) {
            return;
        }
        for (PacketCollector collector : collectors) {
            collector.processPacket(packet);
        }
    }

    public long getConnectionTimeOut() {
        return this.connectionTimeout;
    }
}