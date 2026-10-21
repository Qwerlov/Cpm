package com.oleo.cpmscripts.gui;

import net.minecraft.class_2561;
import net.minecraft.class_327;
import net.minecraft.class_332;

public final class GuiStyle {
   public static final int BG = -267119332;
   public static final int HEADER = -15000281;
   public static final int PANEL = -14802903;
   public static final int PANEL_BORDER = -13355194;
   public static final int ACCENT = -10764098;
   public static final int BTN = -14013126;
   public static final int BTN_HOVER = -12960176;
   public static final int BTN_DISABLED = -14539730;
   public static final int BTN_BORDER = -12236194;
   public static final int TEXT = -1512720;
   public static final int MUTED = -6643536;
   public static final int ERROR = -38037;
   public static final int ON = -8660849;

   private GuiStyle() {
   }

   public static void background(class_332 gg, int w, int h) {
      gg.method_25294(0, 0, w, h, -267119332);
   }

   public static void header(class_332 gg, class_327 font, int w, String title, String subtitle) {
      gg.method_25294(0, 0, w, 26, -15000281);
      gg.method_25294(0, 26, w, 28, -10764098);
      class_2561 titleComponent = class_2561.method_43470(title).method_27694((s) -> s.method_36139(6013118).method_10982(true));
      gg.method_27535(font, titleComponent, 10, 9, -1512720);
      if (subtitle != null && !subtitle.isEmpty()) {
         int tw = font.method_27525(titleComponent);
         gg.method_25303(font, subtitle, 10 + tw + 4, 9, -6643536);
      }

   }

   public static void panel(class_332 gg, int x, int y, int w, int h) {
      gg.method_25294(x, y, x + w, y + h, -14802903);
      gg.method_25294(x, y, x + w, y + 1, -13355194);
      gg.method_25294(x, y + h - 1, x + w, y + h, -13355194);
      gg.method_25294(x, y, x + 1, y + h, -13355194);
      gg.method_25294(x + w - 1, y, x + w, y + h, -13355194);
   }

   public static void divider(class_332 gg, int x, int y, int w) {
      gg.method_25294(x, y, x + w, y + 1, -13355194);
   }
}
