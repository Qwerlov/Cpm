package com.oleo.cpmscripts.nodes;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class GraphCompiler {
   private GraphCompiler() {
   }

   private static NodeGraph.GWire wireInto(NodeGraph g, String nodeId, String port) {
      for(NodeGraph.GWire w : g.wires) {
         if (w.toNode.equals(nodeId) && w.toPort.equals(port)) {
            return w;
         }
      }

      return null;
   }

   private static String luaStr(String s) {
      if (s == null) {
         return "\"\"";
      } else {
         StringBuilder sb = new StringBuilder("\"");

         for(char c : s.toCharArray()) {
            switch (c) {
               case '\n':
                  sb.append("\\n");
                  break;
               case '\r':
                  sb.append("\\r");
                  break;
               case '"':
                  sb.append("\\\"");
                  break;
               case '\\':
                  sb.append("\\\\");
                  break;
               default:
                  sb.append(c);
            }
         }

         return sb.append('"').toString();
      }
   }

   private static String normItem(String s) {
      if (s == null) {
         return "minecraft:air";
      } else {
         s = s.trim().toLowerCase(Locale.ROOT);
         if (s.isEmpty()) {
            return "minecraft:air";
         } else {
            if (!s.contains(":")) {
               s = "minecraft:" + s;
            }

            return s;
         }
      }
   }

   private static String num(double v) {
      return v == Math.floor(v) && !Double.isInfinite(v) ? Long.toString((long)v) : Double.toString(v);
   }

   public static String compile(NodeGraph g) throws CompileException {
      for(NodeGraph.GNode n : g.nodes) {
         if (NodeTypes.byId(n.type) == null) {
            throw new CompileException("нод " + n.id + ": неизвестный тип '" + n.type + "'");
         }
      }

      Map<String, NodeType> types = new HashMap();

      for(NodeGraph.GNode n : g.nodes) {
         types.put(n.id, NodeTypes.byId(n.type));
      }

      for(NodeGraph.GWire w : g.wires) {
         NodeType ft = (NodeType)types.get(w.fromNode);
         NodeType tt = (NodeType)types.get(w.toNode);
         if (ft != null && tt != null) {
            NodeType.Port fp = ft.outputById(w.fromPort);
            NodeType.Port tp = tt.inputById(w.toPort);
            if (fp != null && tp != null) {
               if (fp.type != tp.type) {
                  throw new CompileException("провод " + w.fromNode + "." + w.fromPort + " -> " + w.toNode + "." + w.toPort + ": несовместимые типы");
               }
               continue;
            }

            throw new CompileException("провод " + w.fromNode + "." + w.fromPort + " -> " + w.toNode + "." + w.toPort + ": нет такого порта");
         }

         throw new CompileException("провод ссылается на несуществующую ноду");
      }

      Map<String, Integer> indeg = new LinkedHashMap();

      for(NodeGraph.GNode n : g.nodes) {
         indeg.put(n.id, 0);
      }

      Map<String, List<String>> downstream = new HashMap();

      for(NodeGraph.GWire w : g.wires) {
         indeg.merge(w.toNode, 1, Integer::sum);
         ((List)downstream.computeIfAbsent(w.fromNode, (k) -> new ArrayList())).add(w.toNode);
      }

      Deque<String> queue = new ArrayDeque();

      for(Map.Entry<String, Integer> e : indeg.entrySet()) {
         if ((Integer)e.getValue() == 0) {
            queue.add((String)e.getKey());
         }
      }

      List<String> order = new ArrayList();

      while(!queue.isEmpty()) {
         String id = (String)queue.poll();
         order.add(id);

         for(String dn : (List)downstream.getOrDefault(id, List.of())) {
            int d = (Integer)indeg.merge(dn, -1, Integer::sum);
            if (d == 0) {
               queue.add(dn);
            }
         }
      }

      if (order.size() < g.nodes.size()) {
         throw new CompileException("в графе есть цикл (замкнутые провода)");
      } else {
         StringBuilder sb = new StringBuilder();
         sb.append("-- cpmscripts: граф \"").append(g.name).append("\" — сгенерировано из nodes/").append(g.name).append(".json, правки сотрутся\n");
         sb.append("local st = {}\n\n");
         sb.append("function tick(dt)\n");

         for(String id : order) {
            NodeGraph.GNode n = g.nodeById(id);
            NodeType t = (NodeType)types.get(id);
            emit(sb, g, n, t);
         }

         sb.append("end\n");
         return sb.toString();
      }
   }

   private static String param(NodeGraph.GNode n, NodeType t, String pid) {
      String v = (String)n.params.get(pid);
      if (v == null || v.isEmpty()) {
         NodeType.Param p = t.paramById(pid);
         if (p != null) {
            return p.defText.isEmpty() ? Double.toString(p.def) : p.defText;
         }
      }

      return v == null ? "" : v;
   }

   private static double paramNum(NodeGraph.GNode n, NodeType t, String pid) {
      String v = (String)n.params.get(pid);
      if (v != null && !v.isEmpty()) {
         try {
            return Double.parseDouble(v);
         } catch (NumberFormatException var6) {
            NodeType.Param p = t.paramById(pid);
            return p == null ? (double)0.0F : p.def;
         }
      } else {
         NodeType.Param p = t.paramById(pid);
         return p == null ? (double)0.0F : p.def;
      }
   }

   private static String inExpr(NodeGraph g, Map<String, NodeType> types, NodeGraph.GNode n, String port, String def) {
      NodeGraph.GWire w = wireInto(g, n.id, port);
      return w == null ? def : "o_" + w.fromNode + "_" + w.fromPort;
   }

   private static void emit(StringBuilder sb, NodeGraph g, NodeGraph.GNode n, NodeType t) throws CompileException {
      Map<String, NodeType> types = new HashMap();

      for(NodeGraph.GNode x : g.nodes) {
         types.put(x.id, NodeTypes.byId(x.type));
      }

      String id = n.id;
      sb.append("  -- ").append(id).append(": ").append(t.title).append('\n');
      switch (t.id) {
         case "item_held":
            String item = luaStr(normItem(param(n, t, "item")));
            String var61;
            switch (param(n, t, "hand")) {
               case "off" -> var61 = "(player.offHand.id == " + item + ")";
               case "any" -> var61 = "(player.mainHand.id == " + item + " or player.offHand.id == " + item + ")";
               default -> var61 = "(player.mainHand.id == " + item + ")";
            }

            String cond = var61;
            sb.append("  local o_").append(id).append("_out = ").append(cond).append('\n');
            break;
         case "item_use":
            String item = luaStr(normItem(param(n, t, "item")));
            String cond = "(player.using and player.useItem.id == " + item + ") or (player.useKey and (player.mainHand.id == " + item + " or player.offHand.id == " + item + "))";
            if ("press".equals(param(n, t, "mode"))) {
               sb.append("  local cur = ").append(cond).append('\n');
               sb.append("  local o_").append(id).append("_out = cur and not st.e").append(id).append("; st.e").append(id).append(" = cur\n");
            } else {
               sb.append("  local o_").append(id).append("_out = ").append(cond).append('\n');
            }
            break;
         case "chat_in":
            sb.append("  local o_").append(id).append("_new = player.chatNew\n");
            sb.append("  local o_").append(id).append("_msg = player.chatLast\n");
            sb.append("  local o_").append(id).append("_num = nil\n");
            sb.append("  if o_").append(id).append("_msg ~= nil then o_").append(id).append("_num = tonumber(string.match(o_").append(id).append("_msg, \"%-?%d+%.?%d*\")) end\n");
            break;
         case "chat_match":
            String m = inExpr(g, types, n, "msg", "player.chatLast");
            String f = inExpr(g, types, n, "find", luaStr(param(n, t, "text")));
            boolean full = "full".equals(param(n, t, "mode"));
            sb.append("  local m_").append(id).append(" = ").append(m).append('\n');
            sb.append("  local f_").append(id).append(" = ").append(f).append('\n');
            sb.append("  local o_").append(id).append("_out = m_").append(id).append(" ~= nil and f_").append(id).append(" ~= nil and f_").append(id).append(" ~= \"\" and (");
            if (full) {
               sb.append("m_").append(id).append(" == f_").append(id);
            } else {
               sb.append("string.find(m_").append(id).append(", f_").append(id).append(", 1, true) ~= nil");
            }

            sb.append(")\n");
            break;
         case "equip":
            String slot = param(n, t, "slot");
            String item = luaStr(normItem(param(n, t, "item")));
            sb.append("  local o_").append(id).append("_out = player.armor.").append(slot).append(".id == ").append(item).append('\n');
            break;
         case "effect":
            String eff = luaStr(normItem(param(n, t, "effect")));
            sb.append("  local o_").append(id).append("_has = hasEffect(").append(eff).append(")\n");
            sb.append("  local o_").append(id).append("_amp = effectAmp(").append(eff).append(")\n");
            break;
         case "entities":
            String typeRaw = param(n, t, "type").trim().toLowerCase(Locale.ROOT);
            String type = luaStr(typeRaw.isEmpty() ? "" : normItem(typeRaw));
            String name = luaStr(param(n, t, "name").trim().toLowerCase(Locale.ROOT));
            String radius = num(paramNum(n, t, "radius"));
            sb.append("  local c_").append(id).append(" = countEntities(").append(type).append(", ").append(radius).append(", ").append(name).append(")\n");
            sb.append("  local o_").append(id).append("_count = c_").append(id).append('\n');
            sb.append("  local o_").append(id).append("_has = c_").append(id).append(" > 0\n");
            break;
         case "key":
            String kn = luaStr(param(n, t, "n"));
            String fn = "down".equals(param(n, t, "mode")) ? "down" : "pressed";
            sb.append("  local o_").append(id).append("_out = keys.").append(fn).append("(").append(kn).append(")\n");
            break;
         case "mouse":
            sb.append("  local ap_").append(id).append(" = player.attackKey\n");
            sb.append("  local o_").append(id).append("_attack = ap_").append(id).append('\n');
            sb.append("  local o_").append(id).append("_attackPress = ap_").append(id).append(" and not st.m").append(id).append("a\n");
            sb.append("  local o_").append(id).append("_attackRelease = not ap_").append(id).append(" and st.m").append(id).append("a\n");
            sb.append("  st.m").append(id).append("a = ap_").append(id).append('\n');
            sb.append("  local up_").append(id).append(" = player.useKey\n");
            sb.append("  local o_").append(id).append("_use = up_").append(id).append('\n');
            sb.append("  local o_").append(id).append("_usePress = up_").append(id).append(" and not st.m").append(id).append("u\n");
            sb.append("  local o_").append(id).append("_useRelease = not up_").append(id).append(" and st.m").append(id).append("u\n");
            sb.append("  st.m").append(id).append("u = up_").append(id).append('\n');
            break;
         case "hit":
            sb.append("  local cur = player.attackHit\n");
            sb.append("  local o_").append(id).append("_hit = cur\n");
            sb.append("  local o_").append(id).append("_press = cur and not st.h").append(id).append("; st.h").append(id).append(" = cur\n");
            break;
         case "block_use":
            String blockRaw = param(n, t, "block").trim().toLowerCase(Locale.ROOT);
            String block = luaStr(blockRaw.isEmpty() ? "" : normItem(blockRaw));
            sb.append("  local o_").append(id).append("_new = player.blockUseNew and player.blockUse ~= nil\n");
            sb.append("  local o_").append(id).append("_id = player.blockUse\n");
            sb.append("  local o_").append(id).append("_match = player.blockUseNew and player.blockUse ~= nil and (").append(block).append(" == \"\" or player.blockUse == ").append(block).append(")\n");
            break;
         case "pose":
            String var60;
            switch (param(n, t, "state")) {
               case "sneak" -> var60 = "player.sneaking";
               case "walking" -> var60 = "player.walking";
               case "sprint" -> var60 = "player.sprinting";
               case "swim" -> var60 = "player.swimming";
               case "crawl" -> var60 = "player.crawling";
               case "sleep" -> var60 = "(player.pose == \"SLEEPING\")";
               case "elytra" -> var60 = "player.fallFlying";
               case "ride" -> var60 = "player.riding";
               case "falling" -> var60 = "(not player.onGround and player.velWorld.y < -0.05)";
               case "onGround" -> var60 = "player.onGround";
               case "inWater" -> var60 = "player.inWater";
               case "onFire" -> var60 = "player.onFire";
               default -> var60 = "false";
            }

            String cond = var60;
            sb.append("  local o_").append(id).append("_out = ").append(cond).append('\n');
            break;
         case "vital":
            String stat = param(n, t, "stat");
            String cmp = cmpLua(param(n, t, "cmp"));
            sb.append("  local o_").append(id).append("_out = (player.").append(stat).append(" ").append(cmp).append(" ").append(num(paramNum(n, t, "value"))).append(")\n");
            break;
         case "move":
            sb.append("  local o_").append(id).append("_out = (math.sqrt(player.vel.x * player.vel.x").append(" + player.vel.z * player.vel.z) >= ").append(num(paramNum(n, t, "speed"))).append(")\n");
            break;
         case "and":
         case "or":
         case "xor":
         case "nor":
         case "nand":
            String a = inExpr(g, types, n, "a", "false");
            String b = inExpr(g, types, n, "b", "false");
            String var10000;
            switch (t.id) {
               case "and" -> var10000 = "(" + a + " and " + b + ")";
               case "or" -> var10000 = "(" + a + " or " + b + ")";
               case "xor" -> var10000 = "(" + a + " ~= " + b + ")";
               case "nor" -> var10000 = "(not (" + a + " or " + b + "))";
               default -> var10000 = "(not (" + a + " and " + b + "))";
            }

            String op = var10000;
            sb.append("  local o_").append(id).append("_out = ").append(op).append('\n');
            break;
         case "not":
            String i = inExpr(g, types, n, "in", "false");
            sb.append("  local o_").append(id).append("_out = not ").append(i).append('\n');
            break;
         case "edge":
            String i = inExpr(g, types, n, "in", "false");
            sb.append("  local cur = ").append(i).append('\n');
            sb.append("  local o_").append(id).append("_out = cur and not st.e").append(id).append("; st.e").append(id).append(" = cur\n");
            break;
         case "toggle":
            String i = inExpr(g, types, n, "in", "false");
            sb.append("  local cur = ").append(i).append('\n');
            sb.append("  if cur and not st.e").append(id).append(" then st.tg").append(id).append(" = not st.tg").append(id).append(" end\n");
            sb.append("  st.e").append(id).append(" = cur\n");
            sb.append("  local o_").append(id).append("_out = st.tg").append(id).append(" == true\n");
            break;
         case "latch":
            String s = inExpr(g, types, n, "set", "false");
            String r = inExpr(g, types, n, "reset", "false");
            sb.append("  if ").append(s).append(" then st.la").append(id).append(" = true").append(" elseif ").append(r).append(" then st.la").append(id).append(" = false end\n");
            sb.append("  local o_").append(id).append("_out = st.la").append(id).append(" == true\n");
            break;
         case "timer":
            String i = inExpr(g, types, n, "in", "false");
            sb.append("  local cur = ").append(i).append('\n');
            sb.append("  if cur and not st.e").append(id).append(" then st.tm").append(id).append(" = ").append(num(paramNum(n, t, "sec"))).append(" end\n");
            sb.append("  st.e").append(id).append(" = cur\n");
            sb.append("  local o_").append(id).append("_out = false\n");
            sb.append("  if st.tm").append(id).append(" then st.tm").append(id).append(" = st.tm").append(id).append(" - dt\n");
            sb.append("    if st.tm").append(id).append(" > 0 then o_").append(id).append("_out = true else st.tm").append(id).append(" = nil end\n");
            sb.append("  end\n");
            break;
         case "delay":
            String i = inExpr(g, types, n, "in", "false");
            sb.append("  local cur = ").append(i).append('\n');
            sb.append("  if cur and not st.e").append(id).append(" and not st.arm").append(id).append(" then st.arm").append(id).append(" = true; st.tm").append(id).append(" = ").append(num(paramNum(n, t, "sec"))).append(" end\n");
            sb.append("  if not cur then st.arm").append(id).append(" = nil end\n");
            sb.append("  st.e").append(id).append(" = cur\n");
            sb.append("  local o_").append(id).append("_out = false\n");
            sb.append("  if st.arm").append(id).append(" then st.tm").append(id).append(" = st.tm").append(id).append(" - dt\n");
            sb.append("    if st.tm").append(id).append(" <= 0 then st.arm").append(id).append(" = nil; if cur then o_").append(id).append("_out = true end end\n");
            sb.append("  end\n");
            break;
         case "cooldown":
            String i = inExpr(g, types, n, "in", "false");
            sb.append("  local cur = ").append(i).append('\n');
            sb.append("  local o_").append(id).append("_out = false\n");
            sb.append("  if cur and not st.e").append(id).append(" then\n");
            sb.append("    local now = player.time\n");
            sb.append("    if not st.cd").append(id).append(" or now - st.cd").append(id).append(" >= ").append(num(paramNum(n, t, "sec") * (double)20.0F)).append(" then\n");
            sb.append("      st.cd").append(id).append(" = now; o_").append(id).append("_out = true\n");
            sb.append("    end\n");
            sb.append("  end\n");
            sb.append("  st.e").append(id).append(" = cur\n");
            break;
         case "cycle":
            String i = inExpr(g, types, n, "in", "false");
            sb.append("  local o_").append(id).append("_out = false\n");
            sb.append("  if ").append(i).append(" then\n");
            sb.append("    st.cy").append(id).append(" = (st.cy").append(id).append(" or 0) + 1\n");
            sb.append("    if st.cy").append(id).append(" >= ").append(num(paramNum(n, t, "ticks"))).append(" then st.cy").append(id).append(" = 0; o_").append(id).append("_out = true end\n");
            sb.append("  else st.cy").append(id).append(" = 0\n");
            sb.append("  end\n");
            break;
         case "const_num":
            sb.append("  local o_").append(id).append("_out = ").append(num(paramNum(n, t, "value"))).append('\n');
            break;
         case "compare":
            String a = inExpr(g, types, n, "a", "0");
            String b = inExpr(g, types, n, "b", "0");
            sb.append("  local o_").append(id).append("_out = (").append(a).append(" ").append(cmpLua(param(n, t, "cmp"))).append(" ").append(b).append(")\n");
            break;
         case "play":
            String i = inExpr(g, types, n, "in", "false");
            String anim = luaStr(param(n, t, "anim"));
            if ("while".equals(param(n, t, "mode"))) {
               sb.append("  local cur = ").append(i).append('\n');
               sb.append("  if cur and not st.e").append(id).append(" then cpm.play(").append(anim).append(") end\n");
               sb.append("  if not cur and st.e").append(id).append(" then cpm.stop(").append(anim).append(") end\n");
               sb.append("  st.e").append(id).append(" = cur\n");
            } else {
               sb.append("  local cur = ").append(i).append('\n');
               sb.append("  if cur and not st.e").append(id).append(" then cpm.play(").append(anim).append(") end\n");
               sb.append("  st.e").append(id).append(" = cur\n");
            }
            break;
         case "stop":
            String i = inExpr(g, types, n, "in", "false");
            sb.append("  local cur = ").append(i).append('\n');
            sb.append("  if cur and not st.e").append(id).append(" then cpm.stop(").append(luaStr(param(n, t, "anim"))).append(") end\n");
            sb.append("  st.e").append(id).append(" = cur\n");
            break;
         case "set_layer":
            String i = inExpr(g, types, n, "in", "false");
            String v = inExpr(g, types, n, "value", num((double)128.0F));
            String layer = luaStr(param(n, t, "layer"));
            boolean once = "once".equals(param(n, t, "mode"));
            sb.append("  local cur = ").append(i).append('\n');
            if (once) {
               sb.append("  if cur and not st.e").append(id).append(" then cpm.set(").append(layer).append(", math.floor(").append(v).append(" + 0.5)) end\n");
            } else {
               sb.append("  if cur then cpm.set(").append(layer).append(", math.floor(").append(v).append(" + 0.5)) end\n");
            }

            if ("reset".equals(param(n, t, "release")) && !once) {
               sb.append("  if not cur and st.e").append(id).append(" then cpm.set(").append(layer).append(", 128) end\n");
            }

            sb.append("  st.e").append(id).append(" = cur\n");
            break;
         case "chat":
            String i = inExpr(g, types, n, "in", "false");
            boolean server = "server".equals(param(n, t, "target"));
            sb.append("  local cur = ").append(i).append('\n');
            sb.append("  if cur and not st.e").append(id).append(" then ").append(server ? "sendChat(" : "sendLocalChat(").append(luaStr(param(n, t, "text"))).append(") end\n");
            sb.append("  st.e").append(id).append(" = cur\n");
            break;
         default:
            throw new CompileException("нод " + id + ": тип '" + t.id + "' без генератора");
      }

   }

   private static String cmpLua(String cmp) {
      String var10000;
      switch (cmp) {
         case "<=" -> var10000 = "<=";
         case "==" -> var10000 = "==";
         case ">=" -> var10000 = ">=";
         case ">" -> var10000 = ">";
         default -> var10000 = "<";
      }

      return var10000;
   }

   public static final class CompileException extends Exception {
      public CompileException(String msg) {
         super(msg);
      }
   }
}
