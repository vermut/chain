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
package org.traccar.web.client.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.googlecode.mgwt.dom.client.event.tap.TapEvent;
import com.googlecode.mgwt.ui.client.MGWT;
import com.googlecode.mgwt.ui.client.MGWTSettings;
import com.googlecode.mgwt.ui.client.widget.button.Button;
import com.googlecode.mgwt.ui.client.widget.dialog.Dialog;
import com.googlecode.mgwt.ui.client.widget.dialog.overlay.PopinDialogOverlay;
import com.googlecode.mgwt.ui.client.widget.input.MPasswordTextBox;
import com.googlecode.mgwt.ui.client.widget.input.MTextBox;
import org.traccar.web.client.ApplicationContext;

public class LoginDialog {

    private static LoginDialogUiBinder uiBinder = GWT.create(LoginDialogUiBinder.class);
    @UiField
    PopinDialogOverlay window;
    @UiField
    MTextBox login;
    @UiField
    MPasswordTextBox password;
    @UiField
    Button registerButton;

    private LoginHandler loginHandler;

    public LoginDialog(LoginHandler loginHandler) {
        MGWTSettings.ViewPort viewPort = new MGWTSettings.ViewPort();
        viewPort.setUserScaleAble(false).setMinimumScale(1.0).setMinimumScale(1.0).setMaximumScale(1.0);

        MGWTSettings settings = new MGWTSettings();
        settings.setViewPort(viewPort);
        settings.setFullscreen(true);
        settings.setPreventScrolling(true);
        //settings.setIconUrl("logo.png");
        //settings.setFixIOS71BodyBug(true);

        if (MGWT.getFormFactor().isPhone())
            MGWT.applySettings(settings);

        this.loginHandler = loginHandler;
        uiBinder.createAndBindUi(this);

        if (ApplicationContext.getInstance().getApplicationSettings().getRegistrationEnabled()) {
            registerButton.setDisabled(false);
        }
    }

    public void show() {
        window.show();
    }

    public void hide() {
        window.hide();
    }

    @UiHandler("loginButton")
    public void onLoginClicked(TapEvent event) {
        loginHandler.onLogin(login.getValue(), password.getValue());
    }

    @UiHandler("deviceLoginButton")
    public void onDeviceLoginClicked(TapEvent event) {
        loginHandler.onDeviceLogin(login.getValue(), password.getValue());
    }

    @UiHandler("registerButton")
    public void onRegisterClicked(TapEvent event) {
        loginHandler.onRegister(login.getValue(), password.getValue());
    }

    interface LoginDialogUiBinder extends UiBinder<Dialog, LoginDialog> {
    }

    public interface LoginHandler {
        void onLogin(String login, String password);

        void onDeviceLogin(String login, String password);

        void onRegister(String login, String password);
    }

}
