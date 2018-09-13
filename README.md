<h1 align="center">Ayla</h1>

<p align="center">
  <a href="https://ci.mrgaabriel.space/viewType.html?buildTypeId=Public_Ayla_Build?guest=1">
    <img align="center" src="https://ci.mrgaabriel.space/app/rest/builds/buildType:Public_Ayla_Build/statusIcon"/>
  </a>
  <a href="https://dependabot.com">
    <img align="center" src="https://api.dependabot.com/badges/status?host=github&repo=MrGaabriel/Ayla">
  </a>
</p>

:wave: Olá! Como vai você!? Procurando um bot de terras tupiniquins para alegrar seu servidor!?

Vou me apresentar: Eu me chamo Ayla, cujo nome significa "luar" em Havaiano (eu amo o meu nome!) e eu sou um bot criado em terras tupiniquins para alegrar o seu servidor, com funções de A até Z! 

### Como eu faço para adicionar a Ayla no meu servidor!?
Você pode [clicar aqui](https://discordapp.com/api/oauth2/authorize?client_id=475312446156832768&permissions=1879048374&scope=bot) para ser redirecionado para o link de convite do Discord, mas também pode fazer self-hosting, ou seja, sua própria cópia do bot!

### Como eu faço "self-hosting"!?
Como eu nunca pensei que as pessoas iriam fazer self-hosting, há várias coisas "hard-coded" no código da Ayla, como o nome das pastas, entre outros. Então para remover os códigos hard-coded você precisará saber pelo menos o básico de algo de programação, para fazer com que a Ayla seja compatível com sua máquina ou algo do tipo. O projeto está sob a licença MIT, o que garante que eu não sou obrigado a te dar suporte sobre o self-hosting.

1. Use `git clone https://github.com/MrGaabriel/Ayla` em qualquer pasta para clonar o repositório da Ayla
2. Abra o código com sua IDE de preferência (eu uso o [IntelliJ IDEA](https://jetbrains.com/idea))
3. Substitua os códigos hard-coded para adequar o bot a você
4. Compile-a usando `gradle jar`
5. Pegue o jar que foi gerado na pasta `build/libs/`
6. Execute-o usando `java -Xms512M -Xmx512M -jar Ayla-x.x.x.jar` substituindo a memória alocada por qual você quiser (eu aloco 1GB)
7. Configure-o pelo arquivo gerado `config.json` com todas as informações necessárias
8. Re-execute-o!

Yay! Agora você tem a sua cópia da Ayla!
Vou deixar claro aqui que você não pode usar o nome dela ou o avatar dela no seu clone.

### Pull Requests
Sabe programar e quer me ajudar!? Abra um [Pull Request](https://github.com/MrGaabriel/Ayla/pulls)!
Você pode me ajudar corrigindo um bug, adicionando um comando, adicionando uma função supimpa...
Lembrando que eu sou o proprietário do projeto, então eu posso decidir se eu aceito ou não. Pull Requests com funções NSFW não serão aceitos.

### Issues
Não sabe programar/não quer!? Tudo bem! Se você ainda tem uma sugestão ou um bug a reportar para mim, abra uma [Issue](https://github.com/MrGaabriel/Ayla/issues/new)
Para bugs, tente explicar o bug, o que você fez para com que aconteça ele, só falar "x coisa não funciona" não vai me ajudar!

### Licença
O projeto é licenciado sobre a licença [MIT](https://github.com/MrGaabriel/Ayla/blob/master/LICENSE), o que te permite:
- Uso comercial 
- Modificações
- Distribuição
- Uso privado

mas **NÃO** te garante:
- Responsabilidade por minha parte
- Garantia por minha parte

...e te **OBRIGA** a:
- Ter a mesma licença desse aqui, sem NENHUMA mudança.

<p align="center">Discord", "DiscordApp" and any associated logos are registered trademarks of Discord Inc.</p>
