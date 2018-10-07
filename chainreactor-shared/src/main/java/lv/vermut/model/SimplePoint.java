package lv.vermut.model;

import java.io.Serializable;

/**
 * Created by admin on 03/04/15.
 */
public class SimplePoint implements Cloneable, Serializable {
    public SimplePoint(double px, double py) {
        x = px;
        y = py;
    }

    private static final long serialVersionUID = 6683108902428366910L;
    public double x;
    public double y;

    public SimplePoint() {
    }
}
