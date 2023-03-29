import ilog.concert.IloException;

import java.io.FileNotFoundException;

public class SolverMain {
    public static void main(String[] args) throws FileNotFoundException, IloException {
        Solver solver = new Solver();
        String path = "/mnt/DATA/Ricerca/Tesisti/Federico Di Camillo/Code/Tesi/src/inst/";
        String name = "MATRICE";

//        System.out.println("ILP original");
//        System.out.println("ILP solo su e giù");
        System.out.println("ILP solo su e giù con check sul meno");
//        System.out.println("Heur");

        int nMatrix = 10;
        String[] angoli = {"60", "75", "90"};

        int[][] instance;
        int[][] backup;
        for (int n = 1; n < nMatrix; n++) {
            for (int a = 0; a < angoli.length; a++) {

                instance = solver.readMatrix2(path + name + n + angoli[a] + ".txt");
                backup = new int[instance.length][instance[0].length];
                System.out.println("INSTANCE: " + name + n + angoli[a]);
                int maxPark = 0;
                for (int i = 0; i < instance.length; i++) {
                    for (int j = 0; j < instance[i].length; j++) {
                        if (instance[i][j] == 1) {
                            maxPark++;
                        }
                        backup[i][j] = instance[i][j];
//                        System.out.print(instance[i][j] + " ");
                    }
//                    System.out.println();
                }

                double startT = System.currentTimeMillis();
//                int[][] solution = solver.solverHeur();
//                int[][] solution = solver.solveIlp();
//                int[][] solution = solver.solveIlp_suegiu();
                int[][] solution = solver.solveIlp_checkSulMenoSuEGiu();
                double endT = System.currentTimeMillis();
                int n_parcheggi = 0;
                for (int i = 0; i < solution.length; i++) {
                    for (int j = 0; j < solution[i].length; j++) {
                        if (backup[i][j] == 0) {
                            System.out.print("- ");
                        } else {
                            System.out.print(solution[i][j] + " ");
                        }
                        n_parcheggi += solution[i][j];
                    }
                    System.out.println();
                }
                System.out.println("nPark = " + n_parcheggi + " - MaxNPark: " + maxPark + " - Time: " + (endT - startT) / 1000.0);
                System.out.println();
            }
        }


//        HeurSolver heurSolver = new HeurSolver();
////        int[][] solution = heurSolver.solver(instance);
////        int[][] solution = ilpSolver.solveIlp();
//        int[][] solution = ilpSolver.solveIlp2();
//        int n_parcheggi = 0;
//        System.out.println("Soluzione");
//        for (int i = 0; i < solution.length; i++) {
//            for (int j = 0; j < solution[i].length; j++) {
//                System.out.print(solution[i][j] + " ");
//                n_parcheggi += solution[i][j];
//            }
//            System.out.println();
//        }
//        System.out.println("Numero Parcheggi = " + n_parcheggi);

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
