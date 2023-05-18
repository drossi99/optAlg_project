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
        GRBModel model=modello.getModel();
        ArrayList<Esame> listaCasuale = new ArrayList<Esame>();
        listaCasuale.addAll(modello.getIstanza().getEsami());
        //Collections.shuffle(listaCasuale);

        modello.getVettoreY()[listaCasuale.get(0).getId()-1][0].set(GRB.DoubleAttr.LB, 1); //imposta la y del primo esame di listaCasuale a 1 sullo slot 0
        model.update();
        System.out.println(("settiamo y[" + (listaCasuale.get(0).getId()) + "][" + 1 + "] a "+ modello.getVettoreY()[listaCasuale.get(0).getId()-1][0].get(GRB.DoubleAttr.LB)));
        int cont=1;
        int[][] conflitti = modello.getIstanza().getConflitti();
        int min_t;

        boolean hasCambiato = false;

        for (int i=1; i<listaCasuale.size(); i++) {
            ArrayList<Integer> adiacenti=calcolaEsamiAdiacenti(listaCasuale.get(i).getId(), conflitti);

            min_t=0;
            hasCambiato = false;
            for(int t=0; t<modello.getIstanza().getLunghezzaExaminationPeriod(); t++){
                    for(int j=0; j<adiacenti.size(); j++) {

                        //System.out.println(modello.getVettoreY()[adiacenti.get(j)-1][t].get(GRB.DoubleAttr.LB));
                        if(modello.getVettoreY()[adiacenti.get(j)-1][t].get(GRB.DoubleAttr.LB)==1.0){
                             min_t = t + 1;
                             hasCambiato=true;
                             break;
                        }
                    }
                if(!hasCambiato){
                    break;
                }

            }
            modello.getVettoreY()[listaCasuale.get(i).getId()-1][min_t].set(GRB.DoubleAttr.LB,1.0);
            cont++;
            model.update();
            System.out.println(("settiamo y[" + (listaCasuale.get(i).getId()) + "][" + (min_t+1) + "] a "+ modello.getVettoreY()[listaCasuale.get(i).getId()-1][min_t].get(GRB.DoubleAttr.LB)));
            System.out.println(cont);
        }


    }

    private static ArrayList<Integer> calcolaEsamiAdiacenti(int idEsame, int[][] conflitti) {
        ArrayList<Integer> adiacenti = new ArrayList<Integer>();

        for(int i = 0; i < conflitti.length; i++) {
            if(conflitti[idEsame-1][i] > 0) {
                adiacenti.add(i + 1); //viene aggiunto l'ID dell'esame, non l'indice
            }
        }
        return adiacenti;
    }
}
