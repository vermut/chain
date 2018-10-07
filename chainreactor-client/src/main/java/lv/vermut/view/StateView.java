package lv.vermut.view;

import java.util.LinkedList;
import java.util.List;

import lv.vermut.model.StateItem;
import lv.vermut.model.StateItemProperties;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;

public class StateView {

    private static StateViewUiBinder uiBinder = GWT.create(StateViewUiBinder.class);

    interface StateViewUiBinder extends UiBinder<Widget, StateView> {
    }

    @UiField
    ContentPanel contentPanel;

    public ContentPanel getView() {
        return contentPanel;
    }

    @UiField(provided = true)
    ColumnModel<StateItem> columnModel;

    @UiField(provided = true)
    ListStore<StateItem> stateStore;

    @UiField
    Grid<StateItem> grid;

    public StateView(ListStore<StateItem> stateStore) {
        this.stateStore = stateStore;

        StateItemProperties stateItemProperties = GWT.create(StateItemProperties.class);

        List<ColumnConfig<StateItem, ?>> columnConfigList = new LinkedList<>();
        columnConfigList.add(new ColumnConfig<>(stateItemProperties.name(), 0, "Attribute"));
        columnConfigList.add(new ColumnConfig<>(stateItemProperties.value(), 0, "Value"));
        columnModel = new ColumnModel<>(columnConfigList);

        uiBinder.createAndBindUi(this);

        grid.getSelectionModel().setLocked(true);
    }

}

