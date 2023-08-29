import java.util.ArrayList;

public class Esame {
    private int id;
    private int Utenti_Iscritti;
    private ArrayList<Studente> studenti= new ArrayList<>();
    public Esame(int id, int Utenti_Iscritti){
        this.id=id;
        this.Utenti_Iscritti=Utenti_Iscritti;//numero iscritti
    }
    public void setId(int id) {
        this.id = id;
    }
    public void setUtenti_Iscritti(int utenti_Iscritti) {
        this.Utenti_Iscritti = utenti_Iscritti;
    }
    public int getId() {
        return id;
    }
    public int getUtenti_Iscritti() {
        return Utenti_Iscritti;
    }
    public void addStudente(Studente s){
        studenti.add(s);
    }
    public ArrayList<Studente> getStudenti() {
        return studenti;
    }
    public void setStudenti(ArrayList<Studente> studenti) {
        this.studenti = studenti;
    }

}
