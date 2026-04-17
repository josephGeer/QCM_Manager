package QCM_manager;

import java.util.List;
import java.util.ArrayList;


public class Question {
    int numero;
    String enonce;
    List<QcmOption> options;
    int reponseCorrecte;

    public Question(int numero, String enonce, List<QcmOption> options, int reponseCorrecte) {
        this.numero = numero;
        this.enonce = enonce;
        this.options = options;
        this.reponseCorrecte = reponseCorrecte;
    }

    public int getNumero(){return numero;}
    public String getEnonce(){return enonce;}
    public List<QcmOption> getOption(){return options;}
    public int getReponseCorrecte(){return reponseCorrecte;}
}