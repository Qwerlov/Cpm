package com.oleo.cpmscripts.nodes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class NodeType {
   public final String id;
   public final String title;
   public final String category;
   public final List<Port> inputs = new ArrayList();
   public final List<Port> outputs = new ArrayList();
   public final List<Param> params = new ArrayList();
   public final int defW;

   private NodeType(String id, String title, String category, int defW) {
      this.id = id;
      this.title = title;
      this.category = category;
      this.defW = defW;
   }

   public static NodeType create(String id, String title, String category) {
      return new NodeType(id, title, category, 120);
   }

   public NodeType in(String id, String label, PType type) {
      this.inputs.add(new Port(id, label, type));
      return this;
   }

   public NodeType out(String id, String label, PType type) {
      this.outputs.add(new Port(id, label, type));
      return this;
   }

   public NodeType param(Param p) {
      this.params.add(p);
      return this;
   }

   public static Param dropdown(String id, String label, String[] ids, String[] labels, String def) {
      List<String> o = new ArrayList();
      List<String> l = new ArrayList();
      Collections.addAll(o, ids);
      Collections.addAll(l, labels);
      return new Param(id, label, NodeType.PKind.DROPDOWN, o, l, (double)0.0F, (double)0.0F, (double)0.0F, def);
   }

   public static Param num(String id, String label, double def, double min, double max) {
      return new Param(id, label, NodeType.PKind.NUMBER, (List)null, (List)null, def, min, max, (String)null);
   }

   public static Param text(String id, String label, String def) {
      return new Param(id, label, NodeType.PKind.TEXT, (List)null, (List)null, (double)0.0F, (double)0.0F, (double)0.0F, def);
   }

   public static Param anim(String id, String label) {
      return new Param(id, label, NodeType.PKind.ANIM, (List)null, (List)null, (double)0.0F, (double)0.0F, (double)0.0F, "");
   }

   public static Param layer(String id, String label) {
      return new Param(id, label, NodeType.PKind.LAYER, (List)null, (List)null, (double)0.0F, (double)0.0F, (double)0.0F, "");
   }

   public static Param item(String id, String label) {
      return new Param(id, label, NodeType.PKind.ITEM, (List)null, (List)null, (double)0.0F, (double)0.0F, (double)0.0F, "minecraft:stick");
   }

   public Param paramById(String pid) {
      for(Param p : this.params) {
         if (p.id.equals(pid)) {
            return p;
         }
      }

      return null;
   }

   public Port inputById(String pid) {
      for(Port p : this.inputs) {
         if (p.id.equals(pid)) {
            return p;
         }
      }

      return null;
   }

   public Port outputById(String pid) {
      for(Port p : this.outputs) {
         if (p.id.equals(pid)) {
            return p;
         }
      }

      return null;
   }

   public static Param effect(String id, String label) {
      return new Param(id, label, NodeType.PKind.EFFECT, (List)null, (List)null, (double)0.0F, (double)0.0F, (double)0.0F, "minecraft:speed");
   }

   public static Param entity(String id, String label) {
      return new Param(id, label, NodeType.PKind.ENTITY, (List)null, (List)null, (double)0.0F, (double)0.0F, (double)0.0F, "");
   }

   public static Param block(String id, String label) {
      return new Param(id, label, NodeType.PKind.BLOCK, (List)null, (List)null, (double)0.0F, (double)0.0F, (double)0.0F, "");
   }

   public static enum PType {
      BOOL,
      NUMBER,
      STRING;

      // $FF: synthetic method
      private static PType[] $values() {
         return new PType[]{BOOL, NUMBER, STRING};
      }
   }

   public static enum PKind {
      DROPDOWN,
      NUMBER,
      TEXT,
      ANIM,
      LAYER,
      ITEM,
      EFFECT,
      ENTITY,
      BLOCK;

      // $FF: synthetic method
      private static PKind[] $values() {
         return new PKind[]{DROPDOWN, NUMBER, TEXT, ANIM, LAYER, ITEM, EFFECT, ENTITY, BLOCK};
      }
   }

   public static final class Port {
      public final String id;
      public final String label;
      public final PType type;

      Port(String id, String label, PType type) {
         this.id = id;
         this.label = label;
         this.type = type;
      }
   }

   public static final class Param {
      public final String id;
      public final String label;
      public final PKind kind;
      public final List<String> options;
      public final List<String> optLabels;
      public final double def;
      public final double min;
      public final double max;
      public final String defText;

      Param(String id, String label, PKind kind, List<String> options, List<String> optLabels, double def, double min, double max, String defText) {
         this.id = id;
         this.label = label;
         this.kind = kind;
         this.options = options == null ? Collections.emptyList() : options;
         this.optLabels = optLabels == null ? Collections.emptyList() : optLabels;
         this.def = def;
         this.min = min;
         this.max = max;
         this.defText = defText == null ? "" : defText;
      }
   }
}
