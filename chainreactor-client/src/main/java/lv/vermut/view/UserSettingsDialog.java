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
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell.TriggerAction;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.form.ComboBox;
import lv.vermut.model.EnumKeyProvider;
import lv.vermut.model.UserSettings;
import lv.vermut.model.UserSettingsProperties;

import java.util.Arrays;

public class UserSettingsDialog implements Editor<UserSettings> {

    private static UserSettingsDialogUiBinder uiBinder = GWT.create(UserSettingsDialogUiBinder.class);

    interface UserSettingsDialogUiBinder extends UiBinder<Widget, UserSettingsDialog> {
    }

    private UserSettingsDriver driver = GWT.create(UserSettingsDriver.class);

    interface UserSettingsDriver extends SimpleBeanEditorDriver<UserSettings, UserSettingsDialog> {
    }

    public interface UserSettingsHandler {
        void onSave(UserSettings userSettings);
    }

    private UserSettingsHandler userSettingsHandler;

    @UiField
    Window window;

    @UiField(provided = true)
    ComboBox<UserSettings.SpeedUnit> speedUnit;

    public UserSettingsDialog(UserSettings userSettings, UserSettingsHandler userSettingsHandler) {
        this.userSettingsHandler = userSettingsHandler;

        ListStore<UserSettings.SpeedUnit> speedUnitStore = new ListStore<>(
                new EnumKeyProvider<UserSettings.SpeedUnit>());
        speedUnitStore.addAll(Arrays.asList(UserSettings.SpeedUnit.values()));

        speedUnit = new ComboBox<>(
                speedUnitStore, new UserSettingsProperties.SpeedUnitLabelProvider());
        speedUnit.setForceSelection(true);
        speedUnit.setTriggerAction(TriggerAction.ALL);

        uiBinder.createAndBindUi(this);
        driver.initialize(this);
        driver.edit(userSettings);
    }

    public void show() {
        window.show();
    }

    public void hide() {
        window.hide();
    }

    @UiHandler("saveButton")
    public void onLoginClicked(SelectEvent event) {
        window.hide();
        userSettingsHandler.onSave(driver.flush());
    }

    @UiHandler("cancelButton")
    public void onRegisterClicked(SelectEvent event) {
        window.hide();
    }

}
