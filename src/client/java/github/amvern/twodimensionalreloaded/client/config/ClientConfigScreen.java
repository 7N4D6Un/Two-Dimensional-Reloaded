package github.amvern.twodimensionalreloaded.client.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ClientConfigScreen {

    public static Screen create(Screen parent, ClientConfig config) {
        ConfigBuilder builder = ConfigBuilder.create()
            .setParentScreen(parent)
            .setTitle(Component.translatable("config.twodimensionalreloaded.title"));

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        ConfigCategory generalSettings = builder.getOrCreateCategory(Component.translatable("config.twodimensionalreloaded.category.general"));
        SubCategoryBuilder blockPlacementGuideOptions = entryBuilder.startSubCategory(Component.translatable("config.twodimensionalreloaded.subcategory.block_placement_guide"));

        generalSettings.addEntry(entryBuilder.startTextDescription(Component.translatable("config.twodimensionalreloaded.description.camera_follows_mouse")).build());
        generalSettings.addEntry(entryBuilder.startEnumSelector(Component.translatable("config.twodimensionalreloaded.option.camera_mode"),
            ClientConfig.CameraMode.class, config.cameraMode)
            .setDefaultValue(ClientConfig.CameraMode.DYNAMIC)
            .setSaveConsumer(value ->config.cameraMode = value)
            .setEnumNameProvider(style -> Component.translatable("config.twodimensionalreloaded.enum.camera_mode." + style.name().toLowerCase()))
            .build()
        );

        blockPlacementGuideOptions.add(entryBuilder.startBooleanToggle(Component.translatable("config.twodimensionalreloaded.option.render_block_placement_guide"), config.renderBlockPlacementGuide)
            .setDefaultValue(false)
            .setSaveConsumer(value -> config.renderBlockPlacementGuide = value)
            .setTooltip(Component.translatable("config.twodimensionalreloaded.tooltip.render_placement_preview"))
            .build()
        );

        blockPlacementGuideOptions.add(entryBuilder.startEnumSelector(
            Component.translatable("config.twodimensionalreloaded.option.placement_preview_style"),
                ClientConfig.RenderStyle.class,
                config.blockRenderMode
            ).setDefaultValue(ClientConfig.RenderStyle.GHOST_BLOCK)
            .setSaveConsumer(value -> config.blockRenderMode = value)
            .setEnumNameProvider(style -> Component.translatable("config.twodimensionalreloaded.enum.render_style." + style.name().toLowerCase()))
            .build()
        );

        blockPlacementGuideOptions.add(entryBuilder.startIntSlider(
            Component.translatable("config.twodimensionalreloaded.option.ghost_transparency"),
            (int)(config.blockAlphaValue * 10),
            0,
            10)
            .setSaveConsumer(value -> config.blockAlphaValue = value / 10f)
            .setDefaultValue((int)(0.5f * 10))
            .setTextGetter(value -> Component.literal(String.format("%.1f", value / 10f)))
            .build()
        );

        blockPlacementGuideOptions.add(entryBuilder.startBooleanToggle(
            Component.translatable("config.twodimensionalreloaded.option.show_outline"),
                config.shouldRenderPlacementOutline
            ).setDefaultValue(true)
            .setSaveConsumer(value -> config.shouldRenderPlacementOutline = value)
            .setTooltip(Component.translatable("config.twodimensionalreloaded.tooltip.render_outline"))
            .build()
        );

        blockPlacementGuideOptions.add(entryBuilder.startEnumSelector(
            Component.translatable("config.twodimensionalreloaded.option.placeable_color"),
                ClientConfig.PlacementPreviewColors.class,
                config.placeableColorEnum
            ).setSaveConsumer(value -> config.placeableColorEnum = value)
            .setDefaultValue(ClientConfig.PlacementPreviewColors.GREEN)
            .setEnumNameProvider(c -> Component.translatable("config.twodimensionalreloaded.enum.color." + c.name().toLowerCase()))
            .build()
        );

        blockPlacementGuideOptions.add(entryBuilder.startEnumSelector(
            Component.translatable("config.twodimensionalreloaded.option.non_placeable_color"),
                ClientConfig.PlacementPreviewColors.class,
                config.nonPlaceableColorEnum
            ).setSaveConsumer(value -> config.nonPlaceableColorEnum = value)
            .setDefaultValue(ClientConfig.PlacementPreviewColors.RED)
            .setEnumNameProvider(c -> Component.translatable("config.twodimensionalreloaded.enum.color." + c.name().toLowerCase()))
            .build()
        );

        blockPlacementGuideOptions.add(entryBuilder.startIntSlider(
            Component.translatable("config.twodimensionalreloaded.option.outline_width"),
            (int)(config.placementOutlineWidth * 10),
            (1 * 10),
            (8 * 10))
            .setSaveConsumer(value -> config.placementOutlineWidth = value / 10f)
            .setDefaultValue((int)(4f * 10))
            .setTextGetter(value -> Component.literal(String.format("%.1f", value / 10f)))
            .build()
        );

        blockPlacementGuideOptions.add(entryBuilder.startIntSlider(
            Component.translatable("config.twodimensionalreloaded.option.outline_transparency"),
            (int)(config.outlineAlphaValue * 10),
            0,
            10)
            .setSaveConsumer(value -> config.outlineAlphaValue = value / 10f)
            .setTextGetter(value -> Component.literal(String.format("%.1f", value / 10f)))
            .setDefaultValue((int)(0.5f * 10))
            .build()
        );

        generalSettings.addEntry(entryBuilder.startTextDescription(Component.translatable("config.twodimensionalreloaded.description.fog_environments")).build());
        generalSettings.addEntry(entryBuilder.startBooleanToggle(
            Component.translatable("config.twodimensionalreloaded.option.render_fog"),
            config.renderFogEnvironments)
            .setDefaultValue(true)
            .setSaveConsumer(value -> config.renderFogEnvironments = value)
            .build()
        );

        generalSettings.addEntry(blockPlacementGuideOptions.build());

        builder.setSavingRunnable(()-> {
            AutoConfig.getConfigHolder(ClientConfig.class).save();
        });

        return builder.build();
    }
}