package br.com.fiap.feedback.email;

import br.com.fiap.feedback.model.Avaliacao;
import br.com.fiap.feedback.model.RelatorioSemanal;
import br.com.fiap.feedback.model.Urgencia;
import java.util.Locale;
import java.util.Map;

/** Monta o conteúdo dos e-mails. O conteúdo vindo de estudantes é escapado no HTML. */
public final class ComposicaoEmail {

    private ComposicaoEmail() {
    }

    public static Mensagem urgencia(Avaliacao a) {
        String texto = """
                Nova avaliação crítica recebida.

                Descrição: %s
                Urgência: %s
                Data de envio: %s
                """.formatted(a.descricao(), a.urgencia(), a.dataEnvio());
        String html = """
                <h2>Nova avaliação crítica recebida</h2>
                <ul>
                  <li><strong>Descrição:</strong> %s</li>
                  <li><strong>Urgência:</strong> %s</li>
                  <li><strong>Data de envio:</strong> %s</li>
                </ul>
                """.formatted(escapar(a.descricao()), a.urgencia(), escapar(a.dataEnvio()));
        return new Mensagem("[URGENTE] Avaliação crítica de aula", texto, html);
    }

    public static Mensagem relatorio(RelatorioSemanal r) {
        StringBuilder t = new StringBuilder();
        StringBuilder h = new StringBuilder();
        String periodo = r.de() + " a " + r.ate();

        t.append("Relatório de feedbacks — ").append(periodo).append("\n\n");
        t.append("Total de avaliações: ").append(r.total()).append('\n');
        t.append("Média das notas: ").append(formatarMedia(r.media())).append("\n\n");
        h.append("<h2>Relatório de feedbacks — ").append(periodo).append("</h2>");
        h.append("<p><strong>Total de avaliações:</strong> ").append(r.total())
                .append("<br><strong>Média das notas:</strong> ").append(formatarMedia(r.media())).append("</p>");

        t.append("Avaliações por dia:\n");
        h.append("<h3>Avaliações por dia</h3><ul>");
        for (Map.Entry<String, Integer> e : r.porDia().entrySet()) {
            t.append("  ").append(e.getKey()).append(": ").append(e.getValue()).append('\n');
            h.append("<li>").append(e.getKey()).append(": ").append(e.getValue()).append("</li>");
        }
        h.append("</ul>");

        t.append("\nAvaliações por urgência:\n");
        h.append("<h3>Avaliações por urgência</h3><ul>");
        for (Urgencia u : Urgencia.values()) {
            int qtd = r.porUrgencia().getOrDefault(u, 0);
            t.append("  ").append(u).append(": ").append(qtd).append('\n');
            h.append("<li>").append(u).append(": ").append(qtd).append("</li>");
        }
        h.append("</ul>");

        t.append("\nAvaliações:\n");
        h.append("<h3>Avaliações</h3><table border=\"1\" cellpadding=\"4\" cellspacing=\"0\">"
                + "<tr><th>Data de envio</th><th>Urgência</th><th>Nota</th><th>Descrição</th></tr>");
        for (Avaliacao a : r.avaliacoes()) {
            t.append("  [").append(a.dataEnvio()).append("] ").append(a.urgencia())
                    .append(" (nota ").append(a.nota()).append("): ").append(a.descricao()).append('\n');
            h.append("<tr><td>").append(escapar(a.dataEnvio())).append("</td><td>").append(a.urgencia())
                    .append("</td><td>").append(a.nota()).append("</td><td>").append(escapar(a.descricao()))
                    .append("</td></tr>");
        }
        h.append("</table>");

        return new Mensagem("Relatório semanal de feedbacks (" + periodo + ")", t.toString(), h.toString());
    }

    static String formatarMedia(double media) {
        return String.format(Locale.forLanguageTag("pt-BR"), "%.2f", media);
    }

    static String escapar(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
