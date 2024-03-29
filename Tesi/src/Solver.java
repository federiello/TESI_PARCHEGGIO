import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearIntExpr;
import ilog.cplex.IloCplex;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Scanner;

public class Solver {

    int[][] instance;

    public int[][] readMatrix(String pathName) throws FileNotFoundException {
        Scanner sc = new Scanner(new BufferedReader(new FileReader(pathName)));
        int rows = 6;
        int columns = 15;
        int[][] myArray = new int[rows][columns];
        while (sc.hasNextLine()) {
            for (int i = 0; i < myArray.length; i++) {
                String[] line = sc.nextLine().trim().split(",");
                for (int j = 0; j < line.length; j++) {
                    myArray[i][j] = Integer.parseInt(line[j]);
                }
            }
        }
        instance = myArray;
        return myArray;
    }

    public int[][] readMatrix2(String pathName) throws FileNotFoundException {
        Scanner sc = new Scanner(new BufferedReader(new FileReader(pathName)));
        ArrayList<String[]> tempList = new ArrayList<>();
        while (sc.hasNextLine()) {
            tempList.add(sc.nextLine().trim().split(" "));
        }

        int rows = tempList.size();
        int columns = tempList.get(0).length;
        int[][] myArray = new int[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                if (tempList.get(i)[j].equals("-")) {
                    myArray[i][j] = 0;
                } else {
                    myArray[i][j] = Integer.parseInt(tempList.get(i)[j]);
                }
            }
        }
        instance = myArray;
        return myArray;
    }


    public int[][] solveIlp() throws IloException {
        IloCplex model = new IloCplex();
        model.setOut(null);
        int n = instance.length;
        int m = instance[0].length;
        int k = n + m; // distanza massima uguale somma di righe e colonne

        // fisso la posizione dell'uscita
        int i_uscita = 0;
        int j_uscita = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                if (instance[i][j] == 1) {
                    i_uscita = i;
                    j_uscita = j;
                }
            }
        }

        //Variables posto
        IloIntVar[][] x_ij = new IloIntVar[n][m];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                x_ij[i][j] = model.boolVar("x_" + i + "_" + j);
            }
        }

        //Variabili distanza dall'uscita
        IloIntVar[][][] z_ij_k = new IloIntVar[n][m][k];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                for (int l = 0; l < k; l++) {
                    z_ij_k[i][j][l] = model.boolVar("z_" + i + "_" + j + "^" + k);
                }
            }
        }

//      FO = massimizzo i posti - 1
        IloLinearIntExpr objectiveFunction = model.linearIntExpr();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                objectiveFunction.addTerm(x_ij[i][j], 1);
            }
        }
        model.addMaximize(objectiveFunction);

//        Distanza 0 se parcheggio - 1a
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                for (int l = 0; l < k; l++) {
                    model.addLe(model.sum(z_ij_k[i][j][l], x_ij[i][j]), 1);
                }
            }
        }

//        Distanza k se i vicini sono k-1 - 1b
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                for (int l = 0; l < k; l++) {
                    IloLinearIntExpr listaVicini = model.linearIntExpr();
                    if (i - 1 >= 0) {
                        listaVicini.addTerm(z_ij_k[i - 1][j][l], 1);
                    }
                    if (j - 1 >= 0) {
                        listaVicini.addTerm(z_ij_k[i][j - 1][l], 1);
                    }
                    if (i + 1 < n) {
                        listaVicini.addTerm(z_ij_k[i + 1][j][l], 1);
                    }
                    if (j + 1 < m) {
                        listaVicini.addTerm(z_ij_k[i][j + 1][l], 1);
                    }
                    model.addLe(z_ij_k[i][j][l], listaVicini);
                }
            }
        }

//        un parcheggio deve avere una strada vicina - 1c
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                IloLinearIntExpr listaVicini = model.linearIntExpr();
                for (int l = 0; l < k; l++) {
                    if (i - 1 >= 0) {
                        listaVicini.addTerm(z_ij_k[i - 1][j][l], 1);
                    }
                    if (j - 1 >= 0) {
                        listaVicini.addTerm(z_ij_k[i][j - 1][l], 1);
                    }
                    if (i + 1 < n) {
                        listaVicini.addTerm(z_ij_k[i + 1][j][l], 1);
                    }
                    if (j + 1 < m) {
                        listaVicini.addTerm(z_ij_k[i][j + 1][l], 1);
                    }
                }
                model.addLe(x_ij[i][j], listaVicini);
            }
        }

//        solo una distanza valida - 1d
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                IloLinearIntExpr listaK = model.linearIntExpr();
                for (int l = 0; l < k; l++) {
                    listaK.addTerm(z_ij_k[i][j][l], 1);
                }
                model.addLe(listaK, 1);
            }
        }

        //setto l'uscita - 1e
        model.addEq(z_ij_k[i_uscita][j_uscita][0], 1);
        model.addEq(x_ij[i_uscita][j_uscita], 0);

        //setto a 0 le caselle che non possono essere parcheggi
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                if (instance[i][j] == 0) {
                    model.addEq(x_ij[i][j], 0);
                }
            }
        }


//        model.exportModel("ModelExact.lp");
        model.setParam(IloCplex.Param.TimeLimit, 900);
        //**********************************************************************
        int[][] solution = null;
        if (model.solve()) {
//            System.out.println(model.getObjValue());
            solution = new int[n][m];
            for (int i = 0; i < x_ij.length; i++) {
                for (int j = 0; j < x_ij[i].length; j++) {
                    if (model.getValue(x_ij[i][j]) > 0.8) {
                        solution[i][j] = 1;
                    }
                }
            }
        } else {
            System.err.println("Errore: " + model.getStatus());
        }
        model.end();

        return solution;
    }


    public int[][] solveIlp_suegiu() throws IloException {
        IloCplex model = new IloCplex();
        model.setOut(null);
        int n = instance.length;
        int m = instance[0].length;
        int k = n + m; // distanza massima uguale somma di righe e colonne

        // fisso la posizione dell'uscita
        int i_uscita = 0;
        int j_uscita = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                if (instance[i][j] == 1) {
                    i_uscita = i;
                    j_uscita = j;
                }
            }
        }

        //Variables posto
        IloIntVar[][] x_ij = new IloIntVar[n][m];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                x_ij[i][j] = model.boolVar("x_" + i + "_" + j);
            }
        }

        //Variabili distanza dall'uscita
        IloIntVar[][][] z_ij_k = new IloIntVar[n][m][k];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                for (int l = 0; l < k; l++) {
                    z_ij_k[i][j][l] = model.boolVar("z_" + i + "_" + j + "^" + k);
                }
            }
        }

//      FO = massimizzo i posti - 1
        IloLinearIntExpr objectiveFunction = model.linearIntExpr();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                objectiveFunction.addTerm(x_ij[i][j], 1);
            }
        }
        model.addMaximize(objectiveFunction);

//        Distanza 0 se parcheggio - 1a
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                for (int l = 0; l < k; l++) {
                    model.addLe(model.sum(z_ij_k[i][j][l], x_ij[i][j]), 1);
                }
            }
        }

//        Distanza k se i vicini sono k-1 - 1b
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                for (int l = 0; l < k; l++) {
                    IloLinearIntExpr listaVicini = model.linearIntExpr();
                    if (i - 1 >= 0) {
                        listaVicini.addTerm(z_ij_k[i - 1][j][l], 1);
                    }
//                    if (j - 1 >= 0) {
//                        listaVicini.addTerm(z_ij_k[i][j - 1][l], 1);
//                    }
                    if (i + 1 < n) {
                        listaVicini.addTerm(z_ij_k[i + 1][j][l], 1);
                    }
//                    if (j + 1 < m) {
//                        listaVicini.addTerm(z_ij_k[i][j + 1][l], 1);
//                    }
                    model.addLe(z_ij_k[i][j][l], listaVicini);
                }
            }
        }

//        un parcheggio deve avere una strada vicina - 1c
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                IloLinearIntExpr listaVicini = model.linearIntExpr();
                for (int l = 0; l < k; l++) {
                    if (i - 1 >= 0) {
                        listaVicini.addTerm(z_ij_k[i - 1][j][l], 1);
                    }
//                    if (j - 1 >= 0) {
//                        listaVicini.addTerm(z_ij_k[i][j - 1][l], 1);
//                    }
                    if (i + 1 < n) {
                        listaVicini.addTerm(z_ij_k[i + 1][j][l], 1);
                    }
//                    if (j + 1 < m) {
//                        listaVicini.addTerm(z_ij_k[i][j + 1][l], 1);
//                    }
                }
                model.addLe(x_ij[i][j], listaVicini);
            }
        }

//        solo una distanza valida - 1d
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                IloLinearIntExpr listaK = model.linearIntExpr();
                for (int l = 0; l < k; l++) {
                    listaK.addTerm(z_ij_k[i][j][l], 1);
                }
                model.addLe(listaK, 1);
            }
        }

        //setto l'uscita - 1e
        model.addEq(z_ij_k[i_uscita][j_uscita][0], 1);
        model.addEq(x_ij[i_uscita][j_uscita], 0);

        //setto a 0 le caselle che non possono essere parcheggi
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                if (instance[i][j] == 0) {
                    model.addEq(x_ij[i][j], 0);
                }
            }
        }


//        model.exportModel("ModelExact.lp");
        model.setParam(IloCplex.Param.TimeLimit, 900);
        //**********************************************************************
        int[][] solution = null;
        if (model.solve()) {
//            System.out.println(model.getObjValue());
            solution = new int[n][m];
            for (int i = 0; i < x_ij.length; i++) {
                for (int j = 0; j < x_ij[i].length; j++) {
                    if (model.getValue(x_ij[i][j]) > 0.8) {
                        solution[i][j] = 1;
                    }
                }
            }
        } else {
            System.err.println("Errore: " + model.getStatus());
        }
        model.end();

        return solution;
    }


    public int[][] solveIlp_checkSulMenoSuEGiu() throws IloException {
        IloCplex model = new IloCplex();
        model.setOut(null);
        int n = instance.length;
        int m = instance[0].length;
        int k = n + m; // distanza massima uguale somma di righe e colonne

        // fisso la posizione dell'uscita
        int i_uscita = 0;
        int j_uscita = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                if (instance[i][j] == 1) {
                    i_uscita = i;
                    j_uscita = j;
                }
            }
        }

        //Variables posto
        IloIntVar[][] x_ij = new IloIntVar[n][m];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                x_ij[i][j] = model.boolVar("x_" + i + "_" + j);
            }
        }

        //Variabili distanza dall'uscita
        IloIntVar[][][] z_ij_k = new IloIntVar[n][m][k];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                for (int l = 0; l < k; l++) {
                    z_ij_k[i][j][l] = model.boolVar("z_" + i + "_" + j + "^" + k);
                }
            }
        }

//      FO = massimizzo i posti - 1
        IloLinearIntExpr objectiveFunction = model.linearIntExpr();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                objectiveFunction.addTerm(x_ij[i][j], 1);
            }
        }
        model.addMaximize(objectiveFunction);

//        Distanza 0 se parcheggio - 1a
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                for (int l = 0; l < k; l++) {
                    model.addLe(model.sum(z_ij_k[i][j][l], x_ij[i][j]), 1);
                }
            }
        }

//        Distanza k se i vicini sono k-1 - 1b
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                for (int l = 0; l < k; l++) {
                    IloLinearIntExpr listaVicini = model.linearIntExpr();
                    if (i - 1 >= 0) {
                        listaVicini.addTerm(z_ij_k[i - 1][j][l], 1);
                    }
//                    if (j - 1 >= 0) {
//                        listaVicini.addTerm(z_ij_k[i][j - 1][l], 1);
//                    }
                    if (i + 1 < n) {
                        listaVicini.addTerm(z_ij_k[i + 1][j][l], 1);
                    }
//                    if (j + 1 < m) {
//                        listaVicini.addTerm(z_ij_k[i][j + 1][l], 1);
//                    }
                    model.addLe(z_ij_k[i][j][l], listaVicini);
                }
            }
        }

//        un parcheggio deve avere una strada vicina - 1c
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                IloLinearIntExpr listaVicini = model.linearIntExpr();
                for (int l = 0; l < k; l++) {
                    if (i - 1 >= 0) {
                        if (instance[i - 1][j] != 0) {
                            listaVicini.addTerm(z_ij_k[i - 1][j][l], 1);
                        }
                    }
//                    if (j - 1 >= 0) {
//                        listaVicini.addTerm(z_ij_k[i][j - 1][l], 1);
//                    }
                    if (i + 1 < n) {
                        if (instance[i + 1][j] != 0) {
                            listaVicini.addTerm(z_ij_k[i + 1][j][l], 1);
                        }
                    }
//                    if (j + 1 < m) {
//                        listaVicini.addTerm(z_ij_k[i][j + 1][l], 1);
//                    }
                }
                model.addLe(x_ij[i][j], listaVicini);
            }
        }

//        solo una distanza valida - 1d
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                IloLinearIntExpr listaK = model.linearIntExpr();
                for (int l = 0; l < k; l++) {
                    listaK.addTerm(z_ij_k[i][j][l], 1);
                }
                model.addLe(listaK, 1);
            }
        }

        //setto l'uscita - 1e
        model.addEq(z_ij_k[i_uscita][j_uscita][0], 1);
        model.addEq(x_ij[i_uscita][j_uscita], 0);

        //setto a 0 le caselle che non possono essere parcheggi
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                if (instance[i][j] == 0) {
                    model.addEq(x_ij[i][j], 0);
                }
            }
        }


//        model.exportModel("ModelExact.lp");
        model.setParam(IloCplex.Param.TimeLimit, 900);
        //**********************************************************************
        int[][] solution = null;
        if (model.solve()) {
//            System.out.println(model.getObjValue());
            solution = new int[n][m];
            for (int i = 0; i < x_ij.length; i++) {
                for (int j = 0; j < x_ij[i].length; j++) {
                    if (model.getValue(x_ij[i][j]) > 0.8) {
                        solution[i][j] = 1;
                    }
                }
            }
        } else {
            System.err.println("Errore: " + model.getStatus());
        }
        model.end();

        return solution;
    }

    int[][] solverHeur() {
        int n = instance.length;
        int m = instance[1].length;
        boolean[][] isParking = new boolean[n][m];

        for (int i = 0; i < isParking.length; i++) {
            for (int j = 0; j < isParking[i].length; j++) {
                isParking[i][j] = false;
            }
        }

//per ogni cella controllo se il valore è maggiore di 0 perche ogni volta aggiorna
//il valore della cella che assume un valore in base alle strade intorno, non sarà sempre 1
        for (int i = 0; i < instance.length; i++) {
            for (int j = 0; j < instance[i].length; j++) {
                if (instance[i][j] == 1) {
                    int cont = 0;
                    if (i == 0) {
                        if (j == 0) {
                            if (instance[i + 1][j] > 0)
                                cont++;
                            if (instance[i][j + 1] > 0) {
                                cont++;
                            }
                            instance[i][j] = cont;
                        } else if (j == instance[i].length - 1) {
                            if (instance[i + 1][j] > 0)
                                cont++;
                            if (instance[i][j - 1] > 0) {
                                cont++;
                            }
                            instance[i][j] = cont;
                        } else {
                            if (instance[i + 1][j] > 0)
                                cont++;
                            if (instance[i][j - 1] > 0) {
                                cont++;
                            }
                            if (instance[i][j + 1] > 0) {
                                cont++;
                            }
                            instance[i][j] = cont;
                        }
                    } else if (i == instance.length - 1) {
                        if (j == 0) {
                            if (instance[i - 1][j] > 0)
                                cont++;
                            if (instance[i][j + 1] > 0) {
                                cont++;
                            }
                            instance[i][j] = cont;
                        } else if (j == instance[i].length - 1) {
                            if (instance[i - 1][j] > 0)
                                cont++;
                            if (instance[i][j - 1] > 0) {
                                cont++;
                            }
                            instance[i][j] = cont;
                        } else {
                            if (instance[i - 1][j] > 0)
                                cont++;
                            if (instance[i][j + 1] > 0) {
                                cont++;
                            }
                            if (instance[i][j - 1] > 0)
                                cont++;
                            instance[i][j] = cont;
                        }
                    } else {
                        if (j == 0) {
                            if (instance[i - 1][j] > 0)
                                cont++;
                            if (instance[i][j + 1] > 0) {
                                cont++;
                            }
                            if (instance[i + 1][j] > 0)
                                cont++;
                            instance[i][j] = cont;
                        } else if (j == instance[i].length - 1) {
                            if (instance[i - 1][j] > 0)
                                cont++;
                            if (instance[i][j - 1] > 0) {
                                cont++;
                            }
                            if (instance[i + 1][j] > 0)
                                cont++;
                            instance[i][j] = cont;
                        } else {
                            if (instance[i - 1][j] > 0)
                                cont++;
                            if (instance[i][j + 1] > 0) {
                                cont++;
                            }
                            if (instance[i][j - 1] > 0)
                                cont++;
                            if (instance[i + 1][j] > 0) {
                                cont++;
                            }
                            instance[i][j] = cont;
                        }
                    }
                }

            }
        }

        for (int i = 0; i < instance.length; i++) {
            for (int j = 0; j < instance[i].length; j++) {
                if (instance[i][j] > 0) {
                    if (i == 0) {
                        isParking[i][j] = false;
                    } else if (i == instance.length - 1) {
                        if (j == 0) {
                            if ((instance[i - 1][j] - 1) > 0 && (instance[i][j + 1] - 1) > 0) {
                                instance[i - 1][j]--;
                                instance[i][j + 1]--;
                                isParking[i][j] = true;
                            }
                        } else if (j == instance[i].length - 1) {
                            if ((instance[i - 1][j] - 1) > 0 && (instance[i][j - 1] - 1) > 0) {
                                instance[i - 1][j]--;
                                instance[i][j - 1]--;
                                isParking[i][j] = true;
                            }
                        } else {
                            if ((instance[i - 1][j] - 1) > 0 && (instance[i][j + 1] - 1) > 0 && (instance[i][j - 1] - 1) > 0) {
                                instance[i - 1][j]--;
                                instance[i][j + 1]--;
                                instance[i][j - 1]--;
                                isParking[i][j] = true;
                            }
                        }
                    } else {
                        if (j == 0) {
                            if ((instance[i - 1][j] - 1) > 0 && (instance[i][j + 1] - 1) > 0 && (instance[i + 1][j] - 1) > 0) {
                                instance[i - 1][j]--;
                                instance[i][j + 1]--;
                                instance[i + 1][j]--;
                                isParking[i][j] = true;
                            }
                        } else if (j == instance[i].length - 1) {
                            if ((instance[i - 1][j] - 1) > 0 && (instance[i + 1][j] - 1) > 0 && (instance[i][j - 1] - 1) > 0) {
                                instance[i - 1][j]--;
                                instance[i + 1][j]--;
                                instance[i][j - 1]--;
                                isParking[i][j] = true;
                            }
                        } else {
                            if ((instance[i - 1][j] - 1) > 0 && (instance[i][j + 1] - 1) > 0 && (instance[i][j - 1] - 1) > 0 && (instance[i + 1][j] - 1) > 0) {
                                instance[i - 1][j]--;
                                instance[i][j + 1]--;
                                instance[i][j - 1]--;
                                instance[i + 1][j]--;
                                isParking[i][j] = true;
                            }
                        }
                    }
                }
            }
        }

//METODO PER CONTARE NUMERO PARCHEGGI
        int cont = 0;
        for (int i = 0; i < instance.length; i++) {
            for (int j = 0; j < instance[i].length; j++) {
                if (isParking[i][j] == true) {
                    cont++;
                }
            }
        }

//        System.out.println(cont);

        int[][] sol = new int[n][m];
//METODO CHE NELLA MATRICE INIZIALE IMPOSTA I PARCHEGGI = 8
        for (int i = 0; i < instance.length; i++) {
            for (int j = 0; j < instance[i].length; j++) {
                if (isParking[i][j] == true) {
                    instance[i][j] = 1;
                    sol[i][j] = 1;
                } else {
                    instance[i][j] = 0;
                    sol[i][j] = 0;
                }

            }
        }

//VISUALIZZAZIONE DISPOSIZIONE PARCHEGGI (RAPPRESENTATI COME 8)
//        for (int i = 0; i < n; i++) {
//            for (int j = 0; j < m; j++) {
//                System.out.print(Yard[i][j]);
//            }
//            System.out.println(" ");
//        }
        return sol;
    }

}
