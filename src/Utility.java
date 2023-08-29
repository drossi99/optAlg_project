import java.util.ArrayList;

public class Utility {
    public static int[][] calcolaConflittiEsami(ArrayList<Esame> esami, ArrayList<Studente> studenti) {
        int n[][] = new int[esami.size()][esami.size()];
        for(int i=0; i<esami.size(); i++){
            for(int j=i+1; j<esami.size(); j++){
                ArrayList<Studente> s1 = new ArrayList<>(esami.get(i).getStudenti());
                s1.retainAll(esami.get(j).getStudenti());
                n[i][j]=s1.size();
                n[j][i]=s1.size();
            }
        }
        return n;   
    }
    public static void stampaTabConflitti(int conflitti[][]){
        for(int i=0; i<conflitti.length; i++){
            for(int j=0; j<conflitti[i].length; j++){
                System.out.print(conflitti[i][j] + "\t");
            }
            System.out.println();
        }
    }
}
