package gr.edu.todolizer;

import com.squareup.otto.Bus;

public class GlobalBus {
    private static Bus sBus;
    public static Bus getInstance() {
        if (sBus == null)
            sBus = new Bus();
        return sBus;
    }
}