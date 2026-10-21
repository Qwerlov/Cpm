package com.oleo.cpmscripts;

import com.tom.cpm.api.ICPMPlugin;
import com.tom.cpm.api.IClientAPI;
import com.tom.cpm.api.ICommonAPI;

public class CpmPluginEntry implements ICPMPlugin {
   public void initClient(IClientAPI api) {
      CpmBridge.api = api;
      CpmScriptsClient.LOGGER.info("[CPM Scripts] CPM API connected");
   }

   public void initCommon(ICommonAPI api) {
   }

   public String getOwnerModId() {
      return "cpmscripts";
   }
}
