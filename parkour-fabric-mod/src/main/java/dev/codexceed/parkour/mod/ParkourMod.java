package dev.codexceed.parkour.mod;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import dev.codexceed.parkour.generator.ParkourGenerator;
import dev.codexceed.parkour.model.BlockPlacement;
import dev.codexceed.parkour.model.CourseStructure;
import dev.codexceed.parkour.model.GenParams;
import dev.codexceed.parkour.model.Recipe;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

/**
 * MVP mod entrypoint. Registers /parkourgen <seed> <tier> <length> which calls the
 * deterministic core generator and pastes the course into the world at the player.
 *
 * NOTE: Yarn/Mojmap names below (Identifier.of, getWorld, sendFeedback, etc.) must
 * match the mappings published for Minecraft 26.1.2 -- adjust if the API differs.
 */
public class ParkourMod implements ModInitializer {
    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registry, env) ->
            dispatcher.register(literal("parkourgen")
                .then(argument("seed", LongArgumentType.longArg())
                .then(argument("tier", IntegerArgumentType.integer(1, 10))
                .then(argument("length", IntegerArgumentType.integer(5, 200))
                .executes(ctx -> build(
                        ctx.getSource(),
                        LongArgumentType.getLong(ctx, "seed"),
                        IntegerArgumentType.getInteger(ctx, "tier"),
                        IntegerArgumentType.getInteger(ctx, "length"))))))));
    }

    private int build(ServerCommandSource src, long seed, int tier, int length) {
        ServerPlayerEntity player = src.getPlayer();
        if (player == null) {
            src.sendError(Text.literal("Run this as a player."));
            return 0;
        }
        ServerWorld world = (ServerWorld) player.getWorld();
        BlockPos origin = player.getBlockPos();

        Recipe recipe = new Recipe(ParkourGenerator.GEN_VERSION, seed,
                new GenParams(tier, length, 8, 0.25, "quartz"));
        CourseStructure course = new ParkourGenerator().generate(recipe);

        for (BlockPlacement bp : course.placements) {
            String[] id = bp.block.split(":");
            Block block = Registries.BLOCK.get(Identifier.of(id[0], id[1]));
            // core uses START_Y=64 as local origin; offset relative to the player
            BlockPos pos = origin.add(bp.pos.x, bp.pos.y - 64, bp.pos.z);
            world.setBlockState(pos, block.getDefaultState());
        }

        src.sendFeedback(() -> Text.literal(
                "Generated parkour: " + course.placements.size() + " blocks, difficulty "
                + course.difficulty + " (seed " + seed + ", tier " + tier + ")"), false);
        return 1;
    }
}
