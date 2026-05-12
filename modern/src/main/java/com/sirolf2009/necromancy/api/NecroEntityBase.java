package com.sirolf2009.necromancy.api;

import com.mojang.blaze3d.vertex.PoseStack;
import com.sirolf2009.necromancy.crafting.SewingRecipe;
import com.sirolf2009.necromancy.entity.EntityMinion;
import com.sirolf2009.necromancy.item.NecromancyItems;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for every "necro adapter" -- a description of one vanilla mob
 * (or custom mob) that can be assembled into a minion at the Summoning Altar.
 *
 * <p>This is a near line-by-line port of the legacy
 * {@code com.sirolf2009.necroapi.NecroEntityBase} class.  Field names, default
 * recipe shapes and constructor side effects are preserved so adapter classes
 * lifted from the original repository compile with minimal edits.
 *
 * <h2>Architecture</h2>
 * Each adapter:
 * <ul>
 *     <li>declares which body parts it has (head/torso/arms/legs flags),</li>
 *     <li>which {@link ItemStack}s map to those parts (filled in later by
 *         {@code RegistryNecromancyEntities}),</li>
 *     <li>which sewing recipe yields each part,</li>
 *     <li>which texture, scale and pose hints to use when rendering,</li>
 *     <li>how its attributes scale a freshly summoned {@link EntityMinion}.</li>
 * </ul>
 *
 * <p>The legacy {@code initRecipes()} hook still exists; it is invoked from
 * the constructor, just like in 1.7.10.
 */
public abstract class NecroEntityBase {

    // -- identity / texture --------------------------------------------
    public final String mobName;
    public ResourceLocation texture;
    public int textureWidth  = 64;
    public int textureHeight = 32;

    // -- which body parts are usable -----------------------------------
    public boolean hasHead  = true;
    public boolean hasTorso = true;
    public boolean hasArms  = true;
    public boolean hasLegs  = true;

    // -- representative items ------------------------------------------
    public ItemStack headItem  = ItemStack.EMPTY;
    public ItemStack torsoItem = ItemStack.EMPTY;
    public ItemStack armItem   = ItemStack.EMPTY;
    public ItemStack legItem   = ItemStack.EMPTY;

    // -- 4x4 sewing recipe shapes ("Object[]" matrices in the legacy mod)
    public Object[] headRecipe;
    public Object[] torsoRecipe;
    public Object[] armRecipe;
    public Object[] legRecipe;

    // -- compiled body part trees (assembled lazily on the client) -----
    public BodyPart[] head;
    public BodyPart[] torso;
    public BodyPart[] armLeft;
    public BodyPart[] armRight;
    public BodyPart[] legs;

    // -- saved skull item (used by Isaac, IronGolem etc.) --------------
    public ItemStack skullItem = ItemStack.EMPTY;

    /** Set by {@code Necromancy.java}; lets adapters skip work in dev test rigs. */
    protected boolean isNecromancyInstalled = true;

    private boolean modelInit;

    protected NecroEntityBase(String mobName) {
        this.mobName = mobName;
        // initRecipes runs after the subclass constructor has populated item
        // stacks, so we cannot call it from here -- callers (the registry)
        // will invoke initRecipes() once items are assigned.  Adapter
        // constructors typically call it themselves at the end.
    }

    // ------------------------------------------------------------------ --
    // Recipe hooks: subclasses fill in the {head,torso,arm,leg}Recipe arrays
    // ------------------------------------------------------------------ --

    /** Subclasses override and call {@link #initDefaultRecipes(Object...)}. */
    public void initRecipes() {}

    /**
     * Default 4x4 sewing recipes shared by most adapters.  The single-arg form
     * uses the same essence for every body part; the four-arg form assigns one
     * essence per part (head/torso/arm/leg).
     *
     * <p>Recipe shapes match the original mod 1:1.
     */
    public void initDefaultRecipes(Object... essences) {
        Object headEssence, torsoEssence, armEssence, legEssence;
        if (essences.length == 1) {
            headEssence = torsoEssence = armEssence = legEssence = essences[0];
        } else if (essences.length == 4) {
            headEssence  = essences[0];
            torsoEssence = essences[1];
            armEssence   = essences[2];
            legEssence   = essences[3];
        } else {
            throw new IllegalArgumentException("initDefaultRecipes wants 1 or 4 essences");
        }

        Item skin   = NecromancyItems.SKIN.get();
        Item brain  = NecromancyItems.BRAINS.get();
        Item heart  = NecromancyItems.HEART.get();
        Item lung   = NecromancyItems.LUNGS.get();
        Item muscle = NecromancyItems.MUSCLE.get();

        // Head: rows "SSSS / SBFS / SEES" (3 rows in original); padded to 4x4
        if (hasHead) {
            headRecipe = new Object[] {
                "SSSS",
                "SBFS",
                "SEES",
                'S', new ItemStack(skin),
                'B', new ItemStack(brain),
                'E', new ItemStack(net.minecraft.world.item.Items.SPIDER_EYE),
                'F', headEssence
            };
        }
        // Torso: " LL / BHUB / LEEL / BLLB"
        if (hasTorso) {
            torsoRecipe = new Object[] {
                " LL ",
                "BHUB",
                "LEEL",
                "BLLB",
                'L', new ItemStack(skin),
                'E', torsoEssence,
                'H', new ItemStack(heart),
                'U', new ItemStack(lung),
                'B', new ItemStack(net.minecraft.world.item.Items.BONE)
            };
        }
        // Arm: "LLLL / BMEB / LLLL"
        if (hasArms) {
            armRecipe = new Object[] {
                "LLLL",
                "BMEB",
                "LLLL",
                'L', new ItemStack(skin),
                'E', armEssence,
                'M', new ItemStack(muscle),
                'B', new ItemStack(net.minecraft.world.item.Items.BONE)
            };
        }
        // Legs: "LBBL / LMML / LEEL / LBBL"
        if (hasLegs) {
            legRecipe = new Object[] {
                "LBBL",
                "LMML",
                "LEEL",
                "LBBL",
                'L', new ItemStack(skin),
                'E', legEssence,
                'M', new ItemStack(muscle),
                'B', new ItemStack(net.minecraft.world.item.Items.BONE)
            };
        }
    }

    /**
     * Compiles the four legacy {@code Object[]} recipe arrays into modern
     * {@link SewingRecipe} entries.  Called by
     * {@link com.sirolf2009.necromancy.crafting.CraftingManagerSewing} during
     * common-setup, after every necro adapter has been constructed.
     */
    public List<SewingRecipe> buildRecipes() {
        List<SewingRecipe> out = new ArrayList<>();
        if (hasHead  && headRecipe  != null && !headItem.isEmpty()) {
            out.add(SewingRecipe.shaped(headRecipe,  headItem.copy()));
        }
        if (hasTorso && torsoRecipe != null && !torsoItem.isEmpty()) {
            out.add(SewingRecipe.shaped(torsoRecipe, torsoItem.copy()));
        }
        if (hasArms  && armRecipe   != null && !armItem.isEmpty()) {
            out.add(SewingRecipe.shaped(armRecipe,   armItem.copy()));
        }
        if (hasLegs  && legRecipe   != null && !legItem.isEmpty()) {
            out.add(SewingRecipe.shaped(legRecipe,   legItem.copy()));
        }
        return out;
    }

    // ------------------------------------------------------------------ --
    // Body-part declaration hooks (overridden client-side by adapters).
    // ------------------------------------------------------------------ --

    public BodyPart[] initHead()      { return new BodyPart[0]; }
    public BodyPart[] initTorso()     { return new BodyPart[0]; }
    public BodyPart[] initLegs()      { return new BodyPart[0]; }
    public BodyPart[] initArmLeft()   { return new BodyPart[0]; }
    public BodyPart[] initArmRight()  { return new BodyPart[0]; }

    /** Lazy-initialises the part arrays on first render. */
    public NecroEntityBase updateParts() {
        if (!modelInit) {
            head     = initHead();
            torso    = initTorso();
            armLeft  = initArmLeft();
            armRight = initArmRight();
            legs     = initLegs();
            modelInit = true;
        }
        return this;
    }

    // ------------------------------------------------------------------ --
    // Per-frame animation + transform hooks (override per adapter).
    //
    // These mirror the legacy {@code setRotationAngles}, {@code preRender}
    // and {@code postRender} hooks on {@code NecroEntityBase}.  The renderer
    // calls them in this exact sequence for each location, before the
    // {@link ModelPart} is drawn:
    // <pre>
    //   resetPoses(initial);     // restore pose so animations don't compound
    //   adapter.setAnim(...);    // tilt/swing each {@link ModelPart}
    //   pose.pushPose();
    //   adapter.preRender(...);  // additional matrix nudges (e.g. flip)
    //   modelPart.render(pose, buffer, light, overlay);
    //   adapter.postRender(...); // optional cleanup
    //   pose.popPose();
    // </pre>
    //
    // {@code parts} is the list of {@link ModelPart}s baked from this
    // adapter's {@link BodyPart}s -- one per body part in the same order.
    // {@code initialPoses} is the matching list of initial {@link
    // net.minecraft.client.model.geom.PartPose PartPose}s, useful for
    // animations that need to compose from rest.
    // ------------------------------------------------------------------ --

    /**
     * Apply per-frame rotations to the {@link ModelPart}s for the given
     * {@code location}.  Default: no animation.
     *
     * @param minion         the minion entity (may be {@code null} when
     *                       called from the altar preview)
     * @param parts          baked model parts (one per {@link BodyPart})
     * @param location       which slot is being animated
     * @param limbSwing      walk-cycle phase
     * @param limbSwingAmount walk speed (0 = standing)
     * @param ageInTicks     entity ageInTicks + partial tick
     * @param netHeadYaw     head yaw relative to body (degrees)
     * @param headPitch      head pitch (degrees)
     */
    public void setAnim(LivingEntity minion, ModelPart[] parts, BodyPartLocation location,
                        float limbSwing, float limbSwingAmount, float ageInTicks,
                        float netHeadYaw, float headPitch) {
        // base no-op
    }

    /**
     * Walk-cycle hook used when the minion is in arms-as-legs stance: the arm
     * geometry renders but should animate like legs.  Default pipes through
     * {@link #setAnim} with {@link BodyPartLocation#Legs}; overrides customize
     * hand-walking poses per mob.
     */
    public void setArmsAsLegsAnim(LivingEntity minion, ModelPart[] parts,
                                  float limbSwing, float limbSwingAmount, float ageInTicks,
                                  float netHeadYaw, float headPitch) {
        setAnim(minion, parts, BodyPartLocation.Legs, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    }

    /**
     * Apply additional matrix transforms before the body part is drawn.  Used
     * for things that cannot be expressed by per-{@link ModelPart} rotation
     * (e.g. quadrupeds flipping their saddle when the entity is sneaking).
     */
    public void preRender(LivingEntity minion, PoseStack pose, BodyPartLocation location) {
        // base no-op
    }

    /** Optional cleanup hook (called before {@code pose.popPose()}). */
    public void postRender(LivingEntity minion, PoseStack pose, BodyPartLocation location) {
        // base no-op
    }

    // ------------------------------------------------------------------ --
    // Locomotion + voice contracts (consumed by the v0.5 framework).
    //
    // Both default to "act like a zombie".  Override per adapter to give
    // each mob species its own walking feel (squid swims, rabbit hops,
    // creeper trots) and its own ambient/hurt/death voice.
    // ------------------------------------------------------------------ --

    /**
     * What kind of locomotion this adapter contributes when slotted as
     * the LEGS body part.  Only the legs slot's profile is used by the
     * runtime; adapters can still return a profile for other slots and it
     * will be ignored, which keeps the API symmetrical.
     */
    public LocomotionProfile locomotion() {
        return LocomotionProfile.WALK_DEFAULT;
    }

    /**
     * Sound profile (ambient / hurt / death) contributed by this adapter.
     * The minion picks the voice from the TORSO adapter first, then HEAD,
     * then falls back to the default zombie voice.
     */
    public VoiceProfile voice() {
        return VoiceProfile.ZOMBIE;
    }

    /**
     * Features ({@link com.sirolf2009.necromancy.api.feature.PartFeature})
     * this adapter contributes when slotted at {@code location}.
     *
     * <p>Default behaviour: every adapter that implements {@link ISaddleAble}
     * automatically gets {@link com.sirolf2009.necromancy.api.feature.SaddleFeature}
     * attached to its TORSO slot, so cow/pig/spider/squid torsos are
     * rideable out of the box without bespoke wiring.  Overrides can add or
     * replace features per-slot.
     */
    public java.util.List<com.sirolf2009.necromancy.api.feature.PartFeature> features(BodyPartLocation location) {
        if (location == BodyPartLocation.Torso && this instanceof ISaddleAble) {
            return java.util.List.of(com.sirolf2009.necromancy.api.feature.SaddleFeature.INSTANCE);
        }
        return java.util.List.of();
    }

    // ------------------------------------------------------------------ --
    // Attribute helpers (per body part) -- mirrors legacy setAttributes.
    // ------------------------------------------------------------------ --

    /**
     * Apply the per-body-part attribute scaling to a freshly summoned minion.
     *
     * <p>Subclasses override and typically call {@link #addAttributeMods} with
     * different numbers depending on {@code location}.  The default behaviour
     * (used when an adapter does not override) reproduces the old "default"
     * minion stat profile.
     */
    public void setAttributes(LivingEntity minion, BodyPartLocation location) {
        addAttributeMods(minion, "default", 2D, 1D, 0D, 2D, 2D);
    }

    /**
     * Adds attribute modifiers in the same shape as the original mod's helper.
     * Each non-zero argument adds a permanent {@link AttributeModifier} that
     * is multiplied with the base value
     * ({@link AttributeModifier.Operation#ADD_MULTIPLIED_BASE}).
     */
    protected void addAttributeMods(LivingEntity entity, String tag,
            double health, double followRange, double knockBackResistance,
            double movementSpeed, double attackDamage) {

        if (health != 0) {
            entity.getAttribute(Attributes.MAX_HEALTH).addPermanentModifier(modifier(tag + "_hp", health));
        }
        if (followRange != 0) {
            entity.getAttribute(Attributes.FOLLOW_RANGE).addPermanentModifier(modifier(tag + "_fr", followRange));
        }
        if (knockBackResistance != 0) {
            entity.getAttribute(Attributes.KNOCKBACK_RESISTANCE).addPermanentModifier(modifier(tag + "_kb", knockBackResistance));
        }
        if (movementSpeed != 0) {
            entity.getAttribute(Attributes.MOVEMENT_SPEED).addPermanentModifier(modifier(tag + "_ms", movementSpeed));
        }
        if (attackDamage != 0) {
            entity.getAttribute(Attributes.ATTACK_DAMAGE).addPermanentModifier(modifier(tag + "_dmg", attackDamage));
        }
    }

    private AttributeModifier modifier(String name, double amount) {
        // 1.21 ResourceLocation paths must match [a-z0-9/._-]; the legacy code
        // passed mixed-case tags ("Head", "ArmL", ...) so we lowercase here
        // and replace whitespace with underscores defensively.
        String safeMob  = mobName.toLowerCase().replace(' ', '_');
        String safeName = name.toLowerCase().replace(' ', '_');
        return new AttributeModifier(
            ResourceLocation.fromNamespaceAndPath(com.sirolf2009.necromancy.Reference.MOD_ID,
                "necro/" + safeMob + "/" + safeName),
            amount,
            AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    }

    // ------------------------------------------------------------------ --
    // Convenience -- coerce object recipe inputs to ingredients
    // ------------------------------------------------------------------ --

    /** Coerces "what was passed in" (Item / ItemStack / Ingredient) to {@link Ingredient}. */
    public static Ingredient asIngredient(Object o) {
        if (o instanceof Ingredient i) return i;
        if (o instanceof ItemStack s)  return Ingredient.of(s);
        if (o instanceof Item i)       return Ingredient.of(i);
        if (o instanceof net.minecraft.world.level.ItemLike lk) return Ingredient.of(lk);
        if (o == null)                 return Ingredient.EMPTY;
        throw new IllegalArgumentException("Cannot coerce " + o + " to Ingredient");
    }
}
