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
package lv.vermut.controller;

import java.util.List;

import lv.vermut.Application;
import lv.vermut.ApplicationContext;
import lv.vermut.model.BaseAsyncCallback;
import lv.vermut.model.UserProperties;
import lv.vermut.view.ApplicationSettingsDialog;
import lv.vermut.view.DeviceView;
import lv.vermut.view.UserDialog;
import lv.vermut.view.UserSettingsDialog;
import lv.vermut.view.UsersDialog;
import lv.vermut.model.ApplicationSettings;
import lv.vermut.model.User;
import lv.vermut.model.UserSettings;

import com.google.gwt.core.client.GWT;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.Dialog.PredefinedButton;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import com.sencha.gxt.widget.core.client.box.ConfirmMessageBox;
import com.sencha.gxt.widget.core.client.event.DialogHideEvent;

public class SettingsController implements DeviceView.SettingsHandler {

    @Override
    public void onAccountSelected() {
        new UserDialog(
                ApplicationContext.getInstance().getUser(),
                new UserDialog.UserHandler() {
                    @Override
                    public void onSave(User user) {
                        Application.getDataService().updateUser(user, new BaseAsyncCallback<User>() {
                            @Override
                            public void onSuccess(User result) {
                                ApplicationContext.getInstance().setUser(result);
                            }
                        });
                    }
                }).show();
    }

    @Override
    public void onPreferencesSelected() {
        new UserSettingsDialog(
                ApplicationContext.getInstance().getUserSettings(),
                new UserSettingsDialog.UserSettingsHandler() {
                    @Override
                    public void onSave(UserSettings userSettings) {
                        ApplicationContext.getInstance().setUserSettings(userSettings);
                        User user = ApplicationContext.getInstance().getUser();
                        Application.getDataService().updateUser(user, new BaseAsyncCallback<User>() {
                            @Override
                            public void onSuccess(User result) {
                                ApplicationContext.getInstance().setUser(result);
                            }
                        });
                    }
                }).show();
    }

    @Override
    public void onUsersSelected() {
        Application.getDataService().getUsers(new BaseAsyncCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> result) {
                UserProperties userProperties = GWT.create(UserProperties.class);
                final ListStore<User> userStore = new ListStore<>(userProperties.id());
                userStore.addAll(result);

                new UsersDialog(userStore, new UsersDialog.UserHandler() {

                    @Override
                    public void onAdd() {
                        new UserDialog(
                                new User(),
                                new UserDialog.UserHandler() {
                                    @Override
                                    public void onSave(User user) {
                                        Application.getDataService().addUser(user, new BaseAsyncCallback<User>() {
                                            @Override
                                            public void onSuccess(User result) {
                                                userStore.add(result);
                                            }
                                            @Override
                                            public void onFailure(Throwable caught) {
                                                new AlertMessageBox("Error", "Username is already taken").show();
                                            }
                                        });
                                    }
                                }).show();
                    }

                    @Override
                    public void onRemove(final User user) {
                        final ConfirmMessageBox dialog = new ConfirmMessageBox("Confirm", "Are you sure you want remove user?");
                        dialog.addDialogHideHandler(new DialogHideEvent.DialogHideHandler() {
							@Override
							public void onDialogHide(DialogHideEvent event) {
								if (event.getHideButton() == PredefinedButton.YES) {
                                    Application.getDataService().removeUser(user, new BaseAsyncCallback<User>() {
                                        @Override
                                        public void onSuccess(User result) {
                                            userStore.remove(user);
                                        }
                                    });
								}
							}
						});
                        dialog.show();
                    }

                }).show();
            }
        });
    }

    @Override
    public void onApplicationSelected() {
        new ApplicationSettingsDialog(
                ApplicationContext.getInstance().getApplicationSettings(),
                new ApplicationSettingsDialog.ApplicationSettingsHandler() {
                    @Override
                    public void onSave(ApplicationSettings applicationSettings) {
                        Application.getDataService().updateApplicationSettings(applicationSettings, new BaseAsyncCallback<ApplicationSettings>() {
                            @Override
                            public void onSuccess(ApplicationSettings result) {
                                ApplicationContext.getInstance().setApplicationSettings(result);
                            }
                        });
                    }
                }).show();
    }

}
