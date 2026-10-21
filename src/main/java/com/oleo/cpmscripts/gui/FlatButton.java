package com.oleo.cpmscripts.gui;

import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_4185;

public class FlatButton extends class_4185 {
   private final int baseColor;
   private final int hoverColor;
   private final int textColor;

   public FlatButton(int x, int y, int w, int h, class_2561 msg, class_4185.class_4241 onPress) {
      this(x, y, w, h, msg, onPress, -14013126, -12960176, -1512720);
   }

   public FlatButton(int x, int y, int w, int h, class_2561 msg, class_4185.class_4241 onPress, int base, int hover, int text) {
      super(x, y, w, h, msg, onPress, class_4185.field_40754);
      this.baseColor = base;
      this.hoverColor = hover;
      this.textColor = text;
   }

   public void method_75752(class_332 gg, int mouseX, int mouseY, float partialTick) {
      int c = !this.field_22763 ? -14539730 : (this.method_25367() ? this.hoverColor : this.baseColor);
      int x = this.method_46426();
      int y = this.method_46427();
      int w = this.method_25368();
      int h = this.method_25364();
      gg.method_25294(x, y, x + w, y + h, c);
      int border = this.method_25367() && this.field_22763 ? -10764098 : -12236194;
      gg.method_25294(x, y, x + w, y + 1, border);
      gg.method_25294(x, y + h - 1, x + w, y + h, border);
      gg.method_25294(x, y, x + 1, y + h, border);
      gg.method_25294(x + w - 1, y, x + w, y + h, border);
      int tc = this.field_22763 ? this.textColor : -6643536;
      gg.method_27534(class_310.method_1551().field_1772, this.method_25369(), x + w / 2, y + (h - 8) / 2, tc);
   }
}
