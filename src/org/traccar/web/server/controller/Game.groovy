package org.traccar.web.server.controller

import com.vividsolutions.jts.geom.Coordinate
import org.traccar.web.server.model.DataServiceImpl
import org.traccar.web.server.model.GameField
import org.traccar.web.shared.model.GameInfo
import org.traccar.web.shared.model.Position
import org.traccar.web.shared.model.SimplePoint

import javax.persistence.EntityManager

/**
 * Created by admin on 03/04/15.
 */
// @CompileStatic
class Game implements Runnable {
    GameField field
    GameInfo gameInfo

    boolean started = false

    static Integer TEAM1 = 0
    static Integer TEAM2 = 1

    public static TEAM1_STR = 'team1'
    public static TEAM2_STR = 'team2'

    SimplePoint[] team1link
    SimplePoint[] team2link

    HashMap<Integer, List<Position>> players = new HashMap<>()

    EntityManager em

    void startGame() {
        started = true
        field = new GameField(new Coordinate(56.961385, 24.1306516666667), 0, 100)
        em = DataServiceImpl.initEMF().createEntityManager()

        //Set up gameInfo
        gameInfo = new GameInfo(
                topLeft: new SimplePoint(field.teamOneStart.geom.coordinate.x, field.teamOneStart.geom.coordinate.y),
                bottomLeft: new SimplePoint(field.teamTwoStart.geom.coordinate.x, field.teamTwoStart.geom.coordinate.y),
                bottomRight: new SimplePoint(field.teamOneFinish.geom.coordinate.x, field.teamOneFinish.geom.coordinate.y),
                topRight: new SimplePoint(field.teamTwoFinish.geom.coordinate.x, field.teamTwoFinish.geom.coordinate.y),
                score: [0,0]
        )

        DataServiceImpl.GAME = this
    }

    @Override
    void run() {
        if (!started) return

        fetchDbData()
        processAttacks()
        processRevivals()
        checkForConnection()
        updateScore()
    }

    def processRevivals() {

    }

    void updateScore() {
        if (team1link)
            gameInfo.score[TEAM1]++

        if (team2link)
            gameInfo.score[TEAM2]++
    }

    void checkForConnection() {
        print "TEAM1 "
        def t1 = field.teamOneHasLink(players[TEAM1])
        print "\nTEAM2 "
        def t2 = field.teamTwoHasLink(players[TEAM2])

        println "\nT1 link ${t1 != null} T2 link ${t2 != null}"

        if (t1) {
            t1.with { GameField.Link team ->
                team1link = team.points.collect {
                    new SimplePoint(it.x, it.y)
                } + new SimplePoint(team.finish.x, team.finish.y)

            }
        } else
            team1link = null

        if (t2)
            t2.with { team ->
                team2link = team.points.collect {
                    new SimplePoint(it.x, it.y)
                } + new SimplePoint(team.finish.x, team.finish.y)
            }
        else
            team2link = null
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
