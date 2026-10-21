package com.oleo.cpmscripts.gui;

import com.oleo.cpmscripts.CpmScriptsClient;
import com.oleo.cpmscripts.ScriptEngine;
import com.tom.cpl.gui.Frame;
import com.tom.cpl.gui.elements.FileChooserPopup;
import com.tom.cpl.util.EmbeddedLocalization;
import com.tom.cpm.client.GuiImpl;
import java.lang.reflect.Field;
import java.util.ArrayList;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_310;
import net.minecraft.class_437;

public class CpmFilePickScreen extends GuiImpl {
   public CpmFilePickScreen(class_437 parent) {
      super((gui) -> new Frame(gui) {
            public void initFrame(int width, int height) {
               FileChooserPopup fc = new FileChooserPopup(this);
               fc.setTitle(CpmFilePickScreen.loc("Выбери .lua скрипт"));
               fc.setFileDescText(CpmFilePickScreen.loc("Lua scripts (*.lua)"));
               fc.setCurrentDirectory(FabricLoader.getInstance().getGameDir().toFile());
               fc.setFilter(new FileChooserPopup.FileFilter("lua"));
               fc.setButtonText("Select");
               fc.setAccept((f) -> {
                  if (f != null) {
                     String err = ScriptEngine.addScriptFrom(f.toPath());
                     if (err != null) {
                        CpmScriptsClient.chat("Ошибка копирования: " + err);
                     }
                  }

                  class_310.method_1551().method_1507(parent);
               });
               fc.setOnClosed(() -> class_310.method_1551().method_1507(parent));
               this.openPopup(fc);
            }
         }, parent);
   }

   private static EmbeddedLocalization loc(String text) {
      repairAllEntries();
      EmbeddedLocalization e = new EmbeddedLocalization(text) {
      };
      e.setFallback(text);
      return e;
   }

   private static void repairAllEntries() {
      try {
         Field f = EmbeddedLocalization.class.getDeclaredField("allEntries");
         f.setAccessible(true);
         if (f.get((Object)null) == null) {
            f.set((Object)null, new ArrayList());
         }
      } catch (Throwable var1) {
      }

   }
}
