// From
// https://github.com/Argarian-Network/NachoSpigot/tree/async-kb-hit
package cc.kajus.amy.async.thread;

public class CombatThread extends AsyncPacketThread {
    public CombatThread(String s) {
        super(s);
    }

    // Handle packets
    @Override
    public void run() {
        while (this.packets.size() > 0) {
            this.packets.poll().run();
        }
    }
} 
