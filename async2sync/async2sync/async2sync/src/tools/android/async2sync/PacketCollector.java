package tools.android.async2sync;

import java.util.LinkedList;

public class PacketCollector {

    private static final int MAX_PACKETS = 65536;

    private LinkedList<Packet> resultQueue;

    private Connection connection;

    private PacketFilter packetFilter;

    public PacketCollector() {
        this.resultQueue = new LinkedList<Packet>();
    }

    public PacketCollector(Connection connection, PacketFilter packetFilter) {
        this.connection = connection;
        this.packetFilter = packetFilter;
        this.resultQueue = new LinkedList<Packet>();
    }

    PacketFilter getPacketFilter() {
        return this.packetFilter;
    }

    public synchronized Packet pollResult() {
        if (resultQueue.isEmpty()) {
            return null;
        } else {
            return resultQueue.removeLast();
        }
    }

    public synchronized Packet nextResult() {
        while (resultQueue.isEmpty()) {
            try {
                wait();
            } catch (Exception ie) {
            }
        }
        return resultQueue.removeLast();
    }

    protected synchronized void processPacket(Packet packet) {
        if (packet == null) {
            return;
        }
        if (packetFilter == null || packetFilter.accept(packet)) {
            if (resultQueue.size() == MAX_PACKETS) {
                resultQueue.removeLast();
            }
            resultQueue.addFirst(packet);
            notifyAll();
        }
    }

    public synchronized Packet nextResult(long timeout) {
        // Wait up to the specified amount of time for a result
        if (resultQueue.isEmpty()) {
            long waitTime = timeout;
            long start = System.currentTimeMillis();
            try {
                // keep wait until the specified amount of time has elapsed, or
                // a packet is available to return.
                while (resultQueue.isEmpty()) {
                    if (waitTime <= 0) {
                        break;
                    }
                    wait(waitTime);
                    long now = System.currentTimeMillis();
                    waitTime -= (now - start);
                    start = now;
                }
            } catch (Exception e) {
            }
            // Still haven't found a result, so return null.
            if (resultQueue.isEmpty()) {
                return null;
            }
            // Return the packet that was found.
            else {
                return resultQueue.removeLast();
            }
        }
        // There's already a packet waiting, so return it.
        else {
            return resultQueue.removeLast();
        }
    }

    private boolean cancelled = false;

    public void cancel() {
        if (!cancelled && connection != null) {
            cancelled = true;
            connection.removePacketCollector(this);
        }
    }
}