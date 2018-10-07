/*
 * Copyright 2013 Anton Tananaev (anton.tananaev@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package lv.vermut.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.Style.SelectionMode;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.menu.Item;
import com.sencha.gxt.widget.core.client.menu.MenuItem;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;
import lv.vermut.Application;
import lv.vermut.ApplicationContext;
import lv.vermut.model.BaseAsyncCallback;
import lv.vermut.model.Device;
import lv.vermut.model.DeviceProperties;

import java.util.LinkedList;
import java.util.List;

public class DeviceView implements SelectionChangedEvent.SelectionChangedHandler<Device> {

    public static final double MANUAL_MOVE_STEP = 0.0002;
    private static DeviceViewUiBinder uiBinder = GWT.create(DeviceViewUiBinder.class);

    interface DeviceViewUiBinder extends UiBinder<Widget, DeviceView> {
    }

    public interface DeviceHandler {
        void onSelected(Device device);

        void onAdd();

        void onEdit(Device device);

        void onRemove(Device device);

        void onMove(Device device, double offsetX, double offsetY);
    }

    private DeviceHandler deviceHandler;

    @UiField
    ContentPanel contentPanel;

    public ContentPanel getView() {
        return contentPanel;
    }

    @UiField
    TextButton addButton;

    @UiField
    TextButton editButton;

    @UiField
    TextButton removeButton;

    @UiField(provided = true)
    ColumnModel<Device> columnModel;

    @UiField(provided = true)
    ListStore<Device> deviceStore;

    @UiField
    Grid<Device> grid;

    @UiField
    MenuItem settingsUsers;

    @UiField
    MenuItem settingsGlobal;

    @UiField
    TextButton leftButton;
    @UiField
    TextButton rightButton;
    @UiField
    TextButton upButton;
    @UiField
    TextButton downButton;

    public DeviceView(DeviceHandler deviceHandler, SettingsHandler settingsHandler, ListStore<Device> deviceStore) {
        this.deviceHandler = deviceHandler;
        this.settingsHandler = settingsHandler;
        this.deviceStore = deviceStore;

        DeviceProperties deviceProperties = GWT.create(DeviceProperties.class);

        List<ColumnConfig<Device, ?>> columnConfigList = new LinkedList<>();
        columnConfigList.add(new ColumnConfig<>(deviceProperties.name(), 0, "Name"));
        columnConfigList.add(new ColumnConfig<>(deviceProperties.uniqueId(), 0, "Unique Identifier"));
        columnModel = new ColumnModel<>(columnConfigList);

        uiBinder.createAndBindUi(this);

        grid.getSelectionModel().addSelectionChangedHandler(this);
        grid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        if (ApplicationContext.getInstance().getUser().getAdmin()) {
            settingsUsers.enable();
            settingsGlobal.enable();
        }
    }

    @Override
    public void onSelectionChanged(SelectionChangedEvent<Device> event) {
        editButton.setEnabled(!event.getSelection().isEmpty());
        removeButton.setEnabled(!event.getSelection().isEmpty());

        if (event.getSelection().isEmpty()) {
            deviceHandler.onSelected(null);
        } else {
            deviceHandler.onSelected(event.getSelection().get(0));
        }
    }

    @UiHandler("addButton")
    public void onAddClicked(SelectEvent event) {
        deviceHandler.onAdd();
    }

    @UiHandler("editButton")
    public void onEditClicked(SelectEvent event) {
        deviceHandler.onEdit(grid.getSelectionModel().getSelectedItem());
    }

    @UiHandler("removeButton")
    public void onRemoveClicked(SelectEvent event) {
        deviceHandler.onRemove(grid.getSelectionModel().getSelectedItem());
    }

    @UiHandler("logoutButton")
    public void onLogoutClicked(SelectEvent event) {
        Application.getDataService().logout(new BaseAsyncCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                Window.Location.reload();
            }
        });
    }

    @UiHandler("upButton")
    public void onUpClicked(SelectEvent event) {
        deviceHandler.onMove(grid.getSelectionModel().getSelectedItem(), MANUAL_MOVE_STEP, 0.0);
    }

    @UiHandler("downButton")
    public void onDownClicked(SelectEvent event) {
        deviceHandler.onMove(grid.getSelectionModel().getSelectedItem(), -MANUAL_MOVE_STEP, 0.0);
    }

    @UiHandler("leftButton")
    public void onLeftClicked(SelectEvent event) {
        deviceHandler.onMove(grid.getSelectionModel().getSelectedItem(), 0.0, -MANUAL_MOVE_STEP);
    }

    @UiHandler("rightButton")
    public void onRightClicked(SelectEvent event) {
        deviceHandler.onMove(grid.getSelectionModel().getSelectedItem(), 0.0, MANUAL_MOVE_STEP);
    }


    public void selectDevice(Device device) {
        grid.getSelectionModel().select(deviceStore.findModel(device), false);
    }

    public interface SettingsHandler {
        void onAccountSelected();

        void onPreferencesSelected();

        void onUsersSelected();

        void onApplicationSelected();
    }

    private SettingsHandler settingsHandler;

    @UiHandler("settingsAccount")
    public void onSettingsAccountSelected(SelectionEvent<Item> event) {
        settingsHandler.onAccountSelected();
    }

    @UiHandler("settingsPreferences")
    public void onSettingsPreferencesSelected(SelectionEvent<Item> event) {
        settingsHandler.onPreferencesSelected();
    }

    @UiHandler("settingsUsers")
    public void onSettingsUsersSelected(SelectionEvent<Item> event) {
        settingsHandler.onUsersSelected();
    }

    @UiHandler("settingsGlobal")
    public void onSettingsGlobalSelected(SelectionEvent<Item> event) {
        settingsHandler.onApplicationSelected();
    }

}
