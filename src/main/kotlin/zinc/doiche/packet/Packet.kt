package zinc.doiche.packet;

import io.github.monun.kommand.kommand
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.world.entity.EntityType
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftBlockDisplay
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.util.Transformation
import org.joml.Quaternionf
import org.joml.Vector3f

class Packet: JavaPlugin() {
    override fun onEnable() {
        kommand {
            register("dp") {
                executes {
                    packet(player)
                }
            }
            register("anvil") {
                executes {
                    Anvil(player).open()
                }
            }
        }
    }

    private fun packet(player: Player) {
        player as CraftPlayer
        val entity = EntityType.BLOCK_DISPLAY.create((player.world as CraftWorld).handle)!!.apply {
            with(player.location) {
                moveTo(x, y, z, 0f, 0f)
            }
            with(bukkitEntity as CraftBlockDisplay) {
                block = Material.ANDESITE_WALL.createBlockData()
                transformation = Transformation(Vector3f(), Quaternionf(), Vector3f(5f, 5f, 5f), Quaternionf())
            }
            setGlowingTag(true)
            glowColorOverride = Color.AQUA.asRGB()
        }
        val bukkitEntity = entity.bukkitEntity as BlockDisplay
        val entityDataPacket = ClientboundSetEntityDataPacket(bukkitEntity.entityId, entity.entityData.nonDefaultValues ?: emptyList())
        with(player.handle.connection) {
            send(entity.addEntityPacket, null)
            send(entityDataPacket, null)
        }
    }
}
