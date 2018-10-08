package lv.vermut.controller

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import groovy.json.JsonBuilder
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.servlet.ServletCategory
import lv.vermut.model.Device

import javax.persistence.TypedQuery
import javax.servlet.ServletConfig
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@WebServlet(["/api/authenticate", "/api/gamedata"])
class MobileClient extends HttpServlet {
    static Game GAME
    def application

    private JsonSlurper jsonSlurper
    private Algorithm algorithm = Algorithm.HMAC256("chainsecret");

    void init(ServletConfig config) {
        super.init(config)
        jsonSlurper = new JsonSlurper()
        application = config.servletContext

        use(ServletCategory) {
            application.author = 'mrhaki'
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        def session = request.session

        try {
            use(ServletCategory) {
                def body = jsonSlurper.parse(request.reader)
                session.traccar_device = deviceLogin(body.username, body.password)

                String token = JWT.create()
                        .withIssuer("ChainReactor")
                        .sign(algorithm);
                response.writer.print(new JsonBuilder([
                        id   : session.traccar_device.id,
                        token: token
                ]))

            }
        }
        catch (IllegalStateException ignored) {
            response.status = 403
            response.writer.print "No such user"
        }
    }

    void doGet(HttpServletRequest request, HttpServletResponse response) {
        def session = request.session
        try {
            use(ServletCategory) {
                if (session.counter) {  // We can use . notation to access session attribute.
                    session.counter++  // We can use . notation to set value for session attribute.
                } else {
                    session.counter = 1
                }

                Device device = session.traccar_device
                if (!request.getHeader("Authorization") || !device)
                    throw new IllegalStateException();

                synchronized (GAME.em) {
                    GAME.em.refresh(device);
                    def report = GAME.deviceReport(device)
                    report.score = session.counter      // TODO remove
                    response.writer.print JsonOutput.toJson(report)
                }
            }
        }
        catch (IllegalStateException ignored) {
            response.status = 401
            response.writer.print "Not logged in"
        }
    }


    Device deviceLogin(String login, String password) {
        synchronized (GAME.em) {
            TypedQuery<Device> query = GAME.em.createQuery(
                    "SELECT x FROM Device x WHERE x.name = :name", Device.class);
            query.setParameter("name", login);
            List<Device> results = query.getResultList();

            if (!results.isEmpty()) {
                Device device = results.get(0);
                if (!device.getUniqueId().equals(password))
                    throw new IllegalStateException();

                return device;
            }
            throw new IllegalStateException();
        }
    }
}