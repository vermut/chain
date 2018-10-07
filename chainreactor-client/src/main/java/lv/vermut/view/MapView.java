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

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Command;
import com.sencha.gxt.widget.core.client.ContentPanel;
import lv.vermut.model.Device;
import lv.vermut.model.Position;
import lv.vermut.model.SimplePoint;
import org.gwtopenmaps.openlayers.client.*;
import org.gwtopenmaps.openlayers.client.control.LayerSwitcher;
import org.gwtopenmaps.openlayers.client.control.ScaleLine;
import org.gwtopenmaps.openlayers.client.event.MapClickListener;
import org.gwtopenmaps.openlayers.client.feature.VectorFeature;
import org.gwtopenmaps.openlayers.client.geometry.LineString;
import org.gwtopenmaps.openlayers.client.geometry.MultiLineString;
import org.gwtopenmaps.openlayers.client.geometry.Point;
import org.gwtopenmaps.openlayers.client.layer.*;

import java.util.List;

public class MapView {
    private static final Projection DEFAULT_PROJECTION = new Projection("EPSG:4326");
    private final MapPositionRenderer latestPositionRenderer;
    private final MapPositionRenderer archivePositionRenderer;
    public Vector team1Layer;
    public Vector team2Layer;
    private MapHandler mapHandler;
    private ContentPanel contentPanel;
    private MapWidget mapWidget;
    private Map map;
    private Vector vectorLayer;
    private Markers markerLayer;
    private MapPositionRenderer.SelectHandler latestPositionSelectHandler = new MapPositionRenderer.SelectHandler() {

        @Override
        public void onSelected(Position position) {
            mapHandler.onPositionSelected(position);
        }

    };
    private MapPositionRenderer.SelectHandler archivePositionSelectHandler = new MapPositionRenderer.SelectHandler() {

        @Override
        public void onSelected(Position position) {
            mapHandler.onArchivePositionSelected(position);
        }

    };

    public MapView(final MapHandler mapHandler) {
        this.mapHandler = mapHandler;
        contentPanel = new ContentPanel();
        contentPanel.setHeading("Map");

        MapOptions defaultMapOptions = new MapOptions();

        mapWidget = new MapWidget("100%", "100%", defaultMapOptions);
        map = mapWidget.getMap();
        {
            VectorOptions vectorOptions = new VectorOptions();
            {
                Style style = new Style();
                style.setStrokeColor("blue");
                style.setStrokeWidth(3);
                style.setFillOpacity(1);

                vectorOptions.setStyle(style);
            }
            vectorLayer = new Vector("Vector", vectorOptions);
        }
        {
            VectorOptions team1LayerOptions = new VectorOptions();
            {
                Style style = new Style();
                style.setStrokeColor("green");
                style.setStrokeWidth(5);
                style.setFillOpacity(1);

                team1LayerOptions.setStyle(style);
            }
            team1Layer = new Vector("Team1", team1LayerOptions);
        }
        {
            VectorOptions team2LayerOptions = new VectorOptions();
            {
                Style style = new Style();
                style.setStrokeColor("yellow");
                style.setStrokeWidth(5);
                style.setFillOpacity(1);

                team2LayerOptions.setStyle(style);
            }
            team2Layer = new Vector("Team2", team2LayerOptions);
        }
        MarkersOptions markersOptions = new MarkersOptions();
        markerLayer = new Markers("Markers", markersOptions);

        initMapLayers(map);

        map.addLayer(vectorLayer);
        map.addLayer(team1Layer);
        map.addLayer(team2Layer);
        map.addLayer(markerLayer);

        map.addControl(new LayerSwitcher());
        map.addControl(new ScaleLine());
        map.setCenter(createLonLat(24.103382, 56.954818), 16);

        map.addMapClickListener(new MapClickListener() {
            @Override
            public void onClick(MapClickEvent mapClickEvent) {
                LonLat lonLat = mapClickEvent.getLonLat();
                lonLat.transform(map.getProjection(), DEFAULT_PROJECTION.getProjectionCode()); //transform lonlat to more readable format
                mapHandler.onAttackTargetClicked(lonLat.lat(), lonLat.lon());
            }
        });

        contentPanel.add(mapWidget);

        // Update map size
        contentPanel.addResizeHandler(new ResizeHandler() {
            @Override
            public void onResize(ResizeEvent event) {
                Scheduler.get().scheduleDeferred(new Command() {
                    @Override
                    public void execute() {
                        map.updateSize();
                    }
                });
            }
        });

        latestPositionRenderer = new MapPositionRenderer(this, MarkerIconFactory.IconType.iconLatest, latestPositionSelectHandler);
        archivePositionRenderer = new MapPositionRenderer(this, MarkerIconFactory.IconType.iconArchive, archivePositionSelectHandler);
    }

    public ContentPanel getView() {
        return contentPanel;
    }

    public Map getMap() {
        return map;
    }

    public Vector getVectorLayer() {
        return vectorLayer;
    }

    public Markers getMarkerLayer() {
        return markerLayer;
    }

    public LonLat createLonLat(double longitude, double latitude) {
        LonLat lonLat = new LonLat(longitude, latitude);
        lonLat.transform(DEFAULT_PROJECTION.getProjectionCode(), map.getProjection());
        return lonLat;
    }

    public Point createPoint(double x, double y) {
        Point point = new Point(x, y);
        point.transform(DEFAULT_PROJECTION, new Projection(map.getProjection()));
        return point;
    }

    private void initMapLayers(Map map) {
        map.addLayer(OSM.Mapnik("OpenStreetMap"));

        GoogleV3Options gHybridOptions = new GoogleV3Options();
        gHybridOptions.setNumZoomLevels(20);
        gHybridOptions.setType(GoogleV3MapType.G_HYBRID_MAP);
        map.addLayer(new GoogleV3("Google Hybrid", gHybridOptions));

        GoogleV3Options gNormalOptions = new GoogleV3Options();
        gNormalOptions.setNumZoomLevels(22);
        gNormalOptions.setType(GoogleV3MapType.G_NORMAL_MAP);
        map.addLayer(new GoogleV3("Google Normal", gNormalOptions));

        GoogleV3Options gSatelliteOptions = new GoogleV3Options();
        gSatelliteOptions.setNumZoomLevels(20);
        gSatelliteOptions.setType(GoogleV3MapType.G_SATELLITE_MAP);
        map.addLayer(new GoogleV3("Google Satellite", gSatelliteOptions));

        GoogleV3Options gTerrainOptions = new GoogleV3Options();
        gTerrainOptions.setNumZoomLevels(16);
        gTerrainOptions.setType(GoogleV3MapType.G_TERRAIN_MAP);
        map.addLayer(new GoogleV3("Google Terrain", gTerrainOptions));

        final String bingKey = "AseEs0DLJhLlTNoxbNXu7DGsnnH4UoWuGue7-irwKkE3fffaClwc9q_Mr6AyHY8F";
        map.addLayer(new Bing(new BingOptions("Bing Road", bingKey, BingType.ROAD)));
        map.addLayer(new Bing(new BingOptions("Bing Hybrid", bingKey, BingType.HYBRID)));
        map.addLayer(new Bing(new BingOptions("Bing Aerial", bingKey, BingType.AERIAL)));
    }

    public void showLatestPositions(List<Position> positions) {
        latestPositionRenderer.showPositions(positions);
    }

    public void drawField(Point topLeft, Point topRight, Point bottomRight, Point bottomLeft) {
        /* Point[] linePoints = new Point[]{
                topLeft,    // team1Start.begin
                topRight,   // team2Start.begin
                bottomRight,// team1Finish.begin
                bottomLeft, // team2Finish.begin
                topLeft     // team1Start.begin
        };
        LineString lineString = new LineString(linePoints); */

        MultiLineString team1 = new MultiLineString(new LineString[]{
                new LineString(new Point[]{
                        topLeft, topRight
                }),
                new LineString(new Point[]{
                        bottomRight, bottomLeft
                }),
        });


        MultiLineString team2 = new MultiLineString(new LineString[]{
                new LineString(new Point[]{
                        topRight, bottomRight
                }),
                new LineString(new Point[]{
                        bottomLeft, topLeft
                }),
        });

        // getVectorLayer().addFeature(new VectorFeature(lineString));
        team1Layer.addFeature(new VectorFeature(team1));
        team2Layer.addFeature(new VectorFeature(team2));
    }

    public void drawLink(SimplePoint[] points) {
        Point[] linePoints = new Point[points.length];

        for (int i = 0; i < points.length; i++) {
            //noinspection SuspiciousNameCombination
            linePoints[i] = createPoint(points[i].y, points[i].x);
        }

        LineString lineString = new LineString(linePoints);
        getVectorLayer().addFeature(new VectorFeature(lineString));
    }

    public void drawLines(SimplePoint[] points, Vector layer) {
        Point[] linePoints = new Point[points.length];

        for (int i = 0; i < points.length; i++) {
            //noinspection SuspiciousNameCombination
            linePoints[i] = createPoint(points[i].y, points[i].x);
        }

        LineString lineString = new LineString(linePoints);
        layer.addFeature(new VectorFeature(lineString));
    }

    public void showArchivePositions(List<Position> positions) {
        archivePositionRenderer.showTrack(positions);
        archivePositionRenderer.showPositions(positions);
    }

    public void selectDevice(Device device) {
        latestPositionRenderer.selectDevice(device, true);
    }

    public void selectArchivePosition(Position position) {
        archivePositionRenderer.selectPosition(position, true);
    }

    public interface MapHandler {
        void onPositionSelected(Position position);

        void onArchivePositionSelected(Position position);

        void onAttackTargetClicked(double lat, double lon);
    }

}
