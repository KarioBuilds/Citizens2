package net.citizensnpcs.npc.entity;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.CitizensMobNPC;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHandle;
import net.minecraft.server.EntityVillager;
import net.minecraft.server.PathfinderGoalSelector;
import net.minecraft.server.World;

import org.bukkit.entity.Villager;

public class CitizensVillagerNPC extends CitizensMobNPC {

    public CitizensVillagerNPC(int id, String name) {
        super(id, name, EntityVillagerNPC.class);
    }

    @Override
    public Villager getBukkitEntity() {
        return (Villager) getHandle().getBukkitEntity();
    }

    public static class EntityVillagerNPC extends EntityVillager implements NPCHandle {
        private final CitizensNPC npc;

        public EntityVillagerNPC(World world) {
            this(world, null);
        }

        public EntityVillagerNPC(World world, NPC npc) {
            super(world);
            this.npc = (CitizensNPC) npc;
            goalSelector = new PathfinderGoalSelector();
            targetSelector = new PathfinderGoalSelector();
        }

        @Override
        public void z_() {
            super.z_();
            if (npc != null)
                npc.update();
        }

        @Override
        public NPC getNPC() {
            return npc;
        }
    }
}