package org.traccar.web.client.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
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

    public SingleDeviceApplicationView() {
        VerticalPanel rootElement = ourUiBinder.createAndBindUi(this);
        initWidget(rootElement);
    }
}