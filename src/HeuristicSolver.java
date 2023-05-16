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
        ArrayList<Esame> listaCasuale = new ArrayList<Esame>();
        listaCasuale.addAll(modello.getIstanza().getEsami());
        Collections.shuffle(listaCasuale);
        modello.getVettoreY()[listaCasuale.get(0).getId()-1][0].set(GRB.DoubleAttr.Start, 1.0); //imposta la y del primo esame di listaCasuale a 1 sullo slot 0
        int[][] conflitti = modello.getIstanza().getConflitti();
        int min_t;

        for (int i=1; i<listaCasuale.size(); i++) {
            ArrayList<Integer> adiacenti=calcolaEsamiAdiacenti(listaCasuale.get(i).getId(), conflitti);

            min_t=-1;
            for(int j=0; j<adiacenti.size(); j++) {
                for(int t=1; t<modello.getIstanza().getLunghezzaExaminationPeriod(); t++){
                    if(modello.getVettoreY()[adiacenti.get(j)-1][t-1].get(GRB.DoubleAttr.Start)==1){
                        if(t>min_t){
                            min_t=t;
                        }

                    }
                }
            }
            modello.getVettoreY()[listaCasuale.get(i).getId()-1][min_t+1].set(GRB.DoubleAttr.Start,1.0);
            System.out.println(("settiamo y[" + (listaCasuale.get(i).getId()-1) + "][" + min_t + "] a 1"));

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
