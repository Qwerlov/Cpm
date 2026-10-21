package com.oleo.cpmscripts;

import com.tom.cpm.api.IClientAPI;

public final class CpmBridge {
   static volatile IClientAPI api;

   private CpmBridge() {
   }

   public static boolean isReady() {
      return api != null;
   }

   public static boolean setValue(String name, int value) {
      IClientAPI a = api;
      if (a == null) {
         return false;
      } else {
         if (value < 0) {
            value = 0;
         }

         if (value > 255) {
            value = 255;
         }

         try {
            return a.playAnimation(name, value);
         } catch (Throwable var4) {
            return false;
         }
      }
   }

   public static int getValue(String name) {
      IClientAPI a = api;
      if (a == null) {
         return -1;
      } else {
         try {
            return a.getAnimationPlaying(name);
         } catch (Throwable var3) {
            return -1;
         }
      }
   }

   public static boolean play(String name) {
      IClientAPI a = api;
      if (a == null) {
         return false;
      } else {
         try {
            return a.getAnimationPlaying(name) > 0 ? true : a.playAnimation(name);
         } catch (Throwable var3) {
            return false;
         }
      }
   }

   public static boolean stop(String name) {
      IClientAPI a = api;
      if (a == null) {
         return false;
      } else {
         try {
            return a.getAnimationPlaying(name) == 0 ? true : a.playAnimation(name, 0);
         } catch (Throwable var3) {
            return false;
         }
      }
   }
}
