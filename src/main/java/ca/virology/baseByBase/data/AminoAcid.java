package ca.virology.baseByBase.data;

/**
 * Amino Acid utility class
 *
 * @author Ryan Brodie
 */
public final class AminoAcid
{
    //~ Static fields/initializers /////////////////////////////////////////////

    public static final int        A = 0;
    public static final int        R = 1;
    public static final int        N = 2;
    public static final int        D = 3;
    public static final int        C = 4;
    public static final int        Q = 5;
    public static final int        E = 6;
    public static final int        G = 7;
    public static final int        H = 8;
    public static final int        I = 9;
    public static final int        L = 10;
    public static final int        K = 11;
    public static final int        M = 12;
    public static final int        F = 13;
    public static final int        P = 14;
    public static final int        S = 15;
    public static final int        T = 16;
    public static final int        W = 17;
    public static final int        Y = 18;
    public static final int        V = 19;
    public static final int        B = 20;
    public static final int        Z = 21;
    public static final int        X = 22;
    public static final int        GAP = 23;
    public static final int        STOP = 24;
    public static final int        U = 25;
    public static final int        COUNT = 26;
    protected static final int[][] BLOSUM62 =
    {
        {
            4, -1, -2, -2, 0, -1, -1, 0, -2, -1, -1, -1, -1, -2, -1, 1, 0, -3,
            -2, 0, -2, -1, 0, -4
        },
        {
            -1, 5, 0, -2, -3, 1, 0, -2, 0, -3, -2, 2, -1, -3, -2, -1, -1, -3, -2,
            -3, -1, 0, -1, -4
        },
        {
            -2, 0, 6, 1, -3, 0, 0, 0, 1, -3, -3, 0, -2, -3, -2, 1, 0, -4, -2, -3,
            3, 0, -1, -4
        },
        {
            -2, -2, 1, 6, -3, 0, 2, -1, -1, -3, -4, -1, -3, -3, -1, 0, -1, -4,
            -3, -3, 4, 1, -1, -4
        },
        {
            0, 3, -3, -3, 9, -3, -4, -3, -3, -1, -1, -3, -1, -2, -3, -1, -1, -2,
            -2, -1, -3, -3, -2, -4
        },
        {
            -1, 1, 0, 0, -3, 5, 2, -2, 0, -3, -2, 1, 0, -3, -1, 0, -1, -2, -1,
            -2, 0, 3, -1, -4
        },
        {
            -1, 0, 0, 2, -4, 2, 5, -2, 0, -3, -3, 1, -2, -3, -1, 0, -1, -3, -2,
            -2, 1, 4, -1, -4
        },
        {
            0, -2, 0, -1, -3, -2, -2, 6, -2, -4, -4, -2, -3, -3, -2, 0, -2, -2,
            -3, -3, -1, -2, -1, -4
        },
        {
            -2, 0, 1, -1, -3, 0, 0, -2, 8, -3, -3, -1, -2, -1, -2, -1, -2, -2, 2,
            -3, 0, 0, -1, -4
        },
        {
            -1, -3, -3, -3, -1, -3, -3, -4, -3, 4, 2, -3, 1, 0, -3, -2, -1, -3,
            -1, 3, -3, -3, -1, -4
        },
        {
            -1, -2, -3, -4, -1, -2, -3, -4, -3, 2, 4, -2, 2, 0, -3, -2, -1, -2,
            -1, 1, -4, -3, -1, -4
        },
        {
            -1, 2, 0, -1, -3, 1, 1, -2, -1, -3, -2, 5, -1, -3, -1, 0, -1, -3, -2,
            -2, 0, 1, -1, -4
        },
        {
            -1, -1, -2, -3, -1, 0, -2, -3, -2, 1, 2, -1, 5, 0, -2, -1, -1, -1,
            -1, 1, -3, -1, -1, -4
        },
        {
            -2, -3, -3, -3, -2, -3, -3, -3, -1, 0, 0, -3, 0, 6, -4, -2, -2, 1, 3,
            -1, -3, -3, -1, -4
        },
        {
            -1, -2, -2, -1, -3, -1, -1, -2, -2, -3, -3, -1, -2, -4, 7, -1, -1,
            -4, -3, -2, -2, -1, -2, -4
        },
        {
            1, -1, 1, 0, -1, 0, 0, 0, -1, -2, -2, 0, -1, -2, -1, 4, 1, -3, -2,
            -2, 0, 0, 0, -4
        },
        {
            0, -1, 0, -1, -1, -1, -1, -2, -2, -1, -1, -1, -1, -2, -1, 1, 5, -2,
            -2, 0, -1, -1, 0, -4
        },
        {
            -3, -3, -4, -4, -2, -2, -3, -2, -2, -3, -2, -3, -1, 1, -4, -3, -2,
            11, 2, -3, -4, -3, -2, -4
        },
        {
            -2, -2, -2, -3, -2, -1, -2, -3, 2, -1, -1, -2, -1, 3, -3, -2, -2, 2,
            7, -1, -3, -2, -1, -4
        },
        {
            0, -3, -3, -3, -1, -2, -2, -3, -3, 3, 1, -2, 1, -1, -2, -2, 0, -3,
            -1, 4, -3, -2, -1, -4
        },
        {
            -2, -1, 3, 4, -3, 0, 1, -1, 0, -3, -4, 0, -3, -3, -2, 0, -1, -4, -3,
            -3, 4, 1, -1, -4
        },
        {
            -1, 0, 0, 1, -3, 3, 4, -2, 0, -3, -3, 1, -1, -3, -1, 0, -1, -3, -2,
            -2, 1, 4, -1, -4
        },
        {
            0, -1, -1, -1, -2, -1, -1, -1, -1, -1, -1, -1, -1, -1, -2, 0, 0, -2,
            -1, -1, -1, -1, -1, -4
        },
        {
            -4, -4, -4, -4, -4, -4, -4, -4, -4, -4, -4, -4, -4, -4, -4, -4, -4,
            -4, -4, -4, -4, -4, -4, 1
        },
    };
    protected static final int[][] PAM250 =
    {
        {
            2, -2, 0, 0, -2, 0, 0, 1, -1, -1, -2, -1, -1, -3, 1, 1, 1, -6, -3, 0,
            0, 0, 0, -8
        },
        {
            -2, 6, 0, -1, -4, 1, -1, -3, 2, -2, -3, 3, 0, -4, 0, 0, -1, 2, -4,
            -2, -1, 0, -1, -8
        },
        {
            0, 0, 2, 2, -4, 1, 1, 0, 2, -2, -3, 1, -2, -3, 0, 1, 0, -4, -2, -2,
            2, 1, 0, -8
        },
        {
            0, -1, 2, 4, -5, 2, 3, 1, 1, -2, -4, 0, -3, -6, -1, 0, 0, -7, -4, -2,
            3, 3, -1, -8
        },
        {
            -2, -4, -4, -5, 12, -5, -5, -3, -3, -2, -6, -5, -5, -4, -3, 0, -2,
            -8, 0, -2, -4, -5, -3, -8
        },
        {
            0, 1, 1, 2, -5, 4, 2, -1, 3, -2, -2, 1, -1, -5, 0, -1, -1, -5, -4,
            -2, 1, 3, -1, -8
        },
        {
            0, -1, 1, 3, -5, 2, 4, 0, 1, -2, -3, 0, -2, -5, -1, 0, 0, -7, -4, -2,
            3, 3, -1, -8
        },
        {
            1, -3, 0, 1, -3, -1, 0, 5, -2, -3, -4, -2, -3, -5, 0, 1, 0, -7, -5,
            -1, 0, 0, -1, -8
        },
        {
            -1, 2, 2, 1, -3, 3, 1, -2, 6, -2, -2, 0, -2, -2, 0, -1, -1, -3, 0,
            -2, 1, 2, -1, -8
        },
        {
            -1, -2, -2, -2, -2, -2, -2, -3, -2, 5, 2, -2, 2, 1, -2, -1, 0, -5,
            -1, 4, -2, -2, -1, -8
        },
        {
            -2, -3, -3, -4, -6, -2, -3, -4, -2, 2, 6, -3, 4, 2, -3, -3, -2, -2,
            -1, 2, -3, -3, -1, -8
        },
        {
            -1, 3, 1, 0, -5, 1, 0, -2, 0, -2, -3, 5, 0, -5, -1, 0, 0, -3, -4, -2,
            1, 0, -1, -8
        },
        {
            -1, 0, -2, -3, -5, -1, -2, -3, -2, 2, 4, 0, 6, 0, -2, -2, -1, -4, -2,
            2, -2, -2, -1, -8
        },
        {
            -3, -4, -3, -6, -4, -5, -5, -5, -2, 1, 2, -5, 0, 9, -5, -3, -3, 0, 7,
            -1, -4, -5, -2, -8
        },
        {
            1, 0, 0, -1, -3, 0, -1, 0, 0, -2, -3, -1, -2, -5, 6, 1, 0, -6, -5,
            -1, -1, 0, -1, -8
        },
        {
            1, 0, 1, 0, 0, -1, 0, 1, -1, -1, -3, 0, -2, -3, 1, 2, 1, -2, -3, -1,
            0, 0, 0, -8
        },
        {
            1, -1, 0, 0, -2, -1, 0, 0, -1, 0, -2, 0, -1, -3, 0, 1, 3, -5, -3, 0,
            0, -1, 0, -8
        },
        {
            -6, 2, -4, -7, -8, -5, -7, -7, -3, -5, -2, -3, -4, 0, -6, -2, -5, 17,
            0, -6, -5, -6, -4, -8
        },
        {
            -3, -4, -2, -4, 0, -4, -4, -5, 0, -1, -1, -4, -2, 7, -5, -3, -3, 0,
            10, -2, -3, -4, -2, -8
        },
        {
            0, -2, -2, -2, -2, -2, -2, -1, -2, 4, 2, -2, 2, -1, -1, -1, 0, -6,
            -2, 4, -2, -2, -1, -8
        },
        {
            0, -1, 2, 3, -4, 1, 3, 0, 1, -2, -3, 1, -2, -4, -1, 0, 0, -5, -3, -2,
            3, 2, -1, -8
        },
        {
            0, 0, 1, 3, -5, 3, 3, 0, 2, -2, -3, 0, -2, -5, 0, 0, -1, -6, -4, -2,
            2, 3, -1, -8
        },
        {
            0, -1, 0, -1, -3, -1, -1, -1, -1, -1, -1, -1, -1, -2, -1, 0, 0, -4,
            -2, -1, -1, -1, -1, -8
        },
        {
            -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8,
            -8, -8, -8, -8, -8, -8, 1
        },
    };

    // This is hydropathy index 
    // Kyte, J., and Doolittle, R.F., J. Mol. Biol.
    // 1157, 105-132, 1982
    protected static double[] hyd =
    {
        1.8, -4.5, -3.5, -3.5, 2.5, -3.5, -3.5, -0.4, -3.2, 4.5, 3.8, -3.9, 1.9,
        2.8, -1.6, -0.8, -0.7, -0.9, -1.3, 4.2, -3.5, -3.5, -0.49
    };
    public static final double hydmax = 4.5;
    public static final double hydmin = -3.9;

    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     * create a pam250 matrix for use outside this class
     *
     * @return the matrix
     */
    public static ScoringMatrix createPam250Matrix()
    {
        return new ScoringMatrix() {
                public int getScore(
                    int aa1,
                    int aa2)
                {
                    return AminoAcid.getPam250Score(aa1, aa2);
                }
            };
    }

    /**
     * create a blosum62 matrix for use outside this class
     *
     * @return the matrix
     */
    public static ScoringMatrix createBlosum62Matrix()
    {
        return new ScoringMatrix() {
                public int getScore(
                    int aa1,
                    int aa2)
                {
                    return AminoAcid.getBlosum62Score(aa1, aa2);
                }
            };
    }

    /**
     * Get the hydrophobicity of the given amino acid
     *
     * @param aa The amino acid index to compare
     *
     * @return The hydrophobicity value of the given amino acid
     */
    public static double getHydrophobicity(int aa)
    {
        if (aa >= hyd.length) {
            return 0;
        }

        return hyd[aa];
    }

    /**
     * Gets the hydrophobicity value normalized to 1.0
     *
     * @param aa The amino acid to query
     *
     * @return The hydrophobicity value of that amino acid normailized to 1.0
     */
    public static double getHydrophobicityNorm(int aa)
    {
        if (aa >= hyd.length) {
            return 0;
        }

        return (hyd[aa] - hydmin) / (hydmax - hydmin);
    }

    /**
     * Get the BLOSUM62 score comparing one amino acid with another
     *
     * @param aa1 The first amino acid
     * @param aa2 The second amino acid
     *
     * @return The score in the B62 matrix when comparing aa1 to aa2
     */
    public static int getBlosum62Score(
        int aa1,
        int aa2)
    {
        if (aa1 >= BLOSUM62.length) {
            return 0;
        }

        if (aa2 >= BLOSUM62[aa1].length) {
            return 0;
        }

        return BLOSUM62[aa1][aa2];
    }

    /**
     * Get the PAM250 score for two amino acids compared to each other
     *
     * @param aa1 The first amino acid
     * @param aa2 The second amino acid
     *
     * @return The score in the PAM250 matrix
     */
    public static int getPam250Score(
        int aa1,
        int aa2)
    {
        if (aa1 >= PAM250.length) {
            return 0;
        }

        if (aa2 >= PAM250[aa1].length) {
            return 0;
        }

        return PAM250[aa1][aa2];
    }

    /**
     * return the AminoAcid indexed value of the given sequence character
     *
     * @param c The character to query
     *
     * @return The AminoAcid class index of the given character
     */
    public static int valueOf(char c)
    {
        switch (Character.toUpperCase(c)) {
            case 'A':
                return A;

            case 'R':
                return R;

            case 'N':
                return N;

            case 'D':
                return D;

            case 'B':
                return B;

            case 'C':
                return C;

            case 'Q':
                return Q;

            case 'E':
                return E;

            case 'Z':
                return Z;

            case 'G':
                return G;

            case 'H':
                return H;

            case 'I':
                return I;

            case 'L':
                return L;

            case 'K':
                return K;

            case 'M':
                return M;

            case 'F':
                return F;

            case 'P':
                return P;

            case 'S':
                return S;

            case 'T':
                return T;

            case 'W':
                return W;

            case 'Y':
                return Y;

            case 'V':
                return V;

            case 'U':
                return U;

            case 'X':
                return X;

            case '*':
                return STOP;

            case '-':
                return GAP;

            default:
                return -1;
        }
    }

    /**
     * Return the char value of the given AminoAcid index
     *
     * @param i The AminoAcid index value to query
     *
     * @return The char value representing that index
     */
    public static char charValue(int i)
    {
        switch (i) {
            case A:
                return 'A';

            case R:
                return 'R';

            case N:
                return 'N';

            case D:
                return 'D';

            case B:
                return 'B';

            case C:
                return 'C';

            case Q:
                return 'Q';

            case E:
                return 'E';

            case Z:
                return 'Z';

            case G:
                return 'G';

            case H:
                return 'H';

            case I:
                return 'I';

            case L:
                return 'L';

            case K:
                return 'K';

            case M:
                return 'M';

            case F:
                return 'F';

            case P:
                return 'P';

            case S:
                return 'S';

            case T:
                return 'T';

            case W:
                return 'W';

            case Y:
                return 'Y';

            case V:
                return 'V';

            case U:
                return 'U';

            case X:
                return 'X';

            case STOP:
                return '*';

            case GAP:
                return '-';

            default:
                return 'X';
        }
    }
}
