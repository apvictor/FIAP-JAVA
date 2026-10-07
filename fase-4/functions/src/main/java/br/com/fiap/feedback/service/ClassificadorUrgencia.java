package br.com.fiap.feedback.service;

import br.com.fiap.feedback.model.Urgencia;

/** Regra de negócio: converte a nota em urgência. */
public class ClassificadorUrgencia {

    private final int criticaAte;
    private final int mediaAte;

    public ClassificadorUrgencia(int criticaAte, int mediaAte) {
        if (criticaAte < 0 || mediaAte < criticaAte || mediaAte > 10) {
            throw new IllegalArgumentException("Limiares inválidos: 0 <= critica <= media <= 10");
        }
        this.criticaAte = criticaAte;
        this.mediaAte = mediaAte;
    }

    public Urgencia classificar(int nota) {
        if (nota <= criticaAte) {
            return Urgencia.CRITICA;
        }
        if (nota <= mediaAte) {
            return Urgencia.MEDIA;
        }
        return Urgencia.BAIXA;
    }
}
