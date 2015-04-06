package org.traccar.web.server.model

import com.spatial4j.core.context.jts.JtsSpatialContext
import com.spatial4j.core.distance.DistanceUtils
import com.spatial4j.core.shape.*
import com.spatial4j.core.shape.Shape
import com.spatial4j.core.shape.jts.*
import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.geom.Polygon
import com.vividsolutions.jts.geom.util.AffineTransformation
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
    private static final long serialVersionUID = 1;

    final private JtsSpatialContext geo = JtsSpatialContext.GEO

    private Polygon areaPolygon
    JtsGeometry teamOneStart
    JtsGeometry teamTwoStart
    JtsGeometry teamOneFinish
    JtsGeometry teamTwoFinish

    private static PLAYER_RADIUS_DEG = metersToDeg(50)

    public GameField(Coordinate topLeft, double yAxisOffsetDegrees, double sideSizeMeters) {
        def latSizeDeg = metersToDeg(sideSizeMeters)
        def lonSizeDeg = DistanceUtils.calcLonDegreesAtLat(topLeft.x, latSizeDeg)

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
        hasLink(teamOneStart, teamOneFinish, players.collect ({geo.makePoint(it.latitude, it.longitude)}).toArray() as Point[])
    }
    GraphPath teamTwoHasLink(List<Position> players) {
        hasLink(teamTwoStart, teamTwoFinish, players.collect ({geo.makePoint(it.latitude, it.longitude)}).toArray() as Point[])
    }

    private GraphPath hasLink(Shape start, Shape finish, Point[] players) {
        def graph = new SimpleGraph<Point, DefaultEdge>(DefaultEdge.class)
        graph.with {
            addVertex(start.center)
            addVertex(finish.center)
            players.each { addVertex(it) }

            for (int i = 0; i < players.length; i++) {
                def player = geo.makeCircle(players[i], PLAYER_RADIUS_DEG)

                // Check edges
                if (start.relate(player).intersects()) addEdge(start.center, player.center)
                if (finish.relate(player).intersects()) addEdge(finish.center, player.center)

                for (int j = i + 1; j < players.length; j++) {
                    def anotherPlayer = geo.makeCircle(players[j], PLAYER_RADIUS_DEG)
                    if (anotherPlayer.relate(player).intersects())
                        addEdge(player.center, anotherPlayer.center)
                }
            }
        }
        graph.vertexSet().each { println it.hashCode() + ",,$it.center.x,$it.center.y" }

        new DijkstraShortestPath(graph, start.center, finish.center).path
    }

    static double metersToDeg(double m) { m / 1000 * DistanceUtils.KM_TO_DEG }

    @Override
    public String toString() {
        return """teamOneStart,,$teamOneStart.geom.coordinate.x,$teamOneStart.geom.coordinate.y
teamTwoStart,,$teamTwoStart.geom.coordinate.x,$teamTwoStart.geom.coordinate.y
teamOneFinish,,$teamOneFinish.geom.coordinate.x,$teamOneFinish.geom.coordinate.y
teamTwoFinish,,$teamTwoFinish.geom.coordinate.x,$teamTwoFinish.geom.coordinate.y
""";
    }
}
