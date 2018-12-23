package com.github.mrgaabriel.ayla.commands

enum class CommandCategory(val fancyName: String, val description: String, val showOnHelp: Boolean = true) {

    CONFIG("Configuração", "Comandos relacionados a configuração dos módulos da Ayla no seu servidor"),
    DEVELOPER("Desenvolvedor", "Comandos que só o desenvolvedor da Ayla pode usar", false),
    DISCORD("Discord", "Comandos relacionados ao Discord"),
    IMAGES("Imagens", "Comandos relacionados a manipulação de imagens"),
    MISC("Miscelânea", "Comandos que não encontraram uma categoria específica"),
    MUSIC("Música", "Comandos relacionados a música"),
    UTILS("Utilidades", "Comandos relacionados a utilidades"),

    NONE("Nenhuma", "Comandos que não estão em nenhuma categoria")
}