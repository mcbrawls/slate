package net.mcbrawls.slate.test

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.player.UseItemCallback
import net.mcbrawls.slate.InventorySlate
import net.mcbrawls.slate.Slate.Companion.slate
import net.mcbrawls.slate.SlatePlayer
import net.mcbrawls.slate.screen.slot.ClickType
import net.mcbrawls.slate.tile.StackTile
import net.mcbrawls.slate.tile.Tile.Companion.tile
import net.mcbrawls.slate.tile.TileGrid
import net.mcbrawls.slate.tooltip.TooltipChunk.Companion.tooltipChunk
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.BundleContentsComponent
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.registry.Registries
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Formatting
import net.minecraft.util.math.random.Random

class SlateTest : ModInitializer {
    override fun onInitialize() {
        UseItemCallback.EVENT.register { player, _, hand ->
            if (player.currentScreenHandler != player.playerScreenHandler) {
                return@register ActionResult.PASS
            }

            val stack = player.getStackInHand(hand)

            if (stack.isOf(Items.STICK)) {
                val slatePlayer = player as SlatePlayer
                val slate = slate {
                    title = Text.literal("Slate Innit")

                    layer(4, 3, 2) {
                        for (i in 0 until size) {
                            tiles[i] = tile(Items.STONE) {
                                tooltip(Text.literal("Layer tile at $i"))
                            }
                        }

                        callbacks {
                            onTick { _, _, player ->
                                if (player.age % 20 == 0) {
                                    val maybeItemRef = Registries.ITEM.getRandom(Random.create())
                                    maybeItemRef.ifPresent { ref ->
                                        val item = ref.value()
                                        val tile = tiles[0, 1]
                                        if (tile is StackTile) {
                                            tile.stack = ItemStack(item)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    tiles[0, 0] = tile()
                    tiles[1, 0] = tile(Items.WHITE_WOOL)

                    // bound tests
                    tiles.setInventory(0, 0, tile(Items.ORANGE_WOOL))
                    tiles[tiles.width - 1, 0] = tile(Items.RED_WOOL)
                    tiles[0, tiles.height - 1] = tile(Items.RED_WOOL)
                    tiles[tiles.lastIndex] = tile(Items.RED_WOOL)

                    // center and log tests
                    val (centerX, centerY) = tiles.width / 2 to tiles.height / 2
                    tiles[centerX, centerY] = tile(Items.SAND) {
                        tooltip(Text.literal("Click me!"))

                        onClick { slate, tile, context ->
                            context.player.sendMessage(Text.literal("$slate, $tile"))
                        }
                    }

                    // open subslate test
                    tiles[centerX, centerY + 1] = tile(
                        ItemStack(Items.BLUE_BUNDLE).apply {
                            set(DataComponentTypes.BUNDLE_CONTENTS, BundleContentsComponent(
                                listOf(
                                    ItemStack(Items.STONE),
                                    ItemStack(Items.STONE),
                                    ItemStack(Items.GRASS_BLOCK),
                                    ItemStack(Items.STONE),
                                    ItemStack(Items.STONE),
                                    ItemStack(Items.STONE),
                                    ItemStack(Items.STONE),
                                    ItemStack(Items.STONE),
                                )
                            ))
                        }
                    ) {
                        tooltip(Text.literal("Open a subslate"))

                        onClick(ClickType.THROW) { _, _, context ->
                            println("${context.button}")
                        }

                        onClick { _, _, context ->
                            val subslate = subslate {
                                title = Text.literal("Subslate")

                                tiles[0, 7] = tile(Items.WOODEN_HOE)

                                // on click open parent test
                                tiles[0, 8] = tile(Items.ARROW) {
                                    tooltip(Text.literal("Back (Open this slate's parent)"))
                                    onClick { slate, _, context ->
                                        // open parent slate
                                        slate.openParent(context.player)
                                    }
                                }

                                callbacks {
                                    onOpen { _, _ ->
                                        println("Opened sub")
                                    }

                                    onClose { _, _ ->
                                        println("Closed sub")
                                    }
                                }
                            }

                            subslate.open(context.player)
                        }
                    }

                    // open subslate test
                    tiles[centerX - 1, centerY + 1] = tile(Items.CHEST) {
                        tooltip(Text.literal("Open an inventory subslate"))

                        onClick { _, _, context ->
                            val subslate = subslate(::InventorySlate) {
                                title = Text.literal("Inventory Subslate")

                                for (i in 0 until tiles.size) {
                                    tiles[i] = tile(Items.POPPED_CHORUS_FRUIT) {
                                        onClick(ClickType.LEFT) { _, _, _ ->
                                            println("Inventory left click")
                                        }

                                        onClick(ClickType.RIGHT) { _, _, _ ->
                                            println("Inventory right click")
                                        }
                                    }
                                }

                                tiles.setHotbar(8, tile(Items.ECHO_SHARD) {
                                    tooltip(Text.literal("Back (Reopen main slate)"))

                                    // TODO shortcut way of handling this?
                                    onClick(ClickType.RIGHT) { slate, _, context ->
                                        if (!context.withinScreen) {
                                            slate.openParent(context.player)
                                        }
                                    }

                                    onClick(ClickType.LEFT) { slate, _, context ->
                                        if (context.withinScreen) {
                                            slate.openParent(context.player)
                                        }
                                    }

                                    // END TODO

                                    onClick(ClickType.THROW) { _, _, context ->
                                        println(context)
                                    }
                                })

                                callbacks {
                                    onOpen { _, _ ->
                                        println("Opened inventory")
                                    }

                                    onTick { _, player ->
                                        player.sendMessage(Text.literal("Ticking inventory GUI"), true)
                                    }

                                    onClose { _, _ ->
                                        println("Closed inventory")
                                        player.sendMessage(Text.empty(), true)
                                    }
                                }
                            }

                            subslate.open(context.player)
                        }
                    }

                    // open subslate test
                    tiles[centerX + 1, centerY + 1] = tile(Items.ANVIL) {
                        tooltip("This tile:")

                        tooltip(
                            tooltipChunk("Open an anvil subslate"),
                            tooltipChunk(
                                "Hello",
                                "These are tooltips"
                            ) {
                                styled {
                                    withFormatting(Formatting.GREEN)
                                }
                            },
                            tooltipChunk(
                                "Hello Again!",
                                "These are more tooltips"
                            ) {
                                styled {
                                    withFormatting(Formatting.RED)
                                }
                            },
                        )

                        onClick { _, _, context ->
                            val subslate = subslate {
                                tiles = TileGrid.create(ScreenHandlerType.ANVIL)
                                title = Text.literal("Anvil Subslate")

                                // on click open parent test
                                tiles[0] = tile(Items.ARROW) {
                                    tooltip(Text.literal("Back (Open this slate's parent)"))
                                    onClick { slate, _, context ->
                                        // open parent slate
                                        slate.openParent(context.player)
                                    }
                                }

                                // bounds tests
                                tiles.setInventory(0, 0, tile(Items.ORANGE_WOOL))
                                tiles[tiles.lastIndex] = tile(Items.RED_WOOL)

                                callbacks {
                                    onInput { _, _, input ->
                                        println("Input: $input")
                                    }
                                }
                            }

                            subslate.open(context.player)
                        }
                    }

                    // on click close test
                    tiles[centerX, centerY + 2] = tile(Items.ARROW) {
                        tooltip(Text.literal("Exit this slate"))
                        onClick { slate, _, context ->
                            slate.close(context.player)
                        }
                    }

                    callbacks {
                        onOpen { slate, player ->
                            println("Opened")
                        }

                        onTick { slate, player ->
                            val age = player.age
                            if (age % 10 == 0) {
                                player.sendMessage(Text.literal("Ticked $age"), true)

                                val maybeItemRef = Registries.ITEM.getRandom(Random.create())
                                maybeItemRef.ifPresent { ref ->
                                    val item = ref.value()
                                    val tile = slate.tiles[0, 0]
                                    if (tile is StackTile) {
                                        tile.stack = ItemStack(item)
                                    }
                                }
                            }
                        }

                        onClose { slate, player ->
                            println("Closed")
                        }

                        onChildClose { childSlate, player ->
                            println("Child closed: $childSlate")

                            // ensure that the parent is always opened
                            openSoon(player)
                        }
                    }
                }

                slatePlayer.openSlate(slate)

                ActionResult.SUCCESS_SERVER
            } else {
                ActionResult.PASS
            }
        }
    }
}
