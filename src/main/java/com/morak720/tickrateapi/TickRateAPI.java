package com.morak720.tickrateapi;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

@Mod(TickRateAPI.MOD_ID)
public class TickRateAPI {
    // O ID do mod, usado para identificar unicamente o mod e registrar recursos.
    public static final String MOD_ID = "tickrateapi";
    
    // Logger padrão para registrar informações e erros no console do jogo.
    private static final Logger LOGGER = LogUtils.getLogger();

    public TickRateAPI(IEventBus modEventBus) {
        // Registra o método commonSetup para a inicialização comum do mod.
        modEventBus.addListener(this::commonSetup);
        // Registra o método clientSetup para a inicialização exclusiva do cliente.
        modEventBus.addListener(this::clientSetup);
        // Registra os pacotes de rede
        modEventBus.addListener(this::registerPayloads);

        LOGGER.info("TickRate API inicializado com sucesso!");
    }

    private void registerPayloads(final net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent event) {
        final net.neoforged.neoforge.network.registration.PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(
            com.morak720.tickrateapi.network.TimerSyncPayload.TYPE,
            com.morak720.tickrateapi.network.TimerSyncPayload.STREAM_CODEC,
            (payload, context) -> com.morak720.tickrateapi.client.ClientPacketHandler.handleTimerSync(payload, context)
        );
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Etapa commonSetup concluída para o TickRate API.");
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        LOGGER.info("Etapa clientSetup concluída para o TickRate API.");
    }
}
