package br.com.fiap.feedback.config;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/** Configuração lida das variáveis de ambiente (App Settings no Azure). */
public class Config {

    private final Function<String, String> env;

    public Config(Function<String, String> env) {
        this.env = env;
    }

    public static Config doAmbiente() {
        return new Config(System::getenv);
    }

    public String cosmosEndpoint() {
        return obrigatoria("COSMOS_ENDPOINT");
    }

    public String cosmosDatabase() {
        return obrigatoria("COSMOS_DATABASE");
    }

    public String cosmosContainer() {
        return obrigatoria("COSMOS_CONTAINER");
    }

    public String emailRemetente() {
        return obrigatoria("EMAIL_SENDER");
    }

    public String acsConnectionString() {
        return obrigatoria("ACS_CONNECTION_STRING");
    }

    public List<String> emailsAdmin() {
        return Arrays.stream(obrigatoria("ADMIN_EMAILS").split(","))
                .map(String::trim)
                .filter(e -> !e.isEmpty())
                .toList();
    }

    /** Notas de 0 até este valor são CRITICA. */
    public int urgenciaCriticaAte() {
        return inteiro("URGENCIA_CRITICA_ATE", 3);
    }

    /** Notas acima do limiar crítico e até este valor são MEDIA. */
    public int urgenciaMediaAte() {
        return inteiro("URGENCIA_MEDIA_ATE", 6);
    }

    private String obrigatoria(String nome) {
        String valor = env.apply(nome);
        if (valor == null || valor.isBlank()) {
            throw new IllegalStateException("Variável de ambiente obrigatória ausente: " + nome);
        }
        return valor;
    }

    private int inteiro(String nome, int padrao) {
        String valor = env.apply(nome);
        return valor == null || valor.isBlank() ? padrao : Integer.parseInt(valor.trim());
    }
}
