import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import gurobi.*;
import java.io.BufferedWriter;
import java.io.FileWriter;

public class HeuristicSolver {

    public static void calcolaSoluzioneIniziale(ETPmodel modello) throws GRBException {
        assegnaColoriOrdinamentoCasuale(modello);
        //modello.stampaVariabiliY(modello.getIstanza().getEsami(), modello.getIstanza().getLunghezzaExaminationPeriod());
        return;
    }

    public static void assegnaColoriOrdinamentoCasuale(ETPmodel modello) throws GRBException {
        GRBModel model = modello.getModel();
        //ArrayList<Esame> listanoordinata = new ArrayList<Esame>();
        ArrayList<Esame> lista = new ArrayList<Esame>();
        lista.addAll(modello.getIstanza().getEsami());
        //listanoordinata.addAll(modello.getIstanza().getEsami());
        //lista=ordina(listanoordinata, modello.getIstanza().getConflitti());
        //Collections.shuffle(lista);

        modello.getVettoreY()[lista.get(0).getId() - 1][0].set(GRB.DoubleAttr.LB, 1); //imposta la y del primo esame di lista a 1 sullo slot 0
        model.update();
        System.out.println(("settiamo y[" + (lista.get(0).getId()) + "][" + 1 + "] a " + modello.getVettoreY()[lista.get(0).getId() - 1][0].get(GRB.DoubleAttr.LB)));
        int cont = 1;
        int[][] conflitti = modello.getIstanza().getConflitti();
        int min_t;

        boolean hasCambiato = false;

        for (int i = 1; i < lista.size(); i++) {
            ArrayList<Integer> adiacenti = calcolaEsamiAdiacenti(lista.get(i).getId(), conflitti);

            min_t = 0;
            hasCambiato = false;
            for (int t = 0; t < modello.getIstanza().getLunghezzaExaminationPeriod(); t++) {
                for (int j = 0; j < adiacenti.size(); j++) {

                    //System.out.println(modello.getVettoreY()[adiacenti.get(j)-1][t].get(GRB.DoubleAttr.LB));
                    if (modello.getVettoreY()[adiacenti.get(j) - 1][t].get(GRB.DoubleAttr.LB) == 1.0) {
                        min_t = t + 1;
                        hasCambiato = true;
                        break;
                    }
                }
                if (!hasCambiato) {
                    break;
                }

            }
            modello.getVettoreY()[lista.get(i).getId() - 1][min_t].set(GRB.DoubleAttr.LB, 1.0);
            cont++;
            model.update();
            System.out.println(("settiamo y[" + (lista.get(i).getId()) + "][" + (min_t + 1) + "] a " + modello.getVettoreY()[lista.get(i).getId() - 1][min_t].get(GRB.DoubleAttr.LB)));
            System.out.println(cont);
        }


    }

    private static ArrayList<Integer> calcolaEsamiAdiacenti(int idEsame, int[][] conflitti) {
        ArrayList<Integer> adiacenti = new ArrayList<Integer>();

        for (int i = 0; i < conflitti.length; i++) {
            if (conflitti[idEsame - 1][i] > 0) {
                adiacenti.add(i + 1); //viene aggiunto l'ID dell'esame, non l'indice
            }
        }
        return adiacenti;
    }

    /**
     * Ordina gli esami in base al grado (numero di conflitti)
     */
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
        /*for (int i = 0; i < esami.length; i++) {
            System.out.println(gradi[i]);
        }*/

        ArrayList<Esame> listesami = new ArrayList<Esame>();
        Collections.addAll(listesami, esami);
        return listesami;
    }

    public static void provaSoluzioneIniziale(ETPmodel model) throws GRBException {
        int cont=0;
        StringBuffer stringa= new StringBuffer();
        GRBModel modello = model.getModel();
        ArrayList<Integer> slotOccupati = new ArrayList<Integer>();
        int slotlibero;
        int[][] matConflitti = model.getIstanza().getConflitti();
        boolean isAssegnato=false;
        ArrayList<Esame> esami = new ArrayList<Esame>(model.getIstanza().getEsami());
        ArrayList<Esame> esamiOrdinati = new ArrayList<Esame>();
        esamiOrdinati=ordina(esami, model.getIstanza().getConflitti());
        ArrayList<Esame> extraTimeSlot = new ArrayList<>();


        // assegno il primo esame al primo slot
        model.getVettoreY()[esamiOrdinati.get(0).getId() - 1][0].set(GRB.DoubleAttr.LB, 1);
        modello.update();
        stringa.append(esamiOrdinati.get(0).getId()+" 1\n");
        cont++;
        //System.out.println(("settiamo y[" + (esamiOrdinati.get(0).getId()) + "][" + 1 + "] a " + model.getVettoreY()[esamiOrdinati.get(0).getId() - 1][0].get(GRB.DoubleAttr.LB)));

        for (int e = 1; e < esamiOrdinati.size(); e++) {
            slotOccupati.clear();
            for (int i = 0; i < e; i++) {
                if (matConflitti[esamiOrdinati.get(e).getId() - 1][esamiOrdinati.get(i).getId() - 1] > 0) {
                    for (int t = 0; t < model.getIstanza().getLunghezzaExaminationPeriod(); t++) {
                        if(model.getVettoreY()[esamiOrdinati.get(i).getId()-1][t].get(GRB.DoubleAttr.LB)==1){
                            slotOccupati.add(t);
                        }
                    }
                }
            }

            /*if((esamiOrdinati.get(e).getId()-1)==43){
                slotOccupati.forEach((i)-> System.out.println(i));
            }*/

            for(int q = 0; q < model.getIstanza().getLunghezzaExaminationPeriod(); q++ ){
                isAssegnato=true;
                for (int z = 0; z<slotOccupati.size(); z++){
                    if(q == slotOccupati.get(z)){
                        //System.out.println("q "+q+"= "+slotOccupati.get(z));
                        isAssegnato=false;

                    }
                }
                if(isAssegnato) {
                    model.getVettoreY()[esamiOrdinati.get(e).getId() - 1][q].set(GRB.DoubleAttr.LB, 1);
                    modello.update();
                    stringa.append(esamiOrdinati.get(e).getId()+" "+(q+1)+"\n");
                    cont++;
                    //System.out.println(("settiamo y[" + (esamiOrdinati.get(e).getId()) + "][" + (q + 1) + "] a " + model.getVettoreY()[esamiOrdinati.get(e).getId() - 1][q].get(GRB.DoubleAttr.LB)));
                    break;
                }

            }
            if (!isAssegnato) {
                extraTimeSlot.add(esamiOrdinati.get(e));
                System.out.println("ESAME EXTRA: "+esamiOrdinati.get(e).getId());
            }
        }


        /*
        for (int e = 1; e < esamiOrdinati.size(); e++) {
            isAssegnato = false;

            do {
                for (int t = 0; t < model.getIstanza().getLunghezzaExaminationPeriod(); t++) {
                    for (int i = 0; i < e; i++) {
                        if (matConflitti[esamiOrdinati.get(e).getId() - 1][esamiOrdinati.get(i).getId() - 1] > 0) {
                            //System.out.println("esame " + (esamiOrdinati.get(e).getId() - 1)  + " e esame " + (esamiOrdinati.get(i).getId()-1) + " sono in conflitto ");
                            //System.out.println("lo slot " + t + " è occupato da " + (esamiOrdinati.get(i).getId()-1));

                        } else {
                            System.out.println("lo slot " + t + " è libero -> lo assegno a " + (esamiOrdinati.get(e).getId()-1));
                            model.getVettoreY()[e][t].set(GRB.DoubleAttr.LB, 1);
                            modello.update();
                            isAssegnato = true;
                            break;
                        }
                        if (isAssegnato) {
                            break;
                        }
                    }
                    if (isAssegnato) {
                        break;
                    }
                }

            } while (!isAssegnato);


        }
        */
        //stampaModello(stringa);
        System.out.println(cont);

        if(extraTimeSlot.size()>0){
            tabuSearch(extraTimeSlot, model);
        }


    }

    public static void tabuSearch(ArrayList<Esame> extraTimeSlot, ETPmodel model) throws GRBException {
        StringBuffer stringa= new StringBuffer();
        GRBModel modello = model.getModel();
        ArrayList<Esame> listaEsami = new ArrayList<Esame>(model.getIstanza().getEsami());
        int[][] matConflitti = model.getIstanza().getConflitti();
        int tabu_tenure = model.getIstanza().getLunghezzaExaminationPeriod();
        tabu_tenure=2;
        int numTimeslot = model.getIstanza().getLunghezzaExaminationPeriod();
        int slotConMenoConflitti = 0;
        int minorConflitti = model.getIstanza().getEsami().size();
        int numConflitti = 0;
        int trueMinorConflitti;
        int trueslotConMenoConflitti = 0;
        Random random = new Random();
        boolean mossaproibita=false;
        boolean mossaobbligata=false;
        ArrayList<Esame> esamiInConflitto = new ArrayList<Esame>();
        int []conflittiTimeSLot=new int[numTimeslot];
        int cont=0;
        int indexExtraSlot=0;


        ArrayList<int[]> tabuList = new ArrayList<int[]>(); // int[] ha elementi [e, t, tenure]: l'esame e e' stato tolto dal timeslot t; ha ancora un tabutenure di tenure

        while (extraTimeSlot.size() > 0) {
            cont=0;

            System.out.println(extraTimeSlot.size());
            // seleziona un esame da schedulare (casualmente
            //int indexEsameDaSchedulare = random.nextInt(extraTimeSlot.size());
            //Esame esameDaSchedulare = extraTimeSlot.get(indexEsameDaSchedulare);
            Esame esameDaSchedulare=extraTimeSlot.get(indexExtraSlot);
            System.out.println("esame da schedulare: " +esameDaSchedulare.getId());
            slotConMenoConflitti = 0;
            minorConflitti=1000;

            for (int t = 0; t < numTimeslot; t++) {
                numConflitti = 0;
                for(int i = 0; i < listaEsami.size();i++) {
                    if (model.getVettoreY()[listaEsami.get(i).getId() - 1][t].get(GRB.DoubleAttr.LB) == 1) {
                        if (matConflitti[listaEsami.get(i).getId() - 1][esameDaSchedulare.getId() - 1] > 0) {
                            numConflitti++;
                        }
                    }
                }
                conflittiTimeSLot[t]=numConflitti;
                /*if (numConflitti < minorConflitti) {
                    minorConflitti = numConflitti;
                    slotConMenoConflitti = t;
                }*/
            }

            /*trueMinorConflitti=getSmallest(conflittiTimeSLot);
            for (int c = 0; c < conflittiTimeSLot.length; c++) {
                if (conflittiTimeSLot[c] == trueMinorConflitti) {
                    conflittiTimeSLot[c] = 1000;
                    trueslotConMenoConflitti = c;
                }
            }
            */

            do {

                esamiInConflitto.clear();
                mossaproibita=false;
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

                /*if(cont==numTimeslot){
                    esamiInConflitto.clear();
                    for (int e = 0; e < listaEsami.size(); e++) {
                        if (model.getVettoreY()[listaEsami.get(e).getId() - 1][trueslotConMenoConflitti].get(GRB.DoubleAttr.LB) == 1) {
                            if (matConflitti[listaEsami.get(e).getId() - 1][esameDaSchedulare.getId() - 1] > 0) {
                                esamiInConflitto.add(listaEsami.get(e));
                            }
                        }
                    }

                    for (int r = 0; r < esamiInConflitto.size(); r++) {
                        model.getVettoreY()[esamiInConflitto.get(r).getId() - 1][trueslotConMenoConflitti].set(GRB.DoubleAttr.LB, 0);
                        modello.update();
                        extraTimeSlot.add(esamiInConflitto.get(r));
                        tabuList.add(creaTabuMove(esamiInConflitto.get(r).getId(), trueslotConMenoConflitti, tabu_tenure));
                    }

                    model.getVettoreY()[esameDaSchedulare.getId() - 1][trueslotConMenoConflitti].set(GRB.DoubleAttr.LB, 1);
                    tabuList.add(creaTabuMove(esameDaSchedulare.getId(), -1, tabu_tenure)); //con -1 ci riferiamo all'extratimeslot
                    extraTimeSlot.remove(indexEsameDaSchedulare);
                    modello.update();
                    mossaproibita=false;
                }*/
            }while(mossaproibita && cont<numTimeslot);

            if(!mossaproibita) {
                for (int y = 0; y < tabuList.size(); y++) {
                    if (tabuList.get(y)[2] == 1) {
                        tabuList.remove(y);
                    } else {
                        tabuList.get(y)[2]--;
                    }

                }
                indexExtraSlot=0;
                Collections.shuffle(extraTimeSlot);
            } else{
                indexExtraSlot++;
            }

            if(indexExtraSlot>=extraTimeSlot.size()){
                tabuList = defaultAspirationCriteria(tabuList);
                indexExtraSlot=0;
            }
            /*else {
                //if se abbiamo controllato gia tutti gli esami e non possiamo fare nulla
                tabuList = defaultAspirationCriteria(tabuList);
            }*/

        }
        for (int t = 0; t < numTimeslot; t++) {
            for(int i = 0; i < listaEsami.size();i++) {
                if (model.getVettoreY()[listaEsami.get(i).getId() - 1][t].get(GRB.DoubleAttr.LB) == 1) {
                    stringa.append(listaEsami.get(i).getId()+" "+(t+1)+"\n");
                }

            }
        }
        stampaModello(stringa);

    }

    private static int[] creaTabuMove(int e, int t, int tabu_tenure) {
        int[] tabuMove = new int[3];
        tabuMove[0] = e;
        tabuMove[1] = t;
        tabuMove[2] = tabu_tenure;
        return tabuMove;
    }

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

        try(BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))){
            //writer.write(IDesame.toString()+" "+timeslot.toString()+"\n"); // do something with the file we've opened
             writer.write(stringa.toString());
        }
        catch(IOException e){
            e.printStackTrace();
        }
	}

    public static int getSmallest(int[] a){
        int min=1000;
        for (int i = 0; i < a.length-1; i++){
            if(a[i]<min){
                min=a[i];
            }
        }
        return min;
    }

    public static double improvingLocalSearch(ETPmodel model, double bestSolution) throws GRBException {
        System.out.println("improvingLocalSearch");
        ArrayList<GRBModel> neighborhood = new ArrayList<>();
        double currentSolution = calcolaBestSolutionDelNeighborhood(model,bestSolution);


        System.out.println();
        System.out.println("la soluzione migliore è: "+currentSolution);
        System.out.println();

        return currentSolution;

    }

    /*
    |0|1|2|3|4|5|6|7|8|9|10|11|12|13|14|
     0   6   5     3   4
     1
     2
    */


    private static double calcolaBestSolutionDelNeighborhood(ETPmodel model, double bestSolution) throws GRBException {
        StringBuffer stringa= new StringBuffer();
        ArrayList<Esame> listaEsami = new ArrayList<Esame>(model.getIstanza().getEsami());
        Random random = new Random();
        ArrayList<GRBModel> neighborhood = new ArrayList<>();
        int numeroSlot = model.getIstanza().getLunghezzaExaminationPeriod();
        int indiceEsame1 = random.nextInt(model.getIstanza().getEsami().size());
        int indiceEsame2 = random.nextInt(model.getIstanza().getEsami().size() + 1); //+1 cosi se se esce il numero di esami, facciamo che l'esame 1 viene spostato e non scambiato
        boolean esameEstrattoHasConflitti = false;
        boolean slotIsUsabile = true;
        int[][] matConflitti = model.getIstanza().getConflitti();
        ArrayList<Integer> listaSlotDisponibili = new ArrayList<>();
        ETPmodel bestModel = model;

        int slotEsame1 = cercaSlotEsame(indiceEsame1, model.getVettoreY());
        System.out.println("esame estratto: " + indiceEsame1 + " slot: " + slotEsame1);

        for (int i = 0; i < matConflitti[indiceEsame1].length; i++) {
            if (matConflitti[indiceEsame1][i] > 0) {
                esameEstrattoHasConflitti = true;
                break;
            }
        }

        if (esameEstrattoHasConflitti) {
            for (int t = 0; t < numeroSlot; t++) {
                slotIsUsabile = true;
                if (t != slotEsame1) {
                    for (int e = 0; e < listaEsami.size(); e++) {
                        if (model.getVettoreY()[e][t].get(GRB.DoubleAttr.LB) == 1) {
                            if (matConflitti[indiceEsame1][e] > 0) {
                                slotIsUsabile = false;
                                break;
                            }
                        }
                    }
                    if (slotIsUsabile) {
                        System.out.println("time slot possibile: "+t);
                        listaSlotDisponibili.add(t);
                    }
                }
            }
        }

        //Collections.shuffle(listaSlotDisponibili);
        for (int t : listaSlotDisponibili) {
            System.out.println(t);

            ETPmodel modelloNeighborhood=new ETPmodel(model);
            //GRBModel mod=modelloNeighborhood.getModel();

            modelloNeighborhood.getVettoreY()[indiceEsame1][t].set(GRB.DoubleAttr.LB, 1);
            modelloNeighborhood.getVettoreY()[indiceEsame1][slotEsame1].set(GRB.DoubleAttr.LB, 0);
            //mod.update();
            modelloNeighborhood.solve();

            if(modelloNeighborhood.getObjValue() < bestSolution){
                bestSolution=modelloNeighborhood.getObjValue();
                for (int p = 0; p < numeroSlot; p++) {
                    for(int i = 0; i < listaEsami.size();i++) {
                        if (model.getVettoreY()[listaEsami.get(i).getId() - 1][p].get(GRB.DoubleAttr.LB) == 1) {
                            stringa.append(listaEsami.get(i).getId()+" "+(p+1)+"\n");
                        }

                    }
                }
                stampaModello(stringa);
            }

            modelloNeighborhood.getVettoreY()[indiceEsame1][t].set(GRB.DoubleAttr.LB, 0);
            modelloNeighborhood.getVettoreY()[indiceEsame1][slotEsame1].set(GRB.DoubleAttr.LB, 1);
        }
        return bestSolution;
    }

    private static int cercaSlotEsame(int indiceEsame, GRBVar[][] vettoreY) throws GRBException {
        for (int t = 0; t < vettoreY[indiceEsame].length; t++) {
            if (vettoreY[indiceEsame][t].get(GRB.DoubleAttr.LB) == 1) {
                return t;
            }
        }
        return 0; //ma in teoria non ci dovrebbe arrivare qua
    }


}


