package utils;

public class Log {

    private final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    public Log() {

    }

    public static void print(final String message) {
        Date date = new Date();
        System.out.println(dateFormat.format(date) + message);
    }
}