package org.traccar.web.client.view;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.geolocation.client.Geolocation;
import com.google.gwt.geolocation.client.Position;
import com.google.gwt.geolocation.client.PositionError;
import com.google.gwt.http.client.*;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.HTML;
import org.traccar.web.client.ApplicationContext;

import java.util.Date;

/**
 * Created by admin on 19/04/15.
 */
public class SingleDeviceApplicationView extends Composite {
    private static SingleDeviceApplicationViewUiBinder ourUiBinder = GWT.create(SingleDeviceApplicationViewUiBinder.class);
    @UiField
    public HTML teamLabel;
    @UiField
    public HTML neighborsLabel;
    @UiField
    public HTML otherLink;
    @UiField
    public HTML yourLink;
    @UiField
    public HTML updateId;
    @UiField
    public HTML score;
    @UiField
    public FlowPanel panel;
    @UiField
    public HTML death;
    @UiField
    public Frame chat;

    Geolocation geolocation = Geolocation.getIfSupported();

    public SingleDeviceApplicationView() {
        FlowPanel rootElement = ourUiBinder.createAndBindUi(this);
        initWidget(rootElement);
        if (ApplicationContext.getInstance().getDevice() != null)
            initGeo(ApplicationContext.getInstance().getDevice().getUniqueId());
    }

    void initGeo(final String login) {
        if (geolocation == null) {
            GWT.log("[MapUtil] unsupported!");
            return;
        }

        geolocation.watchPosition(new Callback<Position, PositionError>() {
            @Override
            public void onFailure(PositionError reason) {
                GWT.log("[MapUtil] " + reason.getMessage());
            }

            @Override
            public void onSuccess(Position result) {
                Position.Coordinates c = result.getCoordinates();
                String url = "http://trac.wwc.lv:5056/?id=" + login + "&lat=" + c.getLatitude() + "&lon=" + c.getLongitude()
                        + "&timestamp=" + new Date().getTime() / 1000
                        + "&hdop=0&altitude=0&speed=0";
                // + "&hdop=" + c.getHeading() + "&altitude=" + c.getAltitude() + "&speed=" + c.getSpeed();

                RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);
                try {
                    builder.sendRequest(null, new RequestCallback() {
                        public void onError(Request request, Throwable exception) {
                            GWT.log("[MapUtil] cannot send GPS data");
                            updateId.setText(updateId.getText() + "-");
                        }

                        public void onResponseReceived(Request request, Response response) {
                            updateId.setText(updateId.getText() + "+");
                        }
                    });

                } catch (RequestException e) {
                    GWT.log("[MapUtil] cannot send GPS data2");
                    updateId.setText(updateId.getText() + "-");
                }
            }
        }, new Geolocation.PositionOptions().setHighAccuracyEnabled(true).setTimeout(60000).setMaximumAge(5000));
    }

    interface SingleDeviceApplicationViewUiBinder extends UiBinder<FlowPanel, SingleDeviceApplicationView> {
    }
}