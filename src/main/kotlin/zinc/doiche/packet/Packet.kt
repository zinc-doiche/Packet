package zinc.doiche.packet;

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.wrappers.WrappedDataValue
import com.comphenix.protocol.wrappers.WrappedDataWatcher
import io.github.monun.kommand.kommand
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftBlockDisplay
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.util.*
import kotlin.experimental.or


class Packet: JavaPlugin() {
    override fun onEnable() {
        manager = ProtocolLibrary.getProtocolManager()
        kommand {
            register("dp") {
                executes {
                    packet(player)
                }
            }
        }
    }

    private fun packet(player: Player) {
        player as CraftPlayer
        val display = net.minecraft.world.entity.EntityType.BLOCK_DISPLAY
            .create((player.world as CraftWorld).handle)?.bukkitEntity as CraftBlockDisplay
        display.block = Material.STONE.createBlockData()
        val data = display.handle.entityData.nonDefaultValues ?: emptyList()
        with(player.handle.connection) {
            send(display.handle.addEntityPacket, null)
            send(ClientboundSetEntityDataPacket(display.entityId, data), null)
        }
    }

    private fun pro(player: Player, isData: Boolean) {
        player as CraftPlayer
        val entityId = 10000;
        val location = player.location
        val display = net.minecraft.world.entity.EntityType.BLOCK_DISPLAY
            .create((player.world as CraftWorld).handle)?.bukkitEntity as CraftBlockDisplay
        val data = display.handle.entityData.nonDefaultValues?.map {
            WrappedDataValue(
                it.id,
                WrappedDataWatcher.Serializer(it.value::class.java, it.serializer, false),
                it.value) }

        PacketContainer(PacketType.Play.Server.SPAWN_ENTITY).apply {
            integers.write(0, entityId);
            uuiDs.write(0, UUID.randomUUID());
            entityTypeModifier.write(0, EntityType.BLOCK_DISPLAY);
            doubles
                .write(0, location.x)
                .write(1, location.y)
                .write(2, location.z);
        }.let { manager.sendServerPacket(player, it) }

        PacketContainer(PacketType.Play.Server.ENTITY_METADATA).apply {
            integers.write(0, entityId)
            dataValueCollectionModifier.write(0, if(isData) data else listOf(
                WrappedDataValue(0, WrappedDataWatcher.Registry.get(Class.forName("java.lang.Byte")),
                    0x00.toByte() or 0x40.toByte()),
                WrappedDataValue(23, WrappedDataWatcher.Registry.getBlockDataSerializer(false),
                    Material.STONE.createBlockData()),
            ))
        }.let { manager.sendServerPacket(player, it) }
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}

lateinit var manager: ProtocolManager
    private set

