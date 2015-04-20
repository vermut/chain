package org.traccar.web.server.model

import com.spatial4j.core.context.jts.JtsSpatialContext
import com.spatial4j.core.distance.DistanceUtils
import com.spatial4j.core.shape.Point
import com.spatial4j.core.shape.Shape
import com.spatial4j.core.shape.jts.JtsGeometry
import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.geom.Polygon
import com.vividsolutions.jts.geom.util.AffineTransformation
import com.vividsolutions.jts.operation.distance.DistanceOp
import org.jgrapht.alg.DijkstraShortestPath
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.SimpleGraph
import org.traccar.web.shared.model.Position
import org.traccar.web.shared.model.SimplePoint

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
    private static double FEEL_DISTANCE_KM = TOUCH_DISTANCE_KM / 2
    private static double ATTACK_DISTANCE_KM = TOUCH_DISTANCE_KM / 4

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


    def teamOneHasLink(List<Position> players) {
        hasLink teamOneStart, teamOneFinish, activePoints(players)
    }

    def teamTwoHasLink(List<Position> players) {
        hasLink teamTwoStart, teamTwoFinish, activePoints(players)
    }

    private hasLink(Shape start, Shape finish, Point[] players) {
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
                    addEdge(player, finish.center)
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

        def path = new DijkstraShortestPath<Point, DefaultEdge>(graph, start.center, finish.center).path
        if (!path)
            return null

        def link = new Link()
        link.points = path.edgeList.collect { graph.getEdgeSource(it) as Point }
        link.start = start.center
        link.finish = finish.center
        link
    }

    static double metersToDeg(double m) { m / 1000D * DistanceUtils.KM_TO_DEG }

    private Point[] activePoints(List<Position> players) {
        players.collect { geo.makePoint(it.latitude, it.longitude) }.
                findAll { areaPolygon.contains(geom(it)) }
    }

    private Position[] activePositions(List<Position> players) {
        players.findAll { areaPolygon.contains(geom(geo.makePoint(it.latitude, it.longitude))) }
    }

    def geom(Shape s) { geo.getGeometryFrom s }

    private double calcDistance(Shape a, Shape b) {
        calcDistance(geo.getGeometryFrom(a), geo.getGeometryFrom(b))
    }

    private static double calcDistance(Geometry a, Geometry b) {
        DistanceOp dc = new DistanceOp(a, b)
        haversine(dc.nearestPoints()[0].x, dc.nearestPoints()[0].y, dc.nearestPoints()[1].x, dc.nearestPoints()[1].y)
    }

    private double calcDistance(Position a, Shape b) {
        calcDistance(geo.makePoint(a.latitude, a.longitude), b)
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

    def getNeighbors(Position position, List<Position> positions) {
        def player = geo.makePoint(position.latitude, position.longitude)
        activePositions(positions).findAll { calcDistance(it, player) < TOUCH_DISTANCE_KM }.
                collect { it.device.name }
    }

    def getVictims(double latitude, double longitude, List<Position> positions) {
        def player = geo.makePoint(latitude, longitude)
        positions.findAll { calcDistance(it, player) < ATTACK_DISTANCE_KM }
    }

    boolean isFeelingLink(Position position, SimplePoint[] linkRoute) {
        def player = geom(geo.makePoint(position.latitude, position.longitude))
        def link = geo.geometryFactory.createLineString(linkRoute.collect {
            new Coordinate(it.x, it.y)
        } as Coordinate[])
        calcDistance(player, link) < FEEL_DISTANCE_KM
    }

    public class Link {
        Point start
        Point finish
        List<Point> points
    }
}
