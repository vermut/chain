package lv.vermut.model;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "user_settings")
public class UserSettings implements Serializable {

    private static final long serialVersionUID = 1;

    @Id
    @GeneratedValue
    private long id;

    public UserSettings() {
        speedUnit = SpeedUnit.knots;
    }

    public enum SpeedUnit {
        knots,
        kilometersPerHour,
        milesPerHour
    }

    @Enumerated(EnumType.STRING)
    private SpeedUnit speedUnit;

    public void setSpeedUnit(SpeedUnit speedUnit) {
        this.speedUnit = speedUnit;
    }

    public SpeedUnit getSpeedUnit() {
        return speedUnit;
    }

}
