package eu.the5zig.mod.asm;

public class ReflSimpleTriple extends ReflSimpleTuple {

    @Override
    public String get() {
        return Transformer.FABRIC ? nameFabric : (Transformer.FORGE ? forge : notch);
    }

    private String nameFabric;

    public ReflSimpleTriple(String nameForge, String nameNotch, String nameFabric) {
        super(nameForge, nameNotch);
        this.nameFabric = nameFabric;
    }
}
