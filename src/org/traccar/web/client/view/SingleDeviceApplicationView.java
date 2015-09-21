package org.traccar.web.client.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Created by admin on 19/04/15.
 */
public class SingleDeviceApplicationView extends Composite {
    interface SingleDeviceApplicationViewUiBinder extends UiBinder<VerticalPanel, SingleDeviceApplicationView> {
    }

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

    public SingleDeviceApplicationView() {
        VerticalPanel rootElement = ourUiBinder.createAndBindUi(this);
        initWidget(rootElement);
        initJavascript();
    }

    private native void initJavascript() /*-{
        console.log("[MapUtil] loaded.");

        // Get Position
        var registerPosition = function (enableHighAccuracyInput, timeout, maximumAge) {
            console.log("[MapUtil] enableHighAccuracyInput: ", enableHighAccuracyInput);
            console.log("[MapUtil] timeout: ", timeout);
            console.log("[MapUtil] maximumAge: ", maximumAge);

            options = {
                enableHighAccuracy: enableHighAccuracyInput || DefaultConfig.defaultEnableHighAccuracy,
                timeout: parseInt(timeout, 10) || DefaultConfig.defaultTimeout,
                maximumAge: parseInt(maximumAge, 10) || DefaultConfig.defaultMaximumAge
            };
            console.log("[MapUtil] options: ", options);

            navigator.geolocation.watchPosition(
                function (position) {
                    // On Success
                    console.log(position.coords.latitude + ":" + position.coords.longitude);
                },
                function (error) {
                    var msg = error.message;
                    console.log(msg);
                },
                options
            );
        }
        registerPosition(true, 60, 60)
    }-*/;

}