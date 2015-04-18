package org.traccar.web.server.controller; /**
 * Created by admin on 03/04/15.
 */

import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionEvent;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@WebListener
public class GameProcessListener implements ServletContextListener
        /*, HttpSessionListener, HttpSessionAttributeListener */ {

    private ScheduledExecutorService scheduler;
    private Game game;

    // Public constructor is required by servlet spec
    public GameProcessListener() {
        System.out.println("GameProcessListener.GameProcessListener");
    }

    // -------------------------------------------------------
    // ServletContextListener implementation
    // -------------------------------------------------------
    public void contextInitialized(ServletContextEvent sce) {
        try {
            CoordinateReferenceSystem CRS = org.geotools.referencing.CRS.decode("EPSG:4326");
        } catch (FactoryException e) {
            e.printStackTrace();
        }

        game = new Game();
        game.startGame();

        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(game, 0, 5, TimeUnit.SECONDS);
    }

    public void contextDestroyed(ServletContextEvent sce) {
        scheduler.shutdownNow();
    }

    // -------------------------------------------------------
    // HttpSessionListener implementation
    // -------------------------------------------------------
    public void sessionCreated(HttpSessionEvent se) {
      /* Session is created. */
    }

    public void sessionDestroyed(HttpSessionEvent se) {
      /* Session is destroyed. */
    }

    // -------------------------------------------------------
    // HttpSessionAttributeListener implementation
    // -------------------------------------------------------

    public void attributeAdded(HttpSessionBindingEvent sbe) {
      /* This method is called when an attribute 
         is added to a session.
      */
    }

    public void attributeRemoved(HttpSessionBindingEvent sbe) {
      /* This method is called when an attribute
         is removed from a session.
      */
    }

    public void attributeReplaced(HttpSessionBindingEvent sbe) {
      /* This method is invoked when an attibute
         is replaced in a session.
      */
    }
}
