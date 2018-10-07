package lv.vermut.model;

import java.io.Serializable;

/**
 * Created by admin on 19/04/15.
 */
public class TeamReport implements Serializable, Cloneable {
    SimplePoint[] ownLink;
    Boolean otherTeamHasLink;
    SimplePoint[] attackPoints;

    public SimplePoint[] getOwnLink() {
        return ownLink;
    }

    public void setOwnLink(SimplePoint[] ownLink) {
        this.ownLink = ownLink;
    }

    public Boolean isOtherTeamHasLink() {
        return otherTeamHasLink;
    }

    public void setOtherTeamHasLink(Boolean otherTeamHasLink) {
        this.otherTeamHasLink = otherTeamHasLink;
    }

    public SimplePoint[] getAttackPoints() {
        return attackPoints;
    }

    public void setAttackPoints(SimplePoint[] attackPoints) {
        this.attackPoints = attackPoints;
    }
}
