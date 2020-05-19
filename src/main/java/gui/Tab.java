package main.java.gui;

import javax.swing.*;

public class Tab extends JMenu {

    public Tab(String name, int key, String description){
        super(name);
        this.setMnemonic(key);
        this.getAccessibleContext().setAccessibleDescription(description);
    }

}
