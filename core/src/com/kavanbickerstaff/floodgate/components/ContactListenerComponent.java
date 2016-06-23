package com.kavanbickerstaff.floodgate.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;

public class ContactListenerComponent implements Component {

    public Array<Fixture> fixtureContactsBegun;
    public Array<Fixture> fixtureContactsEnded;
    public IntArray particleContactsBegun;
    public IntArray particleContactsEnded;
    public int totalFixtureContacts;
    public int totalParticleContacts;
}
