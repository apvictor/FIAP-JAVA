# Arquitetura — Plataforma de Feedback de Aulas

Diagrama dos componentes e dos fluxos de dados. O texto de cada componente está na seção 2 do [README](../README.md).

## Visão geral

```mermaid
flowchart LR
    estudante([Estudante])
    admin([Administradores])

    subgraph az["Azure — resource group rg-feedback"]
        direction LR

        subgraph fa["Function App (Java 21, Consumption)"]
            receber["receber_avaliacao<br/>HTTP POST /api/avaliacao"]
            notificar["notificar_urgencia<br/>Cosmos DB trigger"]
            semanal["gerar_relatorio_semanal<br/>Timer seg 11:00 UTC"]
            manual["gerar_relatorio_manual<br/>HTTP POST /api/relatorio"]
        end

        cosmos[("Cosmos DB serverless<br/>avaliacoes / leases")]
        acs["Communication Services<br/>Email"]
        kv["Key Vault<br/>segredos e destinatários"]
        mi{{"Managed Identity"}}

        subgraph mon["Monitoramento"]
            ai["Application Insights<br/>+ Log Analytics"]
            wb["Workbook"]
            alerts["Alertas Azure Monitor"]
            ag["Action Group"]
        end
    end

    estudante -- "chave de função" --> receber
    receber -- "grava" --> cosmos
    cosmos -- "Change Feed" --> notificar
    semanal -- "consulta 7 dias" --> cosmos
    manual -- "consulta N dias" --> cosmos
    notificar -- "só CRITICA" --> acs
    semanal --> acs
    manual --> acs
    acs -- "e-mail" --> admin

    mi -. "RBAC" .-> cosmos
    mi -. "Secrets User" .-> kv
    fa -. "telemetria" .-> ai
    ai --> wb
    ai --> alerts --> ag -- "e-mail" --> admin
```

## Fluxo de uma avaliação crítica

```mermaid
sequenceDiagram
    actor E as Estudante
    participant R as receber_avaliacao
    participant C as Cosmos DB
    participant N as notificar_urgencia
    participant M as Communication Services
    actor A as Administradores

    E->>R: POST /api/avaliacao {descricao, nota}
    R->>R: valida e classifica (0-3 CRITICA, 4-6 MEDIA, 7-10 BAIXA)
    R->>C: grava avaliação
    R-->>E: 201 {id, urgencia}
    C-)N: Change Feed (lote de documentos)
    alt urgência CRITICA
        N->>M: envia e-mail de urgência
        M-)A: [URGENTE] Avaliação crítica
    else MEDIA ou BAIXA
        N->>N: ignora (sem e-mail)
    end
```

## CI/CD

```mermaid
flowchart LR
    dev([Desenvolvedor]) -- "PR" --> ci
    subgraph gh["GitHub Actions (OIDC, sem segredos de longa duração)"]
        ci["fase-4-ci<br/>mvn verify · bicep build · what-if"]
        deploy["fase-4-deploy<br/>Bicep + functions-action"]
    end
    ci -- "merge na main" --> deploy
    deploy -- "login OIDC" --> id["Managed Identity<br/>id-gh-fase4"]
    id --> rg["rg-feedback"]
```
