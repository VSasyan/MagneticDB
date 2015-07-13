package com.uottawa.sasyan.magneticdb.Class;

/**
 * Created by valentin on 30/06/2015.
 */

import java.util.ArrayList;
import java.util.List;

public class AverageVector {
    private int size;
    private List<Vector> vectors;
    private Vector average;

    public AverageVector(int size) {
        this.size = size;
        this.vectors = new ArrayList<Vector>();
        this.average = new Vector(0,0,0);
    }

    public void setSize(int size) {
        this.size = size;
        while (this.vectors.size() > this.size) {
            this.vectors.remove(0);
        }
        this.calculateAverage();
    }

    public Vector getAverage() {
        return this.average;
    }

    public void addVector(Vector vector) {
        this.vectors.add(vector);
        if (this.vectors.size() > this.size) {
            this.vectors.remove(0);
        }
        this.calculateAverage();
    }

    private void calculateAverage() {
        this.average.beMeanOf(this.vectors);
    }
}

