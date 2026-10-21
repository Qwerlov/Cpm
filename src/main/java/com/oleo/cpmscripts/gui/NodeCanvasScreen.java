package com.oleo.cpmscripts.gui;

import com.oleo.cpmscripts.ScriptEngine;
import com.oleo.cpmscripts.nodes.GraphCompiler;
import com.oleo.cpmscripts.nodes.NodeGraph;
import com.oleo.cpmscripts.nodes.NodeGraphs;
import com.oleo.cpmscripts.nodes.NodeType;
import com.oleo.cpmscripts.nodes.NodeTypes;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_437;

public class NodeCanvasScreen extends class_437 {
   private static final int NODE_W = 150;
   private static final int HEADER_H = 14;
   private static final int ROW_H = 12;
   private static final int PORT_R = 3;
   private static final int BOOL_C = -8660849;
   private static final int NUM_C = -1523344;
   private static final int STR_C = -6516760;
   private static final int[] BOOL_PAL = new int[]{-8660849, -11544376, -4663217, -2580401, -2601040, -11563048};
   private static final int[] NUM_PAL = new int[]{-1523344, -1538470, -1187505, -4660913, -10821234, -10831647};
   private static final int[] STR_PAL = new int[]{-6516760, -2584600, -7359256, -1536076, -4943896, -7345960};
   private final class_437 parent;
   private NodeGraph graph;
   private String status = "";
   private long statusUntil;
   private float panX = 40.0F;
   private float panY = 40.0F;
   private float zoom = 1.0F;
   private String dragNode;
   private double grabDX;
   private double grabDY;
   private boolean panning;
   private double lastMX;
   private double lastMY;
   private String[] connectFrom;
   private boolean connectFromInput;
   private String selectedNode;
   private final Set<String> multiSel = new LinkedHashSet();
   private int groupCounter = 1;
   private int selectedWire = -1;
   private double ctxX;
   private double ctxY;
   private double ctxWorldX;
   private double ctxWorldY;
   private String ctxCategory;
   private boolean dirty;
   private final Deque<String> undoStack = new ArrayDeque();
   private String connectSnapshot;
   private String dragSnapshot;
   private boolean dragMoved;
   private boolean ctxOpen;

   private static int portColor(NodeType.PType type) {
      int var10000;
      switch (type) {
         case BOOL -> var10000 = -8660849;
         case NUMBER -> var10000 = -1523344;
         default -> var10000 = -6516760;
      }

      return var10000;
   }

   private int wireColor(NodeGraph.GNode from, String port, int wireIdx) {
      NodeType t = NodeTypes.byId(from.type);
      NodeType.Port p = t == null ? null : t.outputById(port);
      int[] var10000;
      if (p == null) {
         var10000 = BOOL_PAL;
      } else {
         switch (p.type) {
            case BOOL -> var10000 = BOOL_PAL;
            case NUMBER -> var10000 = NUM_PAL;
            default -> var10000 = STR_PAL;
         }
      }

      int[] pal = var10000;
      return pal[wireIdx % pal.length];
   }

   public NodeCanvasScreen(class_437 parent, String graphName) {
      super(class_2561.method_43470("Граф: " + graphName));
      this.parent = parent;

      try {
         this.graph = NodeGraphs.load(graphName);
      } catch (Throwable t) {
         this.graph = new NodeGraph();
         this.graph.name = graphName.replaceFirst("\\.json$", "");
         this.status("Не удалось открыть " + graphName + ": " + t.getMessage(), true);
      }

   }

   private void status(String msg, boolean isErr) {
      this.status = (isErr ? "§c" : "§a") + msg;
      this.statusUntil = System.currentTimeMillis() + 8000L;
   }

   protected void method_25426() {
      this.method_37063(new FlatButton(6, this.field_22790 - 24, 70, 18, class_2561.method_43470("Назад"), (b) -> this.tryClose()));
      this.method_37063(new FlatButton(82, this.field_22790 - 24, 70, 18, class_2561.method_43470("Сохранить"), (b) -> this.save()));
      this.method_37063(new FlatButton(158, this.field_22790 - 24, 80, 18, class_2561.method_43470("Проверить"), (b) -> this.validate()));
      this.method_37063(new FlatButton(244, this.field_22790 - 24, 110, 18, class_2561.method_43470("Lua-код"), (b) -> this.showLua()));
      this.method_37063(new FlatButton(360, this.field_22790 - 24, 80, 18, class_2561.method_43470("В группу"), (b) -> this.groupSelected()));
      this.method_37063(new FlatButton(446, this.field_22790 - 24, 100, 18, class_2561.method_43470("Разгруппировать"), (b) -> this.ungroupSelected()));
   }

   private void save() {
      try {
         NodeGraphs.save(this.graph);
         this.dirty = false;
         ScriptEngine.reload();
         String err = this.errorOf(this.graph.name);
         if (err == null) {
            this.status("Сохранено: " + this.graph.name + ".json", false);
         } else {
            this.status("Сохранено, но граф не работает: " + err, true);
         }
      } catch (Throwable t) {
         this.status("Ошибка сохранения: " + t.getMessage(), true);
      }

   }

   private String errorOf(String name) {
      for(String[] info : ScriptEngine.scriptInfo()) {
         if (info[0].equals("nodes/" + name + ".json")) {
            return info[2].isEmpty() ? null : info[2];
         }
      }

      return null;
   }

   private void validate() {
      try {
         GraphCompiler.compile(this.graph);
         this.status("Граф корректен (" + this.graph.nodes.size() + " нод, " + this.graph.wires.size() + " проводов)", false);
      } catch (Throwable t) {
         this.status("Ошибка: " + t.getMessage(), true);
      }

   }

   private void showLua() {
      String lua;
      try {
         lua = GraphCompiler.compile(this.graph);
      } catch (Throwable t) {
         this.status("Не скомпилировался: " + t.getMessage(), true);
         return;
      }

      class_310.method_1551().method_1507(new TextViewScreen(this, "Сгенерированный Lua — " + this.graph.name, lua));
   }

   public boolean method_25421() {
      return false;
   }

   private double toWorldX(double sx) {
      return (sx - (double)this.panX) / (double)this.zoom;
   }

   private double toWorldY(double sy) {
      return (sy - (double)this.panY) / (double)this.zoom;
   }

   private int nodeH(NodeGraph.GNode n) {
      NodeType t = NodeTypes.byId(n.type);
      int rows = t == null ? 1 : Math.max(t.inputs.size(), t.outputs.size());
      return 14 + rows * 12 + (t == null ? 0 : t.params.size()) * 12 + 6;
   }

   private double[] portPos(NodeGraph.GNode n, String portId, boolean input) {
      NodeType t = NodeTypes.byId(n.type);
      double x = input ? (double)n.x : (double)(n.x + 150);
      double y = (double)(n.y + 14) + (double)6.0F;
      if (t != null) {
         List<NodeType.Port> ports = input ? t.inputs : t.outputs;

         for(int i = 0; i < ports.size(); ++i) {
            if (((NodeType.Port)ports.get(i)).id.equals(portId)) {
               y = (double)(n.y + 14 + i * 12) + (double)6.0F;
               break;
            }
         }
      }

      return new double[]{x, y};
   }

   public boolean method_25402(class_11909 evt, boolean doubleClick) {
      double sx = evt.comp_4798();
      double sy = evt.comp_4799();
      int btn = evt.method_74245();
      if (this.ctxCategory == null && !this.isCtxOpen()) {
         if (!(sy < (double)28.0F) && !(sy > (double)(this.field_22790 - 28))) {
            double wx = this.toWorldX(sx);
            double wy = this.toWorldY(sy);
            if (btn == 1) {
               NodeGraph.GNode n = this.nodeAt(wx, wy);
               if (n != null) {
                  this.pushUndo();
                  this.graph.removeNode(n.id);
                  if (n.id.equals(this.selectedNode)) {
                     this.selectedNode = null;
                  }

                  this.multiSel.remove(n.id);
                  return true;
               } else {
                  int wi = this.wireAt(sx, sy);
                  if (wi >= 0) {
                     this.pushUndo();
                     this.graph.wires.remove(wi);
                     if (this.selectedWire == wi) {
                        this.selectedWire = -1;
                     }

                     return true;
                  } else {
                     this.openCtx(sx, sy);
                     return true;
                  }
               }
            } else if (btn == 0) {
               String[] port = this.portAt(wx, wy, false);
               if (port == null) {
                  port = this.portAt(wx, wy, true);
               }

               if (port != null) {
                  this.connectSnapshot = this.snapshot();
                  this.connectFrom = port;
                  this.connectFromInput = this.portAt(wx, wy, false) == null;
                  return true;
               } else {
                  int wi = this.wireAt(sx, sy);
                  if (wi >= 0) {
                     this.selectedWire = wi;
                     this.selectedNode = null;
                     return true;
                  } else {
                     NodeGraph.GNode n = this.nodeAt(wx, wy);
                     if (n != null && (evt.comp_4797() & 2) != 0) {
                        if (!this.multiSel.remove(n.id)) {
                           this.multiSel.add(n.id);
                        }

                        this.selectedNode = n.id;
                        this.selectedWire = -1;
                        return true;
                     } else if (n == null) {
                        this.panning = true;
                        this.lastMX = sx;
                        this.lastMY = sy;
                        this.selectedNode = null;
                        this.selectedWire = -1;
                        this.multiSel.clear();
                        return true;
                     } else {
                        NodeType t = NodeTypes.byId(n.type);
                        if (t != null) {
                           int rows = Math.max(t.inputs.size(), t.outputs.size());

                           for(int i = 0; i < t.params.size(); ++i) {
                              double py = (double)(n.y + 14 + rows * 12 + i * 12);
                              if (wy >= py && wy < py + (double)12.0F) {
                                 this.openParamEditor(n, (NodeType.Param)t.params.get(i));
                                 return true;
                              }
                           }
                        }

                        this.selectedNode = n.id;
                        this.selectedWire = -1;
                        if (!this.multiSel.contains(n.id)) {
                           this.multiSel.clear();
                        }

                        this.dragNode = n.id;
                        this.dragSnapshot = this.snapshot();
                        this.dragMoved = false;
                        this.grabDX = wx - (double)n.x;
                        this.grabDY = wy - (double)n.y;
                        this.graph.nodes.remove(n);
                        this.graph.nodes.add(n);
                        return true;
                     }
                  }
               }
            } else {
               return super.method_25402(evt, doubleClick);
            }
         } else {
            return super.method_25402(evt, doubleClick);
         }
      } else if (this.handleCtxClick(sx, sy)) {
         return true;
      } else {
         this.closeCtx();
         return true;
      }
   }

   public boolean method_25406(class_11909 evt) {
      double sx = evt.comp_4798();
      double sy = evt.comp_4799();
      int btn = evt.method_74245();
      this.dragNode = null;
      this.panning = false;
      if (this.dragSnapshot != null && this.dragMoved) {
         this.undoStack.push(this.dragSnapshot);
         this.trimUndo();
         this.dirty = true;
      }

      this.dragSnapshot = null;
      this.dragMoved = false;
      if (this.connectFrom != null && btn == 0) {
         double wx = this.toWorldX(sx);
         double wy = this.toWorldY(sy);
         String[] target = this.portAt(wx, wy, !this.connectFromInput);
         if (target != null && !target[0].equals(this.connectFrom[0])) {
            String fromNode = this.connectFrom[0];
            String fromPort = this.connectFrom[1];
            String toNode = target[0];
            String toPort = target[1];
            if (this.connectFromInput) {
               toNode = this.connectFrom[0];
               toPort = this.connectFrom[1];
               fromNode = target[0];
               fromPort = target[1];
            }

            NodeType ft = NodeTypes.byId(this.graph.nodeById(fromNode).type);
            NodeType tt = NodeTypes.byId(this.graph.nodeById(toNode).type);
            if (ft != null && tt != null && ft.outputById(fromPort).type == tt.inputById(toPort).type) {
               if (this.connectSnapshot != null) {
                  this.undoStack.push(this.connectSnapshot);
                  this.trimUndo();
                  this.connectSnapshot = null;
                  this.dirty = true;
               }

               NodeGraph.GWire w = new NodeGraph.GWire();
               w.fromNode = fromNode;
               w.fromPort = fromPort;
               w.toNode = toNode;
               w.toPort = toPort;
               this.graph.wires.add(w);
            }
         }

         this.connectFrom = null;
         return true;
      } else {
         return super.method_25406(evt);
      }
   }

   public boolean method_25403(class_11909 evt, double dx, double dy) {
      double sx = evt.comp_4798();
      double sy = evt.comp_4799();
      if (this.dragNode == null) {
         if (this.panning) {
            this.panX = (float)((double)this.panX + dx);
            this.panY = (float)((double)this.panY + dy);
            return true;
         } else {
            return super.method_25403(evt, dx, dy);
         }
      } else {
         NodeGraph.GNode n = this.graph.nodeById(this.dragNode);
         if (n != null) {
            int nx = n.x + (int)Math.round(dx / (double)this.zoom);
            int ny = n.y + (int)Math.round(dy / (double)this.zoom);
            int mdx = nx - n.x;
            int mdy = ny - n.y;
            if (n.group != null && !n.group.isEmpty() && (mdx != 0 || mdy != 0)) {
               for(NodeGraph.GNode o : this.graph.nodes) {
                  if (o != n && n.group.equals(o.group)) {
                     o.x += mdx;
                     o.y += mdy;
                  }
               }
            }

            n.x = nx;
            n.y = ny;
            this.dragMoved = true;
         }

         return true;
      }
   }

   public boolean method_25401(double sx, double sy, double scrollX, double scrollY) {
      float old = this.zoom;
      this.zoom = (float)Math.max(0.4, Math.min((double)2.5F, (double)this.zoom * (scrollY > (double)0.0F ? 1.15 : 0.8695652173913044)));
      this.panX = (float)(sx - (sx - (double)this.panX) * (double)(this.zoom / old));
      this.panY = (float)(sy - (sy - (double)this.panY) * (double)(this.zoom / old));
      return true;
   }

   public boolean method_25404(class_11908 evt) {
      int key = evt.comp_4795();
      if (key == 259 || key == 261) {
         if (this.selectedWire >= 0 && this.selectedWire < this.graph.wires.size()) {
            this.pushUndo();
            this.graph.wires.remove(this.selectedWire);
            this.selectedWire = -1;
            return true;
         }

         if (this.selectedNode != null) {
            this.pushUndo();
            this.graph.removeNode(this.selectedNode);
            this.multiSel.remove(this.selectedNode);
            this.selectedNode = null;
            return true;
         }
      }

      if (key == 90 && (evt.comp_4797() & 2) != 0) {
         this.undo();
         return true;
      } else if (key == 256) {
         if (this.isCtxOpen()) {
            this.closeCtx();
            return true;
         } else {
            this.tryClose();
            return true;
         }
      } else {
         return super.method_25404(evt);
      }
   }

   private void tryClose() {
      if (!this.dirty) {
         this.method_25419();
      } else {
         class_310.method_1551().method_1507(new SaveConfirmScreen(this, () -> {
            this.save();
            class_310.method_1551().method_1507(this.parent);
         }, () -> class_310.method_1551().method_1507(this.parent)));
      }
   }

   public void method_25419() {
      class_310.method_1551().method_1507(this.parent);
   }

   private String snapshot() {
      return this.graph.toJson();
   }

   private void pushUndo() {
      this.dirty = true;
      this.undoStack.push(this.snapshot());
      this.trimUndo();
   }

   private void trimUndo() {
      while(this.undoStack.size() > 50) {
         this.undoStack.removeLast();
      }

   }

   private void undo() {
      String snap = (String)this.undoStack.poll();
      if (snap == null) {
         this.status("Отменять нечего", true);
      } else {
         try {
            this.graph = NodeGraph.fromJson(snap);
            this.selectedNode = null;
            this.multiSel.clear();
            this.selectedWire = -1;
            this.dragNode = null;
            this.connectFrom = null;
            this.status("Отменено (осталось шагов: " + this.undoStack.size() + ")", false);
         } catch (Throwable t) {
            this.status("Не удалось отменить: " + t.getMessage(), true);
         }

      }
   }

   private List<NodeGraph.GNode> targets() {
      List<NodeGraph.GNode> out = new ArrayList();

      for(String id : this.multiSel) {
         NodeGraph.GNode n = this.graph.nodeById(id);
         if (n != null) {
            out.add(n);
         }
      }

      if (this.selectedNode != null) {
         NodeGraph.GNode n = this.graph.nodeById(this.selectedNode);
         if (n != null && !out.contains(n)) {
            out.add(n);
         }
      }

      return out;
   }

   private void groupSelected() {
      List<NodeGraph.GNode> t = this.targets();
      if (t.isEmpty()) {
         this.status("Сначала выдели ноды: клик по ноде, Ctrl+клик — ещё одна", true);
      } else {
         this.pushUndo();
         int var10002 = this.groupCounter++;
         String g = "g" + var10002;

         for(NodeGraph.GNode n : t) {
            n.group = g;
         }

         this.status("В группе " + g + ": " + t.size() + " нод(ы). Тяни любую — двигаются вместе", false);
      }
   }

   private void ungroupSelected() {
      List<NodeGraph.GNode> t = this.targets();
      if (t.isEmpty()) {
         this.status("Нет выделенных нод", true);
      } else {
         this.pushUndo();

         for(NodeGraph.GNode n : t) {
            n.group = null;
         }

         this.status("Группа расформирована (" + t.size() + " нод)", false);
      }
   }

   private static int groupColor(String g) {
      int h = g == null ? 0 : g.hashCode();
      int r = 90 + (h & 63);
      int gr = 90 + (h >> 6 & 63);
      int b = 90 + (h >> 12 & 63);
      return -16777216 | r << 16 | gr << 8 | b;
   }

   private NodeGraph.GNode nodeAt(double wx, double wy) {
      for(int i = this.graph.nodes.size() - 1; i >= 0; --i) {
         NodeGraph.GNode n = (NodeGraph.GNode)this.graph.nodes.get(i);
         if (wx >= (double)n.x && wx <= (double)(n.x + 150) && wy >= (double)n.y && wy <= (double)(n.y + this.nodeH(n))) {
            return n;
         }
      }

      return null;
   }

   private String[] portAt(double wx, double wy, boolean input) {
      double r = (double)(3.0F + 3.0F / this.zoom);

      for(NodeGraph.GNode n : this.graph.nodes) {
         NodeType t = NodeTypes.byId(n.type);
         if (t != null) {
            for(NodeType.Port p : input ? t.inputs : t.outputs) {
               double[] pos = this.portPos(n, p.id, input);
               if (Math.abs(wx - pos[0]) <= r && Math.abs(wy - pos[1]) <= r) {
                  return new String[]{n.id, p.id};
               }
            }
         }
      }

      return null;
   }

   private int wireAt(double sx, double sy) {
      for(int i = 0; i < this.graph.wires.size(); ++i) {
         NodeGraph.GWire w = (NodeGraph.GWire)this.graph.wires.get(i);
         NodeGraph.GNode fn = this.graph.nodeById(w.fromNode);
         NodeGraph.GNode tn = this.graph.nodeById(w.toNode);
         if (fn != null && tn != null) {
            double[] a = this.portPos(fn, w.fromPort, false);
            double[] b = this.portPos(tn, w.toPort, true);
            double[] s0 = new double[]{a[0] * (double)this.zoom + (double)this.panX, a[1] * (double)this.zoom + (double)this.panY};
            double[] s1 = new double[]{b[0] * (double)this.zoom + (double)this.panX, b[1] * (double)this.zoom + (double)this.panY};

            for(double tt = (double)0.0F; tt <= (double)1.0F; tt += 0.05) {
               double[] p = bezier(s0, s1, tt);
               double d = Math.hypot(p[0] - sx, p[1] - sy);
               if (d < (double)6.0F) {
                  return i;
               }
            }
         }
      }

      return -1;
   }

   private static double[] bezier(double[] a, double[] b, double t) {
      double cx = Math.max((double)40.0F, Math.abs(b[0] - a[0]) * (double)0.5F);
      double x0 = a[0] + cx;
      double x1 = b[0] - cx;
      double mt = (double)1.0F - t;
      double x = mt * mt * mt * a[0] + (double)3.0F * mt * mt * t * x0 + (double)3.0F * mt * t * t * x1 + t * t * t * b[0];
      double y = mt * mt * mt * a[1] + (double)3.0F * mt * mt * t * a[1] + (double)3.0F * mt * t * t * b[1] + t * t * t * b[1];
      return new double[]{x, y};
   }

   private boolean isCtxOpen() {
      return this.ctxOpen;
   }

   private void openCtx(double sx, double sy) {
      this.ctxOpen = true;
      this.ctxCategory = null;
      this.ctxWorldX = this.toWorldX(sx);
      this.ctxWorldY = this.toWorldY(sy);
      int maxEntries = NodeTypes.CATEGORIES.size();

      for(String c : NodeTypes.CATEGORIES) {
         maxEntries = Math.max(maxEntries, NodeTypes.byCategory(c).size());
      }

      int h = maxEntries * 14 + 22;
      int w = 170;
      this.ctxX = Math.max((double)0.0F, Math.min(sx, (double)(this.field_22789 - w)));
      if (sy + (double)h > (double)(this.field_22790 - 44)) {
         this.ctxY = Math.max((double)32.0F, sy - (double)h);
      } else {
         this.ctxY = Math.max((double)32.0F, sy);
      }

   }

   private void closeCtx() {
      this.ctxOpen = false;
      this.ctxCategory = null;
   }

   private List<String> ctxEntries() {
      List<String> e = new ArrayList();
      if (this.ctxCategory == null) {
         e.addAll(NodeTypes.CATEGORIES);
      } else {
         for(NodeType t : NodeTypes.byCategory(this.ctxCategory)) {
            e.add(t.title);
         }
      }

      return e;
   }

   private boolean handleCtxClick(double sx, double sy) {
      List<String> entries = this.ctxEntries();
      int x = (int)this.ctxX;
      int y = (int)this.ctxY;
      int w = 170;
      int idx = ((int)sy - (y + 16)) / 14;
      if (!(sx < (double)x) && !(sx > (double)(x + w)) && idx >= 0 && idx < entries.size()) {
         String picked = (String)entries.get(idx);
         if (this.ctxCategory == null) {
            this.ctxCategory = picked;
         } else {
            for(NodeType t : NodeTypes.byCategory(this.ctxCategory)) {
               if (t.title.equals(picked)) {
                  this.pushUndo();
                  NodeGraph.GNode n = new NodeGraph.GNode();
                  n.id = this.graph.nextId();
                  n.type = t.id;
                  n.x = (int)Math.round(this.ctxWorldX - (double)75.0F);
                  n.y = Math.max(30, (int)Math.round(this.ctxWorldY - (double)14.0F));

                  for(NodeType.Param p : t.params) {
                     String v = !p.defText.isEmpty() ? p.defText : (p.kind == NodeType.PKind.NUMBER ? String.valueOf(p.def) : (!p.options.isEmpty() ? (String)p.options.get(0) : ""));
                     n.params.put(p.id, v);
                  }

                  this.graph.nodes.add(n);
                  break;
               }
            }

            this.closeCtx();
         }

         return true;
      } else {
         return false;
      }
   }

   private void openParamEditor(NodeGraph.GNode n, NodeType.Param p) {
      String cur = (String)n.params.getOrDefault(p.id, "");
      class_310 mc = class_310.method_1551();
      switch (p.kind) {
         case DROPDOWN -> mc.method_1507(NodeParamScreen.dropdown(this, p.label, p.options, p.optLabels, cur, (v) -> {
   this.pushUndo();
   n.params.put(p.id, v);
}));
         case NUMBER -> mc.method_1507(NodeParamScreen.number(this, p.label, parseOr(cur, p.def), (v) -> {
   this.pushUndo();
   n.params.put(p.id, v);
}));
         case TEXT -> mc.method_1507(NodeParamScreen.text(this, p.label, cur, (v) -> {
   this.pushUndo();
   n.params.put(p.id, v);
}));
         case ANIM -> mc.method_1507(NodeParamScreen.anims(this, cur, (v) -> {
   this.pushUndo();
   n.params.put(p.id, v);
}));
         case LAYER -> mc.method_1507(NodeParamScreen.layers(this, cur, (v) -> {
   this.pushUndo();
   n.params.put(p.id, v);
}));
         case ITEM -> mc.method_1507(NodeParamScreen.items(this, cur, (v) -> {
   this.pushUndo();
   n.params.put(p.id, v);
}));
         case EFFECT -> mc.method_1507(NodeParamScreen.effects(this, cur, (v) -> {
   this.pushUndo();
   n.params.put(p.id, v);
}));
         case ENTITY -> mc.method_1507(NodeParamScreen.entities(this, cur, (v) -> {
   this.pushUndo();
   n.params.put(p.id, v);
}));
         case BLOCK -> mc.method_1507(NodeParamScreen.blocks(this, cur, (v) -> {
   this.pushUndo();
   n.params.put(p.id, v);
}));
      }

   }

   private static double parseOr(String s, double def) {
      try {
         return Double.parseDouble(s);
      } catch (Throwable var4) {
         return def;
      }
   }

   public void method_25394(class_332 gg, int mouseX, int mouseY, float partialTick) {
      GuiStyle.background(gg, this.field_22789, this.field_22790);
      gg.method_44379(0, 32, this.field_22789, this.field_22790 - 44);
      this.renderGrid(gg);
      this.renderWires(gg, mouseX, mouseY);

      for(NodeGraph.GNode n : this.graph.nodes) {
         this.renderNode(gg, n, mouseX, mouseY);
      }

      if (this.ctxOpen) {
         this.renderCtx(gg);
      }

      gg.method_44380();
      class_327 var10001 = this.field_22793;
      int var10002 = this.field_22789;
      String var10003 = "Граф: " + this.graph.name;
      int var10004 = this.graph.nodes.size();
      GuiStyle.header(gg, var10001, var10002, var10003, var10004 + " нод · " + this.graph.wires.size() + " проводов");
      super.method_25394(gg, mouseX, mouseY, partialTick);
      gg.method_25303(this.field_22793, "ПКМ — меню нод · Ctrl+клик — выделить · В группу — двигать вместе · Del · Ctrl+Z · колесо — зум", 6, this.field_22790 - 38, -6643536);
      if (System.currentTimeMillis() < this.statusUntil) {
         gg.method_25303(this.field_22793, this.status, 160, this.field_22790 - 38, -1512720);
      }

   }

   private void renderGrid(class_332 gg) {
      int step = (int)(32.0F * this.zoom);
      if (step < 8) {
         step = 8;
      }

      int offX = (int)(this.panX % (float)step);
      int offY = (int)(this.panY % (float)step);

      for(int x = offX; x < this.field_22789; x += step) {
         gg.method_25294(x, 32, x + 1, this.field_22790 - 44, -14473938);
      }

      for(int y = Math.max(32, offY); y < this.field_22790 - 44; y += step) {
         gg.method_25294(0, y, this.field_22789, y + 1, -14473938);
      }

   }

   private void renderWires(class_332 gg, int mouseX, int mouseY) {
      for(int i = 0; i < this.graph.wires.size(); ++i) {
         NodeGraph.GWire w = (NodeGraph.GWire)this.graph.wires.get(i);
         NodeGraph.GNode fn = this.graph.nodeById(w.fromNode);
         NodeGraph.GNode tn = this.graph.nodeById(w.toNode);
         if (fn != null && tn != null) {
            double[] a = this.portPos(fn, w.fromPort, false);
            double[] b = this.portPos(tn, w.toPort, true);
            int color = this.wireColor(fn, w.fromPort, this.wireSlot(i));
            if (i == this.selectedWire) {
               color = -1;
            }

            this.drawBezier(gg, a, b, color);
         }
      }

      if (this.connectFrom != null) {
         NodeGraph.GNode fn = this.graph.nodeById(this.connectFrom[0]);
         if (fn != null) {
            double[] a = this.portPos(fn, this.connectFrom[1], this.connectFromInput);
            double[] b = new double[]{this.toWorldX((double)mouseX), this.toWorldY((double)mouseY)};
            NodeType t0 = NodeTypes.byId(fn.type);
            NodeType.Port p0 = t0 == null ? null : (this.connectFromInput ? t0.inputById(this.connectFrom[1]) : t0.outputById(this.connectFrom[1]));
            int[] var10000;
            if (p0 == null) {
               var10000 = BOOL_PAL;
            } else {
               switch (p0.type) {
                  case BOOL -> var10000 = BOOL_PAL;
                  case NUMBER -> var10000 = NUM_PAL;
                  default -> var10000 = STR_PAL;
               }
            }

            int[] pal = var10000;
            int slot = 0;

            for(NodeGraph.GWire w0 : this.graph.wires) {
               if (w0.fromNode.equals(this.connectFrom[0]) && w0.fromPort.equals(this.connectFrom[1])) {
                  ++slot;
               }
            }

            int color = pal[slot % pal.length];
            this.drawBezier(gg, a, b, color);
         }
      }

   }

   private int wireSlot(int wireIdx) {
      NodeGraph.GWire w = (NodeGraph.GWire)this.graph.wires.get(wireIdx);
      int slot = 0;

      for(int i = 0; i < wireIdx; ++i) {
         NodeGraph.GWire o = (NodeGraph.GWire)this.graph.wires.get(i);
         if (o.fromNode.equals(w.fromNode) && o.fromPort.equals(w.fromPort)) {
            ++slot;
         }
      }

      return slot;
   }

   private void drawBezier(class_332 gg, double[] aWorld, double[] bWorld, int color) {
      double[] a = new double[]{aWorld[0] * (double)this.zoom + (double)this.panX, aWorld[1] * (double)this.zoom + (double)this.panY};
      double[] b = new double[]{bWorld[0] * (double)this.zoom + (double)this.panX, bWorld[1] * (double)this.zoom + (double)this.panY};
      double[] prev = a;

      for(double t = 0.1; t <= 1.001; t += 0.1) {
         double[] p = bezier(a, b, Math.min(t, (double)1.0F));
         drawLine(gg, prev[0], prev[1], p[0], p[1], color);
         prev = p;
      }

   }

   private static void drawLine(class_332 gg, double x0, double y0, double x1, double y1, int color) {
      double steps = Math.max((double)1.0F, Math.hypot(x1 - x0, y1 - y0) / (double)2.0F);

      for(int i = 0; (double)i <= steps; ++i) {
         double t = (double)i / steps;
         int x = (int)Math.round(x0 + (x1 - x0) * t);
         int y = (int)Math.round(y0 + (y1 - y0) * t);
         gg.method_25294(x, y, x + 2, y + 2, color);
      }

   }

   private void renderNode(class_332 gg, NodeGraph.GNode n, int mouseX, int mouseY) {
      NodeType t = NodeTypes.byId(n.type);
      if (t != null) {
         int h = this.nodeH(n);
         boolean sel = n.id.equals(this.selectedNode);
         gg.method_51448().pushMatrix();
         gg.method_51448().translate((float)n.x * this.zoom + this.panX, (float)n.y * this.zoom + this.panY);
         gg.method_51448().scale(this.zoom, this.zoom);
         GuiStyle.panel(gg, 0, 0, 150, h);
         if (sel) {
            gg.method_25294(-1, -1, 151, 0, -10764098);
            gg.method_25294(-1, h, 151, h + 1, -10764098);
            gg.method_25294(-1, 0, 0, h, -10764098);
            gg.method_25294(150, 0, 151, h, -10764098);
         } else if (n.group != null && !n.group.isEmpty() || this.multiSel.contains(n.id)) {
            int c = this.multiSel.contains(n.id) ? -10764098 : groupColor(n.group);
            gg.method_25294(-1, -1, 151, 0, c);
            gg.method_25294(-1, h, 151, h + 1, c);
            gg.method_25294(-1, 0, 0, h, c);
            gg.method_25294(150, 0, 151, h, c);
         }

         gg.method_25294(0, 0, 150, 14, categoryColor(t.category));
         String title = t.title;
         if (this.field_22793.method_1727(title) > 142) {
            String var10000 = this.field_22793.method_27523(title, 136);
            title = var10000 + "…";
         }

         gg.method_25303(this.field_22793, title, 4, 3, -1512720);
         int rows = Math.max(t.inputs.size(), t.outputs.size());

         for(int i = 0; i < t.inputs.size(); ++i) {
            NodeType.Port p = (NodeType.Port)t.inputs.get(i);
            int py = 14 + i * 12 + 6;
            gg.method_25294(-3, py - 3, 3, py + 3, portColor(p.type));
            gg.method_25303(this.field_22793, p.label, 7, py - 3, -6643536);
         }

         for(int i = 0; i < t.outputs.size(); ++i) {
            NodeType.Port p = (NodeType.Port)t.outputs.get(i);
            int py = 14 + i * 12 + 6;
            gg.method_25294(147, py - 3, 153, py + 3, portColor(p.type));
            int lw = this.field_22793.method_1727(p.label);
            gg.method_25303(this.field_22793, p.label, 143 - lw, py - 3, -6643536);
         }

         for(int i = 0; i < t.params.size(); ++i) {
            NodeType.Param p = (NodeType.Param)t.params.get(i);
            int py = 14 + rows * 12 + i * 12;
            String v = (String)n.params.getOrDefault(p.id, "");
            String val = v.isEmpty() ? "—" : this.shortVal(p, v);

            String lab;
            for(lab = p.label; !lab.isEmpty() && this.field_22793.method_1727(lab + ": " + val) > 140; lab = lab.substring(0, lab.length() - 1)) {
            }

            String label = lab + ": " + val;
            gg.method_25294(4, py, 146, py + 12 - 1, -14013126);
            gg.method_25303(this.field_22793, label, 7, py + 2, -10764098);
         }

         gg.method_51448().popMatrix();
      }
   }

   private String shortVal(NodeType.Param p, String v) {
      if (p.kind == NodeType.PKind.ITEM && v.startsWith("minecraft:")) {
         return v.substring("minecraft:".length());
      } else {
         return p.kind == NodeType.PKind.NUMBER && v.endsWith(".0") ? v.substring(0, v.length() - 2) : v;
      }
   }

   private static int categoryColor(String cat) {
      int var10000;
      switch (cat) {
         case "Входы" -> var10000 = -14400164;
         case "Логика" -> var10000 = -12831912;
         case "Время" -> var10000 = -11912662;
         case "Константы" -> var10000 = -13744586;
         default -> var10000 = -11784646;
      }

      return var10000;
   }

   private void renderCtx(class_332 gg) {
      List<String> entries = this.ctxEntries();
      int x = (int)this.ctxX;
      int y = (int)this.ctxY;
      int w = 170;
      int h = entries.size() * 14 + 22;
      GuiStyle.panel(gg, x, y, w, h);
      gg.method_25303(this.field_22793, this.ctxCategory == null ? "Добавить ноду:" : this.ctxCategory, x + 6, y + 4, -10764098);

      for(int i = 0; i < entries.size(); ++i) {
         int ry = y + 16 + i * 14;
         gg.method_25303(this.field_22793, (String)entries.get(i), x + 10, ry, -1512720);
      }

   }
}
