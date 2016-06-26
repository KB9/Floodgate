package com.kavanbickerstaff.floodgate.components;

import com.badlogic.ashley.core.Component;

public class LiquidSpawnComponent implements Component {

    public long spawnTimeMillis;
    public boolean spawnOnNextUpdate;
    public boolean on;
}
