package io.myzticbean.finditemaddon.Handlers.GUIHandler;

import io.myzticbean.finditemaddon.FindItemAddOn;
import io.myzticbean.finditemaddon.Models.FoundShopItemModel;
import io.myzticbean.finditemaddon.Utils.LoggerUtils;
import me.kodysimpson.simpapi.colors.ColorTranslator;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.UUID;

public abstract class PaginatedMenu extends Menu {
    protected int page = 0;
    protected final int maxItemsPerPage = 45;
    protected int index = 0;

    protected ItemStack backButton;
    protected ItemStack firstPageButton;
    protected ItemStack nextButton;
    protected ItemStack lastPageButton;
    protected ItemStack closeInvButton;

    private final String BACK_BUTTON_SKIN_ID = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjZkYWI3MjcxZjRmZjA0ZDU0NDAyMTkwNjdhMTA5YjVjMGMxZDFlMDFlYzYwMmMwMDIwNDc2ZjdlYjYxMjE4MCJ9fX0=";
    private final String FIRST_PAGE_BUTTON_SKIN_ID = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTI5M2E2MDcwNTAzMTcyMDcxZjM1ZjU4YzgyMjA0ZTgxOGNkMDY1MTg2OTAxY2ExOWY3ZGFkYmRhYzE2NWU0NCJ9fX0=";
    private final String NEXT_BUTTON_SKIN_ID = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGFhMTg3ZmVkZTg4ZGUwMDJjYmQ5MzA1NzVlYjdiYTQ4ZDNiMWEwNmQ5NjFiZGM1MzU4MDA3NTBhZjc2NDkyNiJ9fX0=";
    private final String LAST_PAGE_BUTTON_SKIN_ID = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWRhNDRjNzY3Y2NhMjU4NjFkM2E1MmZlMTdjMjY0MjhlNjYwZWUyM2RjMGQ3OTNiZjdiZDg2ZWEyMDJmNzAzZCJ9fX0=";

    protected PaginatedMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
        initMaterialsForBottomBar();
    }

    protected PaginatedMenu(PlayerMenuUtility playerMenuUtility, List<FoundShopItemModel> searchResult) {
        super(playerMenuUtility);
        initMaterialsForBottomBar();
        super.playerMenuUtility.setPlayerShopSearchResult(searchResult);
    }

    private void initMaterialsForBottomBar() {
        createGUIBackButton();
        createGUIFirstPageButton();
        createGUINextButton();
        createGUILastPageButton();
        createGUICloseInvButton();
    }

    public void addMenuBottomBar() {
        inventory.setItem(45, backButton);
        inventory.setItem(46, firstPageButton);
        inventory.setItem(53, nextButton);
        inventory.setItem(52, lastPageButton);
        inventory.setItem(49, closeInvButton);

        inventory.setItem(47, super.GUI_FILLER_ITEM);
        inventory.setItem(48, super.GUI_FILLER_ITEM);
        inventory.setItem(50, super.GUI_FILLER_ITEM);
        inventory.setItem(51, super.GUI_FILLER_ITEM);
    }

    private void createGUIBackButton() {
        Material backButtonMaterial = Material.getMaterial(FindItemAddOn.getConfigProvider().SHOP_GUI_BACK_BUTTON_MATERIAL);
        if(backButtonMaterial == null)
            backButton = createPlayerHead(BACK_BUTTON_SKIN_ID);
        else
            backButton = new ItemStack(backButtonMaterial);
        ItemMeta backButtonMeta = backButton.getItemMeta();
        if(backButtonMeta != null && !StringUtils.isEmpty(FindItemAddOn.getConfigProvider().SHOP_GUI_BACK_BUTTON_TEXT))
            backButtonMeta.setDisplayName(ColorTranslator.translateColorCodes(FindItemAddOn.getConfigProvider().SHOP_GUI_BACK_BUTTON_TEXT));
        int backButtonCMD;
        try {
            if(backButtonMeta != null && !StringUtils.isEmpty(FindItemAddOn.getConfigProvider().SHOP_GUI_BACK_BUTTON_CMD)) {
                backButtonCMD = Integer.parseInt(FindItemAddOn.getConfigProvider().SHOP_GUI_BACK_BUTTON_CMD);
                backButtonMeta.setCustomModelData(backButtonCMD);
            }
        }
        catch (NumberFormatException e) {
            LoggerUtils.logDebugInfo("Invalid Custom Model Data for Back Button in config.yml");
        }
        backButton.setItemMeta(backButtonMeta);
    }

    private void createGUIFirstPageButton() {
        Material firstPageButtonMaterial = Material.getMaterial(FindItemAddOn.getConfigProvider().SHOP_GUI_GOTO_FIRST_PAGE_BUTTON_MATERIAL);
        if(firstPageButtonMaterial == null)
            firstPageButton = createPlayerHead(FIRST_PAGE_BUTTON_SKIN_ID);
        else
            firstPageButton = new ItemStack(firstPageButtonMaterial);
        ItemMeta firstPageButtonMeta = firstPageButton.getItemMeta();
        if(firstPageButtonMeta != null && !StringUtils.isEmpty(FindItemAddOn.getConfigProvider().SHOP_GUI_GOTO_FIRST_PAGE_BUTTON_TEXT))
            firstPageButtonMeta.setDisplayName(ColorTranslator.translateColorCodes(FindItemAddOn.getConfigProvider().SHOP_GUI_GOTO_FIRST_PAGE_BUTTON_TEXT));
        int firstPageButtonCMD;
        try {
            if(firstPageButtonMeta != null && !StringUtils.isEmpty(FindItemAddOn.getConfigProvider().SHOP_GUI_GOTO_FIRST_PAGE_BUTTON_CMD)) {
                firstPageButtonCMD = Integer.parseInt(FindItemAddOn.getConfigProvider().SHOP_GUI_GOTO_FIRST_PAGE_BUTTON_CMD);
                firstPageButtonMeta.setCustomModelData(firstPageButtonCMD);
            }
        }
        catch (NumberFormatException e) {
            LoggerUtils.logDebugInfo("Invalid Custom Model Data for Goto First Page Button in config.yml");
        }
        firstPageButton.setItemMeta(firstPageButtonMeta);
    }

    private void createGUILastPageButton() {
        Material lastPageButtonMaterial = Material.getMaterial(FindItemAddOn.getConfigProvider().SHOP_GUI_GOTO_LAST_PAGE_BUTTON_MATERIAL);
        if(lastPageButtonMaterial == null)
            lastPageButton = createPlayerHead(LAST_PAGE_BUTTON_SKIN_ID);
        else
            lastPageButton = new ItemStack(lastPageButtonMaterial);
        ItemMeta lastPageButtonMeta = lastPageButton.getItemMeta();
        if(lastPageButtonMeta != null && !StringUtils.isEmpty(FindItemAddOn.getConfigProvider().SHOP_GUI_GOTO_LAST_PAGE_BUTTON_TEXT))
            lastPageButtonMeta.setDisplayName(ColorTranslator.translateColorCodes(FindItemAddOn.getConfigProvider().SHOP_GUI_GOTO_LAST_PAGE_BUTTON_TEXT));
        int lastPageButtonCMD;
        try {
            if(lastPageButtonMeta != null && !StringUtils.isEmpty(FindItemAddOn.getConfigProvider().SHOP_GUI_GOTO_LAST_PAGE_BUTTON_CMD)) {
                lastPageButtonCMD = Integer.parseInt(FindItemAddOn.getConfigProvider().SHOP_GUI_GOTO_LAST_PAGE_BUTTON_CMD);
                lastPageButtonMeta.setCustomModelData(lastPageButtonCMD);
            }
        }
        catch (NumberFormatException e) {
            LoggerUtils.logDebugInfo("Invalid Custom Model Data for Goto Last Page Button in config.yml");
        }
        lastPageButton.setItemMeta(lastPageButtonMeta);
    }

    private void createGUINextButton() {
        Material nextButtonMaterial = Material.getMaterial(FindItemAddOn.getConfigProvider().SHOP_GUI_NEXT_BUTTON_MATERIAL);
        if(nextButtonMaterial == null)
            nextButton = createPlayerHead(NEXT_BUTTON_SKIN_ID);
        else
            nextButton = new ItemStack(nextButtonMaterial);
        ItemMeta nextButtonMeta = nextButton.getItemMeta();
        if(nextButtonMeta != null && !StringUtils.isEmpty(FindItemAddOn.getConfigProvider().SHOP_GUI_NEXT_BUTTON_TEXT)) {
            nextButtonMeta.setDisplayName(ColorTranslator.translateColorCodes(FindItemAddOn.getConfigProvider().SHOP_GUI_NEXT_BUTTON_TEXT));
        }
        int nextButtonCMD;
        try {
            if(nextButtonMeta != null && !StringUtils.isEmpty(FindItemAddOn.getConfigProvider().SHOP_GUI_NEXT_BUTTON_CMD)) {
                nextButtonCMD = Integer.parseInt(FindItemAddOn.getConfigProvider().SHOP_GUI_NEXT_BUTTON_CMD);
                nextButtonMeta.setCustomModelData(nextButtonCMD);
            }
        }
        catch (NumberFormatException e) {
            LoggerUtils.logDebugInfo("Invalid Custom Model Data for Next Button in config.yml");
        }
        nextButton.setItemMeta(nextButtonMeta);
    }

    private void createGUICloseInvButton() {
        Material closeInvButtonMaterial = Material.getMaterial(FindItemAddOn.getConfigProvider().SHOP_GUI_CLOSE_BUTTON_MATERIAL);
        if(closeInvButtonMaterial == null)
            closeInvButtonMaterial = Material.BARRIER;

        closeInvButton = new ItemStack(closeInvButtonMaterial);
        ItemMeta closeInvMeta = closeInvButton.getItemMeta();
        if(closeInvMeta != null && !StringUtils.isEmpty(FindItemAddOn.getConfigProvider().SHOP_GUI_CLOSE_BUTTON_TEXT)) {
            closeInvMeta.setDisplayName(ColorTranslator.translateColorCodes(FindItemAddOn.getConfigProvider().SHOP_GUI_CLOSE_BUTTON_TEXT));
        }
        int closeInvButtonCMD;
        try {
            if(closeInvMeta != null && !StringUtils.isEmpty(FindItemAddOn.getConfigProvider().SHOP_GUI_CLOSE_BUTTON_CMD)) {
                closeInvButtonCMD = Integer.parseInt(FindItemAddOn.getConfigProvider().SHOP_GUI_CLOSE_BUTTON_CMD);
                closeInvMeta.setCustomModelData(closeInvButtonCMD);
            }
        }
        catch (NumberFormatException e) {
            LoggerUtils.logDebugInfo("Invalid Custom Model Data for Close Button in config.yml");
        }
        closeInvButton.setItemMeta(closeInvMeta);
    }

    private ItemStack createPlayerHead(String value) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        UUID hashAsId = new UUID(value.hashCode(), value.hashCode());
        return Bukkit.getUnsafe().modifyItemStack(skull,
                "{SkullOwner:{Id:\"" + hashAsId + "\",Properties:{textures:[{Value:\"" + value + "\"}]}}}"
        );
    }
}
