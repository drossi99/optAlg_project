import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import gurobi.*;
import java.io.BufferedWriter;
import java.io.FileWriter;

public class HeuristicSolver {
    private static ArrayList<Integer> calcolaEsamiAdiacenti(int idEsame, int[][] conflitti) {
        ArrayList<Integer> adiacenti = new ArrayList<Integer>();
        for (int i = 0; i < conflitti.length; i++) {
            if (conflitti[idEsame - 1][i] > 0) {
                adiacenti.add(i + 1); //viene aggiunto l'ID dell'esame, non l'indice
            }
        }
        return adiacenti;
    }

    /**     * Ordina gli esami in modo decrescente in base al grado (numero di conflitti)    */
    private static ArrayList<Esame> ordina(ArrayList<Esame> lista, int[][] conflitti) {
        Esame[] esami = new Esame[lista.size()];
        esami = lista.toArray(esami);
        int[] gradi = new int[esami.length];
        int t;
        Esame e;
        for (int i = 0; i < esami.length; i++) {
            gradi[i] = calcolaEsamiAdiacenti(esami[i].getId(), conflitti).size();
        }
        for (int i = 0; i <= gradi.length; i++) {
            for (int j = i + 1; j < gradi.length; j++) {
                if (gradi[i] <= gradi[j]) {
                    //Scambia gradi
                    t = gradi[i];
                    gradi[i] = gradi[j];
                    gradi[j] = t;
                    //Scambia esami
                    e = esami[i];
                    esami[i] = esami[j];
                    esami[j] = e;
                }
            }
        }
        ArrayList<Esame> listesami = new ArrayList<Esame>();
        Collections.addAll(listesami, esami);
        return listesami;
    }

    /**     * Ordina gli esami in modo crescente in base al grado (numero di conflitti)    */
    private static ArrayList<Esame> ordinaInverso(ArrayList<Esame> lista, int[][] conflitti) {
            ArrayList<Esame> listaEsamiOrdinati = ordina(lista, conflitti);
            Collections.reverse(listaEsamiOrdinati);
            return listaEsamiOrdinati;
        }
    /**     * metodo per resettare i lb delle variabili y    */
    private static void reset(ETPmodel model, ArrayList<Esame> esamiOrdinati) throws GRBException {
        for (int e = 0; e < esamiOrdinati.size(); e++) {
            for (int t = 0; t < model.getIstanza().getLunghezzaExaminationPeriod(); t++) {
                if (model.getVettoreY()[esamiOrdinati.get(e).getId() - 1][t].get(GRB.DoubleAttr.LB) == 1) { //da verificare se migliora tempo
                    model.getVettoreY()[esamiOrdinati.get(e).getId() - 1][t].set(GRB.DoubleAttr.LB, 0);
                }
            }
        }
    }
    /**    metodo usato per i test per trovare la soluzione feasible iniziale    */
    public static double provaSoluzioneIniziale2(ETPmodel model) throws GRBException {
        GRBModel modello = model.getModel();
        ArrayList<Integer> slotOccupati = new ArrayList<Integer>();
        int[][] matConflitti = model.getIstanza().getConflitti();
        ArrayList<Esame> esami = new ArrayList<Esame>(model.getIstanza().getEsami());
        ArrayList<Esame> esamiOrdinati = new ArrayList<Esame>();
        esamiOrdinati = ordina(esami, model.getIstanza().getConflitti());
        //esamiOrdinati=ordinaInverso(esami, model.getIstanza().getConflitti());
        ArrayList<Esame> extraTimeSlot = new ArrayList<>();
        extraTimeSlot.clear();
        //reset(model,esamiOrdinati);

        gestisciAssegnamento(model, modello, esamiOrdinati,matConflitti,slotOccupati, extraTimeSlot);

        if(extraTimeSlot.size()>0) {
            long start = System.nanoTime();
            gurobi(extraTimeSlot, model);
            //tabuSearch(extraTimeSlot, model);
            modello.optimize();
            long end = System.nanoTime();
            double durata_esec = (end - start) / Math.pow(10, 9);
            System.out.println("Elapsed Time in seconds of gurobi: " + durata_esec);
            return durata_esec;
        }else{
            modello.optimize();
            return 0.0;
        }
    }

    /**    metodo usato per i test per trovare la soluzione feasible iniziale    */
    public static int provaSoluzioneIniziale3(ETPmodel model, ArrayList<Double> contatore) throws GRBException {
        int numeroRipetizioniEuristica = 100;
        GRBModel modello = model.getModel();
        ArrayList<Integer> slotOccupati = new ArrayList<Integer>();
        int[][] matConflitti = model.getIstanza().getConflitti();
        ArrayList<Esame> esamiOrdinati = new ArrayList<Esame>(model.getIstanza().getEsami());
        ArrayList<Esame> esamiOrdinatiMigliore = new ArrayList<Esame>();
        ArrayList<Esame> extraTimeSlot = new ArrayList<>();
        int sizeMinima=10000;
        int iterzioneDelMinimo=0;
        int k=0;
        Collections.shuffle(esamiOrdinati);
        do {
            extraTimeSlot.clear();
            reset(model,esamiOrdinati);
            k++;
            System.out.println("iterazione numero:"+ k);
            System.out.println(esamiOrdinati.size());
            gestisciAssegnamento(model, modello, esamiOrdinati,matConflitti,slotOccupati, extraTimeSlot);

            if(sizeMinima>extraTimeSlot.size()){
                esamiOrdinatiMigliore.clear();
                sizeMinima=extraTimeSlot.size();
                iterzioneDelMinimo=k;
                for(Esame e: esamiOrdinati){
                    esamiOrdinatiMigliore.add(e);
                }
            }
            Collections.shuffle(esamiOrdinati);
        } while(extraTimeSlot.size()>0 && k<numeroRipetizioniEuristica );

        if(extraTimeSlot.size()==0){
            System.out.println("ok");
            modello.optimize();
            contatore.add(0.0);
        }

        if(k>=numeroRipetizioniEuristica) {
            System.out.println((sizeMinima));
            extraTimeSlot.clear();
            reset(model, esamiOrdinati);
            gestisciAssegnamento(model, modello, esamiOrdinatiMigliore, matConflitti, slotOccupati, extraTimeSlot);
            long start = System.nanoTime();
            gurobi(extraTimeSlot, model);
            long end = System.nanoTime();
            double durata_esec = (end - start) / Math.pow(10, 9);
            contatore.add(durata_esec);
            System.out.println("Elapsed Time in seconds of gurobi: " + durata_esec);
        }
        contatore.add((double)k);
        return iterzioneDelMinimo;
    }

    /**    metodo usato per i test per trovare la soluzione feasible iniziale    */
    public static int provaSoluzioneIniziale(ETPmodel model) throws GRBException {
        int numeroRipetizioniEuristica = 0;
        StringBuffer stringa = new StringBuffer();
        GRBModel modello = model.getModel();
        ArrayList<Integer> slotOccupati = new ArrayList<Integer>();
        int[][] matConflitti = model.getIstanza().getConflitti();
        ArrayList<Esame> esami = new ArrayList<Esame>(model.getIstanza().getEsami());
        ArrayList<Esame> esamiOrdinati = new ArrayList<Esame>();
        ArrayList<Esame> lista = new ArrayList<Esame>();
        ArrayList<Esame> esamiOrdinatiMigliore = new ArrayList<Esame>();
        esamiOrdinati = ordina(esami, model.getIstanza().getConflitti());
        //esamiOrdinati=ordinaInverso(esami, model.getIstanza().getConflitti());
        ArrayList<Esame> extraTimeSlot = new ArrayList<>();
        int sizeMinima=10000;
        int k=0;
        //Collections.shuffle(esamiOrdinati);
        do {
            extraTimeSlot.clear();
            if(k!=0) {
                reset(model, esamiOrdinati);
            }
            k++;

            System.out.println("iterazione numero:"+ k);
            System.out.println(esamiOrdinati.size());

            //gestisciAssegnamento(model, modello, esamiOrdinati,matConflitti,stringa,slotOccupati, extraTimeSlot);
            lista= AssegnamentoRandomAdaptive(model, modello, esamiOrdinati,matConflitti,slotOccupati, extraTimeSlot);
            System.out.println(esamiOrdinati.size());

            if(sizeMinima>extraTimeSlot.size()){
                esamiOrdinatiMigliore.clear();
                sizeMinima=extraTimeSlot.size();
                //esamiOrdinatiMigliore=esamiOrdinati;
                for(Esame e: lista){
                    esamiOrdinatiMigliore.add(e);
                }
            }
            esamiOrdinati=ordina(lista,matConflitti);
            //Collections.shuffle(esamiOrdinati);
        } while(extraTimeSlot.size()>0 && k<numeroRipetizioniEuristica );

        if(extraTimeSlot.size()==0){
            System.out.println("ok");
        }

        if(k>=numeroRipetizioniEuristica) {
            System.out.println((sizeMinima));
            //extraTimeSlot.clear();
            //reset(model, esamiOrdinati);
            //gestisciAssegnamento(model, modello, esamiOrdinatiMigliore, matConflitti, slotOccupati, extraTimeSlot);
            //tabuSearch(extraTimeSlot, model);
            gurobi(extraTimeSlot, model);
            modello.update();
        }
       modello.optimize();

        /**   parte di codice per scrivere su file di testo e per poter verificare che soluzione trovata sia valida attraverso ETPchecker   */
        /*
        for (int t = 0; t < model.getIstanza().getLunghezzaExaminationPeriod(); t++) {
            for (int i = 0; i < esamiOrdinati.size(); i++) {
                if (model.getVettoreY()[esamiOrdinati.get(i).getId() - 1][t].get(GRB.DoubleAttr.X) == 1) {
                    model.getVettoreY()[esamiOrdinati.get(i).getId() - 1][t].set(GRB.DoubleAttr.LB, 1);
                    stringa.append(esamiOrdinati.get(i).getId() + " " + (t + 1) + "\n");
                }
            }
        }
        stampaModello(stringa);
        */
        return k;
    }

    /**   Metodo di assegnamento adattivo ovvero prende tra i primi x migliori */
    private static ArrayList<Esame> AssegnamentoRandomAdaptive(ETPmodel model, GRBModel modello, ArrayList<Esame> esamiOrdinati, int[][] matConflitti, ArrayList<Integer> slotOccupati, ArrayList<Esame> extraTimeSlot) throws GRBException {
        Boolean isAssegnato = false;
        Random rand = new Random();
        ArrayList<Esame> lista=new ArrayList<>();
        int topDaConsiderare = 5;

        do{
            if(esamiOrdinati.size()<5){
                topDaConsiderare=topDaConsiderare-1;
            }
            int randomIndex = rand.nextInt(topDaConsiderare);
            Esame esameValutare=esamiOrdinati.get(randomIndex);
            esamiOrdinati.remove(randomIndex);
            slotOccupati.clear();
            for(int i=0;i<lista.size();i++){
                if (matConflitti[esameValutare.getId() - 1][lista.get(i).getId() - 1] > 0) {
                    for (int t = 0; t < model.getIstanza().getLunghezzaExaminationPeriod(); t++) {
                        if (model.getVettoreY()[lista.get(i).getId() - 1][t].get(GRB.DoubleAttr.LB) == 1) {
                            slotOccupati.add(t);
                        }
                    }
                }
            }
            for (int q = 0; q < model.getIstanza().getLunghezzaExaminationPeriod(); q++) {
                isAssegnato = true;
                for (int z = 0; z < slotOccupati.size(); z++) {
                    if (q == slotOccupati.get(z)) {
                        //System.out.println("q "+q+"= "+slotOccupati.get(z));
                        isAssegnato = false;
                    }
                }
                if (isAssegnato) {
                    model.getVettoreY()[esameValutare.getId() - 1][q].set(GRB.DoubleAttr.LB, 1);
                    modello.update();
                    //System.out.println(("settiamo y[" + (esamiOrdinati.get(e).getId()) + "][" + (q + 1) + "] a " + model.getVettoreY()[esamiOrdinati.get(e).getId() - 1][q].get(GRB.DoubleAttr.LB)));
                    break;
                }
            }
            if (!isAssegnato) {
                extraTimeSlot.add(esameValutare);
                System.out.println("ESAME EXTRA: " + esameValutare.getId());
            }
            lista.add(esameValutare);
        }while(esamiOrdinati.size()>0);
        //esamiOrdinati=ordina(lista,matConflitti);
        return lista;
    }

    /**   Metodo di assegnamento normale, prende gli esami in base all'ordine in cui vengono passati */
    private static void gestisciAssegnamento(ETPmodel model, GRBModel modello, ArrayList<Esame> esamiOrdinati, int[][] matConflitti, ArrayList<Integer> slotOccupati, ArrayList<Esame> extraTimeSlot) throws GRBException {
        Boolean isAssegnato = false;
        int cont = 0;
        // assegno il primo esame al primo slot
        model.getVettoreY()[esamiOrdinati.get(0).getId() - 1][0].set(GRB.DoubleAttr.LB, 1);
        modello.update();
        cont++;

        for (int e = 1; e < esamiOrdinati.size(); e++) {
            slotOccupati.clear();
            for (int i = 0; i < e; i++) {
                if (matConflitti[esamiOrdinati.get(e).getId() - 1][esamiOrdinati.get(i).getId() - 1] > 0) {
                    for (int t = 0; t < model.getIstanza().getLunghezzaExaminationPeriod(); t++) {
                        if (model.getVettoreY()[esamiOrdinati.get(i).getId() - 1][t].get(GRB.DoubleAttr.LB) == 1) {
                            slotOccupati.add(t);
                        }
                    }
                }
            }
            for (int q = 0; q < model.getIstanza().getLunghezzaExaminationPeriod(); q++) {
                isAssegnato = true;
                for (int z = 0; z < slotOccupati.size(); z++) {
                    if (q == slotOccupati.get(z)) {
                        //System.out.println("q "+q+"= "+slotOccupati.get(z));
                        isAssegnato = false;
                    }
                }
                if (isAssegnato) {
                    model.getVettoreY()[esamiOrdinati.get(e).getId() - 1][q].set(GRB.DoubleAttr.LB, 1);
                    modello.update();
                    cont++;
                    break;
                }
            }
            if (!isAssegnato) {
                extraTimeSlot.add(esamiOrdinati.get(e));
                System.out.println("ESAME EXTRA: " + esamiOrdinati.get(e).getId());
            }
        }
    }

    /**   Metodo che si basa sull'uso della forza di gurobi per trovare solzuione feasible */
    public static void gurobi(ArrayList<Esame> extraTimeSlot, ETPmodel model) throws GRBException{
        StringBuffer stringa = new StringBuffer();
        GRBModel modello = model.getModel();
        ArrayList<Esame> listaEsami = new ArrayList<Esame>(model.getIstanza().getEsami());
        int[][] matConflitti = model.getIstanza().getConflitti();
        int numTimeslot = model.getIstanza().getLunghezzaExaminationPeriod();
        try {

            for(int k=0;k<extraTimeSlot.size();k++) {
                Esame esameDaSchedulare = extraTimeSlot.get(k);
                for (int t = 0; t < numTimeslot; t++) {
                    for (int i = 0; i < listaEsami.size(); i++) {
                        if (model.getVettoreY()[listaEsami.get(i).getId() - 1][t].get(GRB.DoubleAttr.LB) == 1) {
                            if (matConflitti[listaEsami.get(i).getId() - 1][esameDaSchedulare.getId() - 1] > 0)
                                model.getVettoreY()[listaEsami.get(i).getId() - 1][t].set(GRB.DoubleAttr.LB, 0);
                                modello.update();
                        }
                    }
                }
            }
            modello.set(GRB.IntParam.SolutionLimit, 1); //settando a 1, costringiamo il solver a fornirci la prima soluzione feasible che trova
            modello.update();
            modello.optimize();

            /**   parte di codice per scrivere su file di testo e per poter verificare che soluzione trovata sia valida attraverso ETPchecker   */
            for (int t = 0; t < numTimeslot; t++) {
                for (int i = 0; i < listaEsami.size(); i++) {
                    if (model.getVettoreY()[listaEsami.get(i).getId() - 1][t].get(GRB.DoubleAttr.X) == 1) {
                        model.getVettoreY()[listaEsami.get(i).getId() - 1][t].set(GRB.DoubleAttr.LB, 1);
                        stringa.append(listaEsami.get(i).getId() + " " + (t + 1) + "\n");
                    }
                }
            }
            stampaModello(stringa);

            modello.set(GRB.IntParam.SolutionLimit,200000000); //settiamo ad un valore alto per ristemare al valore di origine
            modello.update();
        } catch (GRBException e) {
            e.printStackTrace();
            System.exit(0); //fermo programma se gurobi riporta soluzione infeasble (succede a volte se c'è solo un esame extra fuori in alcune istanze)
        }
    }

    /**   Metodo che si basa sul concetto di tabu list per trovare solzuione feasible */
    public static void tabuSearch(ArrayList<Esame> extraTimeSlot, ETPmodel model) throws GRBException {
        StringBuffer stringa = new StringBuffer();
        GRBModel modello = model.getModel();
        ArrayList<Esame> listaEsami = new ArrayList<Esame>(model.getIstanza().getEsami());
        int[][] matConflitti = model.getIstanza().getConflitti();
        int tabu_tenure = model.getIstanza().getLunghezzaExaminationPeriod();
        tabu_tenure = 1;
        int numTimeslot = model.getIstanza().getLunghezzaExaminationPeriod();
        int slotConMenoConflitti = 0;
        int minorConflitti = model.getIstanza().getEsami().size();
        int numConflitti = 0;
        Random random = new Random();
        boolean mossaproibita = false;
        boolean mossaobbligata = false;
        ArrayList<Esame> esamiInConflitto = new ArrayList<Esame>();
        int[] conflittiTimeSLot = new int[numTimeslot];
        int cont = 0;
        int indexExtraSlot = 0;
        ArrayList<int[]> tabuList = new ArrayList<int[]>(); // int[] ha elementi [e, t, tenure]: l'esame e e' stato tolto dal timeslot t; ha ancora un tabutenure di tenure
        while (extraTimeSlot.size() > 0) {
            cont = 0;
            System.out.println(extraTimeSlot.size());
            // seleziona un esame da schedulare (casualmente
            //int indexEsameDaSchedulare = random.nextInt(extraTimeSlot.size());
            //Esame esameDaSchedulare = extraTimeSlot.get(indexEsameDaSchedulare);
            Esame esameDaSchedulare = extraTimeSlot.get(indexExtraSlot);
            System.out.println("esame da schedulare: " + esameDaSchedulare.getId());
            slotConMenoConflitti = 0;
            minorConflitti = 1000;

            for (int t = 0; t < numTimeslot; t++) {
                numConflitti = 0;
                for (int i = 0; i < listaEsami.size(); i++) {
                    if (model.getVettoreY()[listaEsami.get(i).getId() - 1][t].get(GRB.DoubleAttr.LB) == 1) {
                        if (matConflitti[listaEsami.get(i).getId() - 1][esameDaSchedulare.getId() - 1] > 0) {
                            numConflitti++;
                        }
                    }
                }
                conflittiTimeSLot[t] = numConflitti;
            }
            do {
                esamiInConflitto.clear();
                mossaproibita = false;
                minorConflitti = getSmallest(conflittiTimeSLot);
                for (int c = 0; c < conflittiTimeSLot.length; c++) {
                    if (conflittiTimeSLot[c] == minorConflitti) {
                        conflittiTimeSLot[c] = 1000;
                        slotConMenoConflitti = c;
                    }
                }
                for (int e = 0; e < listaEsami.size(); e++) {
                    if (model.getVettoreY()[listaEsami.get(e).getId() - 1][slotConMenoConflitti].get(GRB.DoubleAttr.LB) == 1) {
                        if (matConflitti[listaEsami.get(e).getId() - 1][esameDaSchedulare.getId() - 1] > 0) {
                            for (int w = 0; w < tabuList.size(); w++) {
                                if (tabuList.get(w)[1] == -1) {
                                    if (tabuList.get(w)[0] == listaEsami.get(e).getId()) {
                                        mossaproibita = true;
                                        //System.out.println("tento di togliere esame che è appena stato inserito in questo timeslot");
                                        break;
                                    }
                                }
                            }
                            if (!mossaproibita) {
                                esamiInConflitto.add(listaEsami.get(e));
                            }

                        }
                    }
                }

                if (!mossaproibita) {
                    for (int y = 0; y < tabuList.size(); y++) {
                        if (tabuList.get(y)[1] == slotConMenoConflitti) {
                            if (tabuList.get(y)[0] == esameDaSchedulare.getId()) {
                                mossaproibita = true;
                                //System.out.println("tento di rimettere esame appena tolto nello stesso timeslot");
                                break;
                            }
                        }
                    }
                }

                if (!mossaproibita) {
                    for (int r = 0; r < esamiInConflitto.size(); r++) {
                        model.getVettoreY()[esamiInConflitto.get(r).getId() - 1][slotConMenoConflitti].set(GRB.DoubleAttr.LB, 0);
                        modello.update();
                        extraTimeSlot.add(esamiInConflitto.get(r));
                        tabuList.add(creaTabuMove(esamiInConflitto.get(r).getId(), slotConMenoConflitti, tabu_tenure));
                    }

                    model.getVettoreY()[esameDaSchedulare.getId() - 1][slotConMenoConflitti].set(GRB.DoubleAttr.LB, 1);
                    tabuList.add(creaTabuMove(esameDaSchedulare.getId(), -1, tabu_tenure)); //con -1 ci riferiamo all'extratimeslot
                    extraTimeSlot.remove(indexExtraSlot);
                    modello.update();
                } else {
                    System.out.println("La mossa non è permessa" + cont++);
                }
            } while (mossaproibita && cont < numTimeslot);

            if (!mossaproibita) {
                for (int y = 0; y < tabuList.size(); y++) {
                    if (tabuList.get(y)[2] == 1) {
                        tabuList.remove(y);
                    } else {
                        tabuList.get(y)[2]--;
                    }

                }
                indexExtraSlot = 0;
                Collections.shuffle(extraTimeSlot);
            } else {
                indexExtraSlot++;
            }

            if (indexExtraSlot >= extraTimeSlot.size()) {
                tabuList = defaultAspirationCriteria(tabuList);
                indexExtraSlot = 0;
            }
        }

        /**   parte di codice per scrivere su file di testo e per poter verificare che soluzione trovata sia valida attraverso ETPchecker   */
        for (int t = 0; t < numTimeslot; t++) {
            for (int i = 0; i < listaEsami.size(); i++) {
                if (model.getVettoreY()[listaEsami.get(i).getId() - 1][t].get(GRB.DoubleAttr.LB) == 1) {
                    stringa.append(listaEsami.get(i).getId() + " " + (t + 1) + "\n");
                }
            }
        }
        stampaModello(stringa);
    }

    /**   metdo per creare le mosse proibite   */
    private static int[] creaTabuMove(int e, int t, int tabu_tenure) {
        int[] tabuMove = new int[3];
        tabuMove[0] = e;
        tabuMove[1] = t;
        tabuMove[2] = tabu_tenure;
        return tabuMove;
    }

    /**   metodo di aspiration criteria  */
    private static ArrayList<int[]> defaultAspirationCriteria(ArrayList<int[]> tabuList) {
        //libero mossa più prossima a scadere
        int mossaConTenureMinimo = 0;
        for (int i = 0; i < tabuList.size(); i++) {
            if (tabuList.get(i)[2] < tabuList.get(mossaConTenureMinimo)[2]) {
                mossaConTenureMinimo = i;
            }
        }
        System.out.println("Default asp crit: libero mossa " + mossaConTenureMinimo);
        tabuList.remove(mossaConTenureMinimo);
        //tabuList.clear(); //opzionale e piu drastico, libero tutto
        return tabuList;
    }

    public static void stampaModello(StringBuffer stringa) {
        String fileName = "C:\\Users\\Utente\\OneDrive\\Desktop\\optimization algoritms\\progetto\\instance\\instance.sol";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            //writer.write(IDesame.toString()+" "+timeslot.toString()+"\n"); // do something with the file we've opened
            writer.write(stringa.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**   metodo per ottenere il valore minimo in un vettore   */
    public static int getSmallest(int[] a) {
        int min = 1000;
        for (int i = 0; i < a.length - 1; i++) {
            if (a[i] < min) {
                min = a[i];
            }
        }
        return min;
    }

    /**   Metodo per migliorare soluzione attraverso LOCAL SEARCH   */
    public static double improvingLocalSearch(ETPmodel model, double bestSolution, ArrayList<Integer> esamiliberati, ArrayList<Double> tempi) throws GRBException {
        System.out.println("improvingLocalSearch");
        double currentSolution = calcolaBestSolutionDelNeighborhood(model, bestSolution, esamiliberati, tempi);

        System.out.println();
        System.out.println("la soluzione migliore è: " + currentSolution);
        System.out.println();
        return currentSolution;
    }

    /**   Metodo per migliorare soluzione attraverso LOCAL SEARCH   */
    private static double calcolaBestSolutionDelNeighborhood(ETPmodel model, double bestSolution, ArrayList<Integer> esamiliberati, ArrayList<Double> tempi) throws GRBException {
        ArrayList<Esame> listaEsami = new ArrayList<Esame>(model.getIstanza().getEsami());
        Random random = new Random();
        int totEsami=30;
        int cont=0;
        int[] indiciEsami=new int[totEsami];
        int[] slotsEsami =new int[totEsami];
        int numeroSlot = model.getIstanza().getLunghezzaExaminationPeriod();
        boolean diverso;
        int number;
        long start1 = System.nanoTime();
        for(int i=0;i<totEsami;i++){
            do {
                diverso = true;
                number = random.nextInt(model.getIstanza().getEsami().size());
                for (int k = 0; k < (i - 1); k++) {
                    if (number == indiciEsami[k]) {
                        diverso = false;
                        break;
                    }
                }
            }while(diverso == false);
            indiciEsami[i]=number;
            //System.out.println(indiciEsami[i]);
            slotsEsami[i]=cercaSlotEsame(indiciEsami[i], model.getVettoreY());
            //System.out.println("esame estratto: " + indiciEsami[i] + " slot: " + slotsEsami[i]);
        }
        boolean esameEstrattoHasConflitti = false;
        boolean slotIsUsabile = true;
        int[][] matConflitti = model.getIstanza().getConflitti();
        ArrayList<Integer> listaSlotDisponibili = new ArrayList<>();

        for(int j=0;j<totEsami;j++) {
            esameEstrattoHasConflitti = false;
            for (int i = 0; i < matConflitti[indiciEsami[j]].length; i++) {
                if (matConflitti[indiciEsami[j]][i] > 0) {
                    esameEstrattoHasConflitti = true;
                    break;
                }
            }
            if (esameEstrattoHasConflitti) {
                for (int t = 0; t < numeroSlot; t++) {
                    slotIsUsabile = true;
                    if (t != slotsEsami[j]) {
                        for (int e = 0; e < listaEsami.size(); e++) {
                            if (model.getVettoreY()[e][t].get(GRB.DoubleAttr.LB) == 1) {
                                if (matConflitti[indiciEsami[j]][e] > 0) {
                                    slotIsUsabile = false;
                                    break;
                                }
                            }
                        }
                        if (slotIsUsabile) {
                            model.getVettoreY()[indiciEsami[j]][slotsEsami[j]].set(GRB.DoubleAttr.LB, 0);
                            cont++;
                            break;
                        }
                    }
                }
            }
        }
        model.solve();
        try {
            for(int j=0;j<totEsami;j++){
                for (int l = 0; l < numeroSlot; l++) {
                    if (model.getVettoreY()[indiciEsami[j]][l].get(GRB.DoubleAttr.X) == 1) {
                        model.getVettoreY()[indiciEsami[j]][l].set(GRB.DoubleAttr.LB, 1);
                        break;
                    }
                }
            }
        } catch (GRBException e) {
            e.printStackTrace();
            System.err.println("Il modello e' infeasible: non posso leggere i val de");
        }
        long end1 = System.nanoTime();
        double durata_esec1 = (end1 - start1) / Math.pow(10, 9);
        tempi.add(durata_esec1);
        esamiliberati.add(cont);
        return model.getObjValue();
    }

    /**   metodo che ritorna in che time slot è inserito uno specifico esame  */
    private static int cercaSlotEsame(int indiceEsame, GRBVar[][] vettoreY) throws GRBException {
        for (int t = 0; t < vettoreY[indiceEsame].length; t++) {
            if (vettoreY[indiceEsame][t].get(GRB.DoubleAttr.LB) == 1) {
                return t;
            }
        }
        return 0; //ma in teoria non ci dovrebbe arrivare qua
    }

    /**    metodo per migliorare soluzione con simulated annealing    */
    public static double improvingWithSimulatedAnnealing(ETPmodel model, double T, ArrayList<Integer> contatore, double Solution) throws GRBException {
        GRBModel modello=model.getModel();
        int[][] matConflitti = model.getIstanza().getConflitti();
        GRBVar[][] vettoreY = model.getVettoreY();
        int indiceEsame2=0;
        int indiceEsame1=0;
        boolean flag=true;
        int maxIteration=200;
        int cont=0;
        do{
            indiceEsame1 = estraiEsameConConflitto(matConflitti);
            do{
                indiceEsame2 = estraiEsameConConflitto(matConflitti);
            }while(indiceEsame1==indiceEsame2 || cercaSlotEsame(indiceEsame1, vettoreY) == cercaSlotEsame(indiceEsame2, vettoreY));
            flag=hasConflittiNelNuovoTimeslot(indiceEsame1, indiceEsame2, vettoreY, matConflitti, model);
            cont++;
        }while(flag && cont<maxIteration);

        if (!flag) {
            contatore.add(1);
            System.out.println("scambio esame1: " + indiceEsame1 + " con esame2: " + indiceEsame2);
            double currentBestObjValue = Solution;
            int slotEsame1 = cercaSlotEsame(indiceEsame1, vettoreY);
            int slotEsame2 = cercaSlotEsame(indiceEsame2, vettoreY);

            vettoreY[indiceEsame1][slotEsame1].set(GRB.DoubleAttr.LB, 0);
            vettoreY[indiceEsame1][slotEsame2].set(GRB.DoubleAttr.LB, 1);
            vettoreY[indiceEsame2][slotEsame2].set(GRB.DoubleAttr.LB, 0);
            vettoreY[indiceEsame2][slotEsame1].set(GRB.DoubleAttr.LB, 1);

            modello.update();
            modello.optimize();

           // se è una sol migliore: accetto, else accetto con probabilità p
            if (model.getObjValue() < currentBestObjValue) {
                return model.getObjValue();
                //accetto la soluzione
            } else {
                // simulated annealing: la accetto con prob p
                double p = Math.exp(-(model.getObjValue() - currentBestObjValue) / T);
                Random random = new Random();
                double randomValue = random.nextDouble();

                if (randomValue < p) {
                    System.out.println("sim annealing: accetto");
                    return model.getObjValue();
                } else {
                    System.out.println("sim annealing: non accetto");
                    vettoreY[indiceEsame1][slotEsame1].set(GRB.DoubleAttr.LB, 1);
                    vettoreY[indiceEsame1][slotEsame2].set(GRB.DoubleAttr.LB, 0);
                    vettoreY[indiceEsame2][slotEsame2].set(GRB.DoubleAttr.LB, 1);
                    vettoreY[indiceEsame2][slotEsame1].set(GRB.DoubleAttr.LB, 0);
                    modello.update();
                    modello.optimize();
                    Solution=model.getObjValue();
                }
            } //fine simulated annealing
        } else {
            System.out.println("numero massimo di iterazioni raggiunto");
        }
        return Solution;
    }

    /**    metodo per migliorare soluzione con great deluge   */
    public static double improvingWithGreatDeluge(ETPmodel model, double Solution) throws GRBException {
            GRBModel modello=model.getModel();
            int[][] matConflitti = model.getIstanza().getConflitti();
            GRBVar[][] vettoreY = model.getVettoreY();
            int indiceEsame2=0;
            int indiceEsame1=0;
            boolean flag=true;
            int maxIteration=100;
            int cont=0;

            //var per great deluge
            double B = Solution;
            double deltaB = 0.1;

            while(flag && cont<maxIteration){
                indiceEsame1 = estraiEsameConConflitto(matConflitti);
                do{
                    indiceEsame2 = estraiEsameConConflitto(matConflitti);
                }while(indiceEsame1==indiceEsame2 || cercaSlotEsame(indiceEsame1, vettoreY) == cercaSlotEsame(indiceEsame2, vettoreY));
                flag=hasConflittiNelNuovoTimeslot(indiceEsame1, indiceEsame2, vettoreY, matConflitti, model);
                cont++;
            }

            if (!flag) {
                System.out.println("scambio esame1: " + indiceEsame1 + " con esame2: " + indiceEsame2);
                double currentBestObjValue = Solution;
                int slotEsame1 = cercaSlotEsame(indiceEsame1, vettoreY);
                int slotEsame2 = cercaSlotEsame(indiceEsame2, vettoreY);

                vettoreY[indiceEsame1][slotEsame1].set(GRB.DoubleAttr.LB, 0);
                vettoreY[indiceEsame1][slotEsame2].set(GRB.DoubleAttr.LB, 1);
                vettoreY[indiceEsame2][slotEsame2].set(GRB.DoubleAttr.LB, 0);
                vettoreY[indiceEsame2][slotEsame1].set(GRB.DoubleAttr.LB, 1);

                modello.update();
                modello.optimize();

                // great deluge: alternativo a simulated annealing            double B = model.getObjValue();
                if (model.getObjValue() <= currentBestObjValue || model.getObjValue() <= B) {
                    currentBestObjValue = model.getObjValue();
                } else {
                      System.out.println("great deluge: non accetto");
                      vettoreY[indiceEsame1][slotEsame1].set(GRB.DoubleAttr.LB, 1);
                      vettoreY[indiceEsame1][slotEsame2].set(GRB.DoubleAttr.LB, 0);
                      vettoreY[indiceEsame2][slotEsame2].set(GRB.DoubleAttr.LB, 1);
                      vettoreY[indiceEsame2][slotEsame1].set(GRB.DoubleAttr.LB, 0);
                      modello.update();
                      modello.optimize();
                      Solution=model.getObjValue();
                }
                B = B - deltaB;
            } else {
                System.out.println("numero massimo di iterazioni raggiunto");
            }
            return Solution;
        }

    /**    metodo usato per estrarre un esame che ha almeno un conflitto con un altro esame nel problema considerato   */
    private static int estraiEsameConConflitto(int[][] matConflitti) {
        Random random = new Random();
        int indiceEsame;
        boolean hasConflitti = false;
        do {
            indiceEsame = random.nextInt(matConflitti.length);
            int e = 0;
            do {
                hasConflitti = (matConflitti[indiceEsame][e] > 0);
                e++;
            } while (!hasConflitti && e<matConflitti.length);
        } while (!hasConflitti);
        return indiceEsame;
    }

    /**    metodo che verifica se due esami possono essere scambiati tra loro, ovvero se entrambi nel nuovi time slot non hanno conflitti  */
    private static boolean hasConflittiNelNuovoTimeslot(int indiceEsame1, int indiceEsame2, GRBVar[][] vettoreY, int[][] matConflitti, ETPmodel model) throws GRBException {
        int slotEsame1 = cercaSlotEsame(indiceEsame1, vettoreY);
        int slotEsame2 = cercaSlotEsame(indiceEsame2, vettoreY);
        ArrayList<Integer> listaEsamiSlot1 = getListaEsamiSlot(slotEsame1, vettoreY, model);
        ArrayList<Integer> listaEsamiSlot2 = getListaEsamiSlot(slotEsame2, vettoreY, model);

        //System.out.println("lista esami slot 1");
        for (int idEsame : listaEsamiSlot1) {
            //System.out.println(idEsame);
            if(matConflitti[idEsame][indiceEsame2]>0){
                return true;
            }
        }
        //System.out.println("lista esami slot 2");
        for (int idEsame : listaEsamiSlot2) {
            //System.out.println(idEsame);
            if (matConflitti[idEsame][indiceEsame1] > 0) {
                return true;
            }
        }
        return false;
    }

    /**    metodo per estrarre esami presenti in uno specifico time slot   */
    private static ArrayList<Integer> getListaEsamiSlot(int slot, GRBVar[][] vettoreY, ETPmodel model) throws GRBException {
        ArrayList<Esame> listaEsami = new ArrayList<Esame>(model.getIstanza().getEsami());
        ArrayList<Integer> listaEsamiSlot = new ArrayList<>();
        for (int e=0; e<listaEsami.size(); e++){
            if(vettoreY[e][slot].get(GRB.DoubleAttr.LB)==1){
                listaEsamiSlot.add(e);
            }
        }
        return listaEsamiSlot;
    }
}


