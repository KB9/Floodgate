package com.kavanbickerstaff.floodgate.components;

import com.badlogic.ashley.core.Component;

public class PlacementComponent implements Component {

    public float worldX;
    public float worldY;
    public boolean isHeld;
    public boolean hasInvalidPosition;
}
