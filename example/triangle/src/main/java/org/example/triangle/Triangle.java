package org.example.triangle;

public class Triangle {

    public String getTriangleType(final int a, final int b, final int c) {
        if ((a + b) > c && (b + c) > a && (c + a) > b) {
            if ((a == b) && (b == c)) {
                System.out.println(complicatedComputation(a , b , c));
                return "Equilatral";
            } else if (a == b || b == c || c == a) {
                System.out.println(complicatedComputation(a , b , c));
                return "Isosceles";
            } else {
                System.out.println(complicatedComputation(a , b , c));
                return "Scalene";
            }
        } else {
            return null;
        }
    }

    private static long complicatedComputation(final int a, final int b, final int c) {
        double u = 0;
         for (int k = 0; k < 20_000 * (a + b + c); k++) {
             u += sin(k);
         }
        return (long) u;
    }

    private static double sin(final double r) {
        double ans = r;
        boolean sub = true;
        double term = r;
        for (int k = 3; k < 1_000; k += 2) {
            term *= (r / k) * (r / (k - 1));
            ans += sub ? -term : term;
            sub = !sub;
        }
        return ans;
    }
}
