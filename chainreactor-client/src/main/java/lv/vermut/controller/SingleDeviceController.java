package lv.vermut.controller;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import lv.vermut.Application;
import lv.vermut.model.DeviceReport;
import lv.vermut.view.SingleDeviceApplicationView;

/**
 * Created by admin on 19/04/15.
 */
public class SingleDeviceController {
    private static final int UPDATE_INTERVAL = 5000;
    private final SingleDeviceApplicationView deviceView;
    private Timer updateTimer;
    private Long updateId = 0L;

    public SingleDeviceController(SingleDeviceApplicationView deviceView) {
        this.deviceView = deviceView;
    }

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

                if (!deviceView.chat.getUrl().equalsIgnoreCase(result.teamConferenceUrl))
                    deviceView.chat.setUrl(result.teamConferenceUrl);

                if (result.active) {
                    deviceView.death.setText("");
                    deviceView.deathPanel.widgetContainer.removeStyleName("dead");
                } else {
                    deviceView.deathPanel.widgetContainer.addStyleName("dead");
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
