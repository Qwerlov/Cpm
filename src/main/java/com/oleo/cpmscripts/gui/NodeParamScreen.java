package com.oleo.cpmscripts.gui;

import com.oleo.cpmscripts.nodes.NodeLists;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_342;
import net.minecraft.class_437;

public class NodeParamScreen extends class_437 {
   private final class_437 parent;
   private final String title;
   private final List<String> options;
   private final String initial;
   private final boolean numberMode;
   private final Consumer<String> callback;
   private class_342 search;
   private class_342 input;
   private List<String> filtered = new ArrayList();
   private String selected;
   private int scrollOffset = 0;

   public NodeParamScreen(class_437 parent, String title, List<String> options, String initial, boolean numberMode, Consumer<String> callback) {
      super(class_2561.method_43470(title));
      this.parent = parent;
      this.title = title;
      this.options = options;
      this.initial = initial == null ? "" : initial;
      this.numberMode = numberMode;
      this.callback = callback;
      this.selected = this.initial;
   }

   private boolean smallList() {
      return this.options != null && this.options.size() <= 30;
   }

   private static String fmtNum(String s) {
      return s.endsWith(".0") ? s.substring(0, s.length() - 2) : s;
   }

   protected void method_25426() {
      if (this.options != null) {
         if (!this.smallList()) {
            this.search = new class_342(this.field_22793, this.field_22789 / 2 - 150, 34, 300, 16, class_2561.method_43470("поиск"));
            this.search.method_47404(class_2561.method_43470("поиск..."));
            this.search.method_1863((value) -> this.refilter());
            this.method_37063(this.search);
            this.refilter();
         } else {
            this.filtered.clear();
            this.filtered.addAll(this.options);
         }
      } else {
         this.input = new class_342(this.field_22793, this.field_22789 / 2 - 150, 40, 300, 18, class_2561.method_43470("значение"));
         this.input.method_1852(this.numberMode ? fmtNum(this.initial) : this.initial);
         this.method_37063(this.input);
         this.method_48265(this.input);
      }

      this.method_37063(new FlatButton(this.field_22789 / 2 - 150, this.field_22790 - 30, 145, 18, class_2561.method_43470("OK"), (b) -> this.apply()));
      this.method_37063(new FlatButton(this.field_22789 / 2 + 5, this.field_22790 - 30, 145, 18, class_2561.method_43470("Отмена"), (b) -> this.method_25419()));
   }

   private void refilter() {
      this.filtered.clear();
      String q = this.search == null ? "" : this.search.method_1882().toLowerCase(Locale.ROOT);

      for(String o : this.options) {
         if (q.isEmpty() || o.toLowerCase(Locale.ROOT).contains(q)) {
            this.filtered.add(o);
         }

         if (this.filtered.size() >= 500) {
            break;
         }
      }

      this.scrollOffset = Math.max(0, Math.min(this.scrollOffset, this.maxScrollRows()));
   }

   private int listStartY() {
      return this.search != null ? 58 : 40;
   }

   private int visibleRows() {
      return Math.max(0, (this.field_22790 - 40 - this.listStartY()) / 14);
   }

   private int maxScrollRows() {
      return Math.max(0, this.filtered.size() - this.visibleRows());
   }

   private void apply() {
      if (this.options != null) {
         String v = this.selected;
         if (v == null || v.isEmpty() || v.equals(this.initial)) {
            String q = this.search == null ? "" : this.search.method_1882().trim().toLowerCase(Locale.ROOT);
            if (!q.isEmpty()) {
               String first = null;

               for(String o : this.filtered) {
                  if (first == null) {
                     first = o;
                  }

                  if (o.toLowerCase(Locale.ROOT).equals(q)) {
                     first = o;
                     break;
                  }
               }

               if (first == null) {
                  this.refilter();
               }

               if (first == null && !this.filtered.isEmpty()) {
                  first = (String)this.filtered.get(0);
               }

               if (first != null) {
                  v = first;
               }
            }
         }

         if (v != null && !v.isEmpty()) {
            this.callback.accept(v);
         }
      } else {
         String v = this.input.method_1882().trim().replace(',', '.');
         if (this.numberMode) {
            try {
               Double.parseDouble(v);
            } catch (NumberFormatException var6) {
               this.method_25419();
               return;
            }
         }

         this.callback.accept(v);
      }

      this.method_25419();
   }

   public void method_25394(class_332 gg, int mouseX, int mouseY, float partialTick) {
      GuiStyle.background(gg, this.field_22789, this.field_22790);
      GuiStyle.header(gg, this.field_22793, this.field_22789, this.title, this.options != null ? "вариантов: " + this.filtered.size() : (this.numberMode ? "число" : "текст"));
      if (this.options != null) {
         int y = this.listStartY();
         int x = this.field_22789 / 2 - 150;
         int endY = this.field_22790 - 40;
         gg.method_44379(x, y, x + 300, endY);
         int end = Math.min(this.filtered.size(), this.scrollOffset + this.visibleRows());

         for(int i = this.scrollOffset; i < end; ++i) {
            String o = (String)this.filtered.get(i);
            int rowY = y + (i - this.scrollOffset) * 14;
            boolean sel = o.equals(this.selected);
            boolean hover = mouseX >= x && mouseX < x + 300 && mouseY >= rowY && mouseY < rowY + 13;
            gg.method_25294(x, rowY, x + 300, rowY + 13, hover ? -12960176 : (sel ? -14467014 : -14013126));
            gg.method_25303(this.field_22793, o, x + 4, rowY + 3, sel ? -8660849 : -1512720);
         }

         gg.method_44380();
         if (this.filtered.size() > this.visibleRows()) {
            int var10002 = this.scrollOffset + 1;
            gg.method_25303(this.field_22793, "Колесо: прокрутка · " + var10002 + "–" + Math.min(this.filtered.size(), this.scrollOffset + this.visibleRows()) + "/" + this.filtered.size(), x, endY + 2, -6643536);
         }

         if (this.filtered.isEmpty()) {
            gg.method_25300(this.field_22793, "Ничего не найдено", this.field_22789 / 2, 70, -6643536);
         }
      }

      super.method_25394(gg, mouseX, mouseY, partialTick);
   }

   public boolean method_25402(class_11909 evt, boolean doubleClick) {
      double mx = evt.comp_4798();
      double my = evt.comp_4799();
      int btn = evt.method_74245();
      int startY = this.listStartY();
      if (this.options != null && btn == 0 && my >= (double)startY && my < (double)(this.field_22790 - 40) && mx >= (double)(this.field_22789 / 2 - 150) && mx < (double)(this.field_22789 / 2 + 150)) {
         int idx = this.scrollOffset + ((int)my - startY) / 14;
         if (idx >= this.scrollOffset && idx < Math.min(this.filtered.size(), this.scrollOffset + this.visibleRows())) {
            this.selected = (String)this.filtered.get(idx);
            this.apply();
            return true;
         }
      }

      return super.method_25402(evt, doubleClick);
   }

   public boolean method_25401(double mouseX, double mouseY, double scrollX, double scrollY) {
      if (this.options != null) {
         if (mouseY >= (double)this.listStartY() && mouseY < (double)(this.field_22790 - 40)) {
            this.scrollOffset = Math.max(0, Math.min(this.maxScrollRows(), this.scrollOffset - (int)Math.signum(scrollY) * Math.max(1, (int)Math.ceil(Math.abs(scrollY) * (double)3.0F))));
            return true;
         } else {
            return true;
         }
      } else {
         return super.method_25401(mouseX, mouseY, scrollX, scrollY);
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

   public void method_25419() {
      class_310.method_1551().method_1507(this.parent);
   }

   public boolean method_25421() {
      return false;
   }

   public static NodeParamScreen dropdown(class_437 parent, String title, List<String> ids, List<String> labels, String current, Consumer<String> cb) {
      return new NodeParamScreen(parent, title, labels, labelToId(ids, labels, current), false, (v) -> cb.accept(labelToId(labels, ids, v)));
   }

   private static String labelToId(List<String> from, List<String> to, String v) {
      int i = from.indexOf(v);
      return i >= 0 && i < to.size() ? (String)to.get(i) : v;
   }

   public static NodeParamScreen anims(class_437 parent, String current, Consumer<String> cb) {
      NodeLists.refreshProjects();
      return new NodeParamScreen(parent, "Анимация", NodeLists.anims(), current, false, cb);
   }

   public static NodeParamScreen layers(class_437 parent, String current, Consumer<String> cb) {
      NodeLists.refreshProjects();
      return new NodeParamScreen(parent, "Value-слой", NodeLists.layers(), current, false, cb);
   }

   public static NodeParamScreen items(class_437 parent, String current, Consumer<String> cb) {
      return new NodeParamScreen(parent, "Предмет", NodeLists.items(), current, false, cb);
   }

   public static NodeParamScreen number(class_437 parent, String title, double current, Consumer<String> cb) {
      List<String> none = null;
      return new NodeParamScreen(parent, title, none, String.valueOf(current), true, cb);
   }

   public static NodeParamScreen text(class_437 parent, String title, String current, Consumer<String> cb) {
      return new NodeParamScreen(parent, title, (List)null, current, false, cb);
   }

   public static NodeParamScreen effects(class_437 parent, String current, Consumer<String> cb) {
      return new NodeParamScreen(parent, "Эффект", NodeLists.effects(), current, false, cb);
   }

   public static NodeParamScreen entities(class_437 parent, String current, Consumer<String> cb) {
      return new NodeParamScreen(parent, "Тип сущности", NodeLists.entities(), current, false, cb);
   }

   public static NodeParamScreen blocks(class_437 parent, String current, Consumer<String> cb) {
      return new NodeParamScreen(parent, "Блок", NodeLists.blocks(), current, false, cb);
   }
}
