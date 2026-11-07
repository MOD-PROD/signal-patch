# signal-patch

Patches **Morphe** pour l'application **Signals** (`com.zyncas.signals`).

## Objectif

Remplacer le 5e onglet masqué (Launchpad) par **Auto Trading** (WebView locale ModProd).

```
Home | Trackers | Spot | Futures | Auto Trading
```

## Compatibilité

| App | Package | Version testée |
|-----|---------|----------------|
| Signals / Crypto Trading App | `com.zyncas.signals` | 14.3.1 (154) |

## Patches

| Patch | Type | Description |
|-------|------|-------------|
| **Enable Auto Trading tab** | resource | Rend visible le 5e item du menu, titre « Auto Trading », HTML asset |
| **Wire Auto Trading fragment** | bytecode + extension | Remplace `LaunchPadFragment` (`Lak/c`) par `AutoTradingFragment` dans le ViewPager |

## Utilisation (Morphe Manager)

1. Compiler ce dépôt → artefact `.mpp`
2. Ajouter le fichier patches dans Morphe Manager
3. Sélectionner l'APK Signals **original qui fonctionne**
4. Activer les deux patches
5. Patcher + installer

> Ne pas partir d'un APK déjà cassé par un rebuild apktool complet.

## Build

```bash
./gradlew :patches:build
# ou
./gradlew :patches:publishToMavenLocal
```

Prérequis : accès Maven Morphe (`GITHUB_TOKEN` avec `read:packages` si besoin).

## Sécurité

- Prototype UI uniquement (pas d'ordres OKX)
- Aucune clé API dans le HTML ni l'extension
