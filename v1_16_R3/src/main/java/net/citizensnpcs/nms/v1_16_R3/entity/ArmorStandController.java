package net.citizensnpcs.nms.v1_16_R3.entity;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftArmorStand;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEntity;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.util.Vector;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.nms.v1_16_R3.util.ForwardingNPCHolder;
import net.citizensnpcs.nms.v1_16_R3.util.MobAI;
import net.citizensnpcs.nms.v1_16_R3.util.MobAI.ForwardingMobAI;
import net.citizensnpcs.nms.v1_16_R3.util.NMSBoundingBox;
import net.citizensnpcs.nms.v1_16_R3.util.NMSImpl;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;
import net.minecraft.server.v1_16_R3.AxisAlignedBB;
import net.minecraft.server.v1_16_R3.EntityArmorStand;
import net.minecraft.server.v1_16_R3.EntityHuman;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import net.minecraft.server.v1_16_R3.EntityTypes;
import net.minecraft.server.v1_16_R3.EnumHand;
import net.minecraft.server.v1_16_R3.EnumInteractionResult;
import net.minecraft.server.v1_16_R3.FluidType;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import net.minecraft.server.v1_16_R3.Tag;
import net.minecraft.server.v1_16_R3.Vec3D;
import net.minecraft.server.v1_16_R3.World;

public class ArmorStandController extends MobEntityController {
    public ArmorStandController() {
        super(EntityArmorStandNPC.class);
    }

    @Override
    public ArmorStand getBukkitEntity() {
        return (ArmorStand) super.getBukkitEntity();
    }

    public static class ArmorStandNPC extends CraftArmorStand implements ForwardingNPCHolder {
        public ArmorStandNPC(EntityArmorStandNPC entity) {
            super((CraftServer) Bukkit.getServer(), entity);
        }
    }

    public static class EntityArmorStandNPC extends EntityArmorStand implements NPCHolder, ForwardingMobAI {
        private MobAI ai;
        private final CitizensNPC npc;

        public EntityArmorStandNPC(EntityTypes<? extends EntityArmorStand> types, World world) {
            this(types, world, null);
        }

        public EntityArmorStandNPC(EntityTypes<? extends EntityArmorStand> types, World world, NPC npc) {
            super(types, world);
            this.npc = (CitizensNPC) npc;
            if (npc != null) {
                ai = new BasicMobAI(this);
                NMS.setStepHeight(getBukkitEntity(), 1);
            }
        }

        @Override
        public void a(AxisAlignedBB bb) {
            super.a(NMSBoundingBox.makeBB(npc, bb));
        }

        @Override
        public EnumInteractionResult a(EntityHuman entityhuman, Vec3D vec3d, EnumHand enumhand) {
            if (npc == null)
                return super.a(entityhuman, vec3d, enumhand);
            PlayerInteractEntityEvent event = new PlayerInteractEntityEvent((Player) entityhuman.getBukkitEntity(),
                    getBukkitEntity());
            Bukkit.getPluginManager().callEvent(event);
            return event.isCancelled() ? EnumInteractionResult.FAIL : EnumInteractionResult.SUCCESS;
        }

        @Override
        public boolean a(EntityPlayer player) {
            return NMS.shouldBroadcastToPlayer(npc, () -> super.a(player));
        }

        @Override
        public boolean a(Tag<FluidType> tag, double d0) {
            if (npc == null)
                return super.a(tag, d0);
            Vec3D old = getMot().add(0, 0, 0);
            boolean res = super.a(tag, d0);
            if (!npc.isPushableByFluids()) {
                setMot(old);
            }
            return res;
        }

        @Override
        public void collide(net.minecraft.server.v1_16_R3.Entity entity) {
            // this method is called by both the entities involved - cancelling
            // it will not stop the NPC from moving.
            super.collide(entity);
            if (npc != null) {
                Util.callCollisionEvent(npc, entity.getBukkitEntity());
            }
        }

        @Override
        public boolean d(NBTTagCompound save) {
            return npc == null ? super.d(save) : false;
        }

        @Override
        public MobAI getAI() {
            return ai;
        }

        @Override
        public CraftEntity getBukkitEntity() {
            if (npc != null && !(super.getBukkitEntity() instanceof NPCHolder)) {
                NMSImpl.setBukkitEntity(this, new ArmorStandNPC(this));
            }
            return super.getBukkitEntity();
        }

        @Override
        public NPC getNPC() {
            return npc;
        }

        @Override
        public void i(double x, double y, double z) {
            Vector vector = Util.callPushEvent(npc, x, y, z);
            if (vector != null) {
                super.i(vector.getX(), vector.getY(), vector.getZ());
            }
        }

        @Override
        public void tick() {
            super.tick();
            if (npc != null) {
                npc.update();
                ai.tickAI();
            }
        }
    }
}
