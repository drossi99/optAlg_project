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
        String t="instance09";
        
        Istanza istanza = new Istanza("src/instance/"+t+".exm", "src/instance/"+t+".slo", "src/instance/"+t+".stu");
        ETPmodel model = new ETPmodel(istanza, 5);

        model.buildModel();
        //model.getModel().write("modello.lp");
		//model.solve();
        model.heurSolve();
        //model.rilassato();


		//model.stampaVariabiliY(istanza.getEsami(),istanza.getLunghezzaExaminationPeriod());
		//model.stampaVariabiliU(istanza.getEsami(), istanza.getConflitti(), 5);
        //Utility.stampaTabConflitti(istanza.getConflitti());





    }

}
