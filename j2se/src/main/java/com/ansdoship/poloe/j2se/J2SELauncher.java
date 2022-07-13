package com.ansdoship.poloe.j2se;

import com.ansdoship.poloe.Poloe;

public class J2SELauncher {

    public static void main(String[] args) {
        Poloe.setDelegate(new PoloeFrame());
        Poloe.initialize();
    }

}
