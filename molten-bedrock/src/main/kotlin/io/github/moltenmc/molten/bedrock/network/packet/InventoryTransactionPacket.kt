package io.github.moltenmc.molten.bedrock.network.packet

import io.github.moltenmc.molten.common.network.LazyPacketView

class InventoryTransactionPacket(
    override val packetId: Int,
    val view: LazyPacketView,
) : BedrockPacket
