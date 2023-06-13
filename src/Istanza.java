import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class Istanza{
    private ArrayList<Esame> esami = new ArrayList<>();
    private int lunghezzaExaminationPeriod;
    private ArrayList<Studente> studenti = new ArrayList<>();
    private int conflitti[][];
    private String fileNameExm;
    private String fileNameSlo;
    private String fileNameStu;
    //private final LetturaFile lf;

    public String getFileNameExm() {
        return fileNameExm;
    }

    public String getFileNameSlo() {
        return fileNameSlo;
    }

    public void setFileNameSlo(String fileNameSlo) {
        this.fileNameSlo = fileNameSlo;
    }

    public String getFileNameStu() {
        return fileNameStu;
    }

    public void setFileNameStu(String fileNameStu) {
        this.fileNameStu = fileNameStu;
    }

    public void setFileNameExm(String fileNameExm) {
        this.fileNameExm = fileNameExm;
    }

    public Istanza(String fileNameExm, String fileNameSlo, String fileNameStu) throws FileNotFoundException, IOException{
        this.fileNameExm=fileNameExm;
        this.fileNameSlo=fileNameSlo;
        this.fileNameStu=fileNameStu;
        leggidati();
    }

    public void leggidati() throws FileNotFoundException, IOException{
        this.esami = LetturaFile.leggiExm(fileNameExm);
        this.lunghezzaExaminationPeriod = LetturaFile.leggiSlo(fileNameSlo);
        this.studenti = LetturaFile.leggiStu(fileNameStu, esami);
        this.conflitti = Utility.calcolaConflittiEsami(esami, studenti);
    }

    public ArrayList<Esame> getEsami() {
        return esami;
    }

    public void setEsami(ArrayList<Esame> esami) {
        this.esami = esami;
    }

    public int getLunghezzaExaminationPeriod() {
        return lunghezzaExaminationPeriod;
    }

    /*public void setLunghezzaExaminationPeriod(int lunghezzaExaminationPeriod) {
        this.lunghezzaExaminationPeriod = lunghezzaExaminationPeriod;
    }*/

    public ArrayList<Studente> getStudenti() {
        return studenti;
    }

    /*public void setStudenti(ArrayList<Studente> studenti) {
        this.studenti = studenti;
    }*/

    public int[][] getConflitti(){
        return this.conflitti;
    }

    public int getTotStudenti(){
        return this.studenti.size();
    }
}