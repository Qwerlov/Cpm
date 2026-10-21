package com.oleo.cpmscripts.gui;

import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_437;

public class TextViewScreen extends class_437 {
   private final class_437 parent;
   private final String title;
   private final String text;
   private int scroll;

   public TextViewScreen(class_437 parent, String title, String text) {
      super(class_2561.method_43470(title));
      this.parent = parent;
      this.title = title;
      this.text = text;
   }

   protected void method_25426() {
      this.method_37063(new FlatButton(this.field_22789 - 76, this.field_22790 - 24, 70, 18, class_2561.method_43470("Назад"), (b) -> this.method_25419()));
   }

   public void method_25394(class_332 gg, int mouseX, int mouseY, float partialTick) {
      GuiStyle.background(gg, this.field_22789, this.field_22790);
      GuiStyle.header(gg, this.field_22793, this.field_22789, this.title, "колесо — прокрутка");
      String[] lines = this.text.split("\n", -1);
      int y = 34 - this.scroll;

      for(String line : lines) {
         if (y > 8 && y < this.field_22790 - 28) {
            gg.method_25303(this.field_22793, line, 10, y, line.startsWith("--") ? -6643536 : -1512720);
         }

         y += 11;
      }

      super.method_25394(gg, mouseX, mouseY, partialTick);
   }

   public boolean method_25401(double mx, double my, double sx, double sy) {
      this.scroll = (int)Math.max((double)0.0F, Math.min((double)Math.max(0, this.text.split("\n").length * 11 - this.field_22790 + 60), (double)this.scroll - sy * (double)15.0F));
      return true;
   }

   public void method_25419() {
      class_310.method_1551().method_1507(this.parent);
   }

   public boolean method_25421() {
      return false;
   }
}
