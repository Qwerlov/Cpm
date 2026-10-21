package com.oleo.cpmscripts.gui;

import com.oleo.cpmscripts.ScriptEngine;
import java.lang.reflect.Field;
import net.minecraft.class_11908;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_437;
import net.minecraft.class_7529;
import net.minecraft.class_7530;

public class ScriptEditScreen extends class_437 {
   private final class_437 parent;
   private final String fileName;
   private class_7529 editor;
   private class_7530 textField;
   private int lines = 1;
   private int chars = 0;
   private long savedFlash;

   public ScriptEditScreen(class_437 parent, String fileName) {
      super(class_2561.method_43470("Edit: " + fileName));
      this.parent = parent;
      this.fileName = fileName;
   }

   protected void method_25426() {
      int x = this.field_22789 / 2 - (this.field_22789 - 60) / 2;
      this.editor = class_7529.method_71507().method_71508(x + 6).method_71512(40).method_71510(class_2561.method_43470("-- lua script")).method_71513(false).method_71509(this.field_22793, this.field_22789 - 72, this.field_22790 - 120, class_2561.method_43470(this.fileName));
      this.editor.method_44400(ScriptEngine.readScript(this.fileName));
      this.editor.method_44401((vx) -> {
         this.chars = vx.length();
         int n = 1;

         for(int i = 0; i < vx.length(); ++i) {
            if (vx.charAt(i) == '\n') {
               ++n;
            }
         }

         this.lines = n;
      });
      this.method_37063(this.editor);

      try {
         Field f = class_7529.class.getDeclaredField("textField");
         f.setAccessible(true);
         this.textField = (class_7530)f.get(this.editor);
      } catch (Throwable var5) {
      }

      String v = this.editor.method_44405();
      this.chars = v.length();
      this.lines = 1;

      for(int i = 0; i < v.length(); ++i) {
         if (v.charAt(i) == '\n') {
            ++this.lines;
         }
      }

      int by = this.field_22790 - 58;
      int cx = this.field_22789 / 2;
      this.method_37063(new FlatButton(cx - 112, by, 100, 18, class_2561.method_43470("Save (Ctrl+S)"), (b) -> this.save(), -14470354, -13744835, -8660849));
      this.method_37063(new FlatButton(cx + 12, by, 100, 18, class_2561.method_43470("Cancel"), (b) -> class_310.method_1551().method_1507(this.parent)));
   }

   private void save() {
      ScriptEngine.saveScript(this.fileName, this.editor.method_44405());
      this.savedFlash = System.currentTimeMillis();
   }

   public boolean method_25404(class_11908 event) {
      boolean ctrl = (event.comp_4797() & 2) != 0;
      if (event.comp_4795() == 83 && ctrl) {
         this.save();
         return true;
      } else {
         if (event.comp_4795() == 86 && ctrl && this.textField != null && this.editor.method_25370()) {
            String clip = class_310.method_1551().field_1774.method_1460();
            if (clip != null && !clip.isEmpty()) {
               this.textField.method_44420(clip.replace("\r\n", "\n").replace('\r', '\n').replace("\t", "  "));
               return true;
            }
         }

         return super.method_25404(event);
      }
   }

   public void method_25394(class_332 gg, int mouseX, int mouseY, float partialTick) {
      GuiStyle.background(gg, this.field_22789, this.field_22790);
      GuiStyle.header(gg, this.field_22793, this.field_22789, "CPM Scripts", "edit: " + this.fileName);
      int x = this.field_22789 / 2 - (this.field_22789 - 60) / 2;
      GuiStyle.panel(gg, x, 34, this.field_22789 - 60, this.field_22790 - 108);
      super.method_25394(gg, mouseX, mouseY, partialTick);
      String status = this.lines + " строк, " + this.chars + " симв.";
      if (System.currentTimeMillis() - this.savedFlash < 1500L) {
         status = status + "   сохранено";
      }

      gg.method_25303(this.field_22793, status, x + 8, this.field_22790 - 74, -6643536);
   }

   public void method_25419() {
      class_310.method_1551().method_1507(this.parent);
   }
}
