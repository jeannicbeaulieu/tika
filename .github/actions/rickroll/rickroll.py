#!/usr/bin/env python3
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

import glob
import json
import os
import random
import sys
import urllib.error
import urllib.request
import xml.etree.ElementTree as ET  # nosemgrep: python.lang.security.use-defused-xml.use-defused-xml -- fichiers XML proviennent exclusivement des rapports Surefire du build local, source sûre

VIDEO_URL = "https://www.youtube.com/watch?v=dQw4w9WgXcQ"

GIF_URL = ("https://user-images.githubusercontent.com/37572049/"
           "90699500-0cc3ec00-e2a1-11ea-8d13-989526e86b0e.gif")

def env(name, default=""):
    return os.environ.get(name, default).strip()


def failed_tests(patterns):
    """Retourne la liste des (classe, test, message) en échec ou en erreur."""
    failures, seen = [], set()
    for pattern in patterns.split():
        for path in glob.glob(pattern, recursive=True):
            if path in seen:
                continue
            seen.add(path)
            try:
                root = ET.parse(path).getroot()
            except (ET.ParseError, OSError):
                continue
            for case in root.iter("testcase"):
                for tag in ("failure", "error"):
                    node = case.find(tag)
                    if node is not None:
                        msg = (node.get("message") or node.get("type") or tag).strip()
                        failures.append((case.get("classname", "?"), case.get("name", "?"),
                                         " ".join(msg.split())[:150]))
    return failures


def set_output(name, value):
    out = os.environ.get("GITHUB_OUTPUT")
    if out:
        with open(out, "a", encoding="utf-8") as f:
            f.write(f"{name}={value}\n")


def markdown(failures):
    lines = ["## :microphone: Des tests ont échoué", ""]
    if failures:
        lines += ["| Classe | Test | Message |", "|---|---|---|"]
        for cls, name, msg in failures[:25]:
            lines.append(f"| `{cls.split('.')[-1]}` | `{name}` | {msg.replace('|', '/')} |")
        if len(failures) > 25:
            lines.append(f"| ... | {len(failures) - 25} autre(s) | |")
        lines.append("")
    lines += [
        f"**[:point_right: Ouvrir le rapport détaillé des échecs]({VIDEO_URL})**",
        "",
        f"![rickroll]({GIF_URL})",
        "",
        "_Vous venez d'être rickrollé. Corrigez les tests pour que ça n'arrive plus._",
    ]
    return "\n".join(lines) + "\n"


def comment_on_pr(body):
    token = env("INPUT_GITHUB_TOKEN")
    repo = env("GITHUB_REPOSITORY")
    number = env("PR_NUMBER")
    api = env("GITHUB_API_URL", "https://api.github.com")
    if not (token and repo and number):
        return
    req = urllib.request.Request(
        f"{api}/repos/{repo}/issues/{number}/comments",
        data=json.dumps({"body": body}).encode("utf-8"),
        headers={"Authorization": f"Bearer {token}",
                 "Accept": "application/vnd.github+json",
                 "Content-Type": "application/json"},
        method="POST")
    try:
        with urllib.request.urlopen(req, timeout=20):  # nosemgrep: python.lang.security.audit.dynamic-urllib-use-detected.dynamic-urllib-use-detected -- URL et paramètres entièrement contrôlés (API GitHub officielle), aucune donnée utilisateur
            print("Commentaire rickroll publié sur la PR #" + number)
    except (urllib.error.URLError, OSError) as e:
        print(f"::warning::Impossible de commenter la PR #{number} : {e}")


def main():
    failures = failed_tests(env("INPUT_REPORTS", "**/target/surefire-reports/TEST-*.xml"))
    set_output("failed-tests", str(len(failures)))

    if env("INPUT_ONLY_ON_TEST_FAILURES", "true").lower() == "true" and not failures:
        print("Aucun cas de test en échec dans les rapports : pas de rickroll cette fois.")
        set_output("rickrolled", "false")
        return 0

    try:
        percentage = int(env("INPUT_PERCENTAGE", "100"))
    except ValueError:
        percentage = 100
    if percentage <= random.randrange(100):
        print("Vous avez de la chance : pas de rickroll cette fois.")
        set_output("rickrolled", "false")
        return 0

    print(f"{len(failures)} cas de test en échec.")
    print(f"::error title=Tests en échec ({len(failures)})::"
          f"Rapport détaillé des échecs : {VIDEO_URL}")

    body = markdown(failures)
    summary = os.environ.get("GITHUB_STEP_SUMMARY")
    if summary:
        with open(summary, "a", encoding="utf-8") as f:
            f.write(body)
    if env("INPUT_COMMENT_ON_PR", "true").lower() == "true":
        comment_on_pr(body)

    set_output("rickrolled", "true")
    return 0


if __name__ == "__main__":
    sys.exit(main())