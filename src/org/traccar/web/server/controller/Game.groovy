package org.traccar.web.server.controller

import com.vividsolutions.jts.geom.Coordinate
import org.traccar.web.server.model.DataServiceImpl
import org.traccar.web.server.model.GameField
import org.traccar.web.shared.model.*

import javax.persistence.EntityManager

/**
 * Created by admin on 03/04/15.
 */
// @CompileStatic
class Game implements Runnable {
    GameField field
    GameInfo gameInfo

    boolean started = false

    public static Integer TEAM1 = 0
    public static Integer TEAM2 = 1

    public static TEAM1_STR = 'team1'
    public static TEAM2_STR = 'team2'

    HashMap<Integer, SimplePoint[]> teamlink = [:]
    HashMap<Integer, List<Position>> players = [:]

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
                score: [0, 0]
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
        if (teamlink[TEAM1])
            gameInfo.score[TEAM1]++

        if (teamlink[TEAM2])
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
                teamlink[TEAM1] = team.points.collect {
                    new SimplePoint(it.x, it.y)
                } + new SimplePoint(team.finish.x, team.finish.y)

            }
        } else
            teamlink[TEAM1] = null

        if (t2)
            t2.with { team ->
                teamlink[TEAM2] = team.points.collect {
                    new SimplePoint(it.x, it.y)
                } + new SimplePoint(team.finish.x, team.finish.y)
            }
        else
            teamlink[TEAM2] = null
    }


    void processAttacks() {

    }

    void fetchDbData() {
        players[TEAM1] =
                em.createQuery("SELECT x FROM User u join u.devices d join d.latestPosition x WHERE u.login = '$TEAM1_STR'", Position.class).resultList
        players[TEAM2] =
                em.createQuery("SELECT x FROM User u join u.devices d join d.latestPosition x WHERE u.login = '$TEAM2_STR'", Position.class).resultList
    }

    def getTeamId(Device device) {
        if (players[TEAM1].find { it.device.id == device.id })
            return TEAM1;
        if (players[TEAM2].find { it.device.id == device.id })
            return TEAM2;

        return null;
    }

    static def teamNameById(team) {
        switch (team) {
            case TEAM1: TEAM1_STR; break
            case TEAM2: TEAM2_STR; break
            default: "UNREGISTERED"
        }
    }

    static def Integer teamIdByName(team) {
        switch (team) {
            case TEAM1_STR: TEAM1; break
            case TEAM2_STR: TEAM2; break
            default: null
        }
    }

    SimplePoint[] getTeamlink(Integer team) { teamlink[team] }

    boolean isOtherTeamHasLink(Integer team) { teamlink[otherTeam(team)] }

    static int otherTeam(int team) { team == TEAM1 ? TEAM2 : TEAM1 }

    DeviceReport deviceReport(Device device) {
        def report = new DeviceReport()
        def team = getTeamId(device)

        report.teamName = teamNameById(team)
        report.score = gameInfo.score

        if (team == null)
            return report;

        report.neighbors = field.getNeighbors(device.latestPosition, players[team])

        report.ownLinkStatus = getTeamlink(team) ? DeviceReport.HAVE_LINK : DeviceReport.NO_LINK
        if (getTeamlink(team).find {it.x == device.latestPosition.latitude && it.y == device.latestPosition.longitude})
            report.ownLinkStatus = DeviceReport.HAVE_LINK_WITH_YOU


        report.otherTeamLinkStatus = isOtherTeamHasLink(team) ? DeviceReport.HAVE_LINK : DeviceReport.NO_LINK
        if (isOtherTeamHasLink(team) && field.isFeelingLink(device.latestPosition, teamlink[otherTeam(team)]))
            report.otherTeamLinkStatus = DeviceReport.HAVE_LINK_AROUND_YOU


        report
    }
}
