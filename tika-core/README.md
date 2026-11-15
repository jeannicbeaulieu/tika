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

1. <type> dans `<fichier>`
2. <type> dans `<fichier>`
3. <type> dans `<fichier>`
4. <type> dans `<fichier>`
5. <type> dans `<fichier>`
6. <type> dans `<fichier>`
7. <type> dans `<fichier>`

### 2.1 <Type de vulnérabilité> (CWE-XXX)

**Fichier :** `<chemin>`
**Pourquoi ce fichier :** (une ou deux phrases)

Code avant :

```java
```

Code après :

```java
```

**Pourquoi c'est vulnérable :** (expliquer le problème et comment on pourrait l'exploiter)

**Résultat Semgrep :** règle `<id>`, ligne <n>.

```text
extrait de la sortie
```

### 2.2 <Type de vulnérabilité> (CWE-XXX)

(Même structure que ci-dessus. Répéter jusqu'à la vulnérabilité 7.)

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

Nous avons utilisé Claude (Anthropic) pour :
- compléter et mettre en forme la documentation de ce README ;
- suggérer des extraits de code pour certaines vulnérabilités introduites.

Toutes les décisions de conception (choix des fichiers, types de vulnérabilités, choix de l'action maison) et la validation ont été réalisées par les membres du binôme.
