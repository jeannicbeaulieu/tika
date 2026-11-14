# Tâche 3

Binôme : NOM1 et NOM2
Dépôt du cas d'étude (Tika) : <lien du dépôt>
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
