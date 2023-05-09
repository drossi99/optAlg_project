import gurobi.GRBException;

import java.util.*;
import java.io.IOException;
import java.io.FileNotFoundException;


public class Main{
    /*static ArrayList<Esame> esami = null;
    static int T;
    static ArrayList<Studente> studenti = null;*/
    public static void main(String []args) throws FileNotFoundException, IOException, GRBException {
        /*esami = LetturaFile.leggiExm(".\\instance\\test.exm");
        T = LetturaFile.leggiSlo(".\\instance\\test.slo");
        studenti = LetturaFile.leggiStu(".\\instance\\test.stu", esami);*/

        //System.out.println(T);
        
        Istanza istanza = new Istanza("src/instance/instance01.exm", "src/instance/instance01.slo", "src/instance/instance01.stu");
        ETPmodel model = new ETPmodel(istanza, 5);

        model.buildModel();
        model.getModel().write("modello.lp");
		model.solve();

        //esami.forEach((e) -> System.out.println(e.getId() + " " + e.getUtenti_Iscritti()));

        /*for (Studente s : studenti) {
            System.out.println("\n"+s.getName()+ ":");
            for (Integer e : s.getEsami()) {
                System.out.print(e.toString()+" ");
            }
            
        }*/
/*
        for (Esame e : esami) {
            System.out.println("\n"+e.getId()+ ":");
            for (Studente s: e.getStudenti()) {
                System.out.print(s.getName()+" ");
            }
            System.out.println("lista stud: " + e.getStudenti().size()+", tot.iscritti:"+e.getUtenti_Iscritti());
        }
        int numStudenti = studenti.size();
        System.out.println(numStudenti);
        */

        
      //  int conflitti[][] = Utility.calcolaConflittiEsami(esami, studenti);

        
        /*
         * Inizio parte di Gurobi
         */
       /* GRBEnv env = new GRBEnv(istanza.getNome() + ".log");
        // env.set(GRB.IntParam.Threads, 1);
        int presolve = 0; //un num tra -1, 0, 1, 2
        env.set(GRB.IntParam.Presolve, presolve);
        GRBModel modelGRB = new GRBModel(env);


        
        
        GRBLinExp expr= new GRBLinExp();


        model.setObjective(expr, GRB.MINIZIME);
        
*/        
    }

}
