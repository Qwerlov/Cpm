package com.oleo.cpmscripts.nodes;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.oleo.cpmscripts.CpmBridge;
import com.tom.cpm.client.CustomPlayerModelsClient;
import com.tom.cpm.shared.animation.AnimationRegistry;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.definition.ModelDefinitionLoader;
import com.tom.cpm.shared.model.RenderManager;
import com.tom.cpm.shared.parts.anim.menu.AbstractGestureButtonData;
import com.tom.cpm.shared.parts.anim.menu.CommandAction;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import net.minecraft.class_1291;
import net.minecraft.class_1299;
import net.minecraft.class_1792;
import net.minecraft.class_2248;
import net.minecraft.class_310;
import net.minecraft.class_7923;

public final class NodeLists {
   private static List<String> anims = new ArrayList();
   private static List<String> layers = new ArrayList();
   private static List<String> items = null;
   private static String projectsInfo = "не сканировалось";
   private static List<String> effects = null;
   private static List<String> entities = null;
   private static List<String> blocks = null;

   private NodeLists() {
   }

   public static List<String> anims() {
      return anims;
   }

   public static List<String> layers() {
      return layers;
   }

   public static String projectsInfo() {
      return projectsInfo;
   }

   public static synchronized String refreshProjects() {
      TreeSet<String> a = new TreeSet();
      TreeSet<String> l = new TreeSet();
      String modelInfo = scanCurrentModel(a, l);
      if (modelInfo != null) {
         anims = new ArrayList(a);
         layers = new ArrayList(l);
         projectsInfo = modelInfo;
         return projectsInfo;
      } else {
         int files = 0;
         Path dir = NodeGraphs.projectsDir();

         try {
            Files.createDirectories(dir);
            DirectoryStream<Path> ds = Files.newDirectoryStream(dir, "*.cpmproject");

            try {
               for(Path f : ds) {
                  int[] cnt = scanProject(f, a, l);
                  files += cnt[0];
               }
            } catch (Throwable var10) {
               if (ds != null) {
                  try {
                     ds.close();
                  } catch (Throwable var9) {
                     var10.addSuppressed(var9);
                  }
               }

               throw var10;
            }

            if (ds != null) {
               ds.close();
            }

            anims = new ArrayList(a);
            layers = new ArrayList(l);
            projectsInfo = files + " проект(ов): " + anims.size() + " анимаций, " + layers.size() + " слоёв";
         } catch (IOException e) {
            projectsInfo = "ошибка: " + e.getMessage();
         }

         return projectsInfo;
      }
   }

   private static String scanCurrentModel(TreeSet<String> anims, TreeSet<String> layers) {
      try {
         if (!CpmBridge.isReady()) {
            return null;
         } else {
            class_310 mc = class_310.method_1551();
            if (mc.field_1724 == null) {
               return null;
            } else {
               CustomPlayerModelsClient cpm = CustomPlayerModelsClient.INSTANCE;
               if (cpm != null && cpm.manager != null) {
                  Player<?> player = loadedPlayer(cpm.manager, mc.field_1724.method_7334());
                  if (player == null) {
                     return null;
                  } else {
                     ModelDefinition def = player.getModelDefinition();
                     if (def == null) {
                        return null;
                     } else {
                        AnimationRegistry reg = def.getAnimations();
                        if (reg == null) {
                           return null;
                        } else {
                           int cnt = 0;

                           for(AbstractGestureButtonData btn : reg.getNamedActions()) {
                              String name = btn.getName();
                              if (name != null && !name.isEmpty()) {
                                 switch (btn.getType()) {
                                    case GESTURE:
                                    case POSE:
                                       anims.add(name);
                                       break;
                                    default:
                                       layers.add(name);
                                 }

                                 ++cnt;
                              }
                           }

                           for(Map.Entry<String, ?> e : reg.getCommandActionsMap().entrySet()) {
                              String name = (String)e.getKey();
                              if (name != null && !name.isEmpty() && !anims.contains(name) && !layers.contains(name)) {
                                 try {
                                    int max = ((CommandAction)e.getValue()).getMaxValue();
                                    if (max > 0) {
                                       layers.add(name);
                                    } else {
                                       anims.add(name);
                                    }
                                 } catch (Throwable var12) {
                                    anims.add(name);
                                 }

                                 ++cnt;
                              }
                           }

                           for(String name : reg.getCustomPoses().keySet()) {
                              if (name != null && !name.isEmpty() && !anims.contains(name)) {
                                 anims.add(name);
                                 ++cnt;
                              }
                           }

                           if (cnt == 0) {
                              return null;
                           } else {
                              int var10000 = anims.size();
                              return "текущая модель: " + var10000 + " анимаций, " + layers.size() + " слоёв";
                           }
                        }
                     }
                  }
               } else {
                  return null;
               }
            }
         }
      } catch (Throwable var13) {
         return null;
      }
   }

   private static <G> Player<?> loadedPlayer(RenderManager<G, ?, ?, ?> manager, G profile) {
      try {
         Field f = RenderManager.class.getDeclaredField("loader");
         f.setAccessible(true);
         ModelDefinitionLoader<G> loader = (ModelDefinitionLoader)f.get(manager);
         return loader == null ? null : loader.getLoadedPlayer(profile);
      } catch (Throwable var4) {
         return null;
      }
   }

   private static int[] scanProject(Path file, TreeSet<String> anims, TreeSet<String> layers) {
      int[] cnt = new int[]{0};

      try {
         ZipFile zip = new ZipFile(file.toFile());

         try {
            Enumeration<? extends ZipEntry> en = zip.entries();

            while(en.hasMoreElements()) {
               ZipEntry e = (ZipEntry)en.nextElement();
               if (!e.isDirectory() && e.getName().startsWith("animations/") && e.getName().endsWith(".json")) {
                  try {
                     InputStream in = zip.getInputStream(e);

                     label82: {
                        try {
                           JsonObject o = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
                           if (o.has("name")) {
                              String name = o.get("name").getAsString();
                              if (name.startsWith("$value$")) {
                                 layers.add(name.substring("$value$".length()));
                              } else {
                                 anims.add(name);
                              }

                              int var10002 = cnt[0]++;
                              break label82;
                           }
                        } catch (Throwable var12) {
                           if (in != null) {
                              try {
                                 in.close();
                              } catch (Throwable var11) {
                                 var12.addSuppressed(var11);
                              }
                           }

                           throw var12;
                        }

                        if (in != null) {
                           in.close();
                        }
                        continue;
                     }

                     if (in != null) {
                        in.close();
                     }
                  } catch (Throwable var13) {
                  }
               }
            }
         } catch (Throwable var14) {
            try {
               zip.close();
            } catch (Throwable var10) {
               var14.addSuppressed(var10);
            }

            throw var14;
         }

         zip.close();
      } catch (IOException var15) {
      }

      return cnt;
   }

   public static synchronized List<String> items() {
      if (items == null) {
         TreeSet<String> s = new TreeSet();

         for(class_1792 item : class_7923.field_41178) {
            s.add(String.valueOf(class_7923.field_41178.method_10221(item)));
         }

         items = new ArrayList(s);
      }

      return items;
   }

   public static synchronized List<String> effects() {
      if (effects == null) {
         TreeSet<String> s = new TreeSet();

         for(class_1291 eff : class_7923.field_41174) {
            s.add(String.valueOf(class_7923.field_41174.method_10221(eff)));
         }

         effects = new ArrayList(s);
      }

      return effects;
   }

   public static synchronized List<String> entities() {
      if (entities == null) {
         TreeSet<String> s = new TreeSet();

         for(class_1299<?> ent : class_7923.field_41177) {
            s.add(String.valueOf(class_7923.field_41177.method_10221(ent)));
         }

         entities = new ArrayList(s);
      }

      return entities;
   }

   public static synchronized List<String> blocks() {
      if (blocks == null) {
         TreeSet<String> s = new TreeSet();

         for(class_2248 blk : class_7923.field_41175) {
            s.add(String.valueOf(class_7923.field_41175.method_10221(blk)));
         }

         blocks = new ArrayList(s);
      }

      return blocks;
   }
}
