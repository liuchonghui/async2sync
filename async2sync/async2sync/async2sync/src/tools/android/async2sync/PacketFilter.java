package tools.android.async2sync;

import java.io.Serializable;

public interface PacketFilter extends Serializable {
    boolean accept(Packet packet);
}
