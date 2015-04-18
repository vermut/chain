package org.traccar.web.server.model

import com.spatial4j.core.context.jts.JtsSpatialContext
import com.spatial4j.core.distance.DistanceUtils
import com.spatial4j.core.shape.Point
import com.spatial4j.core.shape.Shape
import com.spatial4j.core.shape.jts.JtsGeometry
import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.geom.Polygon
import com.vividsolutions.jts.geom.util.AffineTransformation
import com.vividsolutions.jts.operation.distance.DistanceOp
import org.jgrapht.GraphPath
import org.jgrapht.alg.DijkstraShortestPath
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.SimpleGraph
import org.traccar.web.shared.model.Position

/**
 * Created by admin on 03/04/15.
 */
/*@Grapes([
        @Grab(group = "org.jgrapht", module = "jgrapht-core", version = '0.9.0'),
        @Grab(group = 'com.spatial4j', module = 'spatial4j', version = '0.4.1'),
        @Grab(group = 'com.vividsolutions', module = 'jts', version = '1.12'),
])*/

class GameField {
    final private JtsSpatialContext geo = JtsSpatialContext.GEO

    private Polygon areaPolygon
    JtsGeometry teamOneStart
    JtsGeometry teamTwoStart
    JtsGeometry teamOneFinish
    JtsGeometry teamTwoFinish

    double latSizeDeg
    double lonSizeDeg

    private static double TOUCH_DISTANCE_KM = 0.070

    public GameField(Coordinate topLeft, double yAxisOffsetDegrees, double sideSizeMeters) {
        latSizeDeg = metersToDeg(sideSizeMeters)
        lonSizeDeg = DistanceUtils.calcLonDegreesAtLat(topLeft.x, latSizeDeg)

        // Build and rotate gamefield rect area
        def gf = geo.geometryFactory
        def af = new AffineTransformation().rotate(Math.toRadians(yAxisOffsetDegrees), topLeft.x, topLeft.y)
        Coordinate[] cs = [
                new Coordinate(topLeft.x, topLeft.y),
                new Coordinate(topLeft.x - latSizeDeg, topLeft.y),
                new Coordinate(topLeft.x - latSizeDeg, topLeft.y + lonSizeDeg),
                new Coordinate(topLeft.x, topLeft.y + lonSizeDeg),
                new Coordinate(topLeft.x, topLeft.y)
        ]

        cs.each { Coordinate it -> af.transform(it, it) }
        areaPolygon = gf.createPolygon(gf.createLinearRing(cs), null)

        // Get separate lines from the polygon
        areaPolygon.exteriorRing.with {
            teamOneStart = geo.makeShape gf.createLineString([getCoordinateN(0), getCoordinateN(1)] as Coordinate[])
            teamTwoStart = geo.makeShape gf.createLineString([getCoordinateN(1), getCoordinateN(2)] as Coordinate[])
            teamOneFinish = geo.makeShape gf.createLineString([getCoordinateN(2), getCoordinateN(3)] as Coordinate[])
            teamTwoFinish = geo.makeShape gf.createLineString([getCoordinateN(3), getCoordinateN(4)] as Coordinate[])
        }
    }


    GraphPath teamOneHasLink(List<Position> players) {
        hasLink(teamOneStart, teamOneFinish, players.collect({
            geo.makePoint(it.latitude, it.longitude)
        }).toArray() as Point[])
    }

    GraphPath teamTwoHasLink(List<Position> players) {
        hasLink(teamTwoStart, teamTwoFinish, players.collect({
            geo.makePoint(it.latitude, it.longitude)
        }).toArray() as Point[])
    }

    private GraphPath hasLink(Shape start, Shape finish, Point[] players) {
        def graph = new SimpleGraph<Point, DefaultEdge>(DefaultEdge.class)
        graph.with {
            addVertex(start.center)
            addVertex(finish.center)
            players.each { addVertex(it) }

            for (int i = 0; i < players.length; i++) {
                def player = players[i]

                // Check edges
                if (calcDistance(start, player) < TOUCH_DISTANCE_KM) {
                    print "hasStart "
                    addEdge(start.center, player)
                }

                if (calcDistance(finish, player) < TOUCH_DISTANCE_KM) {
                    print "hasFinish "
                    addEdge(finish.center, player)
                }

                for (int j = i + 1; j < players.length; j++) {
                    def anotherPlayer = players[j]
                    if (calcDistance(anotherPlayer, player) < TOUCH_DISTANCE_KM) {
                        print "hasPlayer "
                        addEdge(player, anotherPlayer)
                    }
                }
            }
        }
        new DijkstraShortestPath(graph, start.center, finish.center).path
    }

    static double metersToDeg(double m) { m / 1000D * DistanceUtils.KM_TO_DEG }

    @Override
    public String toString() {
        return """teamOneStart,,$teamOneStart.geom.coordinate.x,$teamOneStart.geom.coordinate.y
teamTwoStart,,$teamTwoStart.geom.coordinate.x,$teamTwoStart.geom.coordinate.y
teamOneFinish,,$teamOneFinish.geom.coordinate.x,$teamOneFinish.geom.coordinate.y
teamTwoFinish,,$teamTwoFinish.geom.coordinate.x,$teamTwoFinish.geom.coordinate.y
""";
    }

    private double calcDistance(Shape a, Shape b) {
        DistanceOp dc = new DistanceOp(geo.getGeometryFrom(a), geo.getGeometryFrom(b))
        haversine(dc.nearestPoints()[0].x, dc.nearestPoints()[0].y, dc.nearestPoints()[1].x, dc.nearestPoints()[1].y)
    }

    static double haversine(lat1, lon1, lat2, lon2) {
        def R = 6372.8
        // In kilometers
        def dLat = Math.toRadians(lat2 - lat1)
        def dLon = Math.toRadians(lon2 - lon1)
        lat1 = Math.toRadians(lat1)
        lat2 = Math.toRadians(lat2)

        def a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2)
        def c = 2 * Math.asin(Math.sqrt(a))
        R * c
    }
}
