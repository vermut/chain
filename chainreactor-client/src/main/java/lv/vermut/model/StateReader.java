package lv.vermut.model;

import lv.vermut.ApplicationContext;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class StateReader {

    private static String toString(Object object) {
        if (object != null) {
            return object.toString();
        }
        return null;
    }

    public static List<StateItem> getState(Position position) {
        List<StateItem> state = new LinkedList<>();

        state.add(new StateItem("login", toString(position.getDevice().getName())));
        state.add(new StateItem("alive", position.getDevice().isActive() ? "alive" : "dead"));
        state.add(new StateItem("age", toString(((int) (new Date().getTime() - position.getTime().getTime()) / 1000))));
        state.add(new StateItem("latitude", toString(position.getLatitude())));
        state.add(new StateItem("longitude", toString(position.getLongitude())));
        state.add(new StateItem("time", ApplicationContext.getInstance().getFormatterUtil().getTimeFormat().format(position.getTime())));
/*
        state.add(new StateItem("altitude", toString(position.getAltitude())));
        state.add(new StateItem("speed", ApplicationContext.getInstance().getFormatterUtil().getSpeedFormat().format(position.getSpeed())));
        state.add(new StateItem("course", toString(position.getCourse())));
        state.add(new StateItem("power", toString(position.getPower())));
        state.add(new StateItem("address", position.getAddress()));
        state.add(new StateItem("valid", toString(position.getValid())));


        String other = position.getOther();
        if (other != null) {
            try {
                NodeList nodes = XMLParser.parse(other).getFirstChild().getChildNodes();
                for (int i = 0; i < nodes.getLength(); i++) {
                    Node node = nodes.item(i);
                    state.add(new StateItem(node.getNodeName(), node.getFirstChild().getNodeValue()));
                }
            } catch (Exception error) {
            }
        }*/

        return state;
    }

}
