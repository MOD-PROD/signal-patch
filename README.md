# signal-patch

Patches **Morphe** pour **Signals** (`com.zyncas.signals`), calqués sur le modèle [Morphe-Patches-by-Modprod](https://github.com/MOD-PROD/Morphe-Patches-by-Modprod).

## Objectif

```
Home | Trackers | Spot | Futures | Auto Trading
```

Le 5e onglet (Launchpad masqué) affiche une WebView locale ModProd (prototype OKX, sans clés API).

## Compatibilité

| App | Package | Version |
|-----|---------|---------|
| Signals / Crypto Trading App | `com.zyncas.signals` | 14.3.1 |

## Patches

| Nom | Type | Rôle |
|-----|------|------|
| Enable Auto Trading tab | resource | Menu visible + titre + HTML asset |
| Wire Auto Trading fragment | bytecode + extension | Remplace `Lak/c` (LaunchPad) dans le ViewPager |

## Build (comme le template ModProd)

```bash
# Token GitHub Packages (read:packages) si besoin :
# ~/.gradle/gradle.properties
# gpr.user=TON_USER
# gpr.key=ghp_xxx

./gradlew :patches:build
./gradlew :patches:generatePatchesList
```

Artefact : `patches/build/libs/*.mpp` → à charger dans **Morphe Manager**.

## Utilisation

1. APK Signals **qui démarre déjà** (pas un rebuild apktool cassé)
2. Morphe Manager → importer le `.mpp`
3. Activer les deux patches
4. Patcher → installer

## Structure (miroir du template)

```
settings.gradle.kts
patches/
  build.gradle.kts
  src/main/kotlin/app/modprod/patches/signals/...
  src/main/kotlin/util/PatchListGenerator.kt
  src/main/resources/signals/autotrading/index.html
extensions/extension/
  AutoTradingFragment.java
gradle/
```
