package com.morak720.tickrateapi.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.morak720.tickrateapi.data.TickRateArea;
import com.morak720.tickrateapi.data.TickRateSavedData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import java.util.UUID;

public class TickAreaCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("tick")
                        .requires(source -> source.hasPermission(2)) // Exige privilégios de operador (OP nível 2)
                        .then(Commands.literal("area")
                                .then(Commands.argument("target_tick_rate", IntegerArgumentType.integer(1, 20))
                                        .then(Commands.argument("chunk_radius", IntegerArgumentType.integer(0))
                                                .executes(context -> executeArea(context, false, "world"))
                                                .then(Commands.argument("toggle_on_leave", BoolArgumentType.bool())
                                                        .executes(context -> executeArea(context,
                                                                BoolArgumentType.getBool(context,
                                                                        "toggle_on_leave"), "world"))
                                                        .then(Commands.argument("target_type", StringArgumentType.word())
                                                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(new String[]{"world", "player", "all"}, builder))
                                                                .executes(context -> executeArea(context,
                                                                        BoolArgumentType.getBool(context,
                                                                                "toggle_on_leave"),
                                                                        StringArgumentType.getString(context,
                                                                                "target_type"))))))))
                        .then(Commands.literal("reset")
                                .executes(TickAreaCommand::executeReset)));
    }

    private static int executeArea(CommandContext<CommandSourceStack> context, boolean toggleOnLeave, String targetTypeStr)
            throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException(); // Lança exceção se não for executado por um Player

        int targetTickRate = IntegerArgumentType.getInteger(context, "target_tick_rate");
        int chunkRadius = IntegerArgumentType.getInteger(context, "chunk_radius");

        ServerLevel level = player.serverLevel();
        ChunkPos centerChunk = new ChunkPos(player.blockPosition());
        UUID creatorUuid = player.getUUID();

        TickRateArea.TargetType targetType;
        try {
            targetType = TickRateArea.TargetType.valueOf(targetTypeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            targetType = TickRateArea.TargetType.WORLD;
        }

        // Registra a nova área no banco de dados persistente da dimensão atual
        TickRateSavedData data = TickRateSavedData.get(level);
        TickRateArea area = new TickRateArea(centerChunk.x, centerChunk.z, chunkRadius, targetTickRate, creatorUuid,
                toggleOnLeave, targetType);
        data.addArea(area);

        return 1;
    }

    private static int executeReset(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerLevel level = source.getLevel();

        // Recupera o storage e limpa todas as áreas, resetando o estado
        TickRateSavedData data = TickRateSavedData.get(level);
        int clearedCount = data.getAreas().size();
        data.clearAreas();

        return 1;
    }
}
