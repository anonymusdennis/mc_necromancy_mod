/**
 * TRS math for multipart hierarchies.
 *
 * <p><strong>Ownership:</strong> {@link com.sirolf2009.necromancy.multipart.math.PartTransform} is mutated only by its
 * owning {@link com.sirolf2009.necromancy.multipart.part.BodyPartNode} (or a decoder that immediately assigns via
 * {@link PartTransform#set} / factories). {@link com.sirolf2009.necromancy.multipart.WorldPose} is immutable — compose
 * with {@link TransformCompose#composeInto} into a stack-local {@link com.sirolf2009.necromancy.multipart.WorldPose.Mutable}
 * when avoiding allocations.
 */
package com.sirolf2009.necromancy.multipart.math;
