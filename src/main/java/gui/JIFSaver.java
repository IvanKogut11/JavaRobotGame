package main.java.gui;

import java.awt.*;
import java.io.*;
import java.nio.file.Paths;
import java.util.HashMap;

public class JIFSaver implements Serializable{

    private static final long serialVersionUID = 1000000007L;

    private String name;
    private Point location;
    private boolean isIcon;
    private Dimension size;

    public JIFSaver() {}

    public JIFSaver(String name, Point p, boolean isIcon, Dimension size) {
        this.name = name;
        this.location = p;
        this.isIcon = isIcon;
        this.size = size;
    }

    @Override
    public String toString() {
        return String.format("Name='%s', x=%d, y=%d", name, location.x, location.y);
    }


    public static void saveAll(JIFSaver[] jifSavers) {
        String filePath = getCurrentPath();
        int count = jifSavers.length;
        try {
            FileOutputStream outputStream = new FileOutputStream(filePath);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeInt(count);
            for (JIFSaver jifSaver : jifSavers)
                objectOutputStream.writeObject(jifSaver);
            objectOutputStream.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static HashMap<String, JIFSaver> loadAll() {
        HashMap<String, JIFSaver> downloaded = new HashMap<>();
        String filePath = getCurrentPath();
        try {
            FileInputStream inputStream = new FileInputStream(filePath);
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            int cnt = objectInputStream.readInt();
            for (int i = 0; i < cnt; i++) {
                JIFSaver obj = (JIFSaver) objectInputStream.readObject();
                downloaded.put(obj.getName(), obj);
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return downloaded;
    }

    public String getName() {
        return name;
    }

    public Point getLocation() {
        return location;
    }

    public boolean getIsIcon() {
        return isIcon;
    }

    public Dimension getSize() {
        return size;
    }

    private static String getCurrentPath() {
        String workingDir = System.getProperty("user.dir");
        return Paths.get(workingDir, "savedJIFs.bin").toString();
    }

}
