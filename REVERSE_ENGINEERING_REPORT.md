# REVERSE_ENGINEERING_REPORT.md

## Repositório alvo
- **Nome:** web-template
- **URL:** https://github.com/better-care/web-template
- **Branch padrão (upstream):** master
- **Commit-base analisado:** 8115f49 (Next major development version)
- **Versão do projeto:** 6.0.0-SNAPSHOT (pom.xml)
- **Pasta de trabalho local:** C:\Users\User\audit-repos\web-template
- **Pasta de auditoria:** C:\Users\User\audit-repos\audit-work
- **Data/horário da auditoria:** 2026-11-08 (horário local)
- **Ambiente:** Windows, PowerShell 5.1, Git 2.49.0, Python 3.12.14, OpenJDK 17.0.20.1 (Temurin)

---

## A. Diagnóstico geral

### Arquitetura identificada
O projeto `web-template` é uma implementação Java/Kotlin para openEHR CDR (Clinical Data Repository). Converte Archetypes openEHR (formato ADL/.opt) em WebTemplates (formato JSON) e realiza conversão de dados clínicos entre três formatos de Representation Model (RM):

1. **RAW** - objetos RM nativos openEHR
2. **FLAT** - mapa de caminho web template → valor (formato achatado)
3. **STRUCTURED** - JSON hierárquico (estruturado)

### Módulos principais (src/main/kotlin)
- `builder/` - Constrói WebTemplate a partir de Template (openEHR archetype). Ponto de entrada: `WebTemplateBuilder.build()`.
- `converter/` - Conversores entre formatos RAW/FLAT/STRUCTURED.
- `path/` - Parser e avaliador de WebTemplate Path Expression (baseado em FHIRPath), usando ANTLR 4.13.2 (gramática `WebTemplatePathExpression.g4`) + `WebTemplatePathExpressionFacade` + `WebTemplatePathExpressionEvaluator`.
- `model/` - `WebTemplate` (classe principal, serialização JSON via Jackson), `WebTemplateNode`, `WebTemplateInput`, range de ocorrências.

### Tecnologias e dependências
- Java 25, Kotlin 2.4.21, Maven (pom.xml)
- ANTLR 4.13.2 (gramática de expressão de caminho)
- Jackson 2.22.3 (serialização JSON)
- Caffeine 3.3.1 (cache)
- openEHR common libraries (ehr-common 5.0.0): model, utils, time, json-jackson, terminology, xml-jaxb, path, amnode
- JUnit 5 (jupiter 6.1.3), AssertJ 3.27.7, json-unit 6.3.0 (testes)
- OWASP dependency-check 12.1.9 (segurança de dependências, perfil runSonar)
- JaCoCo 0.8.15, Sonar 5.8.0.7211 (coverage/quality)

### Estado de manutenção
- Ativo: releases recentes (5.0.0 em 2026-10-13, 4.3.x em 2026)
- CI: CircleCI (imagem openjdk:25.0.4), código de cobertura via Codecov
- Licença: Apache 2.0
- 429 arquivos Kotlin (src/main), 115 arquivos de teste, 526 recursos de teste

### Mecanismos de testes existentes
- JUnit 5 + AssertJ + json-unit
- Testes de build, conversão, path expression, compatibilidade
- Recursos de teste: arquivos .opt (archetypes openEHR), JSON de composições em formato FLAT
- CircleCI executa `mvn clean install -P code-coverage` e envia resultados para Codecov

### Limitações da investigação
- **Maven (mvn) não instalado** no ambiente → impossível executar `mvn clean install`, build, testes ou dependency-check.
- **Java 17 disponível**, mas o projeto requer **Java 25** → impossível compilar localmente.
- `rg` (ripgrep) não disponível; usado a ferramenta Grep do sistema e `find`.
- Análise baseada exclusivamente em inspeção estática de código-fonte, CHANGELOG, issues e PRs públicos do GitHub. Nenhum teste executado.

---

## B. Problemas encontrados

### Regressões recorrentes (problemas corrigidos que retornaram) — evidência no CHANGELOG.md

| ID | Problema | Versões onde apareceu | Status | Severidade | Evidência |
|----|----------|----------------------|--------|------------|-----------|
| REA-001 | ELEMENT incluindo DATA_VALUE criado para WebTemplate input mesmo se ELEMENT já existir | 4.0.4 (2024-10-11) e 3.2.9 (2024-09-20) | REGRESSÃO (PROVÁVEL) | P2 | CHANGELOG: mesma correção em duas versões distintas |
| REA-002 | `ctx` value não pode ser null | 4.0.4 (2024-10-11) e 3.2.9 (2024-09-20) | REGRESSÃO (PROVÁVEL) | P2 | CHANGELOG: mesma correção em duas versões distintas |
| REA-003 | `feeder_audit`/`originating_system_audit`/time handling em FLAT format input | 4.0.1 (2024-05-23) e 3.2.7 (2024-03-29) | REGRESSÃO (PROVÁVEL) | P2 | CHANGELOG: mesma correção em duas versões distintas |
| REA-004 | NPE quando Locatable de múltipla ocorrência tem nome null | 4.0.1 (2024-05-23) e 3.2.7 (2024-03-29) | REGRESSÃO (PROVÁVEL) | P2 | CHANGELOG: mesma correção em duas versões distintas |

### Issues abertas no GitHub (better-care/web-template)

| ID | Issue | URL | Status | Severidade |
|----|-------|-----|--------|------------|
| REA-005 | #8: CodedText External Valueset perdido quando LimitTo List = False | https://github.com/better-care/web-template/issues/8 | OPEN | P2 |
| REA-006 | #7: Adicionar original_namespace e custodian_namespace ao web template | https://github.com/better-care/web-template/issues/7 | OPEN | P3 |
| REA-007 | #6: Suporte multi-lingual annotations e tratar 'comments' como keyword | https://github.com/better-care/web-template/issues/6 | OPEN | P3 |
| REA-008 | #9: SCM url + ehr-common.version mismatch (5.0.0→6.0.0-SNAPSHOT) | https://github.com/better-care/web-template/pull/9 | OPEN (PR) | P2 |

### Pontos de risco (análise estática, não confirmados por execução)

| ID | Localização | Problema | Status | Severidade | Evidência |
|----|-------------|----------|--------|------------|-----------|
| REA-009 | `WebTemplateInputBuilderDelegator.kt:101` | `requireNotNull` lança exceção "Unsupported type" para tipo RM não mapeado no mapa `typeBuilders` | HIPÓTESE | P2 | Linha 101: `requireNotNull(typeBuilders[...]) { "Unsupported type: ..." }` |
| REA-010 | `WebTemplatePathExpressionEvaluator.kt:150` | Divisão `left.toDouble() / right.toDouble()` sem verificação de divisor zero (retorna Infinity/NaN em Double, não lança exceção) | HIPÓTESE | P3 | Linha 150 do avaliador |
| REA-011 | `WebTemplateBuilder.kt:170` | `requireNotNull(template.language?.codeString)` lança exceção se template não tiver default language | HIPÓTESE | P3 | Linha 170 |

### Sem problemas P0 ou P1 confirmados
Nenhum problema crítico (exposição de dados, comprometimento de segurança, perda irreversível, indisponibilidade generalizada) foi confirmado nesta auditoria.

---

## C. Problemas recorrentes

Os desenvolvedores já tentaram corrigir, mas os problemas persistem/regridem:

1. **ELEMENT/DATA_VALUE duplicado** — corrigido em 4.0.4 e 3.2.9, mesmo texto de correção. Sugere falta de teste de regressão que impeça o retorno.
2. **ctx value null** — corrigido em 4.0.4 e 3.2.9. Mesmo padrão.
3. **feeder_audit time handling** — corrigido em 4.0.1 e 3.2.7.
4. **NPE null name em Locatable multi-ocorrência** — corrigido em 4.0.1 e 3.2.7.

**Hipótese de causa-raiz:** ausência de testes de regressão automatizados que cubram especificamente esses cenários de borda, permitindo que a correção seja desfeita ou esquecida em mergeamentos futuros.

Links: CHANGELOG.md (versões 3.2.7, 3.2.9, 4.0.1, 4.0.4). Nenhuma issue vinculada diretamente no GitHub para essas regressões específicas.

---

## D. Erros não documentados

Erros importantes encontrados pela auditoria que não estavam documentados em issues/PRs:

1. **SCM/ehr-common versão mismatch** (PR #9 aberto): `pom.xml` declara versão 6.0.0-SNAPSHOT mas `ehr-common.version` é 5.0.0, e a URL do SCM aponta para `master/` (mesmo sendo branch real master, o versão parece inconsistente com o estado de desenvolvimento).
2. **Tipo RM não mapeado lança exceção não tratada**: `WebTemplateInputBuilderDelegator.delegate()` lança `requireNotNull` com mensagem "Unsupported type" se o tipo RM não estiver em `typeBuilders`. Não há fallback nem tratamento gracioso.
3. **Divisão por zero silenciosa** no avaliador de expressões: operação `div` com divisor zero produz `Infinity`/`NaN` sem erro explícito, podendo gerar resultados incorretos em expressões de caminho clínico.

---

## E. Performance

Oportunidades identificadas (não medidas, sem benchmark disponível):

| ID | Oportunidade | Justificativa | Como medir |
|----|-------------|---------------|------------|
| REA-012 | Cache de caminho resolvido (`findWebTemplateNode`) | Busca recursiva na árvore de nós para cada resolução de caminho; pode ser chamada repetidamente para o mesmo caminho | Microbenchmark antes/depois com cache (ex. Caffeine já presente como dependência) |
| REA-013 | `WebTemplatePathExpressionEvaluator` cria `Executors.newSingleThreadExecutor()` a cada avaliação com regex timeout | Criação de executor por chamada de regex pode ser onerosa em avaliações frequentes | Medir throughput de avaliações de expressão com regex |
| REA-014 | `stripWithTemplate` itera `fieldNames()` do ObjectNode a cada avaliação | Operação repetida no ponto de entrada de avaliação | Medir tempo de avaliação para composições grandes |

Nenhuma medição inicial foi possível (sem Maven/Java 25). Recomendado: estabelecer baseline de benchmark antes de otimizar.

---

## F. Melhorias técnicas

1. **Testes de regressão para problemas recorrentes** (REA-001 a REA-004): adicionar testes unitários que cubram os cenários de regressão identificados no CHANGELOG, impedindo retorno dos problemas.
2. **Tratamento gracioso de tipo RM não mapeado** (REA-009): substituir `requireNotNull` por retorno null/Optional com mensagem descritiva, evitando exceção não tratada.
3. **Validação de divisor zero** (REA-010): adicionar verificação antes de operação `div`/`%` no avaliador, lançando `WebTemplatePathExpressionException` descritiva.
4. **Corrigir SCM/ehr-common versão mismatch** (REA-008): alinhar versões no pom.xml e URL do SCM (PR #9 já proposto upstream).
5. **Suporte a nomes multi-lingua e keyword 'comments'** (REA-006, REA-007): melhorias de funcionalidade solicitadas em issues abertas.
6. **Documentação de contribuição**: CONTRIBUTING.md menciona `mvn clean install` mas não menciona requisitos de Java 25 nem Antlr geração.

---

## G. Plano de execução

Correções independentes (podem ser PRs separados):

1. **REA-008 (P2)** - Corrigir versão mismatch pom.xml + SCM URL. Baixo risco, alteração de configuração. PR separado.
2. **REA-009 (P2)** - Tratamento gracioso de tipo RM não mapeado em `WebTemplateInputBuilderDelegator`. Baixo risco, 1 arquivo. PR separado.
3. **REA-010 (P3)** - Validação de divisor zero no avaliador. Baixo risco, 1 arquivo. PR separado.
4. **REA-001 a REA-004 (P2)** - Testes de regressão para problemas recorrentes. Médio risco (necessário compreender cenários exatos), múltiplos arquivos de teste. PR separado ou agrupado.
5. **REA-006, REA-007 (P3)** - Melhorias de funcionalidade (issues #6, #7). Escopo maior, necessária discussão com mantenedores.

Ordem recomendada: REA-008 → REA-009 → REA-010 (baixo risco, independentes), depois testes de regressão.

---

## H. Resultado esperado

Cada correção agrega:
- **REA-008:** consistência de build/release, evita confusão de versão e SCM.
- **REA-009:** robustez do builder para tipos RM futuros ou estendidos, evita falha abrupta.
- **REA-010:** previsibilidade de expressões de caminho clínico, evita resultados silenciosamente incorretos.
- **Testes de regressão:** impedem retorno de problemas já corrigidos, aumentam confiança em releases futuros.

Todas as correções são de baixo risco, com teste reproduzível, e preservam o funcionamento das demais partes do projeto.

---

## Análise de malware e ameaças (estática)

Examinados estaticamente: fontes Kotlin, pom.xml, .circleci/config.yml, gramática ANTLR, recursos de teste, scripts de build.

**Resultado:** Nenhum indicador identificado pela análise estática realizada.
- Não foram encontradas credenciais, chaves, tokens ou senhas embutidas.
- Não foram encontradas URLs, domínios ou IPs suspeitos (apenas URLs de licença Apache).
- Não foram encontrados comandos de shell, execução de processos, persistência, ofuscação, binários empacotados, ou exfiltração de dados.
- GitHub Actions não presente (apenas CircleCI com `mvn` padrão e upload de cobertura para Codecov via token de ambiente `$CODECOV_TOKEN`).

**Limitações:** análise exclusivamente estática; não foram executadas amostras; dependências transitórias não foram analisadas em profundidade (Maven não disponível para `dependency:tree` ou OWASP dependency-check). Não se afirma que o projeto está livre de malware.

---

## Perguntas finais obrigatórias

1. **Quais bugs críticos foram comprovados?** Nenhum P0/P1 confirmado. Apenas regressões PROVÁVEIS (REA-001 a REA-004) baseadas no CHANGELOG.

2. **Quais problemas repetitivos os desenvolvedores ainda não resolveram?** ELEMENT/DATA_VALUE duplicado, ctx null, feeder_audit time, NPE null name — todos corrigidos anteriormente e reaparecidos (regressões), ausência de testes de regressão que os impeçam.

3. **Quais erros importantes não estavam documentados?** SCM/ehr-common versão mismatch (PR #9), exceção "Unsupported type" para RM não mapeado, divisão por zero silenciosa no avaliador.

4. **Quais oportunidades de performance foram encontradas?** Cache de caminho resolvido, reuso de executor de regex no avaliador, otimização de `stripWithTemplate`. Sem medição inicial possível.

5. **Quais melhorias de segurança e confiabilidade são justificáveis?** Tratamento gracioso de tipo RM não mapeado (evita exceção não tratada), validação de divisor zero, testes de regressão para problemas recorrentes.

6. **Quais correções podem ser feitas rapidamente?** Correção de versão mismatch no pom.xml (REA-008), tratamento de tipo não mapeado (REA-009), validação de divisor zero (REA-010). Baixo risco, 1-2 arquivos cada.

7. **Quais são as três contribuições com maior potencial de valor técnico?**
   - (a) Testes de regressão para REA-001 a REA-004 (impede retorno de bugs já corrigidos).
   - (b) Tratamento gracioso de tipo RM não mapeado em `WebTemplateInputBuilderDelegator` (robustez do builder).
   - (c) Correção de versão mismatch SCM/ehr-common (consistência de build/release).

8. **Quais correções devem ser evitadas por falta de evidência?** Nenhuma alteração na lógica de negócio de conversão de dados clínicos sem execução de testes (não há ambiente para validar). Melhorias de funcionalidade #6 e #7 (issues abertas) requerem discussão com mantenedores antes de implementar.

9. **Quais modificações você recomenda implementar primeiro?** REA-008 (versão mismatch), REA-009 (tipo não mapeado), REA-010 (divisor zero) — independente, baixo risco, teste reproduzível possível.

---

*Este relatório é produto da Fase 1 (auditoria estática) e será atualizado na Fase 2 com resultados de correções e testes.*
