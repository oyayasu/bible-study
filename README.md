# Bíblia • Estudos — Android

Aplicativo pessoal de estudo bíblico, com visual inspirado no macOS e armazenamento **inteiramente local**.

## Versão 0.1

- Início com estudo do dia e dois momentos ligados ao mesmo tema: manhã e noite.
- Meditações programadas para **06h00** e **20h30**, usando o fuso horário do aparelho.
- Notificações Android, com opção de solicitar permissões para notificação e alarme exato. Se não autorizadas, o Android pode atrasar as notificações.
- Biblioteca pesquisável com quatro estudos demonstrativos e filtros por tema.
- Estudo com seções **Português**, **Original** e **Contexto**, com separação explícita entre texto bíblico, contexto e interpretação.
- Campo para inserir e salvar localmente o texto português de cada passagem, com armazenamento persistente.
- Reflexões para manhã e noite, diário privado, marcação de meditação concluída.
- Cores marfim/vinho/dourado, cartões arredondados, vidro translúcido leve e iluminação desfocada.
- Sem login, analytics, backend ou anúncios.

## Instalação

Acesse **Actions → Android APK → execução bem-sucedida → Artifacts → Biblia-v0.1-Android-APK** para obter o APK de debug instalável.

Ou abra a pasta em Android Studio (JDK 17), aguarde o Gradle e use **Build APK(s)**. Para terminal com Gradle 8.14 instalado: \`gradle :app:assembleDebug\`.

## Licenciamento e fontes

A **Bíblia de Jerusalém, Paulus, edição de 2002** é uma tradução sujeita a direitos autorais. Esta aplicação **não** a distribui nem afirma disponibilizar seus versículos. O campo de português aceita somente conteúdos introduzidos pelo próprio usuário, guardados neste aparelho. Não há OCR nem download automatizado dessa tradução.

Os quatro estudos incluídos têm **resumos e reflexões editoriais de demonstração**; não constituem comentários teológicos revisados, nem substituem a leitura da passagem integral. Os termos gregos e hebraicos são exemplos curtos de vocabulário, não edições completas dos textos originais. Em versões posteriores, integrar textos originais com licença permissiva, documentação de variantes e referências bibliográficas verificáveis.

## Arquitetura

- Kotlin + Jetpack Compose / Material 3
- AlarmManager para dois alarmes pontuais de agendamento único e renovação diária
- BroadcastReceiver para reinício, mudanças de hora/fuso, reinstalação e permissão de alarmes
- SharedPreferences privadas para textos adicionados, reflexões, preferências e conclusão de sessões
- Sem permissão de Internet
- Testes unitários da lógica de horário em \`ReminderTimesTest\`

## Próximas etapas

1. Importar o repertório editorial do usuário e organizar uma sequência coerente.
2. Integrar texto bíblico licenciado em português e fonte crítica dos textos originais.
3. Referências e bibliografia para notas históricas; revisão teológica.
4. Backup/exportação privada, temas e acessibilidade adicional.

**Limitação atual:** o estudo diário é alternado por data entre quatro exemplos. Ainda não há plano editorial definitivo, sincronização entre aparelhos nem textos integrais pré-instalados.
