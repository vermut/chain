package org.traccar.web.client;

import org.traccar.web.shared.model.ApplicationSettings;
import org.traccar.web.shared.model.Device;
import org.traccar.web.shared.model.User;
import org.traccar.web.shared.model.UserSettings;

public class ApplicationContext {

    private static final ApplicationContext context = new ApplicationContext();
    private FormatterUtil formatterUtil;
    private ApplicationSettings applicationSettings;
    private User user;
    private Device device;

    public static ApplicationContext getInstance() {
        return context;
    }

    public FormatterUtil getFormatterUtil() {
        if (formatterUtil == null) {
            formatterUtil = new FormatterUtil();
        }
        return formatterUtil;
    }

    public void setFormatterUtil(FormatterUtil formatterUtil) {
        this.formatterUtil = formatterUtil;
    }

    public ApplicationSettings getApplicationSettings() {
        if (applicationSettings != null) {
            return applicationSettings;
        } else {
            return new ApplicationSettings(); // default settings
        }
    }

    public void setApplicationSettings(ApplicationSettings applicationSettings) {
        this.applicationSettings = applicationSettings;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public UserSettings getUserSettings() {
        if (user != null && user.getUserSettings() != null) {
            return user.getUserSettings();
        } else {
            return new UserSettings(); // default settings
        }
    }

    public void setUserSettings(UserSettings userSettings) {
        if (user != null) {
            user.setUserSettings(userSettings);
        }
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }
}
