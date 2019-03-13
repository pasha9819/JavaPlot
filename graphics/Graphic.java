package graphics;

public class Graphic {
    private double[] x;
    private double[] y;
    private String name;
    private String color;
    private Integer width;

    public Graphic(double[] x, double[] y, String name, String color, Integer width) {
        this.x = x;
        this.y = y;
        this.name = name;
        this.color = color;
        this.width = width;
    }

    public Graphic(double[] x, double[] y, String name, String color) {
        this.x = x;
        this.y = y;
        this.name = name;
        this.color = color;
    }

    public Graphic(double[] x, double[] y, String name) {
        checkSize(x, y);
        this.name = name;;
        this.x = x;
        this.y = y;
    }

    private boolean checkSize(double[] x, double[] y){
        return x.length == y.length;
    }

    private boolean checkSize(){
        return checkSize(x, y);
    }

    public double[] getX() {
        return x;
    }

    public double[] getY() {
        return y;
    }

    public String getColor() {
        return color;
    }

    public String getName() {
        return name;
    }

    public Integer getWidth() {
        return width;
    }
}
