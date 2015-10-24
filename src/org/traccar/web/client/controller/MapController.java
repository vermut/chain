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
import org.traccar.web.client.Application;
import org.traccar.web.client.model.BaseAsyncCallback;
import org.traccar.web.client.view.MapView;
import org.traccar.web.shared.model.*;

import java.util.*;

public class MapController implements ContentController, MapView.MapHandler {

    public static final double ATTACK_CROSS = 0.0002;
    private static final int UPDATE_INTERVAL = 5000;
    private MapHandler mapHandler;
    private MapView mapView;
    private Timer updateTimer;
    private GameInfo gameInfo;
    private Map<Long, Position> latestPositionMap = new HashMap<Long, Position>();
    private Map<Long, PositionUpdateHandler> positionUpdateMap = new HashMap<Long, PositionUpdateHandler>();
    public MapController(MapHandler mapHandler) {
        this.mapHandler = mapHandler;
        mapView = new MapView(this);
    }

    public static native void console(String text)
/*-{
    console.log(text);
}-*/;

    @Override
    public ContentPanel getView() {
        return mapView.getView();
    }

    @Override
    public void run() {
        Application.getDataService().getGameInfo(new BaseAsyncCallback<GameInfo>() {
            @Override
            public void onSuccess(GameInfo result) {
                gameInfo = result;
                update();
            }
        });
        updateTimer = new Timer() {
            @Override
            public void run() {
                update();
            }
        };
    }

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
        Application.getDataService().getTeamReport(new BaseAsyncCallback<TeamReport>() {
            @Override
            public void onSuccess(TeamReport result) {
                mapView.getVectorLayer().removeAllFeatures();

                mapView.team1Layer.removeAllFeatures();
                mapView.team2Layer.removeAllFeatures();
                //noinspection SuspiciousNameCombination
                mapView.drawField(mapView.createPoint(gameInfo.getTopLeft().y, gameInfo.getTopLeft().x),
                        mapView.createPoint(gameInfo.getTopRight().y, gameInfo.getTopRight().x),
                        mapView.createPoint(gameInfo.getBottomRight().y, gameInfo.getBottomRight().x),
                        mapView.createPoint(gameInfo.getBottomLeft().y, gameInfo.getBottomLeft().x)
                );


                if (result != null) {
                    drawAttackTarget(result.getAttackPoints()[0].x, result.getAttackPoints()[0].y, mapView.team1Layer);
                    drawAttackTarget(result.getAttackPoints()[1].x, result.getAttackPoints()[1].y, mapView.team2Layer);

                    if (result.getOwnLink() != null) {
                        mapView.drawLink(result.getOwnLink());
                    }
                }
            }
        });

        if (false) Application.getDataService().getGameInfo(new BaseAsyncCallback<GameInfo>() {
            @Override
            public void onSuccess(GameInfo result) {
                if (result != null) {
                    mapView.getVectorLayer().removeAllFeatures();
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

    public void drawAttackTarget(double lat, double lon, org.gwtopenmaps.openlayers.client.layer.Vector layer) {
        mapView.drawLines(new SimplePoint[]{
                new SimplePoint(lat - ATTACK_CROSS, lon), new SimplePoint(lat + ATTACK_CROSS, lon)
        }, layer);
        mapView.drawLines(new SimplePoint[]{
                new SimplePoint(lat, lon - ATTACK_CROSS), new SimplePoint(lat, lon + ATTACK_CROSS)
        }, layer);
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

    @Override
    public void onAttackTargetClicked(final double lat, final double lon) {
        Application.getDataService().attack(lat, lon, new BaseAsyncCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                if (result) {
                    mapView.drawLink(new SimplePoint[]{
                            new SimplePoint(lat - ATTACK_CROSS, lon), new SimplePoint(lat + ATTACK_CROSS, lon)
                    });
                    mapView.drawLink(new SimplePoint[]{
                            new SimplePoint(lat, lon - ATTACK_CROSS), new SimplePoint(lat, lon + ATTACK_CROSS)
                    });
                }
            }
        });
    }

    public interface MapHandler {
        void onDeviceSelected(Device device);

        void onArchivePositionSelected(Position position);
    }

    public interface PositionUpdateHandler {
        void onUpdate(Position position);
    }

}
