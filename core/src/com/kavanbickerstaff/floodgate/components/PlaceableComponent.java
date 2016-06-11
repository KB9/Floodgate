package com.kavanbickerstaff.floodgate.components;

import com.badlogic.ashley.core.Component;

public class PlaceableComponent implements Component {

    public boolean store;

    public PlaceableComponent(boolean store) {
        this.store = store;
    }
}
