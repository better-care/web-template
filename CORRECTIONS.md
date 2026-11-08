# CORRECTIONS.md

## Repositório alvo
- **Nome:** web-template
- **URL:** https://github.com/better-care/web-template
- **Branch padrão (upstream):** master
- **Commit-base analisado:** 8115f49
- **Versão do projeto:** 6.0.0-SNAPSHOT
- **Data da elaboração:** 2026-11-08 (horário local)
- **Autor:** admeduardovieira (Kilo, REA 6.7.0)

---

## Propostas de correção

---

### REA-008

**Título:** Fix SCM url and ehr-common.version mismatch in pom.xml  
**Severidade:** P2  
**Status:** PROPOSTA (não implementada)  
**Localização:** `pom.xml` (linhas 36, 583-587)  
**Problema:** O `pom.xml` declara `version 6.0.0-SNAPSHOT` mas `ehr-common.version` é `5.0.0`, e a URL do SCM (`<url>https://github.com/better-care/web-template/tree/master/</url>`) aponta para `master/` sem alinhamento com o estado de desenvolvimento SNAPSHOT. Isso pode causar confusão em releases e builds.  
**Evidência:** PR #9 aberto no GitHub ("fix: SCM url + ehr-common.version mismatch (5.0.0→6.0.0-SNAPSHOT)"), linhas 36 e 583-587 do pom.xml.  
**Causa-raiz:** Desalinhamento entre versão do artefato, versão da dependência e URL do SCM no momento do bump para 6.0.0-SNAPSHOT.  
**Impacto:** Usuários e ferramentas de release podem resolver a versão incorreta do ehr-common; SCM aponta para branch estável enquanto código é SNAPSHOT.  
**Correção proposta:** Atualizar `ehr-common.version` para versão compatível com 6.0.0-SNAPSHOT (ou ajustar versão do artefato) e alinhar URL do SCM conforme convenção do projeto.  
**Arquivos envolvidos:** `pom.xml`  
**Teste de validação:** `mvn validate` (ou `mvn dependency:resolve`) para confirmar resolução de dependências sem erro de versão. Não executável localmente (Maven não instalado, Java 17 vs Java 25 requerido).  
**Risco da alteração:** Baixo (alteração de metadados de build, não de lógica de código).  
**Benefício:** Consistência de build/release, evita resolução de dependência incorreta.  
**PR sugerido:** `fix: align ehr-common version and SCM url for 6.0.0-SNAPSHOT`  
**Prioridade:** Imediata  

---

### REA-009

**Título:** Graceful handling of unmapped RM type in WebTemplateInputBuilderDelegator  
**Severidade:** P2  
**Status:** PROPOSTA (não implementada)  
**Localização:** `src/main/kotlin/care/better/platform/web/template/builder/input/WebTemplateInputBuilderDelegator.kt` (linhas 100-103 e 106-110)  
**Problema:** `delegate()` usa `requireNotNull(typeBuilders[...]) { "Unsupported type: ..." }`, lançando exceção não tratada quando um tipo RM não está mapeado no mapa `typeBuilders`. Não há fallback nem retorno gracioso.  
**Evidência:** Linhas 100-103: `with(requireNotNull(typeBuilders[RmUtils.getNonGenericRmNamePart(amNode.rmType)]) { "Unsupported type: ${amNode.rmType}" })`.  
**Causa-raiz:** O mapa `typeBuilders` não cobre todos os tipos RM possíveis; ausência de tratamento para tipo desconhecido.  
**Impacto:** Build de web template falha abruptamente para tipos RM não mapeados, sem mensagem orientativa nem alternativa.  
**Correção proposta:** Substituir `requireNotNull` por retorno `null` (ou `Optional`-like) quando o tipo não estiver mapeado, permitindo que o chamador decida (pular input, logar aviso, ou lançar exceção controlada).  
**Arquivos envolvidos:** `WebTemplateInputBuilderDelegator.kt`, possivelmente `WebTemplateBuilder.kt` (chamador em `addInputs`).  
**Teste de validação:** Teste unitário passando um `AmNode` com `rmType` não presente em `typeBuilders` e verificando que não lança exceção não tratada. Não executável localmente (sem build).  
**Risco da alteração:** Baixo (mudança localizada em um método delegador).  
**Benefício:** Robustez do builder para tipos RM futuros ou estendidos; evita falha abrupta.  
**PR sugerido:** `fix: handle unmapped RM type gracefully in WebTemplateInputBuilderDelegator`  
**Prioridade:** Imediata  

---

### REA-010

**Título:** Validate divisor zero in path expression arithmetic operations  
**Severidade:** P3  
**Status:** PROPOSTA (não implementada)  
**Localização:** `src/main/kotlin/care/better/platform/web/template/path/evaluator/WebTemplatePathExpressionEvaluator.kt` (linha 150)  
**Problema:** Operação `DIV` (`left.toDouble() / right.toDouble()`) e `MOD` (`left.toDouble() % right.toDouble()`) não verificam se o divisor é zero. Em Kotlin/Double, divisão por zero retorna `Infinity` ou `NaN` sem exceção, produzindo resultados silenciosamente incorretos em expressões de caminho clínico.  
**Evidência:** Linha 150: `PathOperator.DIV -> left.toDouble() / right.toDouble()` e linha 151: `PathOperator.MOD -> left.toDouble() % right.toDouble()`.  
**Causa-raiz:** Ausência de validação de divisor zero antes da operação aritmética.  
**Impacto:** Expressões de caminho com divisão por zero retornam `Infinity`/`NaN` sem erro explícito, podendo gerar dados clínicos incorretos sem que o usuário perceba.  
**Correção proposta:** Adicionar verificação `if (right == 0) throw WebTemplatePathExpressionException("Division by zero")` antes das operações `DIV` e `MOD`.  
**Arquivos envolvidos:** `WebTemplatePathExpressionEvaluator.kt`  
**Teste de validação:** Teste unitário avaliando expressão `10 div 0` e verificando lançamento de `WebTemplatePathExpressionException`. Não executável localmente (sem build).  
**Risco da alteração:** Baixo (adição de validação em um ponto de operação aritmética).  
**Benefício:** Previsibilidade e correção de expressões de caminho clínico; evita resultados silenciosamente incorretos.  
**PR sugerido:** `fix: validate divisor zero in path expression arithmetic`  
**Prioridade:** Baixa  

---

### REA-001 a REA-004 (testes de regressão)

**Título:** Add regression tests for recurring bugs (ELEMENT/DATA_VALUE, ctx null, feeder_audit time, NPE null name)  
**Severidade:** P2  
**Status:** PROPOSTA (não implementada)  
**Localização:** `src/test/kotlin/care/better/platform/web/template/` (novos arquivos de teste)  
**Problema:** Problemas corrigidos em versões anteriores reapareceram (regressões), indicando ausência de testes de regressão que os impeçam de retornar.  
**Evidência:** CHANGELOG.md — mesma correção aplicada em versões distintas (4.0.4 e 3.2.9; 4.0.1 e 3.2.7).  
**Causa-raiz:** Ausência de testes automatizados específicos para cenários de borda que já foram corrigidos.  
**Impacto:** Risco de retorno de bugs já resolvidos em releases futuros.  
**Correção proposta:** Adicionar testes unitários que reproduzam cada cenário de regressão identificado.  
**Arquivos envolvidos:** Novos arquivos de teste em `src/test/kotlin/care/better/platform/web/template/build/` e `src/test/kotlin/care/better/platform/web/template/converter/`.  
**Teste de validação:** Execução dos novos testes com `mvn test`. Não executável localmente.  
**Risco da alteração:** Baixo (adição de testes, não alteração de código de produção).  
**Benefício:** Impede regressão de problemas já corrigidos; aumenta confiança em releases.  
**PR sugerido:** `test: add regression tests for recurring bugs`  
**Prioridade:** Alta  

---

## Correções já realizadas nesta execução

Nenhuma correção foi implementada nesta execução (Fase 1 concluída, Fase 2 pendente). Motivo: ambiente sem Maven e sem Java 25, impossibilitando build, compilação e execução de testes para validação de qualquer alteração de código.

---

## Problemas não corrigidos e riscos remanescentes

- Todas as correções propostas (REA-008, REA-009, REA-010, testes de regressão REA-001 a REA-004) permanecem como propostas aguardando implementação e validação em ambiente com Maven/Java 25.
- Melhorias de funcionalidade (issues #6, #7 do GitHub) não foram abordadas; requerem discussão com mantenedores.
- Análise de dependências com OWASP dependency-check não executada (Maven não disponível).

---

*Este documento será atualizado na Fase 2 conforme as correções forem implementadas, validadas e publicadas.*
