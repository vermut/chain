package org.traccar.web.server.controller

import com.vividsolutions.jts.geom.Coordinate
import groovy.transform.CompileStatic
import org.traccar.web.server.model.DataServiceImpl
import org.traccar.web.server.model.GameField
import org.traccar.web.shared.model.GameInfo
import org.traccar.web.shared.model.Position
import org.traccar.web.shared.model.SimplePoint

import javax.persistence.EntityManager

/**
 * Created by admin on 03/04/15.
 */
@CompileStatic
class Game implements Runnable {
    GameField field
    GameInfo gameInfo

    boolean started = false

    static Integer TEAM1 = 0
    static Integer TEAM2 = 1

    static String TEAM1_STR = "team1"
    static String TEAM2_STR = "team2"


    int[] score
    HashMap<Integer, List<Position>> players = new HashMap<>()

    EntityManager em

    void startGame() {
        score = [0, 0]
        started = true
        field = new GameField(new Coordinate(56.961385, 24.1306516666667), 0, 100)
        em = DataServiceImpl.initEMF().createEntityManager()

        //Set up gameInfo
        gameInfo = new GameInfo(
                topLeft: new SimplePoint(field.teamOneStart.geom.coordinate.x, field.teamOneStart.geom.coordinate.y),
                bottomLeft: new SimplePoint(field.teamTwoStart.geom.coordinate.x, field.teamTwoStart.geom.coordinate.y),
                bottomRight: new SimplePoint(field.teamOneFinish.geom.coordinate.x, field.teamOneFinish.geom.coordinate.y),
                topRight: new SimplePoint(field.teamTwoFinish.geom.coordinate.x, field.teamTwoFinish.geom.coordinate.y)
        )

        DataServiceImpl.GAME = this
    }

    @Override
    void run() {
        print "tick"
        if (!started) return

        fetchDbData()
        processAttacks()
        checkForConnection()
        updateScore()

    }

    void updateScore() {
    }

    void checkForConnection() {
        def t1 = field.teamOneHasLink(players[TEAM1]) != null
        def t2 = false // field.teamTwoHasLink(players[TEAM2])

        println "T1 link $t1 T2 link $t2"
    }

    void processAttacks() {

    }

    void fetchDbData() {
        players[TEAM1] =
                em.createQuery("SELECT x FROM User u join u.devices d join d.latestPosition x WHERE u.login = '$TEAM1_STR'", Position.class).resultList
        players[TEAM2] =
                em.createQuery("SELECT x FROM User u join u.devices d join d.latestPosition x WHERE u.login = '$TEAM2_STR'", Position.class).resultList
    }

}
