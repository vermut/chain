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
package lv.vermut;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.RootPanel;
import lv.vermut.controller.SingleDeviceController;
import lv.vermut.model.DataService;
import lv.vermut.model.DataServiceAsync;
import lv.vermut.view.SingleDeviceApplicationView;

import java.util.logging.Logger;

public class SingleDeviceApplication {

    private static final DataServiceAsync dataService = GWT.create(DataService.class);

    public static DataServiceAsync getDataService() {
        return dataService;
    }

    private static Logger logger = Logger.getLogger("");

    public static Logger getLogger() {
        return logger;
    }

    private final SingleDeviceController singleDeviceController;
    private SingleDeviceApplicationView singleDeviceApplicationView;

    public SingleDeviceApplication() {
        singleDeviceApplicationView = new SingleDeviceApplicationView();
        singleDeviceController = new SingleDeviceController(singleDeviceApplicationView);
    }

    public void run() {
        RootPanel.get().add(singleDeviceApplicationView);
        singleDeviceController.run();
    }
}
