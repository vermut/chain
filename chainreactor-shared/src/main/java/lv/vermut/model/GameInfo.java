package lv.vermut.model;

import java.io.Serializable;

/**
 * Created by admin on 03/04/15.
 */
public class GameInfo implements Serializable, Cloneable {
    public SimplePoint getTopLeft() {
        return topLeft;
    }

    public void setTopLeft(SimplePoint topLeft) {
        this.topLeft = topLeft;
    }

    public SimplePoint getBottomLeft() {
        return bottomLeft;
    }

    public void setBottomLeft(SimplePoint bottomLeft) {
        this.bottomLeft = bottomLeft;
    }

    public SimplePoint getBottomRight() {
        return bottomRight;
    }

    public void setBottomRight(SimplePoint bottomRight) {
        this.bottomRight = bottomRight;
    }

    public SimplePoint getTopRight() {
        return topRight;
    }

    public void setTopRight(SimplePoint topRight) {
        this.topRight = topRight;
    }

    public int[] getScore() {
        return score;
    }

    public void setScore(int[] score) {
        this.score = score;
    }

    private static final long serialVersionUID = 1;
    private SimplePoint topLeft;
    private SimplePoint bottomLeft;
    private SimplePoint bottomRight;
    private SimplePoint topRight;
    int[] score;
}
