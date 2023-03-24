import ilog.concert.IloException;

import java.io.FileNotFoundException;

public class SolverMain {
    public static void main(String[] args) throws FileNotFoundException, IloException {
        ILPSolver ilpSolver = new ILPSolver();
        int[][] instance = ilpSolver.readMatrix("/mnt/DATA/Ricerca/Tesisti/Federico Di Camillo/Code/Matrix.txt");
        System.out.println("INSTANCE");
        for (int i = 0; i < instance.length; i++) {
            for (int j = 0; j < instance[i].length; j++) {
                System.out.print(instance[i][j] + " ");
            }
            System.out.println();
        }

        int[][] solution = ilpSolver.solveIlp();
//        int[][] solution = ilpSolver.solveIlp2();
        int n_parcheggi = 0;
        System.out.println("Soluzione");
        for (int i = 0; i < solution.length; i++) {
            for (int j = 0; j < solution[i].length; j++) {
                System.out.print(solution[i][j] + " ");
                n_parcheggi += solution[i][j];
            }
            System.out.println();
        }
        System.out.println("Numero Parcheggi = " + n_parcheggi);

    }
}


/*
* note euristica
2,2, voglio mettere un parcheggio

controllo 2,1 - 1,2 - 3,2 e 3,3

se 2,1 hai n posizione strade vicine

se n-1 != 0 allora posso aggiungere il parcheggio
se n-1 e' 0 allora non posso
*/
