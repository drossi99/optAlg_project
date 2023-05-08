import java.util.ArrayList;

public class Studente {
    private String name;
    private ArrayList<Integer> esami=new ArrayList<Integer>();
    public Studente(String name, ArrayList<Integer> esami) {
        this.name = name;
        this.esami = esami;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public ArrayList<Integer> getEsami() {
        return esami;
    }
    public void setEsami(ArrayList<Integer> esami) {
        this.esami = esami;
    }

    public void addEsame(Integer esame){
        esami.add(esame);
    }
    
    
}
