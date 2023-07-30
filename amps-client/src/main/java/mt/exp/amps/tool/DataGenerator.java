package mt.exp.amps.tool;

import java.util.Iterator;

public class DataGenerator implements Iterator<String> {
    @Override
    public boolean hasNext() {
        return true;
    }

    @Override
    public String next() {
        return "Msg# " + System.nanoTime();
    }
}
