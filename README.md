## 1. Choix de la classe

| Élément | Valeur |
|---|---|
| Classe | `org.apache.tika.config.TikaExtras` |
| Test existant | `TikaExtrasTest` (5 tests, tous réussis) |
| Couverture JaCoCo des instructions | 81 % |
| Couverture JaCoCo des branches | 89 % (À COMPLÉTER : vérifier le nombre de branches manquées) |
| Lignes non couvertes (rouge) | 82, 83, 87, 91, 116, 117, 162, 163, 164 |
| Lignes partiellement couvertes (jaune) | 86, 90, 122, 157 |

![Couverture JaCoCo de TikaExtras](images/jacoco_rapport.png)
![Code de TikaExtras avec les lignes non couvertes](docs/code_tikaextra1.png)
![Code de TikaExtras avec les lignes non couvertes](docs/code_tikaextra2.png)


**Justification** Classe courte, sans dépendance lourde, logique claire (conditions, tri, construction
d'un classpath), avec un test existant mais incomplet.

**Preuve de mutants vivants (mesure A).** Pitest, avec uniquement `TikaExtrasTest` :

| Mutants générés | Tués | Score de mutation | Sans couverture | Force des tests |
|---|---|---|---|---|
| 21 | 17 | 81 % | 1 | 85 % |

Couverture de ligne (classes mutées) : 48/58 (83 %). Rapport archivé : `docs/pit-A/`.

![Rapport Pitest, mesure A](images/pit_test_coverage_report_A.png)
![Mutants vivants de TikaExtras, mesure A](images/mutations_tikaextra.png)

Mutants vivants (4) :

| Ligne | Mutant | Statut | Pourquoi les tests d'origine ne le détectent pas |
|---|---|---|---|
| 90 | negated conditional (`if (parent == null)`) | SURVIVED | le test fournit un classloader déjà non nul, et le classloader de remplacement est le même objet dans l'environnement de test |
| 95 | removed call to `ServiceLoader::setContextClassLoader` | SURVIVED | le test vérifie le classloader du thread, pas celui de `ServiceLoader` |
| 122 | removed call to `List::sort` | SURVIVED | tous les tests utilisent un seul jar, le tri n'a aucun effet visible |
| 122 | replaced return value with `""` pour `lambda$extraJars$0` | NO_COVERAGE | le comparateur n'est appelé qu'avec au moins deux jars |

Remarque : les lignes rouges 82-83, 116-117 et 162-164 (blocs `catch`) comptent pour la couverture JaCoCo mais
ne produisent aucun mutant vivant : Pitest ignore par défaut les appels aux bibliothèques de log.

## 2. Génération des tests

### 2.1 Exécution sans intervention manuelle

Les deux classes de test (sans la Suite) ont été copiées telles quelles dans
`tika-core/src/test/java/org/apache/tika/config/`.

- **Avec checkstyle activé**, le build s'arrête à la phase `validate` : 31 violations lors de la première
  exécution. Lors d'une seconde exécution, sans modification de notre part, il n'en restait que 8, toutes de la
  même règle (imports avec `.*` interdits, 4 par fichier). Hypothèse : Spotless, exécuté par le build de Tika,
  a corrigé automatiquement l'en-tête, l'ordre et les imports inutilisés (À CONFIRMER : `docs/spotless-diff.txt`).
  Les tests ne passent donc pas sans intervention.
- **Avec checkstyle désactivé** (`-Dcheckstyle.skip=true`), les deux fichiers compilent et 5 tests s'exécutent :

| Fichier | Tests | Réussis | Erreurs |
|---|---|---|---|
| `TikaExtras_extrasDir_3_1_Test` | 1 | 1 | 0 |
| `TikaExtras_appendJarsToClasspath_2_1_Test` | 4 | 0 | 4 |

### 2.2 Explication et critique des tests générés

**`TikaExtras_extrasDir_3_1_Test`.** Ce fichier contient un seul test. Il définit la propriété `tika.extras.dir` 
à `"   "` et vérifie avec `assertNull` que `extrasDir()` renvoie `null`.

Le test a de bons côtés. Il vise un cas que `TikaExtrasTest` ne vérifie pas, soit une propriété définie mais blanche, 
à la ligne 157. Son oracle est précis : sans la condition `isBlank()`, `Path.of("")` renverrait un chemin vide au lieu 
de `null`, et le test échouerait. Le nom est explicite et la structure Arrange, Act, Assert se lit facilement.

Il a aussi plusieurs défauts. La propriété système n'est jamais remise dans son état d'origine, ce qui peut perturber 
les autres tests. Le test passe par la réflexion pour construire un objet à constructeur privé, alors que la méthode 
est statique et s'appelle directement. Il utilise `@ExtendWith(MockitoExtension.class)` sans aucun mock, et ses imports 
utilisent la forme `*`, ce que checkstyle refuse. Enfin, il ne teste qu'un cas, et le chemin invalide des lignes 162 à 164 reste sans test.

**`TikaExtras_appendJarsToClasspath_2_1_Test`.** Ce fichier contient quatre tests, tous en erreur. L'intention est raisonnable, puisqu'il 
veut tester l'absence de jar, la présence de jars et une erreur pendant la résolution d'un jar. Le problème est que les tests vérifient des 
mocks et non le code de `TikaExtras` :

- Ils simulent des méthodes statiques du JDK, `Files.isDirectory` et `Files.newDirectoryStream`, sans passer par `mockStatic`, ce qui provoque une `NullPointerException` dès la préparation du test.
- Ils créent un mock de la classe testée, qui n'a que des méthodes statiques. L'instance passée à `invoke` est donc ignorée et le mock ne sert à rien.
- Deux tests répètent ce que `appendJarsToClasspathOffReturnsInput` vérifie déjà dans `TikaExtrasTest`.

### 2.3 Comparaison des oracles

| Critère | Tests originaux (`TikaExtrasTest`) | Tests générés |
|---|---|---|
| Valeurs vérifiées | chaînes exactes (`"base" + sep + abs`), `assertSame` sur l'objet | une valeur (`assertNull`), ou chaînes inventées |
| Données de test | vrais jars créés dans un `@TempDir` | mocks de `Path`, de `Files`, de la classe testée |
| Portabilité | `File.pathSeparator`, fermeture du `URLClassLoader` pour Windows | `/` codé en dur |
| Nettoyage de l'état global | `finally` qui restaure propriété et classloader | aucun |
| Ce qui est testé | le code réel de `TikaExtras` | surtout les mocks eux-mêmes |

## 3. Mutation

| Mesure | Tests utilisés | Couverture de lignes | Score de mutation | Force des tests |
|---|---|---|---|---|
| **A** | tests d'origine (`TikaExtrasTest`, 5 tests) | 83 % (48/58) | 81 % (17/21) | 85 % |
| **B** | A + test généré par ChatUniTest (1 test) | 86 % (50/58) | 81 % (17/21) | 85 % |
| **C** | B + 4 tests écrits à la main | 88 % (51/58) | **100 %** (21/21) | **100 %** |

#### Mesure A
![Rapport Pitest, mesure A](images/pit_test_coverage_report_A.png)
#### Mesure B
![Rapport Pitest, mesure B](images/pit_test_coverage_report_B.png)
#### Mesure C
![Rapport Pitest, mesure C](images/pit_test_coverage_report_C.png)

### Mesure B : tests d'origine + test généré
La couverture de lignes passe de 83% à 86%, mais le score de mutation reste à 81%.
La raison est que ce test n’exécute que extrasDir(), alors que les mutants vivants sont dans install() et extraJars(). 
Autrement dit, le test de l’IA fait exécuter un peu plus de code, mais ne détecte aucun défaut de plus. Les trois autres \
fichiers générés ont été écartés.

### Mesure C
Le score de mutation passe à 100 % (21 mutants sur 21) et la couverture de lignes à 88 %.


## 4. Documentation des tests manuels

### Test 1 : `extraJarsAreSortedByFileName`
**Intention.** Vérifier que `extraJars()` renvoie les jars triés par nom de fichier. Ce tri rend l'ordre de chargement des jars, 
donc la priorité entre les classes qu'ils contiennent, identique d'une machine à l'autre. Sans lui, l'ordre dépendrait de celui 
dans lequel le système de fichiers liste les fichiers.
**Données de test.** Quatre fichiers vides : `B.jar`, `a.jar`, `C.jar` et `d.jar`. Les noms mélangent majuscules et minuscules 
pour une raison précise. Sous Windows, le système de fichiers liste les fichiers sans tenir compte de la casse, ce qui donne 
`a, B, C, d`. Le tri de `TikaExtras` compare les chaînes en tenant compte de la casse. Les deux ordres sont donc différents, et 
supprimer le tri change le résultat. Avec seulement `a.jar` et `b.jar`, les deux ordres seraient identiques et le mutant survivrait. 
J'ai choisi `d.jar` plutôt que `b.jar` parce que Windows ne distingue pas `B.jar` de `b.jar`, ce qui empêcherait de créer les deux fichiers.
**Oracle.** L'ordre attendu est `B.jar, C.jar, a.jar, d.jar`. Il vient de la façon dont Java compare les chaînes : les caractères 
sont comparés par leur code, et les majuscules, de 65 à 90, précèdent les minuscules, de 97 à 122. Le test compare donc la liste 
des noms obtenue à cette liste exacte avec `assertEquals`.
**Mutants visés.** Ligne 122, `removed call to List::sort`, et ligne 122, `replaced return value with ""` pour le comparateur. Dans 
le second cas, toutes les clés de tri deviennent égales, le tri ne change plus rien et l'ordre redevient celui du système de fichiers, 
ce que le test détecte.

### Test 2 : `installUsesThreadContextClassLoaderAsParent`
**Intention.** Vérifier que, lorsque le thread possède un classloader de contexte, `install()` l'utilise comme parent du nouveau 
`URLClassLoader`. Les classes du projet restent ainsi visibles depuis les jars supplémentaires.
**Données de test.** Un `URLClassLoader` vide, créé par le test et défini comme classloader du thread, et un dossier contenant 
un seul jar vide. Le classloader est créé par le test, donc différent de celui de `TikaExtras`. C'est indispensable : dans l'environnement 
de test, le classloader du thread et celui de `TikaExtras` sont probablement le même objet, ce qui explique que le mutant survivait avec 
les tests d'origine.
**Oracle.** Le code de `install()` prend le classloader du thread comme parent, et ne se rabat sur celui de `TikaExtras` que s'il 
est `null`. Le parent du classloader renvoyé doit donc être exactement l'objet défini sur le thread. Le test le vérifie avec `assertSame`, 
qui compare les références et non le contenu.
**Mutant visé.** Ligne 90, `negated conditional` sur `parent == null`. Avec la condition inversée, le classloader du thread serait 
remplacé par celui de `TikaExtras`, et `assertSame` échouerait.

### Test 3 : `installWithNullContextClassLoaderFallsBackToTikaExtrasLoader`
**Intention.** Vérifier le cas inverse : si le thread n'a aucun classloader de contexte, `install()` utilise celui de `TikaExtras` 
comme parent. Cette branche, aux lignes 90 et 91, n'était jamais exécutée.
**Données de test.** Le classloader du thread est mis à `null`, avec un dossier contenant un seul jar vide. Le jar est nécessaire 
parce que `install()` retourne `null` avant d'atteindre cette branche quand il n'y a rien à charger.
**Oracle.** D'après le code, le repli est `TikaExtras.class.getClassLoader()`. Le parent du classloader renvoyé doit donc être cet 
objet, ce que le test vérifie avec `assertSame`.
**Mutant visé.** Ligne 90, `negated conditional`, dans l'autre branche. Avec la condition inversée, le parent resterait `null` et 
le test échouerait. Ce test fait aussi exécuter la ligne 91, jusque-là sans couverture.

### Test 4 : `installRegistersLoaderWithServiceLoader`
**Intention.** Vérifier que `install()` enregistre le nouveau classloader auprès de `ServiceLoader`, la classe que Tika utilise 
pour découvrir ses parsers et ses détecteurs. Le simple fait de le définir sur le thread ne suffit pas, parce que `ServiceLoader` 
garde sa propre référence.
**Données de test.** Un dossier contenant un seul jar vide, ce qui suffit à ce que `install()` crée un classloader. Le test n'a 
besoin d'aucune autre configuration, et il se trouve dans le même package que `ServiceLoader`, ce qui lui donne accès à sa méthode 
`getContextClassLoader()`.
**Oracle.** La documentation de `ServiceLoader` indique que `getContextClassLoader()` renvoie le classloader enregistré, et son 
propre classloader par défaut si aucun n'a été enregistré. Après `install()`, elle doit donc renvoyer exactement le classloader 
que `install()` a retourné, ce que le test vérifie avec `assertSame`. Sans l'appel, elle renverrait le classloader par défaut, 
qui est un autre objet.
**Mutant visé.** Ligne 95, `removed call to ServiceLoader::setContextClassLoader`.

## 5. Déclaration de l'utilisation de l'IAG
Déclaration DDIA: Outil d'intelligence artificielle: Claude (Anthropic); 
Conceptualisation: L'IAG a été utilisée pour aider à expliquer les consignes de la Tâche 2 en précisant exactement ce qui est attendu et en expliquant les termes moins clairs. L'IAG a partiellement assistée dans l'écriture des tests manuelles puisque la méthode d'écriture de test n'a pas tout à fait été comprise.; 
Exécution: L'IAG a été utilisée pour aider à l'installation et à l'utilisation d'Ollama et de ChatUniTest puisque leur fonctionnement n'était pas clairs et un peu compliqué par moment. L'IAG a aussi été utilisé lors de l'exécution des tests dans le terminal puisque les commandes n'étaient pas tout à fait connues. L'IAG à également été utilisée pour aider dans le déboggage des tests écrits manuellement. Elle a aussi été utilisée pour aider à écrire dans les fichier pom.xml; 
Rédaction - Révision et édition: L'IAG a été utilisée pour aider à formatter le fichier README.md pour sauver du temps.