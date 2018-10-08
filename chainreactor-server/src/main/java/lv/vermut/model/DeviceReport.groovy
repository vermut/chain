package lv.vermut.model;

class DeviceReport implements Serializable {
    static final int NO_LINK = 1;
    static final int HAVE_LINK = 2;
    static final int HAVE_LINK_WITH_YOU = 3;
    static final int HAVE_LINK_AROUND_YOU = 3;

    String teamName;
    String teamConferenceUrl;
    List<String> neighbors;

    int otherTeamLinkStatus = NO_LINK;
    int ownLinkStatus = NO_LINK;
    String score;
    boolean active;
}
