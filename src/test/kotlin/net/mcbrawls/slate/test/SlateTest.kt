package net.mcbrawls.slate.test

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.player.UseItemCallback
import net.mcbrawls.slate.Slate
import net.mcbrawls.slate.Slate.Companion.slate
import net.mcbrawls.slate.SlatePlayer
import net.mcbrawls.slate.tile.Tile.Companion.tile
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.BundleContentsComponent
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.registry.Registries
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.math.random.Random

class SlateTest : ModInitializer {
    override fun onInitialize() {
        UseItemCallback.EVENT.register { player, _, hand ->
            val stack = player.getStackInHand(hand)

            if (stack.isOf(Items.STICK)) {
                val slatePlayer = player as SlatePlayer
                val slate = slate {
                    title = Text.literal("Slate Innit")

                    tiles[0, 0] = tile()
                    tiles[1, 0] = tile(ItemStack(Items.WHITE_WOOL))

                    // bound tests
                    tiles[tiles.width - 1, 0] = tile(ItemStack(Items.RED_WOOL))
                    tiles[0, tiles.height - 1] = tile(ItemStack(Items.RED_WOOL))
                    tiles[tiles.lastIndex] = tile(ItemStack(Items.RED_WOOL))

                    // center and log tests
                    val (centerX, centerY) = tiles.width / 2 to tiles.height / 2
                    tiles[centerX, centerY] = tile(ItemStack(Items.SAND)) {
                        tooltip(Text.literal("Click me and check the log!"))

                        onClick { slate, tile, _ ->
                            println("$slate, $tile")
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

                        onClick { _, _, context ->
                            val subslate = subslate {
                                title = Text.literal("Subslate")

                                tiles[0, 7] = tile(ItemStack(Items.WOODEN_HOE))

                                // on click open parent test
                                tiles[0, 8] = tile(ItemStack(Items.ARROW)) {
                                    tooltip(Text.literal("Back (Open this slate's parent)"))
                                    onClick { slate, _, context ->
                                        // open parent slate
                                        slate.parent?.also { parent ->
                                            Slate.openSlate(parent, context.player)
                                        }
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

                            Slate.openSlate(subslate, context.player)
                        }
                    }

                    // on click close test
                    tiles[centerX, centerY + 2] = tile(ItemStack(Items.ARROW)) {
                        tooltip(Text.literal("Exit this slate"))
                        onClick { slate, _, _ ->
                            slate.tryClose()
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
                                    tile?.stack = ItemStack(item)
                                }
                            }
                        }

                        onClose { slate, player ->
                            println("Closed")
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
