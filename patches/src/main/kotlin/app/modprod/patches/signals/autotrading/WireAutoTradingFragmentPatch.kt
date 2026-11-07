package app.modprod.patches.signals.autotrading

import app.modprod.patches.signals.shared.Constants.COMPATIBILITY_SIGNALS
import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.extensions.InstructionExtensions.removeInstruction
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.TypeReference

private const val MAIN_ACTIVITY = "Lcom/zyncas/signals/ui/main/MainActivity;"
private const val LAUNCHPAD_TYPE = "Lak/c;"
private const val AUTO_TRADING =
    "Lapp/modprod/extension/autotrading/AutoTradingFragment;"

/**
 * Fingerprint large : méthode privée sans paramètre de MainActivity qui référence
 * le type LaunchPad (`Lak/c`) — en pratique `F0()` qui remplit le ViewPager.
 */
private object SetupViewPagerFingerprint : Fingerprint(
    definingClass = MAIN_ACTIVITY,
    returnType = "V",
    parameters = emptyList(),
)

/**
 * Remplace le 5e fragment du ViewPager (LaunchPadFragment / Lak/c)
 * par AutoTradingFragment (extension Morphe).
 *
 * Ne touche pas aux 4 premiers onglets.
 */
@Suppress("unused")
val wireAutoTradingFragmentPatch = bytecodePatch(
    name = "Wire Auto Trading fragment",
    description = "Replaces LaunchPad (5th ViewPager page) with AutoTradingFragment. " +
        "Requires « Enable Auto Trading tab ».",
    default = true,
) {
    compatibleWith(COMPATIBILITY_SIGNALS)

    dependsOn(enableAutoTradingTabPatch)

    extendWith("extensions/extension.mpe")

    execute {
        val method = findViewPagerSetupMethod()
            ?: throw PatchException(
                "Could not find MainActivity ViewPager setup (Lak/c reference). " +
                    "App version may differ from 14.3.1.",
            )

        // Locate: iget-object vX, ...->b1:Lak/c;  (or any iget of Lak/c)
        val igetIndex = method.implementation!!.instructions.indexOfFirst { insn ->
            if (insn.opcode != Opcode.IGET_OBJECT) return@indexOfFirst false
            val ref = (insn as? ReferenceInstruction)?.reference as? FieldReference
            ref?.type == LAUNCHPAD_TYPE || ref?.definingClass == LAUNCHPAD_TYPE
        }

        if (igetIndex < 0) {
            // Fallback: new-instance Lak/c
            val newIndex = method.implementation!!.instructions.indexOfFirst { insn ->
                if (insn.opcode != Opcode.NEW_INSTANCE) return@indexOfFirst false
                val ref = (insn as? ReferenceInstruction)?.reference as? TypeReference
                ref?.type == LAUNCHPAD_TYPE
            }
            if (newIndex < 0) {
                throw PatchException("LaunchPad type Lak/c not found in ViewPager setup method")
            }
            // Too version-specific to rewrite a full new-instance block safely here.
            throw PatchException(
                "Found new-instance Lak/c but not iget field pattern; adjust fingerprint for this APK.",
            )
        }

        val iget = method.getInstruction(igetIndex)
        // After iget, the next useful insn is ArrayList.add(object).
        // Replace iget with: new-instance + invoke-direct <init>
        // Keep the same destination register as the iget.

        val destReg = (iget as com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction).registerA

        method.removeInstruction(igetIndex)
        method.addInstructions(
            igetIndex,
            """
                new-instance v$destReg, $AUTO_TRADING
                invoke-direct { v$destReg }, $AUTO_TRADING-><init>()V
            """.trimIndent(),
        )
    }
}

private fun findViewPagerSetupMethod(): MutableMethod? {
    // Prefer methods that both return void, take no args, and reference Lak/c.
    val candidates = SetupViewPagerFingerprint.methodOrNull?.let { listOf(it) }
        ?: emptyList()

    // If the simple fingerprint is ambiguous, scan MainActivity methods.
    val classDef = app.morphe.patcher.util.classBy {
        it.type == MAIN_ACTIVITY
    } ?: return candidates.firstOrNull()

    return classDef.methods.firstOrNull { method ->
        method.parameterTypes.isEmpty() &&
            method.returnType == "V" &&
            method.implementation?.instructions?.any { insn ->
                val ref = (insn as? ReferenceInstruction)?.reference
                when (ref) {
                    is FieldReference -> ref.type == LAUNCHPAD_TYPE
                    is TypeReference -> ref.type == LAUNCHPAD_TYPE
                    else -> false
                }
            } == true
    } as? MutableMethod ?: candidates.firstOrNull()
}
