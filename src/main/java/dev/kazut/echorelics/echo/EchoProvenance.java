package dev.kazut.echorelics.echo;

public enum EchoProvenance {
    PLAYER_RECORDED(EchoAlignment.PLAYER, true),
    HOSTILE_RECORDED(EchoAlignment.HOSTILE, true),
    DEVICE_RECORDED(EchoAlignment.NEUTRAL, false),
    HOSTILE_COPY_OF_PLAYER(EchoAlignment.HOSTILE, false),
    PLAYER_COPY_OF_HOSTILE(EchoAlignment.PLAYER, false);

    private final EchoAlignment alignment;
    private final boolean copyable;

    EchoProvenance(EchoAlignment alignment, boolean copyable) {
        this.alignment = alignment;
        this.copyable = copyable;
    }

    public EchoAlignment alignment() {
        return alignment;
    }

    public boolean copyable() {
        return copyable;
    }
}
