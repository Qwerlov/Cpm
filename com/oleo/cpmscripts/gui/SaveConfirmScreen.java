package com.oleo.cpmscripts.gui;

import net.minecraft.class_11908;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_437;

public class SaveConfirmScreen extends class_437 {
   private final class_437 parent;
   private final Runnable onSave;
   private final Runnable onDiscard;

   public SaveConfirmScreen(class_437 parent, Runnable onSave, Runnable onDiscard) {
      super(class_2561.method_43470("Сохранить изменения?"));
      this.parent = parent;
      this.onSave = onSave;
      this.onDiscard = onDiscard;
   }

   protected void method_25426() {
      int w = 110;
      int gap = 10;
      int total = w * 3 + gap * 2;
      int x = this.field_22789 / 2 - total / 2;
      int y = this.field_22790 / 2 + 4;
      this.method_37063(new FlatButton(x, y, w, 18, class_2561.method_43470("Сохранить"), (b) -> this.onSave.run(), -14470354, -13744835, -8660849));
      this.method_37063(new FlatButton(x + w + gap, y, w, 18, class_2561.method_43470("Не сохранять"), (b) -> this.onDiscard.run(), -12966362, -11653072, -38037));
      this.method_37063(new FlatButton(x + (w + gap) * 2, y, w, 18, class_2561.method_43470("Отмена"), (b) -> class_310.method_1551().method_1507(this.parent)));
   }

   public void method_25394(class_332 gg, int mouseX, int mouseY, float partialTick) {
      this.parent.method_25394(gg, -1, -1, partialTick);
      gg.method_25294(0, 0, this.field_22789, this.field_22790, -1610612736);
      gg.method_25300(this.field_22793, "Сохранить изменения перед выходом?", this.field_22789 / 2, this.field_22790 / 2 - 14, -1512720);
      super.method_25394(gg, mouseX, mouseY, partialTick);
   }

   public boolean method_25404(class_11908 evt) {
      if (evt.comp_4795() == 256) {
         class_310.method_1551().method_1507(this.parent);
         return true;
      } else {
         return super.method_25404(evt);
      }
   }

   public boolean method_25421() {
      return false;
   }
}
