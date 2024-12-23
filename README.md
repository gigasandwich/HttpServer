# Mamantatra ny ao ambadikan'ny HTTP

- [x] Requête: Request line, headers, body
- [x] Réponse: Response line, headers, body


# Mamorona Serveur Socket mandray Client Socket (Navigateur)

- [x] Manokatra port ho an'ilay serveur
- [x] Mandray client iray amin'iny serveur iny no sady gerena izy (jusqu'à déconnexion)
- [x] Mandray clients maromaro amin'izay (mampiasa Thread)


# Fomba Figerena an'ilay Client (Function handleClient)

- [x] Raisina ny outputStream sy ny inputStream an'ilay client

- [x] Raisina ny requête any (amin'ny alalan'ny inputStream)
  - [x] Raisina ny ligne voalohany (alaina ny méthode sy ny zavatra angatahiny): eg `GET /index.html HTTP 1.0/1`
  - [x] Atao anaty map ny headers: 2e ligne jusqu'à misy ligne VIDE (Misy saut à la ligne alohan'ny body)
  - [x] Raisina ny body RAHA MISY, atao anaty map ohatra

- [x] Manamboatra serveur avy amin'ilay requête
  - [x] Mamorona dossier sy fichier de base itsofohan'ilay client vao manoratra an'ilay IP @ navigateur izy
    - [x] www
    - [x] index.html (misy formulaires ho an'ny méthode GET sy POST)
  - [x] Rehefa fichier fantatra dia affichena ny contenu any
    - [x] Fichier HTML
    - [x] Fichier TXT
    - [x] Sary
    - [x] Fichier PHP
  - [x] Raisina ny méthode an'ilay requête
    - [x] GET
      - [x] Alaina ny paramètres an'ilay query
      - [x] Raha dossier ilay ressource angatahana dia listeny ny fichiers ao @ iny dossier iny
      - [x] Raha fichier dia vakina ilay contenu no atao body ilay izy
      - [x] Raha fichier misy query dia sady affichena ilay query no affichena ilay contenu
    - [x] POST
      - [x] Alaina ny name sy value
      - [x] Raha dossier ilay ressource angatahana dia listeny ny fichiers ao @ iny dossier iny
      - [x] Raha fichier dia vakina ilay contenu no atao body ilay izy
      - [x] Affichena any @ ilay page ilay form datas


# Tohiny

- [x] PHP
  - [x] Mi-install interpreter
    ```bash
    sudo apt install php php-cli
    ```
  - [x] Mampiasa interpreter
    - [x] Antsoina ilay commande (mampiasa class ProcessBuilder)
    - [x] Raisina ny output (raisina ny inputStream an'ny process)
    - [x] Atao body ilay output

- [x] Fichier de Configuration
  - [x] htdocs
  - [x] Port
  - [x] Read PHP

- [x] Interface fanaovana interaction @ fichier de conf
  - [x] Swing

- [x] Fichier index no itsofohana voalohany
  - [x] Rehefa `read_php = yes/true` dia `index.php` no itsofohana voalohany

- [x] Mila feno tsara ny headers

- [ ] Création d'un installateur
