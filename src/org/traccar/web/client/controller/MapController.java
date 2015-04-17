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
package org.traccar.web.client.controller;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sencha.gxt.widget.core.client.ContentPanel;
import org.gwtopenmaps.openlayers.client.geometry.Point;
import org.traccar.web.client.Application;
import org.traccar.web.client.model.BaseAsyncCallback;
import org.traccar.web.client.view.MapView;
import org.traccar.web.shared.model.Device;
import org.traccar.web.shared.model.GameInfo;
import org.traccar.web.shared.model.Position;

import java.util.*;

public class MapController implements ContentController, MapView.MapHandler {

    private static final int UPDATE_INTERVAL = 5000;

    public interface MapHandler {
        public void onDeviceSelected(Device device);

        public void onArchivePositionSelected(Position position);
    }

    private MapHandler mapHandler;

    private MapView mapView;

    public MapController(MapHandler mapHandler) {
        this.mapHandler = mapHandler;
        mapView = new MapView(this);
    }

    @Override
    public ContentPanel getView() {
        return mapView.getView();
    }

    private Timer updateTimer;

    @Override
    public void run() {
        updateTimer = new Timer() {
            @Override
            public void run() {
                update();
            }
        };
        update();
    }

    private Map<Long, Position> latestPositionMap = new HashMap<Long, Position>();

    public static native void console(String text)
/*-{
    console.log(text);
}-*/;

    public void update() {
        updateTimer.cancel();
        Application.getDataService().getLatestPositions(new AsyncCallback<List<Position>>() {
            @Override
            public void onSuccess(List<Position> result) {
                mapView.showLatestPositions(result);
                for (Position position : result) {
                    latestPositionMap.put(position.getDevice().getId(), position);
                }
                for (Map.Entry<Long, PositionUpdateHandler> entry : positionUpdateMap.entrySet()) {
                    entry.getValue().onUpdate(latestPositionMap.get(entry.getKey()));
                }
                updateTimer.schedule(UPDATE_INTERVAL);
            }

            @Override
            public void onFailure(Throwable caught) {
                updateTimer.schedule(UPDATE_INTERVAL);
            }
        });

        Application.getDataService().getGameInfo(new BaseAsyncCallback<GameInfo>() {
            @Override
            public void onSuccess(GameInfo result) {
                if (result != null) {
                    //noinspection SuspiciousNameCombination
                    mapView.drawField(mapView.createPoint(result.getTopLeft().y, result.getTopLeft().x),
                            mapView.createPoint(result.getTopRight().y, result.getTopRight().x),
                            mapView.createPoint(result.getBottomRight().y, result.getBottomRight().x),
                            mapView.createPoint(result.getBottomLeft().y, result.getBottomLeft().x)
                    );
//                    mapView.drawField2(mapView.createLonLat(result.getTopLeft().y, result.getTopLeft().x),
//                            mapView.createLonLat(result.getTopRight().y, result.getTopRight().x),
//                            mapView.createLonLat(result.getBottomRight().y, result.getBottomRight().x),
//                            mapView.createLonLat(result.getBottomLeft().y, result.getBottomLeft().x)
//                    );
                }
            }
        });
    }

    public void selectDevice(Device device) {
        mapView.selectDevice(device);
    }

    public void showArchivePositions(List<Position> positions) {
        List<Position> sortedPositions = new LinkedList<Position>(positions);
        Collections.sort(sortedPositions, new Comparator<Position>() {
            @Override
            public int compare(Position o1, Position o2) {
                return o1.getTime().compareTo(o2.getTime());
            }
        });
        mapView.showArchivePositions(sortedPositions);
    }

    public void selectArchivePosition(Position position) {
        mapView.selectArchivePosition(position);
    }

    public interface PositionUpdateHandler {
        public void onUpdate(Position position);
    }

    private Map<Long, PositionUpdateHandler> positionUpdateMap = new HashMap<Long, PositionUpdateHandler>();


    public void registerPositionUpdate(Device device, PositionUpdateHandler handler) {
        positionUpdateMap.put(device.getId(), handler);
        handler.onUpdate(latestPositionMap.get(device.getId()));
    }

    public void unregisterPositionUpdate(Device device) {
        positionUpdateMap.remove(device.getId());
    }

    @Override
    public void onPositionSelected(Position position) {
        mapHandler.onDeviceSelected(position.getDevice());
    }

    @Override
    public void onArchivePositionSelected(Position position) {
        mapHandler.onArchivePositionSelected(position);
    }

}
