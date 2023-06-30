import gurobi.*;

import java.util.ArrayList;

public class ETPmodel{
    private GRBEnv env;
    private GRBModel model;
    private Istanza istanza;

    public void setEnv(GRBEnv env) {
        this.env = env;
    }

    public void setModel(GRBModel model) {
        this.model = model;
    }

    public void setIstanza(Istanza istanza) {
        this.istanza = istanza;
    }

    public void setiSlot(int iSlot) {
        this.iSlot = iSlot;
    }

    public void setVettoreY(GRBVar[][] vettoreY) {
        this.vettoreY = vettoreY;
    }

    public void setVettoreU(GRBVar[][][] vettoreU) {
        this.vettoreU = vettoreU;
    }

    private int iSlot;
    private GRBVar[][] vettoreY;
    private GRBVar[][][] vettoreU;

    public void copy(ETPmodel model) {
        this.setEnv(model.getEnv());
        this.setModel(model.getModel());
        this.setIstanza(model.getIstanza());
        this.setiSlot(model.getiSlot());
        this.setVettoreY(model.getVettoreY());
        this.setVettoreU(model.getVettoreU());
    }

    public ETPmodel(ETPmodel model){
        this.copy(model);
    }


    public GRBEnv getEnv() {
        return env;
    }

    public Istanza getIstanza() {
        return istanza;
    }

    public int getiSlot() {
        return iSlot;
    }

    public GRBVar[][][] getVettoreU() {
        return vettoreU;
    }

    public ETPmodel(Istanza istanza, int iSlot) { // metodo costruttore
        this.istanza = istanza;
        this.iSlot = iSlot;
    }

    public void buildModel() throws GRBException {
        env = new GRBEnv("modello.log");
        model = new GRBModel(env);
        setParameters();


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
         model.set(GRB.IntParam.Presolve, 2);
         model.set(GRB.DoubleParam.MIPGap, 1e-10);
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
                vettoreY[i][j] = model.addVar(0, 1, 0, GRB.BINARY, "y_" + (i+1) + "," + (j+1));
            }
        }
        return vettoreY;
    }

    public void stampaVariabiliY(ArrayList<Esame> listaEsami, int T)throws GRBException {
        for (int i = 0; i < listaEsami.size(); i++) {
                    for (int j = 0; j < T; j++) {
                        if(vettoreY[i][j].get(GRB.DoubleAttr.X)==1) {
                                System.out.println(vettoreY[i][j].get(GRB.StringAttr.VarName) + " " + vettoreY[i][j].get(GRB.DoubleAttr.X));
                        }


                    }
        }
    }

    public GRBVar[][][] dichiaraVariabiliU(ArrayList<Esame> listaEsami, int[][] matConflitti, int iSlot) throws GRBException {
        GRBVar[][][] vettoreU = new GRBVar[listaEsami.size()][listaEsami.size()][iSlot];

        for (int i = 0; i < listaEsami.size(); i++) {
            for (int j = 0; j < listaEsami.size(); j++) {
                if (matConflitti[i][j] > 0) {
                    for (int k = 0; k < iSlot; k++) {
                        String nomeVar = "u_" + (i+1) + "," + (j+1) + "," + (k+1);
                        //System.out.println("nomeVar: " + nomeVar);
                        vettoreU[i][j][k] = model.addVar(0, GRB.INFINITY, 0, GRB.CONTINUOUS,
                                nomeVar);
                    }
                }
            }
        }
        return vettoreU;
    }

    public void stampaVariabiliU(ArrayList<Esame> listaEsami, int[][] matConflitti, int iSlot) throws GRBException {
        for (int i = 0; i < listaEsami.size(); i++) {
                    for (int j = 0; j < listaEsami.size(); j++) {
                        if (matConflitti[i][j] > 0) {
                            for (int k = 0; k < iSlot; k++) {
                                if(vettoreU[i][j][k].get(GRB.DoubleAttr.X) == 1) {
                                    System.out.println(vettoreU[i][j][k].get(GRB.StringAttr.VarName) + " " + vettoreU[i][j][k].get(GRB.DoubleAttr.X));
                                }
                            }   
                        }
                    }
        }
    }

    public void solve() {
        try {
            model.optimize();
        } catch (GRBException e) {
            e.printStackTrace();
        }
    }

    public void rilassato() throws GRBException{
        double Solution=-5;
        GRBModel rilassato;
        rilassato=model.relax();
        rilassato.optimize();
        Solution=rilassato.get(GRB.DoubleAttr.ObjVal);
        System.out.println("la soluzione del rilassato è: "+Solution);
        
    }

    public void heurSolve() throws GRBException {
        int numeroIterazioni = 50;
        int counterIterazioni = 0;
        double Solution=0;
        double bestSolution=100000;
        //HeuristicSolver.calcolaSoluzioneIniziale(this);
        HeuristicSolver.provaSoluzioneIniziale(this);
        try {
            model.optimize();
            Solution=this.getObjValue();
        } catch (GRBException e) {
            e.printStackTrace();
        }



        System.out.println();System.out.println();
        System.out.println();System.out.println();
        //metodo sim annealing
        double temperatura0=20000;
        double temperatura=temperatura0;
        int j=0;
        double nreset=5;
        double alfa = 0.9;
        int counterSimulated=0;
        double iterzioniSimulated=30;

        for(int i=0;i<3;i++) {
            counterIterazioni=0;
            do {

                Solution = HeuristicSolver.improvingLocalSearch(this, Solution);
                counterIterazioni++;

            } while (numeroIterazioni > counterIterazioni);

            this.solve();

            counterSimulated=0;
            do {

                Solution = HeuristicSolver.improvingWithSimulatedAnnealing(this, temperatura);
                //Solution = HeuristicSolver.improvingWithGreatDeluge(this, temperatura);



                if (Solution < bestSolution) {
                    bestSolution = Solution;
                }
                //temperatura = temperatura * alfa;
                temperatura = temperatura / (1 + 500 * temperatura);

                if (temperatura < 0.0001) {
                    temperatura = temperatura0;
                    j++;
                    if (j > nreset) {
                        j = 0;
                        temperatura0 = temperatura0 * 2.5;
                        nreset += 0.05;
                        iterzioniSimulated += (iterzioniSimulated / 5);
                    }
                }

                System.out.println();
                System.out.println("la soluzione attuale è: " + Solution);
                System.out.println();
                counterSimulated++;
            } while (counterSimulated < iterzioniSimulated);
        }



        System.out.println("la soluzione migliore è: "+bestSolution);

        //this.stampaVariabiliY(this.getIstanza().getEsami(), this.getIstanza().getLunghezzaExaminationPeriod());
        //this.stampaVariabiliU(this.getIstanza().getEsami(), this.getIstanza().getConflitti(), this.getiSlot());
        //Utility.stampaTabConflitti(istanza.getConflitti());

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
                        for (int t = 0; t < T - (k+1); t++) {
                            GRBLinExpr exprVincolo3 = new GRBLinExpr();
                            //exprVincolo3.addTerm(1, model.getVarByName("y_" + i + "," + t));
                            //exprVincolo3.addTerm(1, model.getVarByName("y_" + j + "," + (t + i)));
                            //exprVincolo3.addTerm(-1, model.getVarByName("u_" + i + "," + j + "," + k));

                            exprVincolo3.addTerm(1, vettoreY[i][t]);
                            exprVincolo3.addTerm(1, vettoreY[j][t+(k+1)]);
                            exprVincolo3.addTerm(-1, vettoreU[i][j][k]);
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
                //if ((e1.getId() - 1) > (e2.getId() -1)) {
                    if (matConflitti[e1.getId() - 1][e2.getId() - 1] > 0) {
                        for (int i = 0; i < this.iSlot; i++) {
                            double fattoreMoltiplicativo = ((Math.pow(2, iSlot - (i+1))) * matConflitti[e1.getId() - 1][e2.getId() - 1]) / totStudenti;
                            funObjExpr.addTerm(fattoreMoltiplicativo, vettoreU[e1.getId() - 1][e2.getId() - 1][i]);

                            //funObjExpr.addTerm(fattoreMoltiplicativo, model.getVarByName("u_" + e1.getId() + "," + e2.getId() + "," + i));

                        }
                    }
                //}
            }
        }

        model.setObjective(funObjExpr, GRB.MINIMIZE);
    }

    public GRBVar[][] getVettoreY(){
        return vettoreY;
    }

    public GRBModel getModel() {
        return model;
    }

}