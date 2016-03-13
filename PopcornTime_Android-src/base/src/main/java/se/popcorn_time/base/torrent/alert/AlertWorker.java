package se.popcorn_time.base.torrent.alert;

import com.frostwire.jlibtorrent.swig.alert;
import com.frostwire.jlibtorrent.swig.alert_ptr_deque;
import com.frostwire.jlibtorrent.swig.libtorrent;
import com.frostwire.jlibtorrent.swig.session;
import com.frostwire.jlibtorrent.swig.time_duration;

import se.popcorn_time.base.utils.Logger;

public class AlertWorker extends Thread {

    private session mSession;

    public AlertWorker(session session) {
        mSession = session;
    }

    @Override
    public void run() {
        boolean running = true;
        time_duration waitDuration = libtorrent.milliseconds(500);
        alert_ptr_deque alerts = new alert_ptr_deque();
        while (running) {
            alert ptr = mSession.wait_for_alert(waitDuration);
            if (isInterrupted()) {
                running = false;
                continue;
            }
            if (ptr != null) {
                mSession.pop_alerts(alerts);
                for (int i = 0; i < alerts.size(); i++) {
                    alert _alert = alerts.getitem(i);
                    Logger.debug(_alert.category() + "(" + _alert.type() + "): " + _alert.message());
                }
                alerts.clear();
            }
        }
    }
}