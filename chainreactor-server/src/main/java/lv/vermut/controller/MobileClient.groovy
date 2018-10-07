package lv.vermut.controller

import groovy.json.JsonSlurper
import groovy.xml.MarkupBuilder
import lv.vermut.model.DataServiceImpl
import lv.vermut.model.Device

import javax.persistence.EntityManager
import javax.persistence.TypedQuery
import javax.servlet.annotation.WebServlet
import javax.servlet.http.*
import javax.servlet.*
import groovy.servlet.ServletCategory

@WebServlet("/api/authenticate")
class MobileClient extends HttpServlet {
    def application
    JsonSlurper jsonSlurper
    final EntityManager entityManager = DataServiceImpl.getServletEntityManager();

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
            }
        }
        catch (IllegalStateException ignored) {
            response.setStatus(403)
            response.writer.print("No such user")
        }
    }

    void modoGet(HttpServletRequest request, HttpServletResponse response) {
        def html = new MarkupBuilder(response.writer)
        def session = request.session

        use(ServletCategory) {
            if (session.counter) {  // We can use . notation to access session attribute.
                session.counter++  // We can use . notation to set value for session attribute.
            } else {
                session.counter = 1
            }
            request.pageTitle = 'Groovy Rocks!'


            html.html {
                head {
                    title request.pageTitle
                }
                body {
                    h1 request.pageTitle
                    h2 "$application?.version written by $application.author"
                    p "You have requested this page $session.counter times."
                }
            }
        }
    }

    Device deviceLogin(String login, String password) {
        synchronized (entityManager) {
            TypedQuery<Device> query = entityManager.createQuery(
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