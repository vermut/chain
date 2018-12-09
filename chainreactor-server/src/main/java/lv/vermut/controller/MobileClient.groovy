package lv.vermut.controller

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import groovy.json.JsonBuilder
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.servlet.ServletCategory
import lv.vermut.model.Device
import lv.vermut.model.Position

import javax.persistence.PersistenceException
import javax.persistence.TypedQuery
import javax.servlet.ServletConfig
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession

@WebServlet(["/api/authenticate", "/api/gamedata"])
class MobileClient extends HttpServlet {
    static Game GAME
    def application

    private JsonSlurper jsonSlurper
    private Algorithm algorithm = Algorithm.HMAC256("chainsecret")

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
                session.traccar_device_id = session.traccar_device.id

                String token = JWT.create()
                        .withIssuer("ChainReactor")
                        .sign(algorithm)
                response.writer.print(new JsonBuilder([
                        id   : session.traccar_device_id,
                        token: token
                ]))

            }
        }
        catch (IllegalStateException ignored) {
            response.status = 403
            response.writer.print "No such user - $ignored.message"
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) {
        def session = request.session
        use(ServletCategory) {
            try {
                Device device = extractDeviceFromSession(session)
                def body = jsonSlurper.parse(request.reader)

                if (!request.getHeader("Authorization") || !device)
                    throw new IllegalStateException()

                synchronized (session.traccar_device_id) {
                    try {
                        GAME.em.refresh(device)
                    } catch (IllegalArgumentException ignored)                    {
                        session.traccar_device = null
                        device = extractDeviceFromSession(session)
                        GAME.em.refresh(device)
                    }

                    GAME.em.getTransaction().begin()
                    try {
                        device.setActive(true)
                        Position position = new Position(device, new Date((long) body.timestamp), body.latitude, body.longitude)
                        GAME.em.persist(position)

                        device.setLatestPosition(position)
//                            GAME.em.merge(device)

                        GAME.em.getTransaction().commit()
                    } catch (PersistenceException | NullPointerException e) {
                        GAME.em.getTransaction().rollback()
                        throw new PersistenceException(e)
                    } catch (e) {
                        GAME.em.getTransaction().rollback()
                        throw e
                    }
                }
            }
            catch (IllegalStateException ignored) {
                session?.traccar_device = null
                session?.invalidate()
                response.status = 401
                response.writer.print "Not logged in"
            }
            catch (IllegalArgumentException | PersistenceException ignored) {
                response.status = 503
                response.writer.print "Server overload"
            }
        }
    }

    void doGet(HttpServletRequest request, HttpServletResponse response) {
        def session = request.session
        use(ServletCategory) {
            try {
                if (session.counter) {  // We can use . notation to access session attribute.
                    session.counter++  // We can use . notation to set value for session attribute.
                } else {
                    session.counter = 1
                }

                Device device = extractDeviceFromSession(session)
                if (!request.getHeader("Authorization") || !device)
                    throw new IllegalStateException()

                synchronized (session.traccar_device_id) {
                    def report
                    try {
                        GAME.em.refresh(device)
                        report = GAME.deviceReport(device)
                    } catch (e) {
//                        log("Troubles getting report", e)
                        throw new PersistenceException(e)
                    }
                    report.score = session.counter      // TODO remove
                    response.writer.print JsonOutput.toJson(report)
                }
            }
            catch (IllegalStateException ignored) {
                session?.traccar_device = null
                session?.invalidate()
                response.status = 401
                response.writer.print "Not logged in"
            }
            catch (ConcurrentModificationException | PersistenceException ignored) {
                response.status = 503
                response.writer.print "Server overload"
            }
        }
    }

    private static Device extractDeviceFromSession(HttpSession session) {
        use(ServletCategory) {
            if (!session.traccar_device_id)
                return null

            if (!session.traccar_device)
                session.traccar_device = GAME.em.find(Device.class, session.traccar_device_id)

            return session.traccar_device
        }
    }

    private static Device deviceLogin(String login, String password) {
        synchronized (GAME.em) {
            TypedQuery<Device> query = GAME.em.createQuery("SELECT x FROM Device x WHERE x.name = :name", Device.class)
            query.setParameter("name", login)

            try {
                if (!query.resultList)
                    throw new IllegalStateException("No such user")

                Device device = query.singleResult
                if (device.getUniqueId() != password)
                    throw new IllegalStateException("Wrong Pass")

                return device
            } catch (PersistenceException | NullPointerException ignored) {
                throw new IllegalStateException("BD Failure")
            }
        }
    }
}