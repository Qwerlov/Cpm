package com.oleo.cpmscripts.nodes;

import com.oleo.cpmscripts.ScriptEngine;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class NodeGraphs {
   private NodeGraphs() {
   }

   public static Path dir() {
      return ScriptEngine.getScriptsDir().resolve("nodes");
   }

   public static Path projectsDir() {
      return ScriptEngine.getScriptsDir().resolve("projects");
   }

   public static Path file(String name) {
      if (!name.endsWith(".json")) {
         name = name + ".json";
      }

      return dir().resolve(name).normalize();
   }

   public static List<String> list() {
      List<String> out = new ArrayList();

      try {
         Files.createDirectories(dir());
         DirectoryStream<Path> ds = Files.newDirectoryStream(dir(), "*.json");

         try {
            for(Path f : ds) {
               out.add(f.getFileName().toString());
            }
         } catch (Throwable var5) {
            if (ds != null) {
               try {
                  ds.close();
               } catch (Throwable var4) {
                  var5.addSuppressed(var4);
               }
            }

            throw var5;
         }

         if (ds != null) {
            ds.close();
         }
      } catch (IOException var6) {
      }

      out.sort(String.CASE_INSENSITIVE_ORDER);
      return out;
   }

   public static NodeGraph load(String name) throws IOException {
      return NodeGraph.load(file(name));
   }

   public static void save(NodeGraph g) throws IOException {
      Files.createDirectories(dir());
      g.save(file(g.name));
   }

   public static void delete(String name) throws IOException {
      Files.deleteIfExists(file(name));
   }

   public static String compile(String name) throws IOException, GraphCompiler.CompileException {
      NodeGraph g = load(name);
      return GraphCompiler.compile(g);
   }
}
