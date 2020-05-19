package main.java.gui;

import javax.swing.*;
import java.awt.event.ActionListener;

public class TabItem extends JMenuItem {

    public TabItem(String itemName, int eventNumber, ActionListener e) {
        super(itemName, eventNumber);
        this.addActionListener(e);
    }

}
