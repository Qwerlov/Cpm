package com.oleo.cpmscripts.gui;

import com.oleo.cpmscripts.ScriptEngine;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_437;

public class ScriptListScreen extends class_437 {
   private static final int ROW_H = 24;
   private static final int LIST_W = 340;
   private String pendingDelete;

   public ScriptListScreen() {
      super(class_2561.method_43470("CPM Scripts"));
   }

   protected void method_25426() {
      int x = this.field_22789 / 2 - 170;
      int y = 40;

      for(String[] info : ScriptEngine.scriptInfo()) {
         String name = info[0];
         boolean en = "1".equals(info[1]);
         boolean graph = name.startsWith("nodes/");
         String label = (graph ? "[граф] " : "") + name + (info[2].isEmpty() ? "" : "  (ошибка)");
         FlatButton toggle = new FlatButton(x + 6, y, 210, 18, class_2561.method_43470(label), (b) -> {
            if (this.pendingDelete == null) {
               ScriptEngine.setScriptEnabled(name, !en);
               this.method_41843();
            }
         }, en ? -14470354 : -14013126, en ? -13744835 : -12960176, en ? -8660849 : -6643536);
         this.method_37063(toggle);
         this.method_37063(new FlatButton(x + 340 - 120, y, 38, 18, class_2561.method_43470("edit"), (b) -> {
            if (this.pendingDelete == null) {
               if (graph) {
                  class_310.method_1551().method_1507(new NodeCanvasScreen(this, name.substring("nodes/".length())));
               } else {
                  class_310.method_1551().method_1507(new ScriptEditScreen(this, name));
               }

            }
         }, graph ? -13752512 : -14013126, graph ? -12568992 : -12960176, graph ? -10764098 : -1512720));
         this.method_37063(new FlatButton(x + 340 - 78, y, 34, 18, class_2561.method_43470("ren"), (b) -> {
            if (this.pendingDelete == null) {
               class_310.method_1551().method_1507(new RenameScreen(this, name));
            }
         }));
         this.method_37063(new FlatButton(x + 340 - 40, y, 34, 18, class_2561.method_43470("del"), (b) -> {
            if (this.pendingDelete == null) {
               this.pendingDelete = name;
               this.method_41843();
            }
         }, -12966362, -11653072, -38037));
         y += 24;
         if (y > this.field_22790 - 112) {
            break;
         }
      }

      int by = this.field_22790 - 58;
      this.method_37063(new FlatButton(x + 6, by, 70, 18, class_2561.method_43470("Reload"), (b) -> {
         ScriptEngine.reload();
         this.method_41843();
      }));
      this.method_37063(new FlatButton(x + 82, by, 90, 18, class_2561.method_43470(ScriptEngine.isEnabled() ? "Scripts: ON" : "Scripts: OFF"), (b) -> {
         ScriptEngine.setEnabled(!ScriptEngine.isEnabled());
         this.method_41843();
      }, ScriptEngine.isEnabled() ? -14470354 : -12966362, -12960176, ScriptEngine.isEnabled() ? -8660849 : -38037));
      this.method_37063(new FlatButton(x + 178, by, 46, 18, class_2561.method_43470("New"), (b) -> {
         class_310 var10000 = class_310.method_1551();
         long var10004 = System.currentTimeMillis();
         var10000.method_1507(new ScriptEditScreen(this, "new_" + var10004 % 100000L + ".lua"));
      }));
      this.method_37063(new FlatButton(x + 178, by - 22, 112, 18, class_2561.method_43470("New graph"), (b) -> {
         class_310 var10000 = class_310.method_1551();
         long var10004 = System.currentTimeMillis();
         var10000.method_1507(new NodeCanvasScreen(this, "graph_" + var10004 % 100000L + ".json"));
      }));
      this.method_37063(new FlatButton(x + 230, by, 64, 18, class_2561.method_43470("Add file"), (b) -> this.addFile(), -14405572, -13548454, -10764098));
      this.method_37063(new FlatButton(x + 340 - 40, by, 40, 18, class_2561.method_43470("Done"), (b) -> this.method_25419()));
      if (this.pendingDelete != null) {
         int dw = 220;
         int dx = this.field_22789 / 2 - dw / 2;
         int dy = this.field_22790 / 2 - 30;
         this.method_37063(new FlatButton(dx + 14, dy + 34, 90, 18, class_2561.method_43470("Удалить"), (b) -> {
            ScriptEngine.deleteScript(this.pendingDelete);
            this.pendingDelete = null;
            this.method_41843();
         }, -10868182, -8767946, -38037));
         this.method_37063(new FlatButton(dx + dw - 104, dy + 34, 90, 18, class_2561.method_43470("Отмена"), (b) -> {
            this.pendingDelete = null;
            this.method_41843();
         }));
      }

   }

   private void addFile() {
      if (FabricLoader.getInstance().isModLoaded("cpm")) {
         try {
            class_310.method_1551().method_1507(new CpmFilePickScreen(this));
            return;
         } catch (Throwable var2) {
         }
      }

      class_310.method_1551().method_1507(new FilePickScreen(this));
   }

   public void method_25394(class_332 gg, int mouseX, int mouseY, float partialTick) {
      GuiStyle.background(gg, this.field_22789, this.field_22790);
      GuiStyle.header(gg, this.field_22793, this.field_22789, "CPM Scripts", ScriptEngine.getScriptsDir().toString());
      int x = this.field_22789 / 2 - 170;
      int y0 = 36;
      int rows = Math.min(ScriptEngine.scriptInfo().size(), (this.field_22790 - 152) / 24 + 1);
      GuiStyle.panel(gg, x, y0, 340, Math.max(rows, 1) * 24 + 8);
      GuiStyle.panel(gg, x, this.field_22790 - 62, 340, 26);
      if (this.pendingDelete != null) {
         gg.method_25294(0, 0, this.field_22789, this.field_22790, -1610612736);
         int dw = 220;
         int dx = this.field_22789 / 2 - dw / 2;
         int dy = this.field_22790 / 2 - 30;
         GuiStyle.panel(gg, dx, dy, dw, 60);
      }

      super.method_25394(gg, mouseX, mouseY, partialTick);
      if (this.pendingDelete != null) {
         gg.method_25300(this.field_22793, "Удалить " + this.pendingDelete + "?", this.field_22789 / 2, this.field_22790 / 2 - 18, -1512720);
      }

      if (ScriptEngine.scriptInfo().isEmpty()) {
         gg.method_25300(this.field_22793, "Нет скриптов — New или Add file", this.field_22789 / 2, y0 + 10, -6643536);
      }

      int y = 40;

      for(String[] info : ScriptEngine.scriptInfo()) {
         if (!info[2].isEmpty()) {
            String err = info[2];
            if (err.length() > 55) {
               err = err.substring(0, 55);
            }

            gg.method_25303(this.field_22793, err, x + 10, y + 18, -38037);
         }

         y += 24;
         if (y > this.field_22790 - 112) {
            break;
         }
      }

   }

   public void method_25419() {
      class_310.method_1551().method_1507((class_437)null);
   }
}
