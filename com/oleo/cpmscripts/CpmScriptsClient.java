package com.oleo.cpmscripts;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.oleo.cpmscripts.gui.ScriptListScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.class_1269;
import net.minecraft.class_2561;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_304.class_11900;
import net.minecraft.class_3675.class_307;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CpmScriptsClient implements ClientModInitializer {
   public static final String MOD_ID = "cpmscripts";
   public static final Logger LOGGER = LoggerFactory.getLogger("CPM Scripts");
   public static final class_304[] scriptKeys = new class_304[6];
   public static final boolean[] keyDown = new boolean[6];
   public static final boolean[] keyPressed = new boolean[6];
   public static class_304 openGuiKey;

   public void onInitializeClient() {
      openGuiKey = KeyBindingHelper.registerKeyBinding(new class_304("key.cpmscripts.gui", class_307.field_1668, 75, class_11900.field_62556));
      ScriptEngine.init();
      ClientReceiveMessageEvents.GAME.register((ClientReceiveMessageEvents.Game)(message, overlay) -> ScriptEngine.onChatMessage(message, overlay));
      AttackEntityCallback.EVENT.register((AttackEntityCallback)(player, world, hand, entity, hitResult) -> {
         if (world.method_8608()) {
            ScriptEngine.onAttackEntityLanded();
         }

         return class_1269.field_5811;
      });
      AttackBlockCallback.EVENT.register((AttackBlockCallback)(player, world, hand, pos, direction) -> {
         if (world.method_8608()) {
            ScriptEngine.onAttackBlockLanded();
         }

         return class_1269.field_5811;
      });
      UseBlockCallback.EVENT.register((UseBlockCallback)(player, world, hand, hitResult) -> {
         if (world.method_8608() && hitResult != null) {
            ScriptEngine.onUseBlock(world.method_8320(hitResult.method_17777()));
         }

         return class_1269.field_5811;
      });
      ClientTickEvents.END_CLIENT_TICK.register((ClientTickEvents.EndTick)(client) -> {
         while(openGuiKey.method_1436()) {
            client.execute(() -> client.method_1507(client.field_1755 instanceof ScriptListScreen ? null : new ScriptListScreen()));
         }

         if (client.field_1724 != null && !client.method_1493()) {
            ScriptEngine.tick(client);
         }

      });
      ClientCommandRegistrationCallback.EVENT.register((ClientCommandRegistrationCallback)(dispatcher, registryAccess) -> dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)ClientCommandManager.literal("cpmscripts").then(ClientCommandManager.literal("reload").executes((ctx) -> {
            ScriptEngine.reload();
            return 1;
         }))).then(ClientCommandManager.literal("toggle").executes((ctx) -> {
            ScriptEngine.setEnabled(!ScriptEngine.isEnabled());
            chat("[CPM Scripts] Scripts: " + (ScriptEngine.isEnabled() ? "ON" : "OFF"));
            return 1;
         }))).then(ClientCommandManager.literal("on").executes((ctx) -> {
            ScriptEngine.setEnabled(true);
            chat("[CPM Scripts] Scripts: ON");
            return 1;
         }))).then(ClientCommandManager.literal("off").executes((ctx) -> {
            ScriptEngine.setEnabled(false);
            chat("[CPM Scripts] Scripts: OFF");
            return 1;
         }))).then(ClientCommandManager.literal("run").then(ClientCommandManager.argument("code", StringArgumentType.greedyString()).executes((ctx) -> {
            String code = StringArgumentType.getString(ctx, "code");
            String err = ScriptEngine.runSnippet(code);
            chat(err == null ? "[CPM Scripts] OK" : "[CPM Scripts] Error: " + err);
            return 1;
         })))));
   }

   public static void chat(String msg) {
      class_310 mc = class_310.method_1551();
      if (mc.field_1705 != null) {
         mc.field_1705.method_1743().method_1812(class_2561.method_43470(msg));
      }

   }
}
