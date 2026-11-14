#!/usr/bin/env bash
#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

set -uo pipefail

RULES_DIR="${1:?dossier des règles semgrep manquant}"
BASELINE="${2:-}"
OUT_DIR="${3:-semgrep-out}"
mkdir -p "$OUT_DIR"

# Seules les règles de *sécurité* Java sont utilisées (dossiers */security/).
CONFIG_ARGS=()
while IFS= read -r dir; do
  CONFIG_ARGS+=(--config "$dir")
done < <(find "$RULES_DIR/java" -type d -name security | sort)

if [ "${#CONFIG_ARGS[@]}" -eq 0 ]; then
  echo "::error::Aucune règle Semgrep trouvée dans $RULES_DIR/java"
  exit 2
fi

COMMON_ARGS=(
  scan
  "${CONFIG_ARGS[@]}"
  --metrics=off
  --disable-version-check
  --json-output="$OUT_DIR/nouvelles.json"
  --sarif-output="$OUT_DIR/nouvelles.sarif"
)

if [ -n "$BASELINE" ]; then
  echo "Analyse différentielle : seuls les résultats absents de $BASELINE sont rapportés."
  semgrep "${COMMON_ARGS[@]}" --baseline-commit "$BASELINE" --error
  STATUS=$?
else
  echo "Aucun commit de base : analyse complète, sans barrière."
  semgrep "${COMMON_ARGS[@]}"
  STATUS=$?
fi

# Résumé lisible (Markdown) + annotations GitHub sur les lignes fautives.
python3 - "$OUT_DIR/nouvelles.json" "$OUT_DIR/resume.md" "$BASELINE" <<'PY'
import json, os, sys

src, dst, baseline = sys.argv[1], sys.argv[2], sys.argv[3]
try:
    with open(src, encoding="utf-8") as f:
        data = json.load(f)
except (OSError, ValueError):
    data = {"results": [], "errors": [{"message": "rapport JSON illisible"}]}

results = data.get("results", [])
in_ci = os.environ.get("GITHUB_ACTIONS") == "true"
lines = []
if baseline:
    lines.append(f"## Semgrep : {len(results)} nouvelle(s) vulnérabilité(s) par rapport à `{baseline[:10]}`\n")
else:
    lines.append(f"## Semgrep : {len(results)} résultat(s) (analyse complète)\n")
if results:
    lines.append("| Règle | Sévérité | CWE | Fichier | Ligne |")
    lines.append("|---|---|---|---|---|")
for r in results:
    rule = r["check_id"].split(".")[-1]
    extra = r.get("extra", {})
    sev = extra.get("severity", "")
    cwe = extra.get("metadata", {}).get("cwe", [""])
    cwe = (cwe[0] if isinstance(cwe, list) and cwe else str(cwe)).split(":")[0]
    path, line = r["path"], r["start"]["line"]
    lines.append(f"| `{rule}` | {sev} | {cwe} | `{path}` | {line} |")
    if in_ci and baseline:
        msg = " ".join(extra.get("message", "").split())[:300]
        print(f"::error file={path},line={line},title=Semgrep {rule}::{msg}")
if data.get("errors"):
    lines.append(f"\n{len(data['errors'])} erreur(s) d'analyse Semgrep (voir le journal).")
text = "\n".join(lines) + "\n"
with open(dst, "w", encoding="utf-8") as f:
    f.write(text)
print(text)
PY

if [ -n "${GITHUB_STEP_SUMMARY:-}" ]; then
  cat "$OUT_DIR/resume.md" >> "$GITHUB_STEP_SUMMARY"
fi

# 0 = rien de nouveau, 1 = nouvelles vulnérabilités, >= 2 = erreur d'exécution.
if [ "$STATUS" -eq 1 ]; then
  echo "::error::Le commit introduit au moins une nouvelle vulnérabilité détectée par Semgrep."
fi
exit "$STATUS"