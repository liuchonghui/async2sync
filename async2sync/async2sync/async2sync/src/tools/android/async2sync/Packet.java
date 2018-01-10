package tools.android.async2sync;

public class Packet<T> {
    PacketCollector pc;
    T content;

    public Packet() {
        super();
    }

    public Packet(PacketCollector pc, T content) {
        this();
        this.pc = pc;
        this.content = content;
    }

    public void setContent(T t) {
        this.content = t;
    }

    public T getContent() {
        return this.content;
    }

    public void onReady() {
        pc.processPacket(this);
    }
}
