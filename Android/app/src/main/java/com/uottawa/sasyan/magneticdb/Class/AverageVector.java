package com.uottawa.sasyan.magneticdb.Class;

/**
 * Created by valentin on 30/06/2015.
 */

import java.util.ArrayList;
import java.util.List;

public class AverageVector {
    private int size;
    List<Vector> vectors;

    public AverageVector(int size) {
        this.size = size;
        this.vectors = new ArrayList<Vector>();
    }

    public void setSize(int size) {
        this.size = size;
        while (this.vectors.size() > this.size) {
            this.vectors.remove(0);
        }
    }

    public Vector getAverage() {
        Vector vector = new Vector(0,0,0);
        vector.beMeanOf(this.vectors);
        return vector;
    }

    public void addVector(Vector vector) {
        this.vectors.add(vector);
        if (this.vectors.size() > this.size) {
            this.vectors.remove(0);
        }
    }
}

