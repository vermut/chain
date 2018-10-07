package lv.vermut.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "application_settings")
public class ApplicationSettings implements Serializable {

    private static final long serialVersionUID = 1;

    @Id
    @GeneratedValue
    private long id;

    public ApplicationSettings() {
        registrationEnabled = true;
    }

    private boolean registrationEnabled;

    public void setRegistrationEnabled(boolean registrationEnabled) {
        this.registrationEnabled = registrationEnabled;
    }

    public boolean getRegistrationEnabled() {
        return registrationEnabled;
    }

}
