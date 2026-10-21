package com.oleo.cpmscripts.nodes;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class NodeGraph {
   public static final int FORMAT = 1;
   public int format = 1;
   public String name = "";
   public final List<GNode> nodes = new ArrayList();
   public final List<GWire> wires = new ArrayList();
   private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();

   public GNode nodeById(String id) {
      for(GNode n : this.nodes) {
         if (n.id.equals(id)) {
            return n;
         }
      }

      return null;
   }

   public String nextId() {
      int i;
      for(i = 1; this.nodeById("n" + i) != null; ++i) {
      }

      return "n" + i;
   }

   public void removeNode(String id) {
      this.nodes.removeIf((n) -> n.id.equals(id));
      this.wires.removeIf((w) -> w.fromNode.equals(id) || w.toNode.equals(id));
   }

   public void removeWire(String fromNode, String fromPort, String toNode, String toPort) {
      this.wires.removeIf((w) -> w.fromNode.equals(fromNode) && w.fromPort.equals(fromPort) && w.toNode.equals(toNode) && w.toPort.equals(toPort));
   }

   public static NodeGraph load(Path file) throws IOException, JsonParseException {
      Reader r = Files.newBufferedReader(file, StandardCharsets.UTF_8);

      NodeGraph var3;
      try {
         NodeGraph g = (NodeGraph)GSON.fromJson(r, NodeGraph.class);
         if (g == null) {
            throw new IOException("пустой файл");
         }

         if (g.format < 1 || g.format > 1) {
            throw new IOException("неподдерживаемый формат " + g.format);
         }

         g.name = file.getFileName().toString().replaceFirst("\\.json$", "");
         var3 = g;
      } catch (Throwable var5) {
         if (r != null) {
            try {
               r.close();
            } catch (Throwable var4) {
               var5.addSuppressed(var4);
            }
         }

         throw var5;
      }

      if (r != null) {
         r.close();
      }

      return var3;
   }

   public void save(Path file) throws IOException {
      Writer w = Files.newBufferedWriter(file, StandardCharsets.UTF_8);

      try {
         GSON.toJson(this, w);
      } catch (Throwable var6) {
         if (w != null) {
            try {
               w.close();
            } catch (Throwable var5) {
               var6.addSuppressed(var5);
            }
         }

         throw var6;
      }

      if (w != null) {
         w.close();
      }

   }

   public String toJson() {
      return GSON.toJson(this);
   }

   public static NodeGraph fromJson(String json) {
      NodeGraph g = (NodeGraph)GSON.fromJson(json, NodeGraph.class);
      if (g == null) {
         throw new IllegalStateException("пустой снапшот");
      } else {
         return g;
      }
   }

   public static final class GNode {
      public String id;
      public String type;
      public int x;
      public int y;
      public String group;
      public final Map<String, String> params = new LinkedHashMap();
   }

   public static final class GWire {
      public String fromNode;
      public String fromPort;
      public String toNode;
      public String toPort;
   }
}
