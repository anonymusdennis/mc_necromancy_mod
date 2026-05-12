package com.sirolf2009.necromancy.crafting;

import com.sirolf2009.necromancy.api.NecroEntityBase;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * One body-part recipe in the sewing machine.  Shape lives in a flat
 * {@link Ingredient}[] of size 4x4 (row-major); empty cells are
 * {@link Ingredient#EMPTY}.  Mirrored matching is handled by the manager.
 *
 * <p>Recipes are constructed programmatically from each
 * {@link NecroEntityBase}'s {@code Object[]} arrays (legacy shape spec) at
 * common-setup time -- there is no JSON serialisation, matching the legacy
 * mod's behaviour.
 */
public final class SewingRecipe {

    public static final int SIZE = 4;

    public final Ingredient[] grid; // length 16, row-major
    public final ItemStack    result;
    public final boolean      shapeless;

    private SewingRecipe(Ingredient[] grid, ItemStack result, boolean shapeless) {
        this.grid      = grid;
        this.result    = result;
        this.shapeless = shapeless;
    }

    /** Build a shaped 4x4 recipe from a legacy {@code Object[]} spec. */
    public static SewingRecipe shaped(Object[] spec, ItemStack result) {
        // Legacy convention: any leading String entries are the rows; remaining
        // entries are pairs of (Character, ingredient).
        int idx = 0;
        StringBuilder pattern = new StringBuilder();
        int width = 0;
        int height = 0;
        while (idx < spec.length && spec[idx] instanceof String row) {
            if (width == 0) width = row.length();
            else if (row.length() != width) {
                throw new IllegalArgumentException("Sewing recipe rows must be same width");
            }
            pattern.append(row);
            idx++;
            height++;
        }
        java.util.Map<Character, Ingredient> key = new java.util.HashMap<>();
        for (; idx < spec.length; idx += 2) {
            char ch = (Character) spec[idx];
            key.put(ch, NecroEntityBase.asIngredient(spec[idx + 1]));
        }
        Ingredient[] grid = new Ingredient[SIZE * SIZE];
        java.util.Arrays.fill(grid, Ingredient.EMPTY);
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                char ch = pattern.charAt(row * width + col);
                if (ch == ' ') continue;
                Ingredient ing = key.get(ch);
                if (ing == null) throw new IllegalArgumentException("Unknown key '" + ch + "' in recipe");
                grid[row * SIZE + col] = ing;
            }
        }
        return new SewingRecipe(grid, result, false);
    }

    /** Build a shapeless recipe (any layout, just count ingredients). */
    public static SewingRecipe shapeless(ItemStack result, Object... ingredients) {
        Ingredient[] grid = new Ingredient[SIZE * SIZE];
        java.util.Arrays.fill(grid, Ingredient.EMPTY);
        for (int i = 0; i < ingredients.length && i < grid.length; i++) {
            grid[i] = NecroEntityBase.asIngredient(ingredients[i]);
        }
        return new SewingRecipe(grid, result, true);
    }

    public boolean matches(Container input) {
        if (input.getContainerSize() < SIZE * SIZE) return false;
        if (shapeless) {
            return matchesShapeless(input);
        }
        return matchesShaped(input, false) || matchesShaped(input, true);
    }

    private boolean matchesShaped(Container input, boolean mirrored) {
        // Determine recipe bounding box.
        int minR = SIZE, minC = SIZE, maxR = -1, maxC = -1;
        for (int r = 0; r < SIZE; r++) for (int c = 0; c < SIZE; c++) {
            if (!grid[r * SIZE + c].isEmpty()) {
                if (r < minR) minR = r;
                if (c < minC) minC = c;
                if (r > maxR) maxR = r;
                if (c > maxC) maxC = c;
            }
        }
        if (maxR < 0) return false;
        int recipeH = maxR - minR + 1;
        int recipeW = maxC - minC + 1;

        // Slide recipe over the input and try each position.
        for (int rOff = 0; rOff <= SIZE - recipeH; rOff++) {
            for (int cOff = 0; cOff <= SIZE - recipeW; cOff++) {
                if (matchesAt(input, rOff, cOff, minR, minC, recipeH, recipeW, mirrored)) return true;
            }
        }
        return false;
    }

    private boolean matchesAt(Container input, int rOff, int cOff, int minR, int minC,
                              int recipeH, int recipeW, boolean mirrored) {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                ItemStack inputStack = input.getItem(r * SIZE + c);
                int rr = r - rOff, cc = c - cOff;
                Ingredient need;
                if (rr < 0 || cc < 0 || rr >= recipeH || cc >= recipeW) {
                    need = Ingredient.EMPTY;
                } else {
                    int srcR = minR + rr;
                    int srcC = minC + (mirrored ? (recipeW - 1 - cc) : cc);
                    need = grid[srcR * SIZE + srcC];
                }
                if (need.isEmpty()) {
                    if (!inputStack.isEmpty()) return false;
                } else {
                    if (inputStack.isEmpty() || !need.test(inputStack)) return false;
                }
            }
        }
        return true;
    }

    private boolean matchesShapeless(Container input) {
        java.util.List<Ingredient> required = new java.util.ArrayList<>();
        for (Ingredient ing : grid) if (!ing.isEmpty()) required.add(ing);

        java.util.List<ItemStack> available = new java.util.ArrayList<>();
        for (int i = 0; i < input.getContainerSize(); i++) {
            ItemStack s = input.getItem(i);
            if (!s.isEmpty()) available.add(s);
        }
        if (available.size() != required.size()) return false;

        outer:
        for (Ingredient ing : required) {
            for (java.util.Iterator<ItemStack> it = available.iterator(); it.hasNext();) {
                ItemStack s = it.next();
                if (ing.test(s)) {
                    it.remove();
                    continue outer;
                }
            }
            return false;
        }
        return true;
    }
}
