import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;


public class LetturaFile {

    public static ArrayList<Esame> leggiExm(String path) throws FileNotFoundException, IOException {
        ArrayList<Esame> esami = new ArrayList<Esame>();
        BufferedReader reader1 = new BufferedReader(new FileReader(path));
        String line1 = reader1.readLine();
        while(line1!=null && !line1.isBlank()) {
            String[] splitted =line1.split(" ");
            int utenti = Integer.parseInt(splitted[1]);
            int id = Integer.parseInt(splitted[0]);
            esami.add( new Esame(id, utenti));
            line1 = reader1.readLine();
        }
        reader1.close();
        return esami;
    }
    public static int leggiSlo(String path) throws FileNotFoundException, IOException {
        BufferedReader reader = new BufferedReader(new FileReader(path));
        String line = reader.readLine();
        reader.close();
        return Integer.parseInt(line);
    }
    //Da chiamare dopo leggiEXM
    public static ArrayList<Studente> leggiStu(String path, ArrayList<Esame> esami) throws FileNotFoundException, IOException{
         BufferedReader reader = new BufferedReader(new FileReader(path));
         String line = reader.readLine();
         ArrayList<Studente> studenti = new ArrayList<>();
         boolean esiste=false;
         Esame e;
         while(line!=null && !line.isBlank()){
            String[] splitted =line.split(" ");
            String nomeStudente = splitted[0];
            int idEsame = Integer.parseInt(splitted[1]);
            e=getEsame(esami, idEsame);
            for (Studente s: studenti) {
                if(s.getName().equals(nomeStudente)){
                    esiste= true;
                    s.addEsame(idEsame);
                    e.addStudente(s);
                }
            }
            if(!esiste){
                Studente studente = new Studente(nomeStudente, new ArrayList<Integer>());
                studente.addEsame(idEsame);
                studenti.add(studente);
                e.addStudente(studente);
            }
            esiste=false;
            line = reader.readLine();
        }
        reader.close();
        return studenti;
    }
    public static Esame getEsame(ArrayList<Esame> esami, int idEsame){
        for (Esame e: esami){
            if(e.getId()==idEsame) return e;
        }
        return null;
    }
}
