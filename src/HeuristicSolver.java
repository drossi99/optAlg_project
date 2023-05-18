import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import gurobi.*;


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

        for (int i = 1; i < lista.size() / 2; i++) {
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

        for (int i = 0; i < gradi.length; i++) {
            for (int j = i + 1; j < gradi.length; j++) {
                if (gradi[i] >= gradi[j]) {
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

    public static void provaSoluzioneIniziale(ETPmodel model) throws GRBException {
        GRBModel modello = model.getModel();
        int[][] matConflitti = model.getIstanza().getConflitti();
        boolean isAssegnato;
        ArrayList<Esame> esamiOrdinati = new ArrayList<Esame>(model.getIstanza().getEsami());
        ordina(esamiOrdinati, model.getIstanza().getConflitti());

        // assegno il primo esame al primo slot
        model.getVettoreY()[esamiOrdinati.get(0).getId() - 1][0].set(GRB.DoubleAttr.LB, 1);
        modello.update();
        System.out.println(("settiamo y[" + (esamiOrdinati.get(0).getId()) + "][" + 1 + "] a " + model.getVettoreY()[esamiOrdinati.get(0).getId() - 1][0].get(GRB.DoubleAttr.LB)));

        for (int e = 1; e < esamiOrdinati.size(); e++) {
            isAssegnato = false;

            do {
                for (int t = 0; t < model.getIstanza().getLunghezzaExaminationPeriod(); t++) {
                    for (int i = 0; i < e; i++) {
                        if (matConflitti[esamiOrdinati.get(e).getId() - 1][esamiOrdinati.get(i).getId() - 1] > 0) {
                            System.out.println("lo slot " + t + " è occupato da " + (esamiOrdinati.get(i).getId()-1));

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
    }
}
