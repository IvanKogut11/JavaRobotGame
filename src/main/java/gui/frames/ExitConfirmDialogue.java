package main.java.gui.frames;

import javax.swing.*;
import java.awt.Component;

public abstract class ExitConfirmDialogue extends JOptionPane { //Changed

    public static void closeWindow(JFrame obj) {
        int res = getAnswer(obj);
        if (res == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    public static void closeJIF(JInternalFrame obj) {
        int result = getAnswer(obj);
        if (result == JOptionPane.YES_OPTION)
            obj.dispose();
    }

    private static int getAnswer(Component obj) {
        return JOptionPane.showConfirmDialog(obj, "Уверены, что хотите закрыть окно?",
                "Выход", JOptionPane.YES_NO_OPTION);
    }

}
