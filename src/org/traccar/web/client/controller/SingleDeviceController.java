package org.traccar.web.client.controller;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sencha.gxt.widget.core.client.ContentPanel;
import org.traccar.web.client.Application;
import org.traccar.web.client.view.MapView;
import org.traccar.web.client.view.SingleDeviceApplicationView;
import org.traccar.web.shared.model.DeviceReport;
import org.traccar.web.shared.model.Position;

import java.util.List;
import java.util.Map;

/**
 * Created by admin on 19/04/15.
 */
public class SingleDeviceController  {
    private static final int UPDATE_INTERVAL = 5000;
    private final SingleDeviceApplicationView deviceView;

    public SingleDeviceController(SingleDeviceApplicationView deviceView) {
        this.deviceView = deviceView;
    }

    private Timer updateTimer;

    public void run() {
        updateTimer = new Timer() {
            @Override
            public void run() {
                update();
            }
        };
        update();
    }

    public void update() {
        updateTimer.cancel();
        Application.getDataService().getDeviceReport(new AsyncCallback<DeviceReport>() {
            @Override
            public void onSuccess(DeviceReport result) {
                deviceView.teamLabel.setText(result.teamName);
                updateTimer.schedule(UPDATE_INTERVAL);
            }

            @Override
            public void onFailure(Throwable caught) {
                updateTimer.schedule(UPDATE_INTERVAL);
            }
        });
    }
}
