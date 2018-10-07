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

import com.google.gwt.core.client.GWT;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.ContentPanel;
import lv.vermut.model.Position;
import lv.vermut.model.StateItem;
import lv.vermut.model.StateItemProperties;
import lv.vermut.model.StateReader;
import lv.vermut.view.StateView;

public class StateController implements ContentController {

    private ListStore<StateItem> stateStore;

    private StateView stateView;

    public StateController() {
        StateItemProperties stateItemProperties = GWT.create(StateItemProperties.class);
        stateStore = new ListStore<>(stateItemProperties.id());
        stateView = new StateView(stateStore);
    }

    @Override
    public ContentPanel getView() {
        return stateView.getView();
    }

    @Override
    public void run() {
    }

    public void showState(Position position) {
        if (position != null) {
            stateStore.replaceAll(StateReader.getState(position));
        } else {
            stateStore.clear();
        }
    }

}
