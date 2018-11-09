package ca.virology.baseByBase.gui.CodeHop;


import java.lang.Math;
import java.lang.String;
import java.util.Vector;


public class Temp {
    private static native void fillEnergyArrays(double currentTemp, double concNA, double concMG);

    private static native double computeFreeEnergy(String seq1, int len1, String seq2, int len2);


    // ===============================================================
    // method: getHyfiTM(String primer, String target, double concPrimer, double concTarget, double concNA, double concMG)
    //
    // concentrations are in units of moles per liter.
    //
    // returns the temperature at which half of species
    // a (primer) would be expected to be bound to species b (target)
    // at thermodynamic equilibrium.
    //
    // note that the energy parameters have to
    // be reset after every call to this function,
    // because they are temperature dependent
    // ===============================================================
    public static double getHyfiTM(String primer, String target, double concPrimer, double concTarget, double concNA, double concMG) {

        int err = 0;
        double dg;
        double currentTemp = 0;

        Double annealedFrac;

        double t1start = -10.0;
        double t2start = 100.0;
        double t1 = t1start;
        double t2 = t2start;
        double tol = 0.05; //numerical tolerance for difference fraction of melted strands from 0.5
        double targetVal = 0.5;


        while (t1 < t2) {
            currentTemp = (t1 + t2) / 2.0;
            fillEnergyArrays(currentTemp, concNA, concMG); // temp, Na conc, Mg conc, max loop
            dg = computeFreeEnergy(target, target.length(), primer, primer.length());
            annealedFrac = fracBound(dg, currentTemp, concTarget, concPrimer);

            if (Math.abs(annealedFrac - targetVal) < tol) {
                break;
            } else if (annealedFrac < (targetVal - tol)) {
                // too little annealing - lower upper temp
                t2 = currentTemp;
            } else {
                // too much annealing - raise lower temp
                t1 = currentTemp;
            }

        }
        return currentTemp;
    }


    private static Double fracBound(double dg, double temp, double concTarget, double concPrimer) {
        double a0 = concTarget;
        double b0 = concPrimer;
        double kelvinTemp = 273.15 + temp;
        double k = Math.exp(-1000.0 * dg / (1.987 * kelvinTemp));

        // System.out.println("VALUE OF a0: " + a0);
        // System.out.println("VALUE OF b0: " + b0);
        // System.out.println("VALUE OF temp: " + temp);
        // System.out.println("VALUE OF dg: " + dg);
        // System.out.println("VALUE OF k: " + k);

        if (k == 0) {
            System.out.println("hyfi-tm::frac_bound - paramater 'k' is 0 and will cause division by 0 error.");
            return new Double(1);
        }

        double frac = ((a0 + b0 + 1.0 / k) - Math.sqrt(Math.pow((a0 + b0 + 1.0 / k), 2) - 4.0 * a0 * b0)) / 2.0;
        //System.out.println("VALUE OF frac: " + frac + "\n\n\n");

        if (a0 == 0) {
            System.out.println("hyfi-tm::frac_bound - paramater 'a0' is 0 and will cause division by 0 error.");
            return new Double(1);
        }


        return new Double(frac / a0);
    }


    public static void getHyfiTmRange(int A_n, int B_n, double c_f_i, double c_r_i, double conc_na, double conc_mg, String[] f_p, String[] rc_p, Double tm_low, Double tm_hi) {

        double t_l = 10000.00;
        double t_h = -10000.00;
        double t_tm = 0.0;
        int i, j;

        for (i = 0; i < A_n; i++) {

            for (j = 0; j < B_n; j++) {
                t_tm = getHyfiTM(f_p[i], rc_p[j], c_f_i, c_r_i, 0.05, 0.002);

                if (t_tm < t_l) {
                    t_l = t_tm;
                }
                if (t_tm > t_h) {
                    t_h = t_tm;
                }
            }
        }

        tm_low = t_l;
        tm_hi = t_h;
    }
}