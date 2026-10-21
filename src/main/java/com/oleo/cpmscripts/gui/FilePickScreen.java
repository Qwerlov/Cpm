package com.oleo.cpmscripts.gui;

import com.oleo.cpmscripts.CpmScriptsClient;
import com.oleo.cpmscripts.ScriptEngine;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_437;

public class FilePickScreen extends class_437 {
   private static final int ROWS = 12;
   private static final int ROW_H = 20;
   private static final int LIST_W = 320;
   private final class_437 parent;
   private Path dir;
   private int page;
   private List<Path> entries = new ArrayList();

   public FilePickScreen(class_437 parent) {
      super(class_2561.method_43470("Add script"));
      this.parent = parent;
      this.dir = FabricLoader.getInstance().getGameDir();
   }

   protected void method_25426() {
      this.loadDir();
      int x = this.field_22789 / 2 - 160;
      int y = 40;
      if (this.dir.getParent() != null) {
         this.method_37063(new FlatButton(x + 6, y, 308, 16, class_2561.method_43470("[..]"), (b) -> {
            this.dir = this.dir.getParent();
            this.page = 0;
            this.method_41843();
         }));
         y += 20;
      }

      int start = this.page * 12;

      for(int i = 0; i < 12 && start + i < this.entries.size(); ++i) {
         Path p = (Path)this.entries.get(start + i);
         boolean isDir = Files.isDirectory(p, new LinkOption[0]);
         String label = isDir ? "[" + String.valueOf(p.getFileName()) + "]" : p.getFileName().toString();
         this.method_37063(new FlatButton(x + 6, y, 308, 16, class_2561.method_43470(label), (b) -> {
            if (isDir) {
               this.dir = p;
               this.page = 0;
               this.method_41843();
            } else {
               String err = ScriptEngine.addScriptFrom(p);
               if (err != null) {
                  CpmScriptsClient.chat("Ошибка копирования: " + err);
               }

               class_310.method_1551().method_1507(this.parent);
            }

         }, isDir ? -14013126 : -14405572, -12960176, isDir ? -1512720 : -10764098));
         y += 20;
      }

      int by = this.field_22790 - 58;
      if (this.page > 0) {
         this.method_37063(new FlatButton(x + 6, by, 70, 18, class_2561.method_43470("< prev"), (b) -> {
            --this.page;
            this.method_41843();
         }));
      }

      if ((this.page + 1) * 12 < this.entries.size()) {
         this.method_37063(new FlatButton(x + 320 - 76, by, 70, 18, class_2561.method_43470("next >"), (b) -> {
            ++this.page;
            this.method_41843();
         }));
      }

      this.method_37063(new FlatButton(x + 160 - 40, by, 80, 18, class_2561.method_43470("Cancel"), (b) -> class_310.method_1551().method_1507(this.parent)));
   }

   private void loadDir() {
      this.entries.clear();

      try {
         Stream<Path> stream = Files.list(this.dir);

         try {
            Stream var10000 = stream.filter((p) -> Files.isDirectory(p, new LinkOption[0]) || p.getFileName().toString().endsWith(".lua")).sorted(Comparator.comparing((p) -> !Files.isDirectory(p, new LinkOption[0])).thenComparing((p) -> p.getFileName().toString().toLowerCase()));
            List var10001 = this.entries;
            Objects.requireNonNull(var10001);
            var10000.forEach(var10001::add);
         } catch (Throwable var5) {
            if (stream != null) {
               try {
                  stream.close();
               } catch (Throwable var4) {
                  var5.addSuppressed(var4);
               }
            }

            throw var5;
         }

         if (stream != null) {
            stream.close();
         }
      } catch (IOException e) {
         CpmScriptsClient.chat("Не читается папка: " + e.getMessage());
      }

   }

   public void method_25394(class_332 gg, int mouseX, int mouseY, float partialTick) {
      GuiStyle.background(gg, this.field_22789, this.field_22790);
      GuiStyle.header(gg, this.field_22793, this.field_22789, "CPM Scripts", "add file");
      int x = this.field_22789 / 2 - 160;
      GuiStyle.panel(gg, x, 34, 320, this.field_22790 - 100);
      String path = this.dir.toString();
      if (path.length() > 48) {
         String var10000 = path.substring(path.length() - 45);
         path = "..." + var10000;
      }

      gg.method_25303(this.field_22793, path, x + 8, this.field_22790 - 74, -6643536);
      super.method_25394(gg, mouseX, mouseY, partialTick);
   }

   public void method_25419() {
      class_310.method_1551().method_1507(this.parent);
   }
}
