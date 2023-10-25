package zinc.doiche.packet

import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.AnvilMenu
import net.minecraft.world.inventory.ContainerLevelAccess
import net.minecraft.world.inventory.MenuType
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer
import org.bukkit.entity.Player

class Anvil(private val player: Player) {
    val menu = createContainer(player)
    val syncId = menu.containerId

    private fun createContainer(player: Player): AnvilMenu {
        player as CraftPlayer
        val world = player.handle.serverLevel()
        val containerAccess: ContainerLevelAccess = ContainerLevelAccess.create(world, BlockPos.ZERO)
        val playerInventory: Inventory = player.handle.inventory
        val container = AnvilMenu(player.handle.nextContainerCounter(), playerInventory, containerAccess)
        container.checkReachable = false
        return container
    }

    fun open() {
        player as CraftPlayer
        player.closeInventory()
        player.handle.containerMenu = player.handle.inventoryMenu
        val openPacket = ClientboundOpenScreenPacket(syncId, MenuType.ANVIL, Component.empty())
        player.handle.connection.send(openPacket, null)
        player.handle.containerMenu = menu
        player.handle.initMenu(menu)
    }
}