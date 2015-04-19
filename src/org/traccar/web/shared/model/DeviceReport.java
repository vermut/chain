package org.traccar.web.shared.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by admin on 19/04/15.
 */
public class DeviceReport implements Serializable {
    public static final int NO_LINK = 1;
    public static final int HAVE_LINK = 2;
    public static final int HAVE_LINK_WITH_YOU = 3;
    public static final int HAVE_LINK_AROUND_YOU = 3;

    public String teamName;
    public List<String> neighbors;

    public int otherTeamLinkStatus = NO_LINK;
    public int ownLinkStatus = NO_LINK;
    public String score;
}
