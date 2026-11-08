package com.oyayasu.biblestudy.data

/**
 * Material editorial de demonstração, não a tradução da Bíblia de Jerusalém.
 * Cada entrada descreve um trecho a consultar na Bíblia do próprio usuário.
 * Termos originais são fragmentos lexicais ilustrativos, não versículos integrais.
 */
data class Study(
    val id: String,
    val title: String,
    val reference: String,
    val theme: String,
    val subtitle: String,
    val overview: String,
    val originalLanguage: String,
    val originalWord: String,
    val transliteration: String,
    val originalMeaning: String,
    val historicalContext: String,
    val morningPrompt: String,
    val eveningPrompt: String,
    val practicalStep: String
)

object StudyCatalog {
    val studies: List<Study> = listOf(
        Study(
            id = "lucas-15",
            title = "A misericórdia do Pai",
            reference = "Lucas 15,11–32",
            theme = "Misericórdia",
            subtitle = "A parábola do filho que retorna",
            overview = "A narrativa contrapõe o retorno do filho mais novo, a acolhida do pai e a reação do filho mais velho. Observe os três personagens antes de chegar a uma conclusão.",
            originalLanguage = "Grego koiné",
            originalWord = "ἐσπλαγχνίσθη",
            transliteration = "esplanchnísthē",
            originalMeaning = "Em Lucas 15,20, o verbo descreve a profunda compaixão do pai. É uma forma de σπλαγχνίζομαι, associado a ser movido de compaixão.",
            historicalContext = "Em Lucas 15, Jesus responde a críticas por receber pecadores e comer com eles. As três parábolas do capítulo tratam da perda, da busca e da alegria pelo reencontro. O contexto imediato ajuda a ler o comportamento dos dois irmãos.",
            morningPrompt = "Com qual dos três personagens você mais se identifica hoje? Por quê?",
            eveningPrompt = "Hoje você reconheceu um erro e ofereceu misericórdia a alguém?",
            practicalStep = "Escolha uma situação em que você possa ouvir alguém com menos julgamento."
        ),
        Study(
            id = "salmo-51",
            title = "Um coração renovado",
            reference = "Salmo 51 (50),1–19",
            theme = "Arrependimento",
            subtitle = "Reconhecer o erro sem fugir da verdade",
            overview = "O salmo é uma oração de arrependimento. Sua linguagem reúne reconhecimento da culpa, pedido de misericórdia e desejo de transformação interior.",
            originalLanguage = "Hebraico bíblico",
            originalWord = "לֵב טָהוֹר",
            transliteration = "lev tahor",
            originalMeaning = "Expressão que significa 'coração puro' no pedido de renovação. A numeração dos versículos pode variar entre edições e tradições.",
            historicalContext = "O título tradicional do salmo o associa a Davi e ao episódio de Natã narrado em 2 Samuel 12. O texto é usado historicamente como uma oração penitencial; essa atribuição pertence à tradição do próprio salmo.",
            morningPrompt = "Que atitude você precisa reconhecer com sinceridade, sem justificativas?",
            eveningPrompt = "Você reparou algum erro hoje ou apenas sentiu remorso?",
            practicalStep = "Anote uma atitude específica que pode ser reparada e como pretende fazê-lo."
        ),
        Study(
            id = "miqueias-6",
            title = "Justiça, fidelidade e humildade",
            reference = "Miqueias 6,6–8",
            theme = "Vida prática",
            subtitle = "Uma fé que se expressa nas atitudes",
            overview = "O profeta contrasta a multiplicação de ofertas com o que Deus exige: agir justamente, amar a fidelidade e caminhar humildemente com Ele.",
            originalLanguage = "Hebraico bíblico",
            originalWord = "חֶסֶד",
            transliteration = "ḥesed",
            originalMeaning = "Termo com campo semântico que inclui lealdade, amor fiel e misericórdia. Sua nuance depende da passagem e do contexto.",
            historicalContext = "Miqueias atuou em Judá no século VIII a.C., num contexto de desigualdades e denúncias de injustiça. O capítulo 6 apresenta uma espécie de controvérsia da aliança entre Deus e seu povo.",
            morningPrompt = "Como demonstrar justiça e humildade numa conversa comum de hoje?",
            eveningPrompt = "Você tratou as pessoas com justiça mesmo quando não havia benefício próprio?",
            practicalStep = "Escolha uma ação verificável de honestidade ou consideração pelo próximo."
        ),
        Study(
            id = "lamentacoes-3",
            title = "A esperança se renova",
            reference = "Lamentações 3,19–26",
            theme = "Misericórdia",
            subtitle = "Esperança sem negar o sofrimento",
            overview = "No meio de uma poesia de dor, a voz que lamenta volta a recordar a misericórdia e a fidelidade de Deus. A esperança surge dentro do lamento, não no lugar dele.",
            originalLanguage = "Hebraico bíblico",
            originalWord = "חֲסָדִים",
            transliteration = "ḥasadim",
            originalMeaning = "Plural de ḥesed, frequentemente associado às manifestações de amor leal ou misericórdia. A forma e a tradução exatas dependem da edição textual.",
            historicalContext = "Lamentações reflete a devastação de Jerusalém, tradicionalmente associada à conquista babilônica em 586 a.C. O capítulo 3 faz parte de uma coleção de poemas que unem luto e apelo a Deus.",
            morningPrompt = "O que significa manter esperança sem fingir que uma dificuldade não existe?",
            eveningPrompt = "Que misericórdia você percebeu hoje, mesmo num dia difícil?",
            practicalStep = "Registre um motivo concreto de gratidão sem minimizar os desafios presentes."
        )
    )

    fun find(id: String): Study = studies.firstOrNull { it.id == id } ?: studies.first()
}
