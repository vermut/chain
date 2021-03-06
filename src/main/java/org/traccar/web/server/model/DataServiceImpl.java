/*
 * Copyright 2013 Anton Tananaev (anton.tananaev@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.traccar.web.server.model;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import org.traccar.web.client.model.DataService;
import org.traccar.web.server.controller.Game;
import org.traccar.web.shared.model.*;

import javax.persistence.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;
import java.util.*;

public class DataServiceImpl extends RemoteServiceServlet implements DataService {

    private static final long serialVersionUID = 1;

    private static final String ATTRIBUTE_USER = "traccar.user";
    private static final String ATTRIBUTE_DEVICE = "traccar.device";
    private static final String ATTRIBUTE_ENTITYMANAGER = "traccar.entitymanager";

    private static EntityManagerFactory entityManagerFactory;
    private ApplicationSettings applicationSettings;
    public static Game GAME;

    @Override
    public void init() throws ServletException {
        super.init();

        initEMF();

        // Create Administrator account
        EntityManager entityManager = getServletEntityManager();
        TypedQuery<User> query = entityManager.createQuery("SELECT x FROM User x WHERE x.login = 'admin'", User.class);
        List<User> results = query.getResultList();
        if (results.isEmpty()) {
            User user = new User();
            user.setLogin("admin");
            user.setPassword("admin");
            user.setAdmin(true);
            createUser(entityManager, user);
        }
    }

    public static EntityManagerFactory initEMF() {
        if (entityManagerFactory == null)
        {
            String databaseUrl = System.getenv("CLEARDB_DATABASE_URL");
            StringTokenizer st = new StringTokenizer(databaseUrl, ":@/");
            String dbVendor = st.nextToken(); //if DATABASE_URL is set
            String userName = st.nextToken();
            String password = st.nextToken();
            String host = st.nextToken();
            String databaseName = st.nextToken();
            String jdbcUrl = String.format("jdbc:mysql://%s/%s?reconnect=true", host, databaseName);
            Map<String, String> properties = new HashMap<String, String>();

/*
            properties.put("javax.persistence.jdbc.url", jdbcUrl );
            properties.put("javax.persistence.jdbc.user", userName );
            properties.put("javax.persistence.jdbc.password", password );
            properties.put("javax.persistence.jdbc.driver", "com.mysql.jdbc.Driver");
*/

            properties.put("hibernate.connection.url", jdbcUrl );
            properties.put("hibernate.connection.username", userName );
            properties.put("hibernate.connection.password", password );
            properties.put("hibernate.connection.driver_class", "com.mysql.jdbc.Driver");
            properties.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");

            entityManagerFactory = Persistence.createEntityManagerFactory("default", properties);
        }

        return entityManagerFactory;
    }

    private EntityManager servletEntityManager;

    private EntityManager getServletEntityManager() {
        if (servletEntityManager == null) {
            servletEntityManager = entityManagerFactory.createEntityManager();
        }
        return servletEntityManager;
    }

    private EntityManager getSessionEntityManager() {
        HttpSession session = getThreadLocalRequest().getSession();
        EntityManager entityManager = (EntityManager) session.getAttribute(ATTRIBUTE_ENTITYMANAGER);
        if (entityManager == null) {
            entityManager = entityManagerFactory.createEntityManager();
            session.setAttribute(ATTRIBUTE_ENTITYMANAGER, entityManager);
        }
        return entityManager;
    }

    private User getSessionUser() {
        HttpSession session = getThreadLocalRequest().getSession();
        User user = (User) session.getAttribute(ATTRIBUTE_USER);
        if (user == null) {
            throw new IllegalStateException();
        }
        return user;
    }

    private void setSessionUser(User user) {
        HttpSession session = getThreadLocalRequest().getSession();
        if (user != null) {
            session.setAttribute(ATTRIBUTE_USER, user);
        } else {
            session.removeAttribute(ATTRIBUTE_USER);
        }
    }

    private Device getSessionDevice() {
        HttpSession session = getThreadLocalRequest().getSession();
        Device device = (Device) session.getAttribute(ATTRIBUTE_DEVICE);
        if (device == null) {
            throw new IllegalStateException();
        }
        return device;
    }

    private void setSessionDevice(Device device) {
        HttpSession session = getThreadLocalRequest().getSession();
        if (device != null) {
            session.setAttribute(ATTRIBUTE_DEVICE, device);
        } else {
            session.removeAttribute(ATTRIBUTE_DEVICE);
        }
    }

    @Override
    public User authenticated() throws IllegalStateException {
        return getSessionUser();
    }

    @Override
    public User login(String login, String password) {
        EntityManager entityManager = getSessionEntityManager();
        synchronized (entityManager) {
            TypedQuery<User> query = entityManager.createQuery(
                    "SELECT x FROM User x WHERE x.login = :login", User.class);
            query.setParameter("login", login);
            List<User> results = query.getResultList();

            if (!results.isEmpty() && password.equals(results.get(0).getPassword())) {
                User user = results.get(0);
                setSessionUser(user);
                return user;
            }
            throw new IllegalStateException();
        }
    }

    @Override
    public Device deviceLogin(String login, String password) {
        EntityManager entityManager = getSessionEntityManager();
        synchronized (entityManager) {
            TypedQuery<Device> query = entityManager.createQuery(
                    "SELECT x FROM Device x WHERE x.name = :name", Device.class);
            query.setParameter("name", login);
            List<Device> results = query.getResultList();

            if (!results.isEmpty()) {
                Device device = results.get(0);
                if (!device.getUniqueId().equals(password))
                    throw new IllegalStateException();

                setSessionDevice(device);
                return device;
            }
            throw new IllegalStateException();
        }
    }

    @Override
    public boolean logout() {
        setSessionUser(null);
        return true;
    }

    @Override
    public User register(String login, String password) {
        if (getApplicationSettings().getRegistrationEnabled()) {
            EntityManager entityManager = getSessionEntityManager();
            synchronized (entityManager) {

                TypedQuery<User> query = entityManager.createQuery(
                        "SELECT x FROM User x WHERE x.login = :login", User.class);
                query.setParameter("login", login);
                List<User> results = query.getResultList();
                if (results.isEmpty()) {
                    User user = new User();
                    user.setLogin(login);
                    user.setPassword(password);
                    createUser(getSessionEntityManager(), user);
                    setSessionUser(user);
                    return user;
                } else {
                    throw new IllegalStateException();
                }
            }
        } else {
            throw new SecurityException();
        }
    }

    @Override
    public List<User> getUsers() {
        EntityManager entityManager = getSessionEntityManager();
        synchronized (entityManager) {
            List<User> users = new LinkedList<User>();
            users.addAll(entityManager.createQuery("SELECT x FROM User x", User.class).getResultList());
            return users;
        }
    }

    @Override
    public User addUser(User user) {
        User currentUser = getSessionUser();
        if (user.getLogin().isEmpty() || user.getPassword().isEmpty()) {
            throw new IllegalArgumentException();
        }
        if (currentUser.getAdmin()) {
            EntityManager entityManager = getSessionEntityManager();
            synchronized (entityManager) {

                String login = user.getLogin();
                TypedQuery<User> query = entityManager.createQuery("SELECT x FROM User x WHERE x.login = :login", User.class);
                query.setParameter("login", login);
                List<User> results = query.getResultList();

                if (results.isEmpty()) {
                    entityManager.getTransaction().begin();
                    try {
                        entityManager.persist(user);
                        entityManager.getTransaction().commit();
                        return user;
                    } catch (RuntimeException e) {
                        entityManager.getTransaction().rollback();
                        throw e;
                    }
                } else {
                    throw new IllegalStateException();
                }
            }
        } else {
            throw new SecurityException();
        }
    }

    @Override
    public User updateUser(User user) {
        User currentUser = getSessionUser();
        if (user.getLogin().isEmpty() || user.getPassword().isEmpty()) {
            throw new IllegalArgumentException();
        }
        if (currentUser.getAdmin() || (currentUser.getId() == user.getId() && !user.getAdmin())) {
            EntityManager entityManager = getSessionEntityManager();
            synchronized (entityManager) {
                entityManager.getTransaction().begin();
                try {
                    // TODO: better solution?
                    if (currentUser.getId() == user.getId()) {
                        currentUser.setLogin(user.getLogin());
                        currentUser.setPassword(user.getPassword());
                        currentUser.setUserSettings(user.getUserSettings());
                        currentUser.setAdmin(user.getAdmin());
                        entityManager.merge(currentUser);
                        user = currentUser;
                    } else {
                        // TODO: handle other users
                    }

                    entityManager.getTransaction().commit();
                    setSessionUser(user);
                    return user;
                } catch (RuntimeException e) {
                    entityManager.getTransaction().rollback();
                    throw e;
                }
            }
        } else {
            throw new SecurityException();
        }
    }

    @Override
    public User removeUser(User user) {
        User currentUser = getSessionUser();
        if (currentUser.getAdmin()) {
            EntityManager entityManager = getSessionEntityManager();
            synchronized (entityManager) {
                entityManager.getTransaction().begin();
                try {
                    user = entityManager.merge(user);
                    user.getDevices().clear();
                    entityManager.remove(user);
                    entityManager.getTransaction().commit();
                    return user;
                } catch (RuntimeException e) {
                    entityManager.getTransaction().rollback();
                    throw e;
                }
            }
        } else {
            throw new SecurityException();
        }
    }

    private void createUser(EntityManager entityManager, User user) {
        synchronized (entityManager) {
            entityManager.getTransaction().begin();
            try {
                entityManager.persist(user);
                entityManager.getTransaction().commit();
            } catch (RuntimeException e) {
                entityManager.getTransaction().rollback();
                throw e;
            }
        }
    }

    @Override
    public List<Device> getDevices() {
        List<Device> devices = new LinkedList<Device>();
        User user = getSessionUser();
        devices.addAll(user.getDevices());
        return devices;
    }

    @Override
    public Device addDevice(Device device) {
        EntityManager entityManager = getSessionEntityManager();
        synchronized (entityManager) {
            TypedQuery<Device> query = entityManager.createQuery("SELECT x FROM Device x WHERE x.uniqueId = :id", Device.class);
            query.setParameter("id", device.getUniqueId());
            List<Device> results = query.getResultList();

            User user = getSessionUser();

            if (results.isEmpty()) {
                entityManager.getTransaction().begin();
                try {
                    entityManager.persist(device);
                    user.getDevices().add(device);
                    entityManager.getTransaction().commit();
                    return device;
                } catch (RuntimeException e) {
                    entityManager.getTransaction().rollback();
                    throw e;
                }
            } else {
                throw new IllegalStateException();
            }
        }
    }

    @Override
    public Device updateDevice(Device device) {
        EntityManager entityManager = getSessionEntityManager();
        synchronized (entityManager) {

            TypedQuery<Device> query = entityManager.createQuery("SELECT x FROM Device x WHERE x.uniqueId = :id AND x.id <> :primary_id", Device.class);
            query.setParameter("primary_id", device.getId());
            query.setParameter("id", device.getUniqueId());
            List<Device> results = query.getResultList();

            if (results.isEmpty()) {
                entityManager.getTransaction().begin();
                try {
                    Device tmp_device = entityManager.find(Device.class, device.getId());
                    tmp_device.setName(device.getName());
                    tmp_device.setUniqueId(device.getUniqueId());
                    entityManager.getTransaction().commit();
                    return tmp_device;
                } catch (RuntimeException e) {
                    entityManager.getTransaction().rollback();
                    throw e;
                }
            } else {
                throw new IllegalStateException();
            }
        }
    }

    @Override
    public Device removeDevice(Device device) {
        EntityManager entityManager = getSessionEntityManager();
        synchronized (entityManager) {
            User user = getSessionUser();
            entityManager.getTransaction().begin();
            try {
                device = entityManager.merge(device);
                user.getDevices().remove(device);
                device.setLatestPosition(null);
                entityManager.flush();
                Query query = entityManager.createQuery("DELETE FROM Position x WHERE x.device = :device");
                query.setParameter("device", device);
                query.executeUpdate();
                entityManager.remove(device);
                entityManager.getTransaction().commit();
                return device;
            } catch (RuntimeException e) {
                entityManager.getTransaction().rollback();
                throw e;
            }
        }
    }

    @Override
    public List<Position> getPositions(Device device, Date from, Date to) {
        EntityManager entityManager = getSessionEntityManager();
        synchronized (entityManager) {
            List<Position> positions = new LinkedList<Position>();
            TypedQuery<Position> query = entityManager.createQuery(
                    "SELECT x FROM Position x WHERE x.device = :device AND x.time BETWEEN :from AND :to", Position.class);
            query.setParameter("device", device);
            query.setParameter("from", from);
            query.setParameter("to", to);
            positions.addAll(query.getResultList());
            return positions;
        }
    }

    @Override
    public List<Position> getLatestPositions() {
        EntityManager entityManager = getSessionEntityManager();
        synchronized (entityManager) {
            List<Position> positions = new LinkedList<Position>();
            User user = getSessionUser();
            if (user.getDevices() != null && !user.getDevices().isEmpty()) {
                TypedQuery<Position> query = entityManager.createQuery(
                        "SELECT x FROM Position x WHERE x.id IN (" +
                                "SELECT y.latestPosition FROM Device y WHERE y IN (:devices))", Position.class);
                query.setParameter("devices", user.getDevices());
                positions.addAll(query.getResultList());
            }
            return positions;
        }
    }

    private ApplicationSettings getApplicationSettings() {
        if (applicationSettings == null) {
            EntityManager entityManager = getServletEntityManager();
            synchronized (entityManager) {
                TypedQuery<ApplicationSettings> query = entityManager.createQuery("SELECT x FROM ApplicationSettings x", ApplicationSettings.class);
                List<ApplicationSettings> resultList = query.getResultList();
                if (resultList == null || resultList.isEmpty()) {
                    applicationSettings = new ApplicationSettings();
                    entityManager.getTransaction().begin();
                    try {
                        entityManager.persist(applicationSettings);
                        entityManager.getTransaction().commit();
                    } catch (RuntimeException e) {
                        entityManager.getTransaction().rollback();
                        throw e;
                    }
                } else {
                    applicationSettings = resultList.get(0);
                }
            }
        }
        return applicationSettings;
    }

    @Override
    public ApplicationSettings updateApplicationSettings(ApplicationSettings applicationSettings) {
        if (applicationSettings == null) {
            return getApplicationSettings();
        } else {
            EntityManager entityManager = getServletEntityManager();
            synchronized (entityManager) {
                User user = getSessionUser();
                if (user.getAdmin()) {
                    entityManager.getTransaction().begin();
                    try {
                        entityManager.merge(applicationSettings);
                        entityManager.getTransaction().commit();
                        this.applicationSettings = applicationSettings;
                        return applicationSettings;
                    } catch (RuntimeException e) {
                        entityManager.getTransaction().rollback();
                        throw e;
                    }
                } else {
                    throw new SecurityException();
                }
            }
        }
    }

    @Override
    public GameInfo getGameInfo() {
        if (GAME != null)
            return GAME.getGameInfo();
        else
            return null;
    }

    @Override
    public TeamReport getTeamReport() {
        User user = getSessionUser();
        TeamReport report = new TeamReport();

        Integer team = Game.teamIdByName(user.getLogin());
        if (team == null)
            return report;

        report.setOwnLink(GAME.getTeamlink(team));
        report.setOtherTeamHasLink(GAME.isOtherTeamHasLink(team));
        report.setAttackPoints(GAME.getAttackPoints());

        return report;
    }

    @Override
    public Boolean moveDevice(Device device, double offsetX, double offsetY) {
        // TODO: disable on play
        if (device == null)
            return false;

        if (!getSessionUser().getAdmin())
            return false;

        EntityManager entityManager = getSessionEntityManager();
        synchronized (entityManager) {
            entityManager.getTransaction().begin();
            try {
                device.setActive(true);
                TypedQuery<Position> query = entityManager.createQuery(
                        "SELECT x FROM Position x WHERE x.id IN (" +
                                "SELECT y.latestPosition FROM Device y WHERE y = :device)", Position.class);
                query.setParameter("device", device);

                Position position = new Position(query.getSingleResult());
                position.setId(null);
                position.setLatitude(position.getLatitude() + offsetX);
                position.setLongitude(position.getLongitude() + offsetY);
                position.setTime(new Date());
                entityManager.persist(position);

                device.setLatestPosition(position);
                entityManager.merge(device);

                entityManager.getTransaction().commit();

                return true;
            } catch (RuntimeException e) {
                entityManager.getTransaction().rollback();
                throw e;
            }
        }
    }

    @Override
    public DeviceReport getDeviceReport() {
        EntityManager entityManager = getSessionEntityManager();
        synchronized (entityManager) {
            Device device = getSessionDevice();
            entityManager.refresh(device);
            return GAME.deviceReport(device);
        }
    }

    @Override
    public Boolean attack(double lat, double lon) {
        return GAME.attack(lat, lon, Game.teamIdByName(getSessionUser().getLogin()));
    }

    @Override
    public String getChatUrl() {
        return GAME.getChatUrl(Game.teamIdByName(getSessionUser().getLogin()));
    }
}
