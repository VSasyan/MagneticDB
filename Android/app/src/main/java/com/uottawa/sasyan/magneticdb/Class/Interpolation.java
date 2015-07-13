/**
 * Created by valentin on 03/06/2015.
 */
package com.uottawa.sasyan.magneticdb.Class;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.heatmaps.WeightedLatLng;

import org.la4j.Matrix;
import org.la4j.linear.LeastSquaresSolver;

import java.util.ArrayList;
import java.util.List;

public class Interpolation {
    // Attributes :
    private List<Vector> list;
    private LatLngBounds listBounds;
    private LatLngBounds showBounds;

    // Utilitaires :
    private int nbPointsLat, nbPointsLon;
    private float[] pointLat, pointLon;
    private double[][] pointZ;
    private int type; // -1 : raw, 0 : nn, 1 : lin, 2 : inv, 3 : spl

    public Interpolation(List<Vector> list) {
        setList(list);
    }

    public boolean interpolate() {
        boolean r;
        switch (this.type) {
            case -1:
                r = true;
                break;
            case 0:
                r = this.interp_nn();
                break;
            case 1:
                r = this.interp_lin();
                break;
            case 2:
                r = this.interp_inv(2);
                break;
            case 3:
                r = this.interp_spl(1000);
                break;
            default:
                r = false;
                break;
        }
        return r;
    }

    public boolean interpolate(int para) {
        boolean r;
        switch (this.type) {
            case 2:
                r = this.interp_inv(para);
                break;
            case 3:
                r = this.interp_spl(para);
                break;
            default:
                r = this.interpolate();
                break;
        }
        return r;
    }

    /**************************************************************/
    /*** INTERPOLATION FUNCTIONS **********************************/
    /**************************************************************/

    // Nearest-neighbor interpolation:
    private boolean interp_nn() {
        int N = this.list.size();
        double temp;

        // Calculate zi(i,j) = z(plus pres):
        for (int i = 0; i < this.nbPointsLat; i++) {
            for (int j = 0; j < this.nbPointsLon; j++) {
                double distance = distance(this.pointLat[i], this.pointLon[j], this.list.get(0).getX(), this.list.get(0).getY());
                int indice = 0;
                for (int n = 1; n < N; n++) {
                    temp = distance(this.pointLat[i], this.pointLon[j], this.list.get(n).getX(), this.list.get(n).getY());
                    if (temp < distance) {
                        indice = n;
                        distance = temp;
                    }
                }
                this.pointZ[i][j] = this.list.get(indice).getZ();
            }
        }

        return true;
    }

    // Linear interpolation:
    private boolean interp_lin() {
        int N = this.list.size();
        double temp,distance1, distance2;
        int indice1, indice2;

        // Calculate zi(i,j) = Moyenne ponderee 2 z(plus pres):
        for (int i = 0; i < this.nbPointsLat; i++) {
            for (int j = 0; j < this.nbPointsLon; j++) {
                // Initialisation:
                distance1 = distance(this.pointLat[i], this.pointLon[j], this.list.get(0).getX(), this.list.get(0).getY());
                distance2 = distance(this.pointLat[i], this.pointLon[j], this.list.get(1).getX(), this.list.get(1).getY());
                if (distance1 > distance2) {
                    temp = distance1;
                    distance1 = distance2;
                    distance2 = temp;
                    indice2 = 0;
                    indice1 = 1;
                } else {
                    indice1 = 0;
                    indice2 = 1;
                }
                // Go: distance1 < distance 2
                for (int n = 2; n < N; n++) {
                    temp = distance(this.pointLat[i], this.pointLon[j], this.list.get(n).getX(), this.list.get(n).getY());
                    if (temp < distance1) {
                        indice2 = indice1;
                        distance2 = distance1;
                        distance1 = temp;
                        indice1 = n;
                    } else if (temp < indice2) {
                        indice2 = n;
                        distance2 = temp;
                    }
                }
                this.pointZ[i][j] = (this.list.get(indice1).getZ()/distance1 + this.list.get(indice2).getZ()/distance2) / (1/distance1+1/distance2);
            }
        }
        return true;
    }

    // Interpolation 1/dist^p:
    private boolean interp_inv(int p) {
        int N = this.list.size();

        // Calculate zi(i,j) :
        for (int i = 0; i < this.nbPointsLat; i++) {
            for (int j = 0; j < this.nbPointsLon; j++) {
                // Initialisation:
                double SN = 0, SD = 0; // numerator and denumerator sum
                for (int n = 0; n < N; n++) {
                    double D = Math.pow(distance(this.pointLat[i], this.list.get(n).getX(), this.pointLon[i], this.list.get(n).getY()), p);
                    SN += this.list.get(n).getZ()/D;
                    SD += 1/D;
                }
                this.pointZ[i][j] = SN/SD;
                //Log.e("pointZ", String.valueOf(this.pointZ[i][j]));
            }
        }
        return true;
    }

    // Spline of interpolation (rho == 0) / of lissage (rho != 0)
    private boolean interp_spl(int rho) {
        int I;

        // Number of measure:
        int N = this.list.size();

        // Construction of A:
        double[][] A = new double[N+3][N+3];
        for (int i = 0; i < N+3; i++) {
            for (int j = 0; j < N+3; j++) {
                if (i < N) {
                    if (j == 0) {
                        A[i][j] = 1;
                    } else if (j == 1) {
                        A[i][j] = this.list.get(i).getX();
                    } else if (j == 2) {
                        A[i][j] = this.list.get(i).getY();
                    } else if (j > 2) { //             lat du pt i           lon du pt i                  lat du pt j-3                  lon du pt j-3             rho
                        A[i][j] = K(this.list.get(i).getX(), this.list.get(i).getY(), this.list.get(j-3).getY(), this.list.get(j-3).getY(), rho);
                    }
                } else if (i == N) {
                    if (j < 3) {
                        A[i][j] = 0;
                    } else {
                        A[i][j] = 1;
                    }
                } else if (i == N+1) {
                    if (j < 3) {
                        A[i][j] = 0;
                    } else {
                        A[i][j] = this.list.get(j-3).getX();
                    }
                } else if (i == N+2) {
                    if (j < 3) {
                        A[i][j] = 0;
                    } else {
                        A[i][j] = this.list.get(j-3).getY();
                    }
                }
            }
        }

        // Construction of Y:
        double[] Y = new double[N+3];
        for (int i = 0; i < N; i++) {
            Y[i] = this.list.get(i).getZ();
        }
        Y[N] = 0; Y[N+1] = 0; Y[N+2] = 0;

        // Calculate Xs:
        Matrix a = new org.la4j.matrix.dense.Basic2DMatrix(A);
        org.la4j.Vector y = new org.la4j.vector.dense.BasicVector(Y);
        LeastSquaresSolver solver = new LeastSquaresSolver(a);
        org.la4j.Vector Xs = solver.solve(y);

        // Calculate:
        for (int i = 0; i < this.nbPointsLat; i++) {
            for (int j = 0; j < this.nbPointsLon; j++) {
                double sum = 0;
                for (int n = 0; n < N; n++) {
                    sum += Xs.get(3+n)*K(this.pointLat[i], this.pointLon[j], this.list.get(n).getX(), this.list.get(n).getY(), rho);
                }
                this.pointZ[i][j] = Xs.get(0) + Xs.get(1)*this.pointLat[i] + Xs.get(2)*this.pointLon[i] + sum;
            }
        }

        return true;
    }

    /**************************************************************/
    /*** USED FUNCTIONS *******************************************/
    /**************************************************************/

    private double K(double x, double y, double X, double Y, int rho) {
        if ((x == X) && (y == Y)) {
            return rho;
        } else {
            double h = Math.sqrt((x - X) * (x - X) + (y - Y) * (y - Y));
            return h*h + Math.log(h);
        }
    }

    private double distance(double x, double y, double X, double Y) {
        return Math.sqrt((x - X) * (x - X) + (y - Y) * (y - Y));
    }

    public LatLngBounds intersection(LatLngBounds b1, LatLngBounds b2) {
        double ne_lat = Math.min(b1.northeast.latitude, b2.northeast.latitude);
        double ne_lon = Math.min(b1.northeast.longitude, b2.northeast.longitude);
        double sw_lat = Math.max(b1.southwest.latitude, b2.southwest.latitude);
        double sw_lon = Math.max(b1.southwest.longitude, b2.southwest.longitude);
        return new LatLngBounds(new LatLng(Math.min(ne_lat, sw_lat), Math.min(ne_lon, sw_lon)), new LatLng(Math.max(ne_lat, sw_lat), Math.max(ne_lon, sw_lon)));
    }

    public double normalization(double x, double min, double max, int normalize) {
        // Normalization type - 0 : not, 1 : lin
        if (normalize == 1) { // linear
            return (x-min)/(max-min);
        } else { // no normalization
            return x;
        }
    }

    /**************************************************************/
    /*** SETTER ***************************************************/
    /**************************************************************/

    public void setList(List<Vector> list) {
        this.list = list;
        // Calculate bounds :
        this.resetBounds();
    }

    public void setType(int type) {
        this.type = type;
    }

    private void setMesh() {
        double deltaLat, deltaLon;
        // Made a mesh: 31x31 points (sum = 961 points < maximum of 1000)
        this.nbPointsLat = 31;
        this.nbPointsLon = 31;
        deltaLat = Math.abs(this.showBounds.northeast.latitude - this.showBounds.southwest.latitude) / (this.nbPointsLat-1);
        deltaLon = Math.abs(this.showBounds.northeast.longitude - this.showBounds.southwest.longitude) / (this.nbPointsLon-1);
        this.pointLat = new float[nbPointsLat];
        this.pointLon = new float[nbPointsLon];
        this.pointZ = new double[nbPointsLat][nbPointsLon];
        for (int i = 0; i < nbPointsLat; i++) {this.pointLat[i] = (float)(this.showBounds.southwest.latitude + i*deltaLat);}
        for (int i = 0; i < nbPointsLon; i++) {this.pointLon[i] = (float)(this.showBounds.southwest.longitude + i*deltaLon);}
    }

    public void fitToMap(GoogleMap map) {
        LatLngBounds mapBounds = map.getProjection().getVisibleRegion().latLngBounds;
        this.showBounds = intersection(this.listBounds, mapBounds);
        this.setMesh();
    }

    public void resetBounds() {
        LatLng ll;
        this.listBounds = new LatLngBounds.Builder().include(list.get(0).toLatLng()).build();
        // We calculate the bounds :
        for (int i = 1; i < list.size(); i++) {
            ll = new LatLng(list.get(i).getX(), list.get(i).getY());
            if (!this.listBounds.contains(ll)) {
                this.listBounds = this.listBounds.including(ll);
            }
        }
        this.showBounds = this.listBounds;
        this.setMesh();
    }

    /**************************************************************/
    /*** GETTER ***************************************************/
    /**************************************************************/

    public int getType() {
        return this.type;
    }

    public LatLngBounds getBounds() {
        return this.showBounds;
    }

    public List<WeightedLatLng> getDataPoint() {
        return getDataPoint(0);
    }

    public List<WeightedLatLng> getDataPoint(int normalize) {
        List<WeightedLatLng> dataPoint = new ArrayList<WeightedLatLng>();
        if (this.type != -1) { // interpolated data
            // Now Normalization:
            double min = this.pointZ[0][0], max = this.pointZ[0][0];
            if (normalize > 1) {
                // Get min, max :
                for (int i = 0; i < this.nbPointsLat; i++) {
                    for (int j = 0; j < this.nbPointsLon; j++) {
                        min = Math.min(min, this.pointZ[i][j]);
                        max = Math.min(max, this.pointZ[i][j]);
                    }
                }
            }
            for (int i = 0; i < this.nbPointsLat; i++) {
                for (int j = 0; j < this.nbPointsLon; j++) {
                    LatLng ll = new LatLng(this.pointLat[i], this.pointLon[j]);
                    WeightedLatLng wll = new WeightedLatLng(ll, normalization(this.pointZ[i][j], min, max, normalize));
                    dataPoint.add(wll);
                }
            }
        } else { // raw data
            // Now Normalization:
            double min = this.list.get(0).getZ(), max = this.list.get(1).getZ();
            int N = this.list.size();
            if (normalize > 1) {
                // Get min, max :
                for (int n = 0; n < N; n++) {
                    min = Math.min(min, this.list.get(n).getZ());
                    max = Math.min(max, this.list.get(n).getZ());
                }
            }
            for (int n = 0; n < N; n++) {
                    WeightedLatLng wll = new WeightedLatLng(this.list.get(n).toLatLng(), normalization(this.list.get(n).getZ(), min, max, normalize));
                    dataPoint.add(wll);
            }
        }
        return dataPoint;
    }
}
