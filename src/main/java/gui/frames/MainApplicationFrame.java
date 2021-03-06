package main.java.gui.frames;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyVetoException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.*;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import main.java.gui.JIFSaver;
import main.java.log.Logger;

public class MainApplicationFrame extends JFrame {
    private final JDesktopPane desktopPane = new JDesktopPane();
    private HashMap<String, JInternalFrame> allFrames = new HashMap<>();

    public MainApplicationFrame() {
        int inset = 50;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(inset, inset,
                screenSize.width - inset * 2,
                screenSize.height - inset * 2);

        setContentPane(desktopPane);
        setVisible(true);

        LogWindow logWindow = createLogWindow();
        allFrames.put(logWindow.getTitle(), logWindow);

        GameWindow gameWindow = createGameWindow();
        allFrames.put(gameWindow.getTitle(), gameWindow);

        setJMenuBar(generateMenuBar());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        downloadWindows();

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveWindows();
                ExitConfirmDialogue.closeWindow(MainApplicationFrame.this);
            }
        });
    }

    protected GameWindow createGameWindow() {
        GameWindow gameWindow = new GameWindow();
        gameWindow.setLocation(310, 10);
        gameWindow.setSize(400, 400);
        return gameWindow;
    }

    protected LogWindow createLogWindow() {
        LogWindow logWindow = new LogWindow(Logger.getDefaultLogSource());
        logWindow.setLocation(10, 10);
        logWindow.setSize(300, 800);
        setMinimumSize(logWindow.getSize());
        logWindow.pack();
        Logger.debug("Протокол работает");
        return logWindow;
    }

    protected void addWindow(JInternalFrame internalFrame) {
        desktopPane.add(internalFrame);
        internalFrame.setVisible(true);
        internalFrame.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        internalFrame.addInternalFrameListener(new InternalFrameAdapter() {
            @Override
            public void internalFrameClosing(InternalFrameEvent e) {
                ExitConfirmDialogue.closeJIF(internalFrame);
            }
        });
    }

    private ActionListener getActionListenerSettingExactLookAndFeel(String className) {
        return (event) -> {
            setLookAndFeel(className);
            this.invalidate();
        };
    }

    private JMenuBar generateMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu lookAndFeelMenu = new Tab("Режим отображения", KeyEvent.VK_V, "Управление режимом отображения");
        lookAndFeelMenu.add(new TabItem("Cистемная схема", KeyEvent.VK_S,
                getActionListenerSettingExactLookAndFeel(UIManager.getSystemLookAndFeelClassName())));
        lookAndFeelMenu.add(new TabItem("Универсальная схема", KeyEvent.VK_S,
                getActionListenerSettingExactLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName())));
        lookAndFeelMenu.add(new TabItem("Nimbus схема", KeyEvent.VK_S,
                getActionListenerSettingExactLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel")));

        JMenu testMenu = new Tab("Тесты", KeyEvent.VK_T, "Тестовые команды.");
        testMenu.add(new TabItem("Сообщение в лог", KeyEvent.VK_S,
                (event) -> Logger.debug("Новая строка")));

        JMenu mainMenu = new Tab("Программа", KeyEvent.VK_P, "Программное меню");
        mainMenu.add(new TabItem("Выход", KeyEvent.VK_ESCAPE,
                (event) -> {
                    saveWindows();
                    ExitConfirmDialogue.closeWindow(MainApplicationFrame.this);
                }));
        menuBar.add(mainMenu);
        menuBar.add(lookAndFeelMenu);
        menuBar.add(testMenu);
        return menuBar;
    }

    private void setLookAndFeel(String className) {
        try {
            UIManager.setLookAndFeel(className);
            SwingUtilities.updateComponentTreeUI(this);
        } catch (ClassNotFoundException | InstantiationException
                | IllegalAccessException | UnsupportedLookAndFeelException ignored) {
        }
    }

    private void saveWindows() { //jif - JInternalFrame
        JInternalFrame[] jifs = desktopPane.getAllFrames();
        JIFSaver[] jifSavers = new JIFSaver[jifs.length];
        int len = jifs.length;
        for (int i = 0; i < len; i++) {
            JIFSaver jif = new JIFSaver(jifs[i].getTitle(), jifs[i].getLocation(),
                    jifs[i].isIcon(), jifs[i].getSize());
            jifSavers[i] = jif;
        }
        JIFSaver.saveAll(jifSavers);
    }

    private void downloadWindows() { //jif - JInternalFrame
        HashMap<String, JIFSaver> loaded = JIFSaver.loadAll();
        for (Map.Entry<String, JInternalFrame> jifEntry : allFrames.entrySet()) {
            String jifName = jifEntry.getKey();
            JInternalFrame jif = jifEntry.getValue();
            JIFSaver loadedJIF = loaded.get(jifName);
            addWindow(jif);
            if (loadedJIF == null){
                System.out.println(jifName + " is restored with default settings");
                continue;
            }
            try {
                jif.setSize(loadedJIF.getSize());
                jif.setLocation(loadedJIF.getLocation());
                jif.setIcon(loadedJIF.getIsIcon());
            } catch (PropertyVetoException e) { //What is it?
                System.out.println(e.getMessage());
            }
        }
    }

}
