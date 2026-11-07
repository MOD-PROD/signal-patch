package app.modprod.patches.signals.autotrading

import app.modprod.patches.signals.shared.Constants.COMPATIBILITY_SIGNALS
import app.morphe.patcher.patch.resourcePatch
import app.morphe.util.asSequence
import org.w3c.dom.Element

/**
 * Rend visible le 5e onglet (navigation_launchpads), le renomme « Auto Trading »,
 * et embarque la page HTML locale du prototype OKX.
 *
 * Ne change pas encore le fragment ViewPager : voir [wireAutoTradingFragmentPatch].
 */
@Suppress("unused")
val enableAutoTradingTabPatch = resourcePatch(
    name = "Enable Auto Trading tab",
    description = "Shows the 5th bottom-nav item and renames it to Auto Trading. " +
        "Adds the local HTML UI under assets/modprod/autotrading/.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_SIGNALS)

    execute {
        // 1) Menu: visible=true on navigation_launchpads
        listOf(
            "res/menu/bottom_nav_menu.xml",
            "res/layout/bottom_nav_menu.xml",
        ).forEach { path ->
            runCatching {
                document(path).use { document ->
                    document.getElementsByTagName("item").asSequence()
                        .map { it as Element }
                        .filter { it.getAttribute("android:id").endsWith("navigation_launchpads") }
                        .forEach { item ->
                            item.setAttribute("android:visible", "true")
                            // Keep @string/launchpad id; value rewritten below.
                            if (item.getAttribute("android:title").isEmpty()) {
                                item.setAttribute("android:title", "@string/launchpad")
                            }
                        }
                }
            }
        }

        // 2) String title
        runCatching {
            document("res/values/strings.xml").use { document ->
                document.getElementsByTagName("string").asSequence()
                    .map { it as Element }
                    .filter { it.getAttribute("name") == "launchpad" }
                    .forEach { it.textContent = "Auto Trading" }
            }
        }

        // 3) Optional nav graph destination name (ViewPager is authoritative)
        runCatching {
            document("res/navigation/mobile_navigation.xml").use { document ->
                document.getElementsByTagName("fragment").asSequence()
                    .map { it as Element }
                    .filter { it.getAttribute("android:id").endsWith("navigation_launchpads") }
                    .forEach { frag ->
                        frag.setAttribute(
                            "android:name",
                            "app.modprod.extension.autotrading.AutoTradingFragment",
                        )
                        frag.setAttribute("android:label", "@string/launchpad")
                    }
            }
        }

        // 4) HTML asset (prototype, no API keys)
        val html = EnableAutoTradingTabPatch::class.java
            .getResourceAsStream("/signals/autotrading/index.html")
            ?.use { it.readBytes() }
            ?: DEFAULT_HTML.toByteArray(Charsets.UTF_8)

        get("assets/modprod/autotrading/index.html", copy = false).apply {
            parentFile.mkdirs()
            writeBytes(html)
        }
    }
}

private object EnableAutoTradingTabPatch

private val DEFAULT_HTML = """
<!doctype html>
<html lang="fr">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Auto Trading OKX</title>
<style>
:root{color-scheme:dark;font-family:system-ui;background:#0d1421;color:#eef3fc}
body{margin:0;padding:18px}h1{font-size:24px}
.panel{background:#172235;border:1px solid #2b3b54;border-radius:14px;padding:16px;margin:14px 0}
label{display:block;margin:12px 0}
input,select{background:#101b2d;color:white;border:1px solid #40506a;border-radius:8px;padding:9px;max-width:100%}
button{padding:12px;border-radius:8px;border:0;background:#51667f;color:white}
small,p{color:#b5c2d5}#summary{white-space:pre-wrap}
</style>
</head>
<body>
<small>MODPROD · SIGNALS</small>
<h1>Auto Trading — OKX</h1>
<p>Prototype local, aucune connexion OKX ni exécution d'ordres.</p>
<section class="panel">
<h2>Stratégie</h2>
<label><input type="checkbox" id="spot" checked> Signaux Spot</label>
<label><input type="checkbox" id="futures"> Signaux Futures</label>
<label>Montant max / position (USDT) <input id="amount" type="number" min="1" value="25"></label>
<label>Risque max (%) <input id="risk" type="number" min="0.1" max="5" step="0.1" value="1"></label>
<label>Levier maximal <input id="lev" type="number" min="1" max="5" value="2"></label>
<label>Direction
<select id="direction">
<option value="both">Long et Short</option>
<option value="long">Long</option>
<option value="short">Short</option>
</select>
</label>
<button id="preview">Prévisualiser</button>
<pre id="summary"></pre>
</section>
<section class="panel">
<h2>État</h2>
<p>OKX : non connecté</p>
<p>Trading automatique : arrêté</p>
<button disabled>Démarrer (indisponible)</button>
</section>
<small>Aucune clé API dans la WebView.</small>
<script>
document.getElementById('preview').onclick=function(){
  const v=id=>document.getElementById(id).value;
  document.getElementById('summary').textContent=
    'Spot: '+document.getElementById('spot').checked+
    '\nFutures: '+document.getElementById('futures').checked+
    '\nMontant: '+v('amount')+' USDT'+
    '\nRisque: '+v('risk')+'%'+
    '\nLevier: x'+v('lev')+
    '\nDirection: '+v('direction');
};
</script>
</body>
</html>
""".trimIndent()
