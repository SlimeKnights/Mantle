package mantle.utils;

import static mantle.lib.CoreRepo.logger;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import cpw.mods.fml.common.registry.GameRegistry;
import mantle.lib.CoreConfig;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CraftingHandler {
    private static boolean ia1NG;

    public static void logConflicts () {

        //GameRegistry.addShapelessRecipe(new ItemStack(Items.diamond_hoe), Blocks.planks, Items.stick, Items.chainmail_boots);
        //GameRegistry.addShapelessRecipe(new ItemStack(Items.apple), Blocks.planks, Items.stick, Items.chainmail_boots);
        if (CoreConfig.dumpRecipeConflicts) {
            for (Map.Entry<IRecipe, List<IRecipe>> me : getPotentialConflicts().entrySet()) {
                String s = "";
                for (IRecipe i : me.getValue()) {
                    s += print(i);
                }
                logger.error("found conflict: \n" + s);
            }
        }
    }

    public static Map<IRecipe, List<IRecipe>> getPotentialConflicts () {
        logger.info("dumping recipe conflicts to log this can take a while");
        Map<IRecipe, List<IRecipe>> conflicts = Maps.newHashMap();
        CraftingManager inst = CraftingManager.getInstance();
        List<IRecipe> recipes = inst.getRecipeList();
        for (IRecipe i : recipes) {
            for (int r = 0; r < recipes.size(); r++) {
                IRecipe rc = recipes.get(r);
                if (i != null && rc != null) {
                    boolean comp = compare(i, rc);
                    if (comp && !areItemStacksEqual(i.getRecipeOutput(), rc.getRecipeOutput())) {
                        if (conflicts.containsKey(i)) {
                            List<IRecipe> l = conflicts.get(i);
                            conflicts.remove(i);
                            if (!l.contains(r)) {
                                l.add(recipes.get(r));
                            }
                            conflicts.put(i, l);
                        } else if (conflicts.containsKey(rc)) {
                            List<IRecipe> l = conflicts.get(rc);
                            conflicts.remove(rc);
                            if (!l.contains(i)) {
                                l.add(i);
                            }
                            conflicts.put(rc, l);
                        } else {
                            List<IRecipe> l = Lists.newArrayList(i, rc);
                            conflicts.put(i, l);
                        }
                        if (ia1NG) {
                            ia1NG = false;
                            break;
                        }
                    }
                }
            }
        }
        return conflicts;
    }

    private static boolean compare (IRecipe i1, IRecipe i2) {
        if (areItemStacksEqual(i1.getRecipeOutput(), i2.getRecipeOutput())) {
            return false;
        }
        boolean is1l = false;
        boolean is2l = false;
        List<ItemStack> il1 = Lists.newArrayList();
        List<ItemStack> il2 = Lists.newArrayList();
        ItemStack[] ia1 = null;
        boolean invalid = false;
        ItemStack[] ia2 = null;
        if (i1 instanceof ShapedRecipes) {
            ia1 = ((ShapedRecipes) i1).recipeItems.clone();
        } else if (i1 instanceof ShapelessRecipes) {
            il1 = new ArrayList<ItemStack>(((ShapelessRecipes) i1).recipeItems);
            //logger.error("SL Recipe: \n" + print(i1));
            is1l = true;
        } else if (i1 instanceof ShapedOreRecipe) {
            Object[] o = ((ShapedOreRecipe) i1).getInput();
            ia1 = new ItemStack[o.length];
            for (int in = 0; in < o.length; in++) {
                if (o[in] != null && o[in] instanceof ItemStack) {
                    ia1[in] = (ItemStack) o[in];
                } else if (o[in] != null) {
                    ia1[in] = ((ArrayList<ItemStack>) o[in]).get(0);
                } else {
                    ia1[in] = null;
                }
            }
        } else if (i1 instanceof ShapelessOreRecipe) {
            il1 = new ArrayList<ItemStack>();
            for (Object o : ((ShapelessOreRecipe) i1).getInput()) {
                if (o != null && o instanceof ItemStack) {
                    ItemStack is = (ItemStack) o;
                    il1.add(is);
                } else if (o != null) {
                    ItemStack is = ((ArrayList<ItemStack>) o).get(0);
                    il1.add(is);
                } else {
                    il1.add(null);
                }
            }
            is1l = true;
        } else {
            ia1NG = true;
            invalid = true;
        }
        if (i2 instanceof ShapedRecipes) {
            ia2 = ((ShapedRecipes) i2).recipeItems.clone();
        } else if (i2 instanceof ShapelessRecipes) {
            il2 = new ArrayList<ItemStack>(((ShapelessRecipes) i2).recipeItems);
            is2l = true;
        } else if (i2 instanceof ShapedOreRecipe) {
            Object[] o = ((ShapedOreRecipe) i2).getInput();
            ia2 = new ItemStack[o.length];
            for (int in = 0; in < o.length; in++) {
                if (o.length == 6) {
                    print(i2);
                }
                if (o[in] != null && o[in] instanceof ItemStack) {
                    ia2[in] = (ItemStack) o[in];
                } else if (o[in] != null) {
                    ItemStack is = ((ArrayList<ItemStack>) o[in]).get(0);
                    ia2[in] = is;
                } else {
                    ia2[in] = null;
                }
            }
        } else if (i2 instanceof ShapelessOreRecipe) {
            il2 = new ArrayList<ItemStack>();
            for (Object o : ((ShapelessOreRecipe) i2).getInput()) {
                if (o != null && o instanceof ItemStack) {
                    ItemStack is = (ItemStack) o;
                    il2.add(is);
                } else if (o != null) {
                    ItemStack is = ((ArrayList<ItemStack>) o).get(0);
                    il2.add(is);
                } else {
                    il2.add(null);
                }
            }
            is2l = true;
        } else {
            invalid = true;
        }
        if (!invalid) {
            if (!is1l && !is2l) {
                if (ia1 == null) {
                    logger.error("ia1 null");
                }
                if (ia2 == null) {
                    logger.error("ia2 null");
                }
                if (ia1.length != ia2.length) {
                    return false;
                }
                for (int i = 0; i < ia1.length; i++) {
                    if (ia1[i] != null && ia2[i] != null) {
                        if (!areItemStacksEqual(ia1[i], ia2[i])) {
                            return false;
                        }
                    } else {
                        return false;
                    }
                }
            } else if (is1l && is2l) {
                if (il1.size() == il2.size()) {
                    boolean has = false;
                    for (int i = 0; i < il1.size(); i++) {
                        ItemStack ti1 = il1.get(i);
                        for (int j = 0; j < il2.size(); j++) {
                            if (areItemStacksEqual(ti1, il2.get(j))) {
                                il2.remove(j);
                                has = true;
                                break;
                            }
                        }
                    }
                    if (!has) {
                        return false;
                    }
                    if (il2.size() != 0) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                List<ItemStack> stacks = is1l ? il1 : il2;
                ItemStack[] stcks = is2l ? ia1 : ia2;
                for (ItemStack i : stcks) {
                    if (!stacks.contains(i)) {
                        return false;
                    } else {
                        stacks.remove(i);
                    }
                }
            }
        } else {
            return false;
        }
        if (i1 instanceof ShapedOreRecipe && i2 instanceof ShapedOreRecipe) {
            int i1w, i2w, i1h, i2h;
            try {
                Class cl = ShapedOreRecipe.class;
                Field w = cl.getDeclaredField("width");
                w.setAccessible(true);
                Field h = cl.getDeclaredField("height");
                h.setAccessible(true);
                i1w = (Integer) w.get((ShapedOreRecipe) i1);
                i2w = (Integer) w.get((ShapedOreRecipe) i2);
                i1h = (Integer) h.get((ShapedOreRecipe) i1);
                i2h = (Integer) h.get((ShapedOreRecipe) i2);
                if (i1w != i2w || i1h != i2h) {
                    return false;
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        //logger.error("CONFLICT SET: \n" + print(i1) + print(i2));
        return true;
    }

    private static String print (IRecipe i1) {
        String out = "";
        if (i1 instanceof ShapedRecipes) {
            out += "SHAPED " + toStr(i1.getRecipeOutput()) + "\n";
            for (ItemStack i : ((ShapedRecipes) i1).recipeItems) {
                if (i != null && i.getItem() != null) {
                    out += toStr((ItemStack) i) + "\n";
                } else {
                    out += "null \n";
                }
            }

        } else if (i1 instanceof ShapelessRecipes) {
            out += "SHAPELESS " + toStr(i1.getRecipeOutput()) + "\n";
            for (Object i : ((ShapelessRecipes) i1).recipeItems) {
                if ((ItemStack) i != null && ((ItemStack) i).getItem() != null) {
                    out += toStr((ItemStack) i) + "\n";
                } else {
                    out += "null \n";
                }
            }
        } else if (i1 instanceof ShapedOreRecipe) {
            out += "SHAPED ORE  " + toStr(i1.getRecipeOutput()) + "\n";
            Object[] o = ((ShapedOreRecipe) i1).getInput();
            ItemStack[] ia1 = new ItemStack[o.length];
            for (int in = 0; in < o.length; in++) {
                if (o[in] != null && o[in] instanceof ItemStack) {
                    ia1[in] = (ItemStack) o[in];
                } else if (o[in] != null) {
                    ia1[in] = ((ArrayList<ItemStack>) o[in]).get(0);
                } else {
                    ia1[in] = null;
                }
            }

            for (Object i : ia1) {
                if (i != null && ((ItemStack) i).getItem() != null) {
                    out += toStr((ItemStack) i) + "\n";
                } else {
                    out += "null \n";
                }
            }
        } else if (i1 instanceof ShapelessOreRecipe) {
            out += "SHAPELESS ORE " + toStr(i1.getRecipeOutput()) + "\n";
            ItemStack i;
            for (Object ob : ((ShapelessOreRecipe) i1).getInput()) {
                if (ob instanceof ItemStack) {
                    i = (ItemStack) ob;
                } else {
                    i = ((ArrayList<ItemStack>) ob).get(0);
                }
                if ((ItemStack) i != null && ((ItemStack) i).getItem() != null) {
                    out += toStr((ItemStack) i) + "\n";
                } else {
                    out += "null \n";
                }
            }
        }
        return out;
    }

    private static String toStr (ItemStack is) {
        return GameRegistry.findUniqueIdentifierFor(((ItemStack) is).getItem()) + " " + ((ItemStack) is).toString();
    }

    public static boolean areItemStacksEqual (ItemStack a, ItemStack b) {
        if ((a == null || b == null) && !(a == null && b == null)) {
            return false;
        }
        if ((a == null && b == null)) {
            return true;
        }
        return a.stackSize != b.stackSize
                ? false
                : (a.getItem() != b.getItem()
                           ? false
                           : (compdamage(a, b)
                                      ? false
                                      : (a.stackTagCompound == null && b.stackTagCompound != null ? false : a.stackTagCompound == null || a.stackTagCompound.equals(b.stackTagCompound))));
    }

    public static boolean compdamage (ItemStack a, ItemStack b) {
        return a.getItemDamage() != b.getItemDamage() || a.getItemDamage() == OreDictionary.WILDCARD_VALUE || b.getItemDamage() == OreDictionary.WILDCARD_VALUE;
    }
}