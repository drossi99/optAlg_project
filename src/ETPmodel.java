import gurobi.*;

import java.util.ArrayList;

public class ETPmodel {
    private GRBEnv env;
    private GRBModel model;
    private Istanza istanza;
    private int iSlot;
    private GRBVar[][] vettoreY;
    private GRBVar[][][] vettoreU;

    public ETPmodel(Istanza istanza, int iSlot) { // metodo costruttore
        this.istanza = istanza;
        this.iSlot = iSlot;
    }

    public void buildModel() throws GRBException {
        env = new GRBEnv("modello.log");
        setParameters();
        model = new GRBModel(env);

        // aggiunta variabili
        this.vettoreY = dichiaraVariabiliY(istanza.getEsami(), istanza.getLunghezzaExaminationPeriod());
        this.vettoreU = dichiaraVariabiliU(istanza.getEsami(), istanza.getConflitti(), iSlot);

        funzioneObiettivo(istanza.getConflitti(), iSlot, istanza.getTotStudenti());
        // model.set(GRB.IntAttr.ModelSense, GRB.MINIMIZE); //dire che minimizzare

        // vincoli
        aggiungiVincolo1(istanza.getEsami(), istanza.getLunghezzaExaminationPeriod());
        aggiungiVincolo2(istanza.getEsami(), istanza.getLunghezzaExaminationPeriod(), istanza.getConflitti());
        aggiungiVincolo3(istanza.getEsami(), iSlot, istanza.getLunghezzaExaminationPeriod(), istanza.getConflitti());
    }

    private void setParameters() throws GRBException {
        // env.set(GRB.DoubleParam.TimeLimit, timeLimit);
        // env.set(GRB.IntParam.Threads, 8);
        // env.set(GRB.IntParam.Presolve, 0);
        // env.set(IntParam.Method, 0);
        // env.set(IntParam.Cuts, 3);
        // env.set(IntParam.GomoryPasses,0);
        // env.set(DoubleParam.Heuristics, 1);
        // env.set(IntParam.PoolSearchMode, 2);
    }

    public GRBVar[][] dichiaraVariabiliY(ArrayList<Esame> listaEsami, int T) throws GRBException {
        GRBVar[][] vettoreY = new GRBVar[listaEsami.size()][T];
        for (int i = 0; i < listaEsami.size(); i++) {
            for (int j = 0; j < T; j++) {
                vettoreY[i][j] = model.addVar(0, 1, 0, GRB.BINARY, "y_" + i + "," + j);
            }
        }
        return vettoreY;
    }

    public GRBVar[][][] dichiaraVariabiliU(ArrayList<Esame> listaEsami, int[][] matConflitti, int iSlot) throws GRBException {
        GRBVar[][][] vettoreU = new GRBVar[listaEsami.size()][listaEsami.size()][iSlot];

        for (int i = 0; i < listaEsami.size(); i++) {
            for (int j = 0; j < listaEsami.size(); j++) {
                if (matConflitti[i][j] > 0) {
                    for (int k = 0; k < iSlot; k++) {
                        String nomeVar = "u_" + (i+1) + "," + (j+1) + "," + (k+1);
                        System.out.println("nomeVar: " + nomeVar);
                        vettoreU[i][j][k] = model.addVar(0, GRB.INFINITY, 0, GRB.CONTINUOUS,
                                nomeVar);
                    }
                }
            }
        }
        return vettoreU;
    }

    public void solve() {
        try {
            model.optimize();
        } catch (GRBException e) {
            e.printStackTrace();
        }
    }

    public void dispose() {
        try {
            model.dispose();
            env.dispose();
            model = null;
            env = null;
        } catch (GRBException e) {
            e.printStackTrace();
        }
    }

    public double getObjValue() { // getter valore funzione obiettivo
        try {
            return model.get(GRB.DoubleAttr.ObjVal);
        } catch (GRBException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void aggiungiVincolo1(ArrayList<Esame> listaEsami, int T) throws GRBException {

        for (int i = 0; i < listaEsami.size(); i++) {
            GRBLinExpr exprVincolo1 = new GRBLinExpr();
            for (int j = 0; j < T; j++) {
                //exprVincolo1.addTerm(1, model.getVarByName("y_" + i + "," + j));
                exprVincolo1.addTerm(1, vettoreY[i][j]);
            }
            model.addConstr(exprVincolo1, GRB.EQUAL, 1, "Vincolo1_" + "e" + i);

        }
    }

    public void aggiungiVincolo2(ArrayList<Esame> listaEsami, int T, int[][] matConflitti) throws GRBException {
        for (int i = 0; i < listaEsami.size(); i++) {
            for (int j = 0; j < listaEsami.size(); j++) {
                if (matConflitti[i][j] > 0) {
                    for (int t = 0; t < T; t++) {
                        GRBLinExpr exprVincolo2 = new GRBLinExpr();
                        //exprVincolo2.addTerm(1, model.getVarByName("y_" + i + "," + t));
                        //exprVincolo2.addTerm(1, model.getVarByName("y_" + j + "," + t));
                        exprVincolo2.addTerm(1, vettoreY[i][t]);
                        exprVincolo2.addTerm(1, vettoreY[j][t]);
                        model.addConstr(exprVincolo2, GRB.LESS_EQUAL, 1, "Vincolo2_" + "e" + i + ",e" + j + ",t" + t);
                    }
                }
            }
        }
    }

    public void aggiungiVincolo3(ArrayList<Esame> listaEsami, int iSlot, int T, int[][] matConflitti) throws GRBException {
        for (int i = 0; i < listaEsami.size(); i++) {
            for (int j = 0; j < listaEsami.size(); j++) {
                if (matConflitti[i][j] > 0) {
                    for (int k = 0; k < iSlot; k++) {
                        for (int t = 0; t < T - k; t++) {
                            GRBLinExpr exprVincolo3 = new GRBLinExpr();
                            //exprVincolo3.addTerm(1, model.getVarByName("y_" + i + "," + t));
                            //exprVincolo3.addTerm(1, model.getVarByName("y_" + j + "," + (t + i)));
                            //exprVincolo3.addTerm(-1, model.getVarByName("u_" + i + "," + j + "," + k));

                            exprVincolo3.addTerm(1, vettoreY[i][t]);
                            exprVincolo3.addTerm(1, vettoreY[j][t+k]);
                            exprVincolo3.addTerm(1, vettoreU[i][j][k]);
                            model.addConstr(exprVincolo3, GRB.LESS_EQUAL, 1,
                                    "Vincolo3_" + "e" + i + ",e" + j + ",t" + t + ",k" + k);
                        }
                    }
                }
            }
        }
    }

    public void funzioneObiettivo(int[][] matConflitti, int iSlot, int totStudenti) throws GRBException {
        GRBLinExpr funObjExpr = new GRBLinExpr();

        for (GRBVar var : this.model.getVars()) {
            System.out.println("vriabile: " + var.get(GRB.StringAttr.VarName));
        }
        for (Esame e1 : istanza.getEsami()) {
            for (Esame e2 : istanza.getEsami()) {
                if (matConflitti[e1.getId()-1][e2.getId()-1] > 0) {
                    for (int i = 0; i < this.iSlot; i++) {
                        double fattoreMoltiplicativo = ((Math.pow(2, iSlot-i))*matConflitti[e1.getId()-1][e2.getId()-1])/ totStudenti;
                        funObjExpr.addTerm(fattoreMoltiplicativo, vettoreU[e1.getId()-1][e2.getId()-1][i]);

                        //funObjExpr.addTerm(fattoreMoltiplicativo, model.getVarByName("u_" + e1.getId() + "," + e2.getId() + "," + i));

                    }
                }
            }
        }

        model.setObjective(funObjExpr, GRB.MINIMIZE);
    }


    public GRBModel getModel() {
        return model;
    }

}