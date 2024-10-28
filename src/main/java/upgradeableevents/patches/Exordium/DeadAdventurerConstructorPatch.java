package upgradeableevents.patches.Exordium;

import basemod.BaseMod;
import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.events.RoomEventDialog;
import com.megacrit.cardcrawl.events.exordium.DeadAdventurer;

@SpirePatch(clz = DeadAdventurer.class, method = SpirePatch.CONSTRUCTOR)
public class DeadAdventurerConstructorPatch {
    @SpirePostfixPatch
    public static void Postfix(DeadAdventurer __instance) {
        // Reset the selected option when entering the event
        if (__instance.roomEventText != null) {
            ReflectionHacks.setPrivate(__instance.roomEventText, RoomEventDialog.class, "selectedOption", -1);
        }
    }
}