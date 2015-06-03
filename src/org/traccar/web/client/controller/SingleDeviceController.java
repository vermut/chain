package org.traccar.web.client.controller;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.traccar.web.client.Application;
import org.traccar.web.client.view.SingleDeviceApplicationView;
import org.traccar.web.shared.model.DeviceReport;

/**
 * Created by admin on 19/04/15.
 */
public class SingleDeviceController {
    private static final int UPDATE_INTERVAL = 5000;
    private final SingleDeviceApplicationView deviceView;

    public SingleDeviceController(SingleDeviceApplicationView deviceView) {
        this.deviceView = deviceView;
    }

    private Timer updateTimer;
    private Long updateId = 0l;

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
                updateId++;
                deviceView.updateId.setText(updateId.toString());

                deviceView.teamLabel.setText(result.teamName);
                deviceView.score.setText(result.score);

                if (result.active)
                {
                    deviceView.death.setText("");
                    deviceView.panel.setStylePrimaryName("active");
                } else {
                    deviceView.panel.setStylePrimaryName("dead");
                    deviceView.death.setText("You are DEAD! Proceed of the limits to respawn.");
                }


                if (result.neighbors != null)
                    deviceView.neighborsLabel.setText(result.neighbors.toString());

                switch (result.ownLinkStatus) {
                    case DeviceReport.NO_LINK:
                        deviceView.yourLink.setText("You got no link");
                        break;
                    case DeviceReport.HAVE_LINK:
                        deviceView.yourLink.setText("You got link!");
                        break;
                    case DeviceReport.HAVE_LINK_WITH_YOU:
                        deviceView.yourLink.setText("You got link AND YOU ARE PART OF IT!");
                        break;
                }

                switch (result.otherTeamLinkStatus) {
                    case DeviceReport.NO_LINK:
                        deviceView.otherLink.setText(null);
                        break;
                    case DeviceReport.HAVE_LINK:
                        deviceView.otherLink.setText("Other team has link!");
                        break;
                    case DeviceReport.HAVE_LINK_AROUND_YOU:
                        deviceView.otherLink.setText("Other team has link AND YOU FEEL IT AROUND!");
                        break;
                }

                updateTimer.schedule(UPDATE_INTERVAL);
            }

            @Override
            public void onFailure(Throwable caught) {
                updateTimer.schedule(UPDATE_INTERVAL);
            }
        });
    }
}
