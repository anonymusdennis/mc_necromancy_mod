package com.sirolf2009.necromancy.bodypart;

@SuppressWarnings("unused")
public final class BodypartFlagsJson {
    public boolean head;
    public boolean arm;
    public boolean leg;
    public boolean torso;
    public boolean special;

    public BodypartFlagsJson() {}

    public BodypartFlagsJson(boolean head, boolean arm, boolean leg, boolean torso, boolean special) {
        this.head = head;
        this.arm = arm;
        this.leg = leg;
        this.torso = torso;
        this.special = special;
    }

    public BodypartFlagsJson copy() {
        return new BodypartFlagsJson(head, arm, leg, torso, special);
    }
}
