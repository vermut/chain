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
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Created by admin on 19/04/15.
 */
public class SingleDeviceApplicationView extends Composite {
    private static SingleDeviceApplicationViewUiBinder ourUiBinder = GWT.create(SingleDeviceApplicationViewUiBinder.class);
    @UiField
    public Label teamLabel;
    @UiField
    public Label neighborsLabel;
    @UiField
    public Label otherLink;
    @UiField
    public Label yourLink;
    @UiField
    public Label updateId;
    @UiField
    public Label score;
    @UiField
    public VerticalPanel panel;
    @UiField
    public Label death;
    Geolocation geolocation = Geolocation.getIfSupported();
    public SingleDeviceApplicationView() {
        VerticalPanel rootElement = ourUiBinder.createAndBindUi(this);
        initWidget(rootElement);
        initGeo();
    }

    private void initGeo() {
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
                String url = "http://trac.wwc.lv:5055/?id=123456&lat=" + c.getLatitude() + "&lon=" + c.getLongitude()
                        + "&timestamp=" + result.getTimestamp()
                        + "&hdop=" + c.getHeading() + "&altitude=" + c.getAltitude() + "&speed=" + c.getSpeed();

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
        }, new Geolocation.PositionOptions().setHighAccuracyEnabled(true).setTimeout(60).setMaximumAge(60));
    }

    interface SingleDeviceApplicationViewUiBinder extends UiBinder<VerticalPanel, SingleDeviceApplicationView> {
    }
}