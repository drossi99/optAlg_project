import gurobi.GRBException;
import java.io.IOException;
import java.io.FileNotFoundException;

public class Main{
    public static void main(String []args) throws FileNotFoundException, IOException, GRBException {
        String t="instance08";  //selezione instanza da eseguire
        long start = System.nanoTime();
        Istanza istanza = new Istanza("src/instance/"+t+".exm", "src/instance/"+t+".slo", "src/instance/"+t+".stu");
        ETPmodel model = new ETPmodel(istanza, 5);
        model.buildModel();
        //model.getModel().write("modello.lp");
        long end = System.nanoTime();
        double durata_esec = (end - start) / Math.pow(10, 9);
		//model.solve();
        model.heurSolve();
        System.out.println("creation time of the model in seconds: " + durata_esec);  //tempo di lettura dei file + creazione modello
    }

}
