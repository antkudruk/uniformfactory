public class WriteToIntField {
    private int number;

    public void setNumber(Integer e) {
        e.byteValue();
        number = e.intValue();
    }

    public void setFromObject(Object o) {
        number = (int)o;
    }

    public void setFromObjectWrapper(Object o) {
        number = (Integer) o;
    }
}
