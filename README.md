# Mamantatra ny ao ambadikan'ny http
    [x] Requete: Request line, headers, body

    [x] Reponse: Response line, headers, body

# Mamorona Serveur socket mandray client socket (navigateur)
    [x] Manokatra port ho an'ilay serveur

    [x] Mandray client iray amin'iny serveur iny no sady gerena izy (jusqu'a deconnexion)

    [x] Mandray clients maromaro amin'izay (mampiasa Thread)

# Fomba figerena an'ilay client (foction handleClient)
    [x] Raisina ny outputStream sy ny inputStream an'ilay client

    [x] Raisina ny requete any (amin'ny alalan'ny inputStream)
        [x] Raisina ny ligne voalohany (alaina ny methode sy ny zavatra angatahiny): eg GET /index.html HTTP 1.0/1       
        [x] Atao anaty map ny headers: 2e ligne jusqu'a misy ligne VIDE (Misy saut a la ligne alohan'ny body)
        [x] Raisina ny body RAHA MISY atao anaty map ohatra

    [x] Manamboatra serveur avy amin'ilay requete
        [x] Mamorona dossier sy fichier de base itsofohan'ilay client vao manoratra an'ilay IP @ navigateur izy
            [x] www
            [x] index.html (misy formulaires ho an'ny methode get sy post)

        [x] Rehefa fichier fantatra dia affichena ny contenu any
            [x] fichier html
            [x] fichier txt
            [X] Sary
            [x] fichier php

        [x] Raisina ny methode an'ilay requete 
            [x] GET
                [x] Alaina ny parametres an'ilay query 
                [x] Raha dossier ilay ressourse angatahana dia listeny ny fichiers ao @ iny dossier iny
                [x] Raha fichier dia vakina ilay contenu no atao body ilay izy
                [x] Raha fichier misy query dia sady affichena ilay query no affichena le contenu 

            [x] POST
                [x] Alaina ny name sy value
                [x] Raha dossier ilay ressourse angatahana dia listeny ny fichiers ao @ iny dossier iny
                [x] Raha fichier dia vakina ilay contenu no atao body ilay izy
                [x] Affichena any @ ilay page ilay form datas

# Tohiny
    [X] php
        [X] Miinstalle interpretteur
        ```bash
            sudo apt install php php-cli
        ```

        [X] Mampiasa interpreteur
            [X] Antsoina ilay commande (mampiasa class ProcessBuilder) 
            [X] Raisina ny output (raisina ny inputStream an'ny process)
            [X] atao body ilay output

    [X] Fichier de configuration
        [X] htdocs
        [X] Port
        [x] Read php

    [x] Interface fanaovana interation @ fichier de conf
        [x] Swing

    [x] Fichier index no itsofohana voalohany
        [x] rehefa read_php = yes/true dia index.php no itsofohana voalohany

    [] Mila feno tsara ny headers

    [] Creation d'un installateur

    
