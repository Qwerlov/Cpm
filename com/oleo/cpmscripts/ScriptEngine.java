package com.oleo.cpmscripts;

import com.oleo.cpmscripts.nodes.GraphCompiler;
import com.oleo.cpmscripts.nodes.NodeGraph;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_1291;
import net.minecraft.class_1293;
import net.minecraft.class_1297;
import net.minecraft.class_1304;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_265;
import net.minecraft.class_2680;
import net.minecraft.class_310;
import net.minecraft.class_3675;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_746;
import net.minecraft.class_7923;
import net.minecraft.class_239.class_240;
import net.minecraft.class_3959.class_242;
import net.minecraft.class_3959.class_3960;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.VarArgFunction;
import org.luaj.vm2.lib.jse.JsePlatform;

public final class ScriptEngine {
   private static Path scriptsDir;
   private static final List<Script> scripts = new ArrayList();
   private static final Set<String> disabled = new HashSet();
   private static final Map<String, String> loadErrors = new HashMap();
   private static long generation = 0L;
   private static volatile boolean enabled = true;
   private static volatile String chatLast;
   private static volatile boolean chatFresh;
   private static volatile int attackEntityHitTicks;
   private static volatile int attackBlockHitTicks;
   private static volatile String blockUseLast;
   private static volatile boolean blockUseFresh;
   private static final LuaTable playerTable = new LuaTable();
   private static final LuaTable velLocal = new LuaTable();
   private static final LuaTable velWorld = new LuaTable();
   private static final LuaTable viewVec = new LuaTable();
   private static float prevBodyYaw = 0.0F;
   private static boolean firstTick = true;

   private ScriptEngine() {
   }

   public static void init() {
      scriptsDir = FabricLoader.getInstance().getConfigDir().resolve("cpmscripts");

      try {
         Files.createDirectories(scriptsDir);
         Files.createDirectories(scriptsDir.resolve("lib"));
         Path help = scriptsDir.resolve("ИНСТРУКЦИЯ.txt");
         if (!Files.exists(help, new LinkOption[0])) {
            InputStream in = ScriptEngine.class.getResourceAsStream("/cpmscripts_help.txt");

            try {
               if (in != null) {
                  Files.copy(in, help, new CopyOption[0]);
               }
            } catch (Throwable var18) {
               if (in != null) {
                  try {
                     in.close();
                  } catch (Throwable var14) {
                     var18.addSuppressed(var14);
                  }
               }

               throw var18;
            }

            if (in != null) {
               in.close();
            }
         }

         boolean zipOk = false;
         InputStream in = ScriptEngine.class.getResourceAsStream("/cpmscripts_libs.zip");

         try {
            ZipInputStream zin = in == null ? null : new ZipInputStream(in);

            try {
               if (zin != null) {
                  zipOk = true;

                  ZipEntry e;
                  while((e = zin.getNextEntry()) != null) {
                     if (!e.isDirectory() && e.getName().endsWith(".lua")) {
                        Path dst = scriptsDir.resolve("lib").resolve(e.getName());
                        if (!Files.exists(dst, new LinkOption[0])) {
                           Files.copy(zin, dst, new CopyOption[0]);
                        }
                     }
                  }
               }
            } catch (Throwable var16) {
               if (zin != null) {
                  try {
                     zin.close();
                  } catch (Throwable var13) {
                     var16.addSuppressed(var13);
                  }
               }

               throw var16;
            }

            if (zin != null) {
               zin.close();
            }
         } catch (Throwable var17) {
            if (in != null) {
               try {
                  in.close();
               } catch (Throwable var12) {
                  var17.addSuppressed(var12);
               }
            }

            throw var17;
         }

         if (in != null) {
            in.close();
         }

         if (!zipOk) {
            String[] bundled = new String[]{"util.lua", "spring.lua", "noise.lua", "cloth.lua", "vec.lua", "easing.lua", "waves.lua", "fsm.lua", "rope.lua", "collision.lua"};

            for(String lib : bundled) {
               Path dst = scriptsDir.resolve("lib").resolve(lib);
               if (!Files.exists(dst, new LinkOption[0])) {
                  InputStream in = ScriptEngine.class.getResourceAsStream("/cpmscripts_lib/" + lib);

                  try {
                     if (in != null) {
                        Files.copy(in, dst, new CopyOption[0]);
                     }
                  } catch (Throwable var15) {
                     if (in != null) {
                        try {
                           in.close();
                        } catch (Throwable var11) {
                           var15.addSuppressed(var11);
                        }
                     }

                     throw var15;
                  }

                  if (in != null) {
                     in.close();
                  }
               }
            }
         }
      } catch (IOException e) {
         CpmScriptsClient.LOGGER.error("[CPM Scripts] Failed to create scripts dir", e);
      }

      loadDisabled();
      reload();
   }

   private static void loadDisabled() {
      disabled.clear();
      Path f = scriptsDir.resolve("disabled.txt");

      try {
         if (Files.exists(f, new LinkOption[0])) {
            disabled.addAll(Files.readAllLines(f, StandardCharsets.UTF_8));
         }
      } catch (IOException var2) {
      }

   }

   private static void saveDisabled() {
      try {
         Files.write(scriptsDir.resolve("disabled.txt"), new ArrayList(disabled), StandardCharsets.UTF_8);
      } catch (IOException var1) {
      }

   }

   private static String safeName(String name) {
      String n = (new File(name)).getName();
      return n.endsWith(".lua") ? n : n + ".lua";
   }

   public static Path getScriptsDir() {
      return scriptsDir;
   }

   public static synchronized List<String[]> scriptInfo() {
      List<String[]> out = new ArrayList();

      try {
         DirectoryStream<Path> ds = Files.newDirectoryStream(scriptsDir, "*.lua");

         try {
            for(Path f : ds) {
               String n = f.getFileName().toString();
               out.add(new String[]{n, disabled.contains(n) ? "0" : "1", (String)loadErrors.getOrDefault(n, "")});
            }
         } catch (Throwable var11) {
            if (ds != null) {
               try {
                  ds.close();
               } catch (Throwable var8) {
                  var11.addSuppressed(var8);
               }
            }

            throw var11;
         }

         if (ds != null) {
            ds.close();
         }
      } catch (IOException var12) {
      }

      try {
         DirectoryStream<Path> ds = Files.newDirectoryStream(scriptsDir.resolve("nodes"), "*.json");

         try {
            for(Path f : ds) {
               String fn = f.getFileName().toString();
               String scriptName = "nodes/" + fn;
               boolean off = disabled.contains(fn) || disabled.contains(scriptName);
               out.add(new String[]{scriptName, off ? "0" : "1", (String)loadErrors.getOrDefault(scriptName, "")});
            }
         } catch (Throwable var9) {
            if (ds != null) {
               try {
                  ds.close();
               } catch (Throwable var7) {
                  var9.addSuppressed(var7);
               }
            }

            throw var9;
         }

         if (ds != null) {
            ds.close();
         }
      } catch (IOException var10) {
      }

      out.sort(Comparator.comparing((a) -> a[0]));
      return out;
   }

   public static boolean isGraphName(String name) {
      return name.endsWith(".json") || name.startsWith("nodes/");
   }

   public static void setScriptEnabled(String name, boolean en) {
      if (!name.startsWith("nodes/")) {
         name = safeName(name);
      }

      if (en) {
         disabled.remove(name);
      } else {
         disabled.add(name);
      }

      saveDisabled();
      reload();
   }

   public static String readScript(String name) {
      try {
         return Files.readString(scriptsDir.resolve(safeName(name)), StandardCharsets.UTF_8);
      } catch (IOException var2) {
         return "";
      }
   }

   public static void saveScript(String name, String content) {
      try {
         Files.writeString(scriptsDir.resolve(safeName(name)), content, StandardCharsets.UTF_8);
      } catch (IOException e) {
         CpmScriptsClient.chat("[CPM Scripts] Save failed: " + e.getMessage());
      }

      reload();
   }

   public static void deleteScript(String name) {
      try {
         if (name.startsWith("nodes/")) {
            Files.deleteIfExists(scriptsDir.resolve(name));
         } else {
            Files.deleteIfExists(scriptsDir.resolve(safeName(name)));
         }
      } catch (IOException var2) {
      }

      reload();
   }

   public static String renameScript(String oldName, String newName) {
      if (newName == null) {
         return "пустое имя";
      } else {
         newName = newName.trim();
         if (newName.isEmpty()) {
            return "пустое имя";
         } else {
            boolean graph = oldName.startsWith("nodes/");
            if (graph) {
               if (!newName.endsWith(".json")) {
                  newName = newName + ".json";
               }
            } else {
               newName = safeName(newName);
               oldName = safeName(oldName);
            }

            String bare = graph && newName.startsWith("nodes/") ? newName.substring("nodes/".length()) : newName;
            if (bare.isEmpty()) {
               return "пустое имя";
            } else if (!bare.contains("/") && !bare.contains("\\") && !bare.contains("..") && !bare.contains(":")) {
               if (oldName.equals(newName)) {
                  return null;
               } else {
                  try {
                     Path src = scriptsDir.resolve(oldName);
                     Path dst = graph ? scriptsDir.resolve("nodes").resolve(bare) : scriptsDir.resolve(newName);
                     if (Files.exists(dst, new LinkOption[0])) {
                        return "такое имя уже занято";
                     } else {
                        Files.move(src, dst);
                        if (disabled.remove(oldName)) {
                           disabled.add(newName);
                           saveDisabled();
                        }

                        if (!graph && disabled.remove(safeName(oldName))) {
                           disabled.add(newName);
                           saveDisabled();
                        }

                        reload();
                        return null;
                     }
                  } catch (IOException e) {
                     return e.getMessage();
                  }
               }
            } else {
               return "недопустимые символы в имени";
            }
         }
      }
   }

   public static String addScriptFrom(Path src) {
      try {
         Path dst = scriptsDir.resolve(safeName(src.getFileName().toString()));
         Files.copy(src, dst, StandardCopyOption.REPLACE_EXISTING);
         reload();
         return null;
      } catch (IOException e) {
         return e.getMessage();
      }
   }

   public static synchronized void reload() {
      scripts.clear();
      loadErrors.clear();
      ++generation;
      int ok = 0;
      List<String> failed = new ArrayList();

      try {
         DirectoryStream<Path> ds = Files.newDirectoryStream(scriptsDir, "*.lua");

         try {
            for(Path f : ds) {
               String n = f.getFileName().toString();
               if (!disabled.contains(n)) {
                  try {
                     String code = Files.readString(f, StandardCharsets.UTF_8);
                     Globals g = makeGlobals();
                     g.load(new StringReader(code), n).call();
                     scripts.add(new Script(n, g));
                     ++ok;
                  } catch (Throwable t) {
                     loadErrors.put(n, errMsg(t));
                     failed.add(n + ": " + errMsg(t));
                  }
               }
            }
         } catch (Throwable var18) {
            if (ds != null) {
               try {
                  ds.close();
               } catch (Throwable var12) {
                  var18.addSuppressed(var12);
               }
            }

            throw var18;
         }

         if (ds != null) {
            ds.close();
         }
      } catch (IOException e) {
         CpmScriptsClient.chat("[CPM Scripts] Reload failed: " + e.getMessage());
         return;
      }

      int okGraphs = 0;
      Path nodesDir = scriptsDir.resolve("nodes");

      try {
         DirectoryStream<Path> ds = Files.newDirectoryStream(nodesDir, "*.json");

         try {
            for(Path f : ds) {
               String fileName = f.getFileName().toString();
               String scriptName = "nodes/" + fileName;
               if (!disabled.contains(fileName) && !disabled.contains(scriptName)) {
                  try {
                     NodeGraph graph = NodeGraph.load(f);
                     String lua = GraphCompiler.compile(graph);
                     Globals g = makeGlobals();
                     g.load(new StringReader(lua), scriptName).call();
                     scripts.add(new Script(scriptName, g));
                     ++okGraphs;
                  } catch (Throwable t) {
                     loadErrors.put(scriptName, errMsg(t));
                     failed.add(fileName + ": " + errMsg(t));
                  }
               }
            }
         } catch (Throwable var16) {
            if (ds != null) {
               try {
                  ds.close();
               } catch (Throwable var13) {
                  var16.addSuppressed(var13);
               }
            }

            throw var16;
         }

         if (ds != null) {
            ds.close();
         }
      } catch (IOException var17) {
      }

   }

   public static String runSnippet(String code) {
      try {
         Globals g = makeGlobals();
         fillPlayerData(class_310.method_1551());
         g.load(new StringReader(code), "snippet").call();
         return null;
      } catch (Throwable t) {
         return errMsg(t);
      }
   }

   private static void installLibSearcher(final Globals g) {
      LuaValue pkg = g.get("package");
      if (pkg.istable()) {
         LuaValue searchers = pkg.get("searchers");
         if (searchers.isnil()) {
            searchers = pkg.get("loaders");
         }

         if (searchers.istable()) {
            LuaValue loader = new OneArgFunction() {
               public LuaValue call(LuaValue modname) {
                  String var10000 = modname.tojstring();
                  String rel = var10000.replace('.', '/') + ".lua";
                  Path p = ScriptEngine.scriptsDir.resolve("lib").resolve(rel).normalize();
                  if (p.startsWith(ScriptEngine.scriptsDir.resolve("lib").normalize()) && Files.exists(p, new LinkOption[0])) {
                     try {
                        String code = Files.readString(p, StandardCharsets.UTF_8);
                        return g.load(new StringReader(code), "@" + String.valueOf(p.getFileName()));
                     } catch (IOException e) {
                        return LuaValue.valueOf("\n\tcan't read " + rel + ": " + e.getMessage());
                     }
                  } else {
                     return LuaValue.valueOf("\n\tno lib file '" + rel + "' in cpmscripts/lib");
                  }
               }
            };
            LuaTable tbl = searchers.checktable();
            if (tbl.get("cpmscripts_lib").isnil()) {
               for(int i = tbl.length() + 1; i > 2; --i) {
                  tbl.set(i, tbl.get(i - 1));
               }

               tbl.set(2, loader);
               tbl.set("cpmscripts_lib", LuaValue.TRUE);
            }

         }
      }
   }

   private static Globals makeGlobals() {
      Globals g = JsePlatform.standardGlobals();
      installLibSearcher(g);
      LuaValue pkg = g.get("package");
      if (!pkg.isnil()) {
         String base = scriptsDir.toAbsolutePath().normalize().toString().replace('\\', '/');
         String path = base + "/?.lua;" + base + "/lib/?.lua";
         pkg.get("path");
         pkg.set("path", LuaValue.valueOf(path + ";" + pkg.get("path").optjstring("")));
      }

      g.set("player", playerTable);
      LuaTable cpm = new LuaTable();
      cpm.set("set", new TwoArgFunction() {
         public LuaValue call(LuaValue name, LuaValue value) {
            boolean okRes = CpmBridge.setValue(name.checkjstring(), value.checkint());
            return LuaValue.valueOf(okRes);
         }
      });
      cpm.set("get", new OneArgFunction() {
         public LuaValue call(LuaValue name) {
            return LuaValue.valueOf(CpmBridge.getValue(name.checkjstring()));
         }
      });
      cpm.set("play", new OneArgFunction() {
         public LuaValue call(LuaValue name) {
            return LuaValue.valueOf(CpmBridge.play(name.checkjstring()));
         }
      });
      cpm.set("stop", new OneArgFunction() {
         public LuaValue call(LuaValue name) {
            return LuaValue.valueOf(CpmBridge.stop(name.checkjstring()));
         }
      });
      cpm.set("ready", new VarArgFunction() {
         public Varargs invoke(Varargs args) {
            return LuaValue.valueOf(CpmBridge.isReady());
         }
      });
      g.set("cpm", cpm);
      LuaTable world = new LuaTable();
      world.set("solid", new VarArgFunction() {
         public Varargs invoke(Varargs args) {
            double x = args.arg(1).checkdouble();
            double y = args.arg(2).checkdouble();
            double z = args.arg(3).checkdouble();
            return LuaValue.valueOf(ScriptEngine.isSolid(x, y, z));
         }
      });
      world.set("block", new VarArgFunction() {
         public Varargs invoke(Varargs args) {
            double x = args.arg(1).checkdouble();
            double y = args.arg(2).checkdouble();
            double z = args.arg(3).checkdouble();
            class_310 mc = class_310.method_1551();
            if (mc.field_1687 == null) {
               return LuaValue.NIL;
            } else {
               class_2338 pos = class_2338.method_49637(x, y, z);
               class_2680 st = mc.field_1687.method_8320(pos);
               LuaTable t = new LuaTable();
               t.set("id", LuaValue.valueOf(String.valueOf(class_7923.field_41175.method_10221(st.method_26204()))));
               t.set("solid", LuaValue.valueOf(!st.method_26215() && st.method_26216()));
               t.set("air", LuaValue.valueOf(st.method_26215()));
               t.set("liquid", LuaValue.valueOf(!st.method_26227().method_15769()));
               return t;
            }
         }
      });
      world.set("shape", new VarArgFunction() {
         public Varargs invoke(Varargs args) {
            double x = args.arg(1).checkdouble();
            double y = args.arg(2).checkdouble();
            double z = args.arg(3).checkdouble();
            LuaTable arr = new LuaTable();
            class_310 mc = class_310.method_1551();
            if (mc.field_1687 == null) {
               return arr;
            } else {
               class_2338 pos = class_2338.method_49637(x, y, z);
               class_2680 st = mc.field_1687.method_8320(pos);
               class_265 vs = st.method_26220(mc.field_1687, pos);
               int i = 1;

               for(class_238 b : vs.method_1090()) {
                  LuaTable t = new LuaTable();
                  t.set("x1", LuaValue.valueOf(b.field_1323 - (double)pos.method_10263()));
                  t.set("y1", LuaValue.valueOf(b.field_1322 - (double)pos.method_10264()));
                  t.set("z1", LuaValue.valueOf(b.field_1321 - (double)pos.method_10260()));
                  t.set("x2", LuaValue.valueOf(b.field_1320 - (double)pos.method_10263()));
                  t.set("y2", LuaValue.valueOf(b.field_1325 - (double)pos.method_10264()));
                  t.set("z2", LuaValue.valueOf(b.field_1324 - (double)pos.method_10260()));
                  arr.set(i++, t);
               }

               return arr;
            }
         }
      });
      world.set("raycast", new VarArgFunction() {
         public Varargs invoke(Varargs args) {
            LuaTable t = new LuaTable();
            class_310 mc = class_310.method_1551();
            if (mc.field_1687 != null && mc.field_1724 != null) {
               class_243 a = new class_243(args.arg(1).checkdouble(), args.arg(2).checkdouble(), args.arg(3).checkdouble());
               class_243 b = new class_243(args.arg(4).checkdouble(), args.arg(5).checkdouble(), args.arg(6).checkdouble());
               class_3965 hit = mc.field_1687.method_17742(new class_3959(a, b, class_3960.field_17558, class_242.field_1348, mc.field_1724));
               boolean isHit = hit.method_17783() != class_240.field_1333;
               t.set("hit", LuaValue.valueOf(isHit));
               class_243 loc = hit.method_17784();
               t.set("x", LuaValue.valueOf(loc.field_1352));
               t.set("y", LuaValue.valueOf(loc.field_1351));
               t.set("z", LuaValue.valueOf(loc.field_1350));
               if (isHit) {
                  t.set("side", LuaValue.valueOf(hit.method_17780().method_10151()));
                  class_2680 st = mc.field_1687.method_8320(hit.method_17777());
                  t.set("id", LuaValue.valueOf(String.valueOf(class_7923.field_41175.method_10221(st.method_26204()))));
               }

               return t;
            } else {
               t.set("hit", LuaValue.FALSE);
               return t;
            }
         }
      });
      world.set("entities", new VarArgFunction() {
         public Varargs invoke(Varargs args) {
            double r = args.arg(1).checkdouble();
            LuaTable arr = new LuaTable();
            class_310 mc = class_310.method_1551();
            if (mc.field_1687 != null && mc.field_1724 != null) {
               int i = 1;

               for(class_1297 e : mc.field_1687.method_18112()) {
                  if (e != mc.field_1724) {
                     double d = (double)e.method_5739(mc.field_1724);
                     if (!(d > r)) {
                        LuaTable t = new LuaTable();
                        t.set("id", LuaValue.valueOf(String.valueOf(class_7923.field_41177.method_10221(e.method_5864()))));
                        t.set("name", LuaValue.valueOf(e.method_5477().getString()));
                        t.set("x", LuaValue.valueOf(e.method_23317()));
                        t.set("y", LuaValue.valueOf(e.method_23318()));
                        t.set("z", LuaValue.valueOf(e.method_23321()));
                        t.set("dist", LuaValue.valueOf(d));
                        t.set("player", LuaValue.valueOf(e instanceof class_1657));
                        arr.set(i++, t);
                        if (i > 64) {
                           break;
                        }
                     }
                  }
               }

               return arr;
            } else {
               return arr;
            }
         }
      });
      g.set("world", world);
      LuaTable keys = new LuaTable();
      final Map<String, Boolean> keyPrev = new HashMap();
      keys.set("down", new OneArgFunction() {
         public LuaValue call(LuaValue n) {
            return LuaValue.valueOf(ScriptEngine.keyDownByName(n.checkjstring()));
         }
      });
      keys.set("pressed", new OneArgFunction() {
         public LuaValue call(LuaValue n) {
            String k = ScriptEngine.normKeyName(n.checkjstring());
            boolean down = ScriptEngine.keyDownByName(k);
            boolean prev = (Boolean)keyPrev.getOrDefault(k, false);
            keyPrev.put(k, down);
            return LuaValue.valueOf(down && !prev);
         }
      });
      g.set("keys", keys);
      g.set("log", new OneArgFunction() {
         public LuaValue call(LuaValue v) {
            CpmScriptsClient.chat(v.tojstring());
            return LuaValue.NIL;
         }
      });
      g.set("sendLocalChat", new OneArgFunction() {
         public LuaValue call(LuaValue v) {
            class_310 mc = class_310.method_1551();
            if (mc.field_1705 != null) {
               mc.field_1705.method_1743().method_1812(class_2561.method_43470(v.tojstring()));
            }

            return LuaValue.NIL;
         }
      });
      g.set("sendChat", new OneArgFunction() {
         public LuaValue call(LuaValue v) {
            class_310 mc = class_310.method_1551();
            if (mc.field_1724 != null && mc.method_1562() != null) {
               mc.method_1562().method_45729(v.tojstring());
            }

            return LuaValue.NIL;
         }
      });
      g.set("hasEffect", new OneArgFunction() {
         public LuaValue call(LuaValue v) {
            return LuaValue.valueOf(ScriptEngine.effect(v) != null);
         }
      });
      g.set("effectAmp", new OneArgFunction() {
         public LuaValue call(LuaValue v) {
            class_1293 e = ScriptEngine.effect(v);
            return LuaValue.valueOf(e == null ? 0 : e.method_5578());
         }
      });
      g.set("countEntities", new VarArgFunction() {
         public Varargs invoke(Varargs args) {
            class_310 mc = class_310.method_1551();
            if (mc.field_1687 != null && mc.field_1724 != null) {
               double r = Math.max((double)1.0F, Math.min((double)64.0F, args.optdouble(2, (double)8.0F)));
               String type = args.optjstring(1, "").trim().toLowerCase(Locale.ROOT);
               String name = args.optjstring(3, "").trim().toLowerCase(Locale.ROOT);
               class_238 box = mc.field_1724.method_5829().method_1014(r);
               int count = 0;

               for(class_1297 ent : mc.field_1687.method_8335(mc.field_1724, box)) {
                  if ((type.isEmpty() || type.equals(String.valueOf(class_7923.field_41177.method_10221(ent.method_5864())))) && (name.isEmpty() || ent.method_5477().getString().toLowerCase(Locale.ROOT).contains(name))) {
                     ++count;
                  }
               }

               return LuaValue.valueOf(count);
            } else {
               return LuaValue.ZERO;
            }
         }
      });
      return g;
   }

   public static boolean isEnabled() {
      return enabled;
   }

   public static void setEnabled(boolean v) {
      enabled = v;
   }

   public static synchronized void tick(class_310 mc) {
      if (enabled && !scripts.isEmpty()) {
         fillPlayerData(mc);

         for(Script s : scripts) {
            if (!s.errored && s.tickFn.isfunction()) {
               try {
                  s.tickFn.call(LuaValue.valueOf(0.05));
               } catch (Throwable t) {
                  s.errored = true;
                  String var10000 = s.name;
                  CpmScriptsClient.chat("[CPM Scripts] " + var10000 + " stopped: " + errMsg(t));
               }
            }
         }

         chatFresh = false;
         blockUseFresh = false;
         if (attackEntityHitTicks > 0) {
            --attackEntityHitTicks;
         }

         if (attackBlockHitTicks > 0) {
            --attackBlockHitTicks;
         }

      }
   }

   public static void onChatMessage(class_2561 message, boolean overlay) {
      if (!overlay) {
         chatLast = message.getString();
         chatFresh = true;
      }
   }

   public static void onAttackEntityLanded() {
      attackEntityHitTicks = 2;
   }

   public static void onAttackBlockLanded() {
      attackBlockHitTicks = 2;
   }

   public static void onUseBlock(class_2680 state) {
      blockUseLast = String.valueOf(class_7923.field_41175.method_10221(state.method_26204()));
      blockUseFresh = true;
   }

   private static String normKeyName(String name) {
      String k = name.trim().toLowerCase(Locale.ROOT);
      if (k.startsWith("key.keyboard.")) {
         k = k.substring("key.keyboard.".length());
      }

      return k;
   }

   private static boolean keyDownByName(String name) {
      class_310 mc = class_310.method_1551();
      if (mc.method_22683() == null) {
         return false;
      } else {
         String k = normKeyName(name);
         if (k.isEmpty()) {
            return false;
         } else {
            try {
               class_3675.class_306 key = class_3675.method_15981("key.keyboard." + k);
               return class_3675.method_15987(mc.method_22683(), key.method_1444());
            } catch (Throwable var4) {
               return false;
            }
         }
      }
   }

   private static class_1293 effect(LuaValue idV) {
      class_310 mc = class_310.method_1551();
      if (mc.field_1724 == null) {
         return null;
      } else {
         String id = idV.tojstring().trim().toLowerCase(Locale.ROOT);
         if (id.isEmpty()) {
            return null;
         } else {
            if (!id.contains(":")) {
               id = "minecraft:" + id;
            }

            for(class_1293 e : mc.field_1724.method_6026()) {
               if (id.equals(String.valueOf(class_7923.field_41174.method_10221((class_1291)e.method_5579().comp_349())))) {
                  return e;
               }
            }

            return null;
         }
      }
   }

   private static boolean isSolid(double x, double y, double z) {
      class_310 mc = class_310.method_1551();
      if (mc.field_1687 == null) {
         return false;
      } else {
         class_2338 pos = class_2338.method_49637(x, y, z);
         class_2680 st = mc.field_1687.method_8320(pos);
         return !st.method_26215() && st.method_26216();
      }
   }

   private static String errMsg(Throwable t) {
      if (t instanceof LuaError && t.getCause() != null) {
         return t.getCause().toString();
      } else {
         String m = t.getMessage();
         return m != null ? m : t.toString();
      }
   }

   private static void fillPlayerData(class_310 mc) {
      class_746 p = mc.field_1724;
      if (p != null) {
         float yaw = p.field_6283;
         float delta = firstTick ? 0.0F : (yaw - prevBodyYaw + 540.0F) % 360.0F - 180.0F;
         prevBodyYaw = yaw;
         firstTick = false;
         class_243 v = p.method_18798();
         double rad = Math.toRadians((double)yaw);
         double sin = Math.sin(rad);
         double cos = Math.cos(rad);
         double lx = -v.field_1352 * cos - v.field_1350 * sin;
         double lz = -v.field_1352 * sin + v.field_1350 * cos;
         velLocal.set("x", LuaValue.valueOf(lx));
         velLocal.set("y", LuaValue.valueOf(v.field_1351));
         velLocal.set("z", LuaValue.valueOf(lz));
         velWorld.set("x", LuaValue.valueOf(v.field_1352));
         velWorld.set("y", LuaValue.valueOf(v.field_1351));
         velWorld.set("z", LuaValue.valueOf(v.field_1350));
         playerTable.set("vel", velLocal);
         playerTable.set("velWorld", velWorld);
         playerTable.set("x", LuaValue.valueOf(p.method_23317()));
         playerTable.set("y", LuaValue.valueOf(p.method_23318()));
         playerTable.set("z", LuaValue.valueOf(p.method_23321()));
         playerTable.set("yaw", LuaValue.valueOf((double)yaw));
         playerTable.set("yawDelta", LuaValue.valueOf((double)delta));
         playerTable.set("onGround", LuaValue.valueOf(p.method_24828()));
         playerTable.set("inWater", LuaValue.valueOf(p.method_5799()));
         playerTable.set("sprinting", LuaValue.valueOf(p.method_5624()));
         playerTable.set("sneaking", LuaValue.valueOf(p.method_5715()));
         playerTable.set("fallFlying", LuaValue.valueOf(p.method_6128()));
         playerTable.set("swimming", LuaValue.valueOf(p.method_5681()));
         playerTable.set("crawling", LuaValue.valueOf(p.method_20448()));
         playerTable.set("pose", LuaValue.valueOf(p.method_18376().name()));
         playerTable.set("time", LuaValue.valueOf((double)p.method_73183().method_75260()));
         playerTable.set("mainHand", itemTable(p.method_6047()));
         playerTable.set("offHand", itemTable(p.method_6079()));
         playerTable.set("headYaw", LuaValue.valueOf((double)p.field_6241));
         playerTable.set("pitch", LuaValue.valueOf((double)p.method_36455()));
         class_243 view = p.method_5828(1.0F);
         viewVec.set("x", LuaValue.valueOf(view.field_1352));
         viewVec.set("y", LuaValue.valueOf(view.field_1351));
         viewVec.set("z", LuaValue.valueOf(view.field_1350));
         playerTable.set("view", viewVec);
         playerTable.set("walkPos", LuaValue.valueOf((double)p.field_42108.method_48569()));
         playerTable.set("walkSpeed", LuaValue.valueOf((double)p.field_42108.method_48566()));
         playerTable.set("walking", LuaValue.valueOf(p.method_24828() && (double)p.field_42108.method_48566() > 0.01 && !p.method_5624()));
         playerTable.set("running", LuaValue.valueOf(p.method_24828() && p.method_5624()));
         playerTable.set("pushup", LuaValue.valueOf(false));
         playerTable.set("swinging", LuaValue.valueOf(p.field_6252));
         playerTable.set("swingTime", LuaValue.valueOf(p.field_6279));
         playerTable.set("using", LuaValue.valueOf(p.method_6115()));
         playerTable.set("useItem", itemTable(p.method_6030()));
         playerTable.set("useKey", LuaValue.valueOf(mc.field_1690.field_1904.method_1434()));
         playerTable.set("attackKey", LuaValue.valueOf(mc.field_1690.field_1886.method_1434()));
         playerTable.set("attackEntityHit", LuaValue.valueOf(attackEntityHitTicks > 0));
         playerTable.set("attackBlockHit", LuaValue.valueOf(attackBlockHitTicks > 0));
         playerTable.set("blockUse", (LuaValue)(blockUseLast == null ? LuaValue.NIL : LuaValue.valueOf(blockUseLast)));
         playerTable.set("blockUseNew", LuaValue.valueOf(blockUseFresh));
         playerTable.set("useTicks", LuaValue.valueOf(p.method_6014()));
         playerTable.set("health", LuaValue.valueOf((double)p.method_6032()));
         playerTable.set("maxHealth", LuaValue.valueOf((double)p.method_6063()));
         playerTable.set("absorption", LuaValue.valueOf((double)p.method_6067()));
         playerTable.set("food", LuaValue.valueOf(p.method_7344().method_7586()));
         playerTable.set("saturation", LuaValue.valueOf((double)p.method_7344().method_7589()));
         playerTable.set("air", LuaValue.valueOf(p.method_5669()));
         playerTable.set("maxAir", LuaValue.valueOf(p.method_5748()));
         playerTable.set("fallDistance", LuaValue.valueOf(p.field_6017));
         playerTable.set("onFire", LuaValue.valueOf(p.method_5809()));
         playerTable.set("dayTime", LuaValue.valueOf((double)p.method_73183().method_8532()));
         playerTable.set("raining", LuaValue.valueOf(p.method_73183().method_8419()));
         playerTable.set("thundering", LuaValue.valueOf(p.method_73183().method_8546()));
         playerTable.set("riding", LuaValue.valueOf(p.method_5765()));
         class_1297 veh = p.method_5854();
         playerTable.set("vehicle", LuaValue.valueOf(veh == null ? "" : String.valueOf(class_7923.field_41177.method_10221(veh.method_5864()))));
         LuaTable effects = new LuaTable();
         int i = 1;

         for(class_1293 eff : p.method_6026()) {
            LuaTable e = new LuaTable();
            e.set("id", LuaValue.valueOf(String.valueOf(class_7923.field_41174.method_10221((class_1291)eff.method_5579().comp_349()))));
            e.set("amp", LuaValue.valueOf(eff.method_5578()));
            e.set("duration", LuaValue.valueOf(eff.method_5584()));
            effects.set(i++, e);
         }

         playerTable.set("effects", effects);
         LuaTable armor = new LuaTable();
         armor.set("head", itemTable(p.method_6118(class_1304.field_6169)));
         armor.set("chest", itemTable(p.method_6118(class_1304.field_6174)));
         armor.set("legs", itemTable(p.method_6118(class_1304.field_6172)));
         armor.set("feet", itemTable(p.method_6118(class_1304.field_6166)));
         playerTable.set("armor", armor);
         playerTable.set("chatLast", (LuaValue)(chatLast == null ? LuaValue.NIL : LuaValue.valueOf(chatLast)));
         playerTable.set("chatNew", LuaValue.valueOf(chatFresh));
      }
   }

   private static LuaTable itemTable(class_1799 stack) {
      LuaTable t = new LuaTable();
      if (stack != null && !stack.method_7960()) {
         t.set("id", LuaValue.valueOf(String.valueOf(class_7923.field_41178.method_10221(stack.method_7909()))));
         t.set("name", LuaValue.valueOf(stack.method_7964().getString()));
         t.set("count", LuaValue.valueOf(stack.method_7947()));
      } else {
         t.set("id", LuaValue.valueOf("minecraft:air"));
         t.set("name", LuaValue.valueOf(""));
         t.set("count", LuaValue.valueOf(0));
      }

      return t;
   }

   private static final class Script {
      final String name;
      final Globals globals;
      final LuaValue tickFn;
      boolean errored;

      Script(String name, Globals globals) {
         this.name = name;
         this.globals = globals;
         this.tickFn = globals.get("tick");
      }
   }
}
