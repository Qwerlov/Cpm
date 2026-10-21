package com.oleo.cpmscripts.gui;

import com.oleo.cpmscripts.CpmScriptsClient;
import com.oleo.cpmscripts.ScriptEngine;
import net.minecraft.class_11908;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_342;
import net.minecraft.class_437;

public class RenameScreen extends class_437 {
   private final class_437 parent;
   private final String oldName;
   private class_342 input;
   private final String ext;
   private final boolean isNode;

   public RenameScreen(class_437 parent, String oldName) {
      this(parent, oldName, oldName.startsWith("nodes/"));
   }

   private RenameScreen(class_437 parent, String oldName, boolean isNode) {
      super(class_2561.method_43470("Переименовать"));
      this.parent = parent;
      this.isNode = isNode;
      String base = isNode ? oldName.substring("nodes/".length()) : oldName;
      this.ext = base.endsWith(".json") ? ".json" : (base.endsWith(".lua") ? ".lua" : "");
      this.oldName = oldName;
   }

   private String baseShown() {
      String base = this.isNode ? this.oldName.substring("nodes/".length()) : this.oldName;
      return this.ext.isEmpty() ? base : base.substring(0, base.length() - this.ext.length());
   }

   protected void method_25426() {
      this.input = new class_342(this.field_22793, this.field_22789 / 2 - 150, this.field_22790 / 2 - 30, 300, 18, class_2561.method_43470("имя"));
      this.input.method_1852(this.baseShown());
      this.input.method_25365(true);
      this.input.method_1872(false);
      this.input.method_1884(0);
      this.method_48265(this.input);
      this.method_37063(this.input);
      this.method_37063(new FlatButton(this.field_22789 / 2 - 150, this.field_22790 / 2 + 4, 145, 18, class_2561.method_43470("Переименовать"), (b) -> this.apply(), -14470354, -13744835, -8660849));
      this.method_37063(new FlatButton(this.field_22789 / 2 + 5, this.field_22790 / 2 + 4, 145, 18, class_2561.method_43470("Отмена"), (b) -> this.method_25419()));
   }

   private void apply() {
      String var10000 = this.input.method_1882().trim();
      String newName = var10000 + this.ext;
      if (this.isNode) {
         newName = "nodes/" + newName;
      }

      String err = ScriptEngine.renameScript(this.oldName, newName);
      if (err != null) {
         CpmScriptsClient.chat("Переименование: " + err);
      } else {
         class_310.method_1551().method_1507(this.parent);
         class_437 var4 = this.parent;
         if (var4 instanceof ScriptListScreen) {
            ScriptListScreen sl = (ScriptListScreen)var4;
            sl.method_25410(sl.field_22789, sl.field_22790);
         }

      }
   }

   public boolean method_25404(class_11908 evt) {
      if (evt.comp_4795() == 257) {
         this.apply();
         return true;
      } else {
         return super.method_25404(evt);
      }
   }

   public void method_25394(class_332 gg, int mouseX, int mouseY, float partialTick) {
      GuiStyle.background(gg, this.field_22789, this.field_22790);
      GuiStyle.header(gg, this.field_22793, this.field_22789, "Переименовать", this.oldName);
      gg.method_25300(this.field_22793, "Новое имя:", this.field_22789 / 2, this.field_22790 / 2 - 44, -6643536);
      super.method_25394(gg, mouseX, mouseY, partialTick);
   }

   public void method_25419() {
      class_310.method_1551().method_1507(this.parent);
   }
}
