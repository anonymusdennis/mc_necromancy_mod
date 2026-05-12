/**
 * Modular multipart mob architecture: one authoritative {@link com.sirolf2009.necromancy.multipart.RootMobEntity}
 * owns a {@link com.sirolf2009.necromancy.multipart.TransformHierarchy} of {@link com.sirolf2009.necromancy.multipart.part.BodyPartNode}
 * instances. Intended as an ECS-inspired boundary—composition over inheritance—while staying compatible with
 * vanilla {@link net.minecraft.world.entity.LivingEntity} networking (authority on server, transforms derived locally
 * where possible).
 *
 * <p>Subsystems: {@code multipart.math} (TRS + compose), {@code multipart.collision} (OBB narrow-phase / broad-phase),
 * {@code multipart.damage} (per-part HP / armor / routing), {@code multipart.interpolation} (render smoothing hooks),
 * {@code multipart.network} (revision-based payloads), {@code multipart.debug} (queries for wireframe overlays).
 *
 * <p><strong>Wiring:</strong> {@link com.sirolf2009.necromancy.entity.EntityMinion} implements {@link com.sirolf2009.necromancy.multipart.RootMobEntity}
 * with a {@link com.sirolf2009.necromancy.bodypart.MinionSkeletonBinder}-filled hierarchy unless {@link com.sirolf2009.necromancy.NecromancyConfig#MINION_LEGACY_COMPOSITE_COLLISION}
 * forces altar-era {@link com.sirolf2009.necromancy.bodypart.MinionCompositeCollision}. LAN multipart topology deltas will arrive once hierarchy identities stabilize.</p>
 */
package com.sirolf2009.necromancy.multipart;
