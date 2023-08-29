import gurobi.*;
import java.util.ArrayList;

public class ETPmodel{
    private GRBEnv env;
    private GRBModel model;
    private Istanza istanza;
    private int iSlot;
    private GRBVar[][] vettoreY;
    private GRBVar[][][] vettoreU;
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

        //funzione obiettivo
        funzioneObiettivo(istanza.getConflitti(), iSlot, istanza.getTotStudenti());

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
                        vettoreU[i][j][k] = model.addVar(0, GRB.INFINITY, 0, GRB.CONTINUOUS, nomeVar);
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

    public void heurSolve() throws GRBException {

        /** test ciclo per topConsiderare=x, ripetizioni=y, ordine(decrescente) ma fatto su ultimo ordinamento */
        int i=0;
        double durata_esec=0;
        double media_tempo=0;
        int media_iterazioni=0;
        for(i=0;i<1;i++) {
            long start = System.nanoTime();
            int n_iteraz = HeuristicSolver.provaSoluzioneIniziale(this);
            long end = System.nanoTime();
            durata_esec = (end - start) / Math.pow(10, 9);
            System.out.println("i=" +(i+1)+" -> Elapsed Time to find feasible solution in seconds: " + durata_esec);
            System.out.println("i=" +(i+1)+" -> iterations to find feasible solution: " + n_iteraz);
            media_tempo += durata_esec;
            media_iterazioni += n_iteraz;
        }

        media_tempo = media_tempo/i;
        media_iterazioni = media_iterazioni/i;
        System.out.println("\ndurata media:" + media_tempo);
        System.out.println("n-iterazioni medio:" + media_iterazioni);

        /**  test LS fino a non miglioramento per x volte, SA per uscire da minimi locali */

        //inizializzazione variabili necessarie
        double temperatura0=20000;
        double temperatura=temperatura0;
        int j=0;
        double nreset=5;
        double alfa = 0.9;
        int counterSimulated=0;
        int counterReset=0;
        int counterIterazioniLS = 0;
        double iterzioniSimulated=10;
        boolean changes=false;
        int contCambiamenti=0;
        double Solution=0;
        double Solution1=0;
        double bestSolution=100000;
        double soluzione_iniziale=0;
        double timelimit=30.0;
        double durata_esec1=0.0;
        ArrayList<Integer> esamiLiberi=new ArrayList<>();
        ArrayList<Double> tempi=new ArrayList<>();
        ArrayList<Integer> contatore=new ArrayList<>();

        soluzione_iniziale=this.getObjValue();
        Solution=soluzione_iniziale;
        bestSolution=soluzione_iniziale;

        long start1 = System.nanoTime();
        do{
            contCambiamenti=0;
            changes=true;

            /**  local search part */
            do{
                long end1 = System.nanoTime();
                durata_esec1 = (end1 - start1) / Math.pow(10, 9);
                if(durata_esec1>timelimit){ //controllo sul tempo limite
                    break;
                }
                Solution1 = HeuristicSolver.improvingLocalSearch(this, Solution, esamiLiberi, tempi);
                counterIterazioniLS++;

                if(Solution1==Solution){
                    contCambiamenti++;
                    System.out.println(contCambiamenti);
                }else{
                    contCambiamenti=0;
                }
                if (Solution1 < bestSolution) {
                    bestSolution = Solution;
                }
                if(contCambiamenti>0){
                    changes=false;
                }
                Solution=Solution1;
            }while(changes);
            model.update();

            /**  SA/GD part */
            do{
                long end1 = System.nanoTime();
                durata_esec1 = (end1 - start1) / Math.pow(10, 9);
                if(durata_esec1>timelimit){
                    break;
                }

                Solution = HeuristicSolver.improvingWithSimulatedAnnealing(this, temperatura, contatore, Solution);     //SA
                //Solution = HeuristicSolver.improvingWithGreatDeluge(this, Solution);                                         //GD

                if (Solution < bestSolution) {
                    bestSolution = Solution;
                }

                //Cooling Schedules
                temperatura = temperatura * alfa;   //test 1 alfa=0.9
                //temperatura = temperatura / (1 + 500 * temperatura); //test 2

                if (temperatura < 0.0001) {
                    counterReset++;
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
            }while(Solution==Solution1);

            long end1 = System.nanoTime();
            durata_esec1 = (end1 - start1) / Math.pow(10, 9);

        }while(durata_esec1<timelimit);

        System.out.println("soluzione iniziale: "+soluzione_iniziale);
        System.out.println("soluzione migliore: "+bestSolution);
        System.out.println("miglioramento soluzione: "+(soluzione_iniziale-bestSolution));
        System.out.println("numero di iterazioni LS eseguite: "+counterIterazioniLS);
        System.out.println("numero di iterazioni SA eseguite: "+counterSimulated);
        System.out.println("tempo usato per migliorare soluzioni con LS e SA/GD+ "+durata_esec1+ "in secondi");
        System.out.println("Elapsed Time to find the feasible solution in seconds: " + durata_esec);



        /** test --> ordinamneto random per esami */
        /*
        int i=0;
        ArrayList<Double> contatore=new ArrayList<>(0);
        double media_tempo=0;
        double media_tempo_gurobi=0;
        int media_iterazioni_migliore=0;
        int media_iterazioni_eseguite=0;
        for(i=0;i<3;i++) {
            contatore.clear();
            long start = System.nanoTime();
            int iterazione = HeuristicSolver.provaSoluzioneIniziale3(this,contatore);
            long end = System.nanoTime();
            double durata_esec = (end - start) / Math.pow(10, 9);

            System.out.println("i=" +(i+1)+"Elapsed Time in seconds: " + durata_esec);
            System.out.println("i=" +(i+1)+"iteratione istanza migliore: " + iterazione);
            System.out.println("i=" +(i+1)+"iteratione eseguite dal metodo: " + contatore.get(1));
            System.out.println("i=" +(i+1)+"Elapsed Time in seconds for gurobi: " + contatore.get(0));

            media_tempo += durata_esec;
            media_tempo_gurobi += contatore.get(0);
            media_iterazioni_migliore += iterazione;
            media_iterazioni_eseguite += contatore.get(1);
        }

        media_tempo = media_tempo / i;
        media_tempo_gurobi = media_tempo_gurobi/i;
        media_iterazioni_migliore = media_iterazioni_migliore / i;
        media_iterazioni_eseguite = media_iterazioni_eseguite /i;

        System.out.println("durata media:" + media_tempo);
        System.out.println("durata media gurobi:" + media_tempo_gurobi);
        System.out.println("media iterazione istanza migliore:" + media_iterazioni_migliore);
        System.out.println("media iterazioni eseguite:" + media_iterazioni_eseguite);
        */
        
        /** test 2 ordine decresscente + tabu/gurobi */
        /*
        double media_tempo_tot=0;
        double media_tempo_gurobi=0;
        double tempo_prima_es_gurobi=0;
        int i;
        for(i=0;i<1;i++) {
            long start = System.nanoTime();
            double tempo_gurobi=HeuristicSolver.provaSoluzioneIniziale2(this);
            long end = System.nanoTime();
            double durata_esec_tot = (end - start) / Math.pow(10, 9);
            if(i==0){
                tempo_prima_es_gurobi=tempo_gurobi;
                System.out.println("i=" + (i) + "Elapsed Time in seconds of provaSoluzioneIniziale: " + durata_esec_tot);
                System.out.println("i=" + (i) + "Elapsed Time in seconds of gurobi: " + tempo_gurobi);
            }else {
                System.out.println("i=" + (i) + "Elapsed Time in seconds of provaSoluzioneIniziale: " + durata_esec_tot);
                System.out.println("i=" + (i) + "Elapsed Time in seconds of gurobi: " + tempo_gurobi);
                media_tempo_tot += durata_esec_tot;
                media_tempo_gurobi += tempo_gurobi;
            }
        }

        System.out.println(i);
        media_tempo_tot = media_tempo_tot / (i-1);
        media_tempo_gurobi = media_tempo_gurobi / (i-1);
        System.out.println("\ndurata media totale:" + media_tempo_tot);
        System.out.println("durata media gurobi:" + media_tempo_gurobi);
        System.out.println("tempo inizializzazione del modello alla prima esecuzione = "+(tempo_prima_es_gurobi-media_tempo_gurobi));
         */

        /** test LS solo + grafici */
        /*
        //model.optimize();
        //int numeroIterazioniLS = 30;
        int counterIterazioniLS = 0;
        int counterCambiambiamentiSol=0;
        double Solution=0;
        double bestSolution=100000;
        double soluzione_iniziale=0;

        double timelimit=180.0;
        double durata_esec1=0.0;
        ArrayList<Integer> esamiLiberi=new ArrayList<>();
        ArrayList<Double> tempi=new ArrayList<>();
        ArrayList<Double> soluzioni=new ArrayList<>();
        ArrayList<Integer> iterazioni=new ArrayList<>();

        Solution=this.getObjValue();
        bestSolution=Solution;
        soluzione_iniziale=Solution;
        soluzioni.add(soluzione_iniziale);
        iterazioni.add(counterIterazioniLS);

        long start1 = System.nanoTime();
        do {
            Solution = HeuristicSolver.improvingLocalSearch(this, Solution, esamiLiberi, tempi);
            soluzioni.add(Solution);
            counterIterazioniLS++;
             iterazioni.add(counterIterazioniLS);
            if (Solution < bestSolution) {
                counterCambiambiamentiSol++;
                bestSolution = Solution;
            }
            long end1 = System.nanoTime();
            durata_esec1 = (end1 - start1) / Math.pow(10, 9);
        } while (durata_esec1 < timelimit);

        System.out.println("soluzione iniziale: "+soluzione_iniziale);
        System.out.println("soluzione migliore: "+bestSolution);
        System.out.println("miglioramento soluzione: "+(soluzione_iniziale-bestSolution));
        System.out.println("numero di iterazioni eseguite: "+counterIterazioniLS+" in "+durata_esec1+ " secondi");
        System.out.println("numero cambiamenti di soluzione: "+counterCambiambiamentiSol);
        double[] tmp = tempi.stream().mapToDouble(Double::doubleValue).toArray();

        double[][] plot_y = new double[][] { tmp};
        String[] plot_x = esamiLiberi.stream().map(e -> e.toString()).toArray(String[]::new);

        Plot.plot("titolo", "N_esami_liberi", "Tempi in secondi",
                plot_x, plot_y, "out.PNG", 800, 600, 2, 3);

        double[] tmp1 = soluzioni.stream().mapToDouble(Double::doubleValue).toArray();
        double[][] plot_y1 = new double[][] { tmp1};

        String[] plot_x1 = iterazioni.stream().map(e -> e.toString()).toArray(String[]::new);
        Plot.plot("titolo1", "N_iterazioni", "Solution", plot_x1, plot_y1, "out1.PNG", 800, 800, 48, 25);
         */

        /** test SA solo */
        /*
        double temperatura0=20000;
        double temperatura=temperatura0;
        int j=0;
        double nreset=5;
        double alfa = 0.9;
        int counterSimulated=0;
        int counterReset=0;
        double iterzioniSimulated=10;
        double Solution=0;
        double bestSolution=100000;
        double soluzione_iniziale=0;
        double timelimit=60.0;
        double durata_esec1=0.0;
        ArrayList<Integer> contatore=new ArrayList<>();
        //ArrayList<Integer> esamiLiberi=new ArrayList<>();
        //ArrayList<Double> tempi=new ArrayList<>();

        soluzione_iniziale=this.getObjValue();
        Solution=soluzione_iniziale;
        bestSolution=Solution;

        long start1 = System.nanoTime();
        do {
            Solution = HeuristicSolver.improvingWithSimulatedAnnealing(this, temperatura, contatore);
            //Solution = HeuristicSolver.improvingWithGreatDeluge(this);
            if (Solution < bestSolution) {
                bestSolution = Solution;
            }
            //Cooling Schedules
            //temperatura = temperatura * alfa;   //test 1 alfa=0.9
            //temperatura = temperatura0 * Math.pow(alfa, counterSimulated);   //test 2 alfa=0.9
            temperatura = temperatura / (1 + 500 * temperatura); //test 3

            if (temperatura < 0.0001) {
                counterReset++;
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

            long end1 = System.nanoTime();
            durata_esec1 = (end1 - start1) / Math.pow(10, 9);
        } while (durata_esec1 < timelimit);

        System.out.println("soluzione iniziale: "+soluzione_iniziale);
        System.out.println("soluzione migliore: "+bestSolution);
        System.out.println("miglioramento soluzione: "+(soluzione_iniziale-bestSolution));
        System.out.println("numero di iterazioni eseguite: "+counterSimulated+" in "+durata_esec1+ " secondi");
        System.out.println("numero reset tempertura: "+counterReset);
        System.out.println("numero vere iterazioni: "+ contatore.size());
         */

        /** LS=x  GD/SA=y  Cicli=k */
        /*
        double temperatura0=20000;
        double temperatura=temperatura0;
        int j=0;
        double nreset=5;
        double alfa = 0.9;
        int counterSimulated=0;
        int counterReset=0;
        int counterIterazioniLS = 0;
        int counterCambiambiamentiSol=0;
        int iterazioniLS=10;
        int iterzioniSimulated=10;
        boolean changes=false;
        int contCambiamenti=0;
        int k=0;
        double Solution=0;
        double Solution1=0;
        double bestSolution=100000;
        double soluzione_iniziale=0;
        double timelimit=10000.0;
        double durata_esec1=0.0;
        ArrayList<Integer> esamiLiberi=new ArrayList<>();
        ArrayList<Double> tempi=new ArrayList<>();
        ArrayList<Integer> contatore=new ArrayList<>();

        soluzione_iniziale=this.getObjValue();
        Solution=soluzione_iniziale;
        bestSolution=soluzione_iniziale;

        long start1 = System.nanoTime();
        for(k=0;k<5;k++) {
            counterIterazioniLS=0;
            do {
                Solution = HeuristicSolver.improvingLocalSearch(this, Solution,esamiLiberi,tempi);
                counterIterazioniLS++;
            } while (iterazioniLS > counterIterazioniLS);
            if (Solution < bestSolution) {
                bestSolution = Solution;
            }
            this.solve();
            model.update();
            counterSimulated=0;
            do {
                Solution = HeuristicSolver.improvingWithSimulatedAnnealing(this, temperatura,contatore, Solution);
                //Solution = HeuristicSolver.improvingWithGreatDeluge(this, temperatura, Solution);
                if (Solution < bestSolution) {
                    bestSolution = Solution;
                }
                //Cooling Schedules
                temperatura = temperatura * alfa;   //test 1 alfa=0.9
                //temperatura = temperatura / (1 + 500 * temperatura); //test 2

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

            long end1 = System.nanoTime();
            durata_esec1 = (end1 - start1) / Math.pow(10, 9);
            //System.out.println("il tempo di esecuzione del "+(k+1)+" ciclo fatto di "+iterazioniLS+" iterazioni " +
            //      "di LS e di"+iterzioniSimulated+" iterazioni di SA è di :" +durata_esec1);
            if(durata_esec1>timelimit ){
                break;
            }
        }
        System.out.println("soluzione iniziale: "+soluzione_iniziale);
        System.out.println("soluzione migliore: "+bestSolution);
        System.out.println("miglioramento soluzione: "+(soluzione_iniziale-bestSolution));
        System.out.println("cicli eseguiti: "+k);
        System.out.println("tempo trascorso+ "+durata_esec1+ "in secondi");

        //this.stampaVariabiliY(this.getIstanza().getEsami(), this.getIstanza().getLunghezzaExaminationPeriod());
        //this.stampaVariabiliU(this.getIstanza().getEsami(), this.getIstanza().getConflitti(), this.getiSlot());
        //Utility.stampaTabConflitti(istanza.getConflitti());
         */
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
                if (matConflitti[e1.getId() - 1][e2.getId() - 1] > 0) {
                    for (int i = 0; i < this.iSlot; i++) {
                        double fattoreMoltiplicativo = ((Math.pow(2, iSlot - (i+1))) * matConflitti[e1.getId() - 1][e2.getId() - 1]) / totStudenti;
                        funObjExpr.addTerm(fattoreMoltiplicativo, vettoreU[e1.getId() - 1][e2.getId() - 1][i]);

                    }
                }
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