package org.traccar.web.shared.model

import com.google.gwt.user.client.rpc.GwtTransient
import com.vividsolutions.jts.geom.Coordinate
import groovy.transform.TupleConstructor

/**
 * Created by admin on 03/04/15.
 */
class SimplePoint implements Cloneable, Serializable {
    private static final long serialVersionUID = 6683108902428366910L

    public double x
    public double y

    SimplePoint(Coordinate c) {
        x = c.x
        y = c.y
    }
}
