package fr.ralmn.wakemeup.object;

/**
 * Created by ralmn on 20/09/15.
 */
public class AndroidCalendar {

    private int id;
    private String name;
    private String color;

    public AndroidCalendar(int id, String name, String color) {
        this.color = color;
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }
}
